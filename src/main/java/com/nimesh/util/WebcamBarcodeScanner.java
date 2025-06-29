package com.nimesh.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebcamBarcodeScanner {
    
    private FrameGrabber grabber;
    private Java2DFrameConverter converter;
    private volatile boolean running = false;
    private ScheduledExecutorService timer;
    private MultiFormatReader barcodeReader;
    
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private final StringProperty barcodeProperty = new SimpleStringProperty();
    
    public WebcamBarcodeScanner() {
        converter = new Java2DFrameConverter();
        barcodeReader = new MultiFormatReader();
    }
    
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        
        // Initialize the webcam grabber
        try {
            grabber = new OpenCVFrameGrabber(0); // 0 = default camera
            grabber.start();
            
            // Start a timer to grab frames
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(this::grabFrame, 0, 100, TimeUnit.MILLISECONDS);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            running = false;
        }
    }
    
    public void stop() {
        running = false;
        
        if (timer != null) {
            timer.shutdown();
            try {
                timer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            timer = null;
        }
        
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
                grabber = null;
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void grabFrame() {
        if (!running) return;
        
        try {
            // Grab a frame from the camera
            Frame frame = grabber.grab();
            if (frame == null) return;
            
            // Convert to a BufferedImage
            BufferedImage bufferedImage = converter.convert(frame);
            if (bufferedImage == null) return;
            
            // Try to decode a barcode
            try {
                LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = barcodeReader.decode(bitmap);
                
                // We found a barcode!
                Platform.runLater(() -> barcodeProperty.set(result.getText()));
            } catch (NotFoundException e) {
                // No barcode found - this is normal
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Update the image property
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            Platform.runLater(() -> imageProperty.set(fxImage));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ObjectProperty<Image> imageProperty() {
        return imageProperty;
    }
    
    public StringProperty barcodeProperty() {
        return barcodeProperty;
    }
    
    public boolean isRunning() {
        return running;
    }
}
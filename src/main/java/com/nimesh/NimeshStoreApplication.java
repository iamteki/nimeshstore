package com.nimesh;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class NimeshStoreApplication extends Application {
    
    private ConfigurableApplicationContext springContext;
    
    @Override
    public void init() {
        this.springContext = SpringApplication.run(NimeshStoreApplication.class);
    }
    
    @Override
    public void start(Stage primaryStage) {
        com.nimesh.util.StageManager stageManager = springContext.getBean(com.nimesh.util.StageManager.class);
     
        
        // Set application icon
    primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/nimesh-store-icon.png")));
        
        stageManager.setPrimaryStage(primaryStage);
        stageManager.showLoginScreen();
    }
    
    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
}
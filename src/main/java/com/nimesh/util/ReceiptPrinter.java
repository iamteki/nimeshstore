package com.nimesh.util;

import com.nimesh.model.Invoice;
import com.nimesh.model.InvoiceItem;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Perfect Receipt Printer for A3 Paper with Pagination
 * Features precise alignment of all columns and monetary values,
 * split of long product names into multiple lines,
 * and ensures all content fits perfectly with pagination for long receipts.
 */
public class ReceiptPrinter {
    
    // Printer and font settings
    private static final double PRINTER_WIDTH = 160; // Reduced for 80mm paper
    private static final String FONT_FAMILY = "Courier New"; // Monospaced font for perfect alignment
    private static final String FONT_FAMILY2 = "Nirmala UI";
    
    // Column widths and positioning constants
    private static final int NO_WIDTH = 3;     // "No" column width
    private static final int ITEM_WIDTH = 22;  // Item name column width (increased for better readability)
    private static final int QTY_WIDTH = 5;    // Quantity column width
    private static final int PRICE_WIDTH = 8;  // Price column width
    private static final int TOTAL_WIDTH = 40; // Total receipt width
    
    // Summary section formatting to ensure all values align perfectly with price column
    private static final int SUMMARY_LABEL_WIDTH = 15; // Width for summary labels (reduced)
    private static final int SUMMARY_VALUE_WIDTH = 8;  // Width for summary values (adjusted to match PRICE_WIDTH)
    
    // Font sizes
    private static final double HEADER_FONT_SIZE = 12;
    private static final double ITEM_FONT_SIZE = 8;  // Smaller font size for items
    private static final double SUMMARY_FONT_SIZE = 9;
    private static final double FOOTER_FONT_SIZE = 8;
    
    // Pagination settings
    private static final int ITEMS_PER_PAGE = 50; // Maximum items per page
    private static final int MAX_LINES_PER_PAGE = 100; // Maximum lines per page (header+items+footer)
    private static final int HEADER_LINES = 12; // Approximate number of lines used by the header
    private static final int FOOTER_LINES = 10; // Approximate number of lines used by the footer
    
    /**
     * Prints a receipt for the given invoice
     */
    public static void printReceipt(Invoice invoice) {
        // Calculate if pagination is needed
        boolean needsPagination = invoice.getItems().size() > ITEMS_PER_PAGE;
        
        if (needsPagination) {
            printMultiPageReceipt(invoice);
        } else {
            printSinglePageReceipt(invoice);
        }
    }
    
    /**
     * Prints a single-page receipt (original method)
     */
    private static void printSinglePageReceipt(Invoice invoice) {
        VBox receipt = createReceiptContent(invoice, false, 1, 1);
        printReceiptPage(receipt, invoice.getInvoiceNumber());
    }
    
    /**
     * Prints a multi-page receipt by dividing items across pages
     */
    private static void printMultiPageReceipt(Invoice invoice) {
        // Split items into pages
        List<List<InvoiceItem>> pages = paginateItems(invoice.getItems());
        int totalPages = pages.size();
        
        // Print each page
        for (int i = 0; i < pages.size(); i++) {
            int pageNumber = i + 1;
            List<InvoiceItem> pageItems = pages.get(i);
            
            // Create invoice copy with just this page's items
            Invoice pageInvoice = createPageInvoice(invoice, pageItems);
            
            // Create receipt with pagination info
            VBox receiptPage = createReceiptContent(pageInvoice, true, pageNumber, totalPages);
            
            // Print this page
            printReceiptPage(receiptPage, invoice.getInvoiceNumber() + " (Page " + pageNumber + ")");
        }
    }
    
    /**
     * Creates a copy of the invoice with only the specified items
     */
    private static Invoice createPageInvoice(Invoice original, List<InvoiceItem> pageItems) {
        Invoice pageInvoice = new Invoice();
        // Copy all relevant invoice properties
        pageInvoice.setInvoiceNumber(original.getInvoiceNumber());
        pageInvoice.setDate(original.getDate());
        pageInvoice.setCustomer(original.getCustomer());
        pageInvoice.setCustomerType(original.getCustomerType());
        pageInvoice.setPaymentMethod(original.getPaymentMethod());
        pageInvoice.setDiscountPercentage(original.getDiscountPercentage());
        pageInvoice.setCashReceived(original.getCashReceived());
        
        // Set only this page's items
        pageInvoice.setItems(pageItems);
        
        // Keep the original totals (only show totals on the last page)
        pageInvoice.setTotalAmount(original.getTotalAmount());
        pageInvoice.setItemDiscountsTotal(original.getItemDiscountsTotal());
        pageInvoice.setFinalAmount(original.getFinalAmount());
        
        return pageInvoice;
    }
    
    /**
     * Divides items into pages based on maximum items per page
     */
    private static List<List<InvoiceItem>> paginateItems(List<InvoiceItem> allItems) {
        List<List<InvoiceItem>> pages = new ArrayList<>();
        List<InvoiceItem> currentPage = new ArrayList<>();
        int currentPageItems = 0;
        int currentPageLines = 0;
        int availableLinesForItems = MAX_LINES_PER_PAGE - HEADER_LINES - FOOTER_LINES;
        
        for (InvoiceItem item : allItems) {
            // Estimate how many lines this item will take (considering long product names)
            int itemLines = estimateItemLines(item.getProduct().getName());
            
            // Check if adding this item would exceed the page limit
            if (currentPageItems >= ITEMS_PER_PAGE || 
                currentPageLines + itemLines > availableLinesForItems) {
                // Start a new page
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                currentPageItems = 0;
                currentPageLines = 0;
            }
            
            // Add item to current page
            currentPage.add(item);
            currentPageItems++;
            currentPageLines += itemLines;
        }
        
        // Add the final page if it has items
        if (!currentPage.isEmpty()) {
            pages.add(currentPage);
        }
        
        return pages;
    }
    
    /**
     * Estimates how many lines an item will take on the receipt based on product name length
     */
    private static int estimateItemLines(String productName) {
        if (productName.length() <= ITEM_WIDTH) {
            return 1; // Single line
        } else {
            // Calculate number of lines needed to display the full name
            return (int) Math.ceil((double) productName.length() / ITEM_WIDTH);
        }
    }
    
    /**
     * Prints a specific receipt page
     */
    private static void printReceiptPage(VBox receipt, String description) {
        Printer printer = Printer.getDefaultPrinter();
        
        if (printer == null) {
            AlertHelper.showWarningAlert("No Printer", "No Default Printer Found", 
                    "Receipt has been generated but no printer is available.");
            return;
        }
        
        PrinterJob job = PrinterJob.createPrinterJob(printer);
        
        if (job != null) {
            PrinterAttributes printerAttributes = printer.getPrinterAttributes();
            PageLayout pageLayout = printer.createPageLayout(
                Paper.A3,
                PageOrientation.PORTRAIT, 
                0, 0, 0, 0
            );
            job.getJobSettings().setPageLayout(pageLayout);
            
            boolean printed = job.printPage(receipt);
            
            if (printed) {
                job.endJob();
                AlertHelper.showInformationAlert("Receipt Printed", "Receipt Printed Successfully", 
                        "Receipt for invoice " + description + " has been sent to printer.");
            } else {
                AlertHelper.showErrorAlert("Print Error", "Printing Failed", 
                        "Failed to print receipt. Please check printer status.");
            }
        } else {
            AlertHelper.showErrorAlert("Print Error", "Printing Failed", 
                    "Could not create printer job. Please check printer status.");
        }
    }
    
    /**
     * Creates the receipt content with perfectly aligned formatting
     * @param invoice The invoice data
     * @param isPaginated Whether this is part of a paginated receipt
     * @param pageNumber Current page number if paginated
     * @param totalPages Total number of pages if paginated
     */
    private static VBox createReceiptContent(Invoice invoice, boolean isPaginated, 
                                            int pageNumber, int totalPages) {
        VBox receipt = new VBox(1);
        receipt.setStyle("-fx-background-color: white; -fx-padding: 2;");
        receipt.setPrefWidth(PRINTER_WIDTH);

        // Create a consistent separator line to be used throughout
        String separatorLine = createSeparatorLine(TOTAL_WIDTH);

        // Header section - Split address into multiple lines for narrow paper
        Text storeName = createCenteredText("  UPALI STORES", FontWeight.BOLD, 16);
        Text storeAddress1 = createCenteredText("     Infront of school", FontWeight.BOLD, ITEM_FONT_SIZE);
        Text storeAddress2 = createCenteredText("     Keenapalassa", FontWeight.BOLD, ITEM_FONT_SIZE);
        Text storeAddress3 = createCenteredText("     Hasalaka", FontWeight.BOLD, ITEM_FONT_SIZE);
        Text storeContact1 = createCenteredText("     Tel: 077-1723478,075-2718060", FontWeight.BOLD, ITEM_FONT_SIZE);
        
        Text separator1 = new Text(separatorLine);
        separator1.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));

        // Invoice Details section
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        Text invoiceNumber = new Text("Invoice: #" + invoice.getInvoiceNumber());
        invoiceNumber.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
        
        Text date = new Text("Date: " + invoice.getDate().format(dateFormatter));
        date.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
        
        Text customer = new Text("Customer: " + (invoice.getCustomer() != null ? 
                invoice.getCustomer().getName() : "Walk-in Customer"));
        customer.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));

        // Customer and Payment Info
        Text customerType = new Text("Type: " + invoice.getCustomerType());
        customerType.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
        
        Text paymentMethod = new Text("Payment: " + invoice.getPaymentMethod());
        paymentMethod.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
        
        // Add pagination info if needed
        Text paginationInfo = null;
        if (isPaginated) {
            paginationInfo = new Text(String.format("Page %d of %d", pageNumber, totalPages));
            paginationInfo.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, ITEM_FONT_SIZE));
        }

        receipt.getChildren().addAll(storeName, storeAddress1, storeAddress2, storeAddress3, 
                storeContact1, separator1, invoiceNumber, date, customer, 
                customerType, paymentMethod);
                
        if (paginationInfo != null) {
            receipt.getChildren().add(paginationInfo);
        }

        // Items Section
        Text separator2 = new Text(separatorLine);
        separator2.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
        receipt.getChildren().add(separator2);
        
        // Item Header with perfect alignment
        String itemHeaderFormat = "%-" + NO_WIDTH + "s %-" + ITEM_WIDTH + "s %" + QTY_WIDTH + "s %" + PRICE_WIDTH + "s";
        Text itemsHeader = new Text(String.format(itemHeaderFormat, "No", "Item", "Qty", "Price"));
        itemsHeader.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, ITEM_FONT_SIZE));
        receipt.getChildren().add(itemsHeader);
        
        Text separator3 = new Text(separatorLine);
        separator3.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
        receipt.getChildren().add(separator3);

        // Process items and handle long product names
        int itemNumber = 1;
        
        // If paginated, adjust the item number based on the page
        if (isPaginated && pageNumber > 1) {
            // Calculate the starting item number for this page
            itemNumber = ((pageNumber - 1) * ITEMS_PER_PAGE) + 1;
        }
        
        for (InvoiceItem item : invoice.getItems()) {
            String name = item.getProduct().getName();
            String itemNum = String.format("%d.", itemNumber++); // Use simple numeric format (1., 2., 3.)
            
            // Handle very long product names by splitting into multiple lines if needed
            if (name.length() > ITEM_WIDTH) {
                // Split into multiple lines for long names
                String[] nameParts = splitName(name, ITEM_WIDTH);
                
                // First line with item number and first part of name
                String firstLineFormat = "%-" + NO_WIDTH + "s %-" + ITEM_WIDTH + "s";
                Text nameLine1 = new Text(String.format(firstLineFormat, itemNum, nameParts[0]));
                nameLine1.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
                receipt.getChildren().add(nameLine1);
                
                // Middle lines (if any)
                for (int i = 1; i < nameParts.length - 1; i++) {
                    Text middleLine = new Text(String.format(firstLineFormat, "", nameParts[i]));
                    middleLine.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
                    receipt.getChildren().add(middleLine);
                }
                
                // Last line with the final part of name, qty and price
                String lastLineFormat = "%-" + NO_WIDTH + "s %-" + ITEM_WIDTH + "s %" + QTY_WIDTH + ".2f %" + PRICE_WIDTH + ".2f";
                Text itemLine = new Text(String.format(lastLineFormat, 
                        "", nameParts[nameParts.length-1], item.getQuantity(), item.getTotal()));
                itemLine.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
                receipt.getChildren().add(itemLine);
            } else {
                // Single line for short product names with perfect alignment
                String lineFormat = "%-" + NO_WIDTH + "s %-" + ITEM_WIDTH + "s %" + QTY_WIDTH + ".2f %" + PRICE_WIDTH + ".2f";
                Text itemLine = new Text(String.format(lineFormat, 
                        itemNum, name, item.getQuantity(), item.getTotal()));
                itemLine.setFont(Font.font(FONT_FAMILY, ITEM_FONT_SIZE));
                receipt.getChildren().add(itemLine);
            }
        }

        // Add "Continued on next page" message for all but the last page
        if (isPaginated && pageNumber < totalPages) {
            Text continuedText = createCenteredText("--- Continued on next page ---", FontWeight.BOLD, ITEM_FONT_SIZE);
            receipt.getChildren().add(continuedText);
            
            // Add the last separator and thank you only on the last page
            Text separator7 = new Text(separatorLine);
            separator7.setFont(Font.font(FONT_FAMILY, FOOTER_FONT_SIZE));
            receipt.getChildren().add(separator7);
            
            return receipt;
        }

        // Summary section - only shown on the last page (or single page receipts)
        Text separator4 = new Text(separatorLine);
        separator4.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
        receipt.getChildren().add(separator4);
        
        // FIXED: Create a format that ensures summary values align properly with the price column
        // Calculate the correct position for the monetary values to align with the price column
        int labelStartPosition = NO_WIDTH + 1 + ITEM_WIDTH + 1; // Start position after "No" and "Item" columns plus spaces
        int valueStartPosition = labelStartPosition + QTY_WIDTH; // Position where the price column starts
        
        // Format for summary section that aligns perfectly with the price column
        String summaryFormat = "%-" + labelStartPosition + "s%" + PRICE_WIDTH + ".2f";
        
        // Subtotal
        Text subtotal = new Text(String.format(summaryFormat, "Subtotal:", invoice.getTotalAmount()));
        subtotal.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
        receipt.getChildren().add(subtotal);

        // Item discounts
        if (invoice.getItemDiscountsTotal().compareTo(BigDecimal.ZERO) > 0) {
            Text itemDiscounts = new Text(String.format(summaryFormat, 
                    "Item Discounts:", invoice.getItemDiscountsTotal().negate()));
            itemDiscounts.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
            receipt.getChildren().add(itemDiscounts);
        }
        
        // Add percentage discount if it exists and is greater than zero
        if (invoice.getDiscountPercentage() != null && invoice.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            // Calculate the discount amount (this assumes invoice.getTotalAmount() is pre-discount)
            BigDecimal subtotalAfterItemDiscounts = invoice.getTotalAmount().subtract(invoice.getItemDiscountsTotal());
            BigDecimal percentageDiscountAmount = subtotalAfterItemDiscounts
                    .multiply(invoice.getDiscountPercentage())
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            
            // Show both percentage and amount
            Text percentageDiscount = new Text(String.format(summaryFormat, 
                    "Discount (" + invoice.getDiscountPercentage() + "%):", percentageDiscountAmount.negate()));
            percentageDiscount.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
            receipt.getChildren().add(percentageDiscount);
        }

        // Total with separator lines
        Text separator5 = new Text(separatorLine);
        separator5.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
        receipt.getChildren().add(separator5);
        
        // Use the same format for TOTAL to maintain perfect alignment with other monetary values
        Text total = new Text(String.format(summaryFormat, "TOTAL:", invoice.getFinalAmount()));
        total.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 9.5));
        receipt.getChildren().add(total);
        
        Text separator6 = new Text(separatorLine);
        separator6.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
        receipt.getChildren().add(separator6);

        // Cash received and change - using the same right-aligned format
        if ("CASH".equals(invoice.getPaymentMethod()) && invoice.getCashReceived() != null 
                && invoice.getCashReceived().compareTo(BigDecimal.ZERO) > 0) {
            Text cashReceived = new Text(String.format(summaryFormat, 
                    "Cash Received:", invoice.getCashReceived()));
            cashReceived.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
            
            Text change = new Text(String.format(summaryFormat, "Change:", invoice.getChangeAmount()));
            change.setFont(Font.font(FONT_FAMILY, SUMMARY_FONT_SIZE));
            
            receipt.getChildren().addAll(cashReceived, change);
        }

        // Footer - Handle long text by breaking into multiple lines if needed
        Text separator7 = new Text(separatorLine);
        separator7.setFont(Font.font(FONT_FAMILY, FOOTER_FONT_SIZE));
        receipt.getChildren().add(separator7);
         
        // Split footer text into multiple lines to fit paper width
        Text thankYou1 = createCenteredText("       පැමිණි ඔබට ස්තූතියි..! නැවත එන්න", FontWeight.BOLD, 8 );
        thankYou1.setFont(Font.font(FONT_FAMILY2, 8));
        
        receipt.getChildren().addAll(thankYou1);
        
        Text separator8 = new Text(separatorLine);
        separator8.setFont(Font.font(FONT_FAMILY, FOOTER_FONT_SIZE));
        receipt.getChildren().add(separator8);

        return receipt;
    }

    /**
     * Splits a product name into multiple parts that fit within the specified width
     */
    private static String[] splitName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return new String[]{name};
        }
        
        // Try to split at spaces first
        int splitPoint = name.lastIndexOf(' ', maxLength);
        if (splitPoint > 0) {
            String firstPart = name.substring(0, splitPoint).trim();
            String remaining = name.substring(splitPoint).trim();
            return combineArrays(new String[]{firstPart}, splitName(remaining, maxLength));
        }
        
        // If no space found, split at max length
        String firstPart = name.substring(0, maxLength);
        String remaining = name.substring(maxLength);
        return combineArrays(new String[]{firstPart}, splitName(remaining, maxLength));
    }
    
    /**
     * Combines two string arrays
     */
    private static String[] combineArrays(String[] arr1, String[] arr2) {
        String[] result = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    /**
     * Creates a separator line with exact length to ensure consistent alignment
     */
    private static String createSeparatorLine(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append("-");
        }
        return sb.toString();
    }
    
    /**
     * Creates centered text with specified font weight and size
     */
    private static Text createCenteredText(String content, FontWeight weight, double size) {
        Text text = new Text(content);
        text.setFont(Font.font(FONT_FAMILY, weight, size));
        text.setWrappingWidth(PRINTER_WIDTH);
        text.setTextAlignment(TextAlignment.CENTER);
        return text;
    }
}
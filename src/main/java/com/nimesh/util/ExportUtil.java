package com.nimesh.util;

import com.nimesh.model.Category;
import com.nimesh.model.Customer;
import com.nimesh.model.Invoice;
import com.nimesh.model.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    
    /**
     * Export data to CSV file
     */
    public void exportToCSV(Object data, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            if (data instanceof List<?>) {
                exportListToCSV((List<?>) data, writer);
            } else if (data instanceof Map<?, ?>) {
                exportMapToCSV((Map<?, ?>) data, writer);
            } else if (data instanceof Invoice) {
                exportInvoiceToCSV((Invoice) data, writer);
            } else {
                throw new IllegalArgumentException("Unsupported data type for CSV export");
            }
        }
    }
    
    /**
     * Export data to Excel file
     */
    public void exportToExcel(Object data, String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");
            
            if (data instanceof List<?>) {
                exportListToExcel((List<?>) data, workbook, sheet);
            } else if (data instanceof Map<?, ?>) {
                exportMapToExcel((Map<?, ?>) data, workbook, sheet);
            } else if (data instanceof Invoice) {
                exportInvoiceToExcel((Invoice) data, workbook, sheet);
            } else {
                throw new IllegalArgumentException("Unsupported data type for Excel export");
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            }
        }
    }
    
    private void exportListToCSV(List<?> data, FileWriter writer) throws IOException {
        if (data.isEmpty()) {
            writer.write("No data available");
            return;
        }
        
        Object firstItem = data.get(0);
        
        if (firstItem instanceof Invoice) {
            // Write header
            writer.write("Invoice #,Date,Customer,Customer Type,Payment Method,Amount\n");
            
            // Write data
            for (Object item : data) {
                Invoice invoice = (Invoice) item;
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        invoice.getInvoiceNumber(),
                        invoice.getDate().format(DATE_FORMATTER),
                        invoice.getCustomer() != null ? invoice.getCustomer().getName().replace(",", " ") : "Walk-in Customer",
                        invoice.getCustomerType(),
                        invoice.getPaymentMethod(),
                        invoice.getFinalAmount()));
            }
        } else if (firstItem instanceof Map.Entry) {
            // Handle Map.Entry lists (like product sales, category sales, etc.)
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) firstItem;
            
            if (entry.getKey() instanceof Product) {
                // Product sales report
                writer.write("Product,Category,Quantity Sold,Sales Value\n");
                
                for (Object item : data) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<Product, Map<String, Object>> productEntry = 
                            (Map.Entry<Product, Map<String, Object>>) item;
                    
                    Product product = productEntry.getKey();
                    Map<String, Object> salesData = productEntry.getValue();
                    
                    writer.write(String.format("%s,%s,%s,%s\n",
                            product.getName().replace(",", " "),
                            product.getCategory() != null ? product.getCategory().getName().replace(",", " ") : "Uncategorized",
                            salesData.get("quantity"),
                            salesData.get("salesValue")));
                }
            } else if (entry.getKey() instanceof Category) {
                // Category sales report
                writer.write("Category,Sales Value,Percentage\n");
                
                BigDecimal totalSales = BigDecimal.ZERO;
                for (Object item : data) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<Category, BigDecimal> categoryEntry = (Map.Entry<Category, BigDecimal>) item;
                    totalSales = totalSales.add(categoryEntry.getValue());
                }
                
                for (Object item : data) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<Category, BigDecimal> categoryEntry = (Map.Entry<Category, BigDecimal>) item;
                    
                    Category category = categoryEntry.getKey();
                    BigDecimal salesValue = categoryEntry.getValue();
                    BigDecimal percentage = salesValue.multiply(new BigDecimal("100"))
                            .divide(totalSales, 2, BigDecimal.ROUND_HALF_UP);
                    
                    writer.write(String.format("%s,%s,%s%%\n",
                            category != null ? category.getName().replace(",", " ") : "Uncategorized",
                            salesValue,
                            percentage));
                }
            } else if (entry.getKey() instanceof LocalDate) {
                // Daily sales report
                writer.write("Date,Sales Value\n");
                
                for (Object item : data) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<LocalDate, BigDecimal> dateEntry = (Map.Entry<LocalDate, BigDecimal>) item;
                    
                    writer.write(String.format("%s,%s\n",
                            dateEntry.getKey().format(DATE_FORMATTER),
                            dateEntry.getValue()));
                }
            } else if (entry.getKey() instanceof Customer) {
                // Customer purchases report
                writer.write("Customer,Contact,Purchase Value\n");
                
                for (Object item : data) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<Customer, BigDecimal> customerEntry = (Map.Entry<Customer, BigDecimal>) item;
                    
                    Customer customer = customerEntry.getKey();
                    
                    writer.write(String.format("%s,%s,%s\n",
                            customer.getName().replace(",", " "),
                            customer.getContactNo(),
                            customerEntry.getValue()));
                }
            }
        } else {
            // Default list export
            writer.write("Data cannot be exported in CSV format");
        }
    }
    
    private void exportMapToCSV(Map<?, ?> data, FileWriter writer) throws IOException {
        if (data.isEmpty()) {
            writer.write("No data available");
            return;
        }
        
        Object firstKey = data.keySet().iterator().next();
        
        if (firstKey instanceof String) {
            // Handle String -> BigDecimal maps (like payment method analysis)
            writer.write("Type,Amount\n");
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                writer.write(String.format("%s,%s\n",
                        entry.getKey(),
                        entry.getValue()));
            }
        } else if (firstKey instanceof Product) {
            // Handle Product -> Double maps (like inventory turnover rate)
            writer.write("Product,Category,Rate\n");
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                Product product = (Product) entry.getKey();
                
                writer.write(String.format("%s,%s,%s\n",
                        product.getName().replace(",", " "),
                        product.getCategory() != null ? product.getCategory().getName().replace(",", " ") : "Uncategorized",
                        entry.getValue()));
            }
        } else if (firstKey instanceof Category) {
            // Handle Category -> BigDecimal maps (like inventory value by category)
            writer.write("Category,Value\n");
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                Category category = (Category) entry.getKey();
                
                writer.write(String.format("%s,%s\n",
                        category != null ? category.getName().replace(",", " ") : "Uncategorized",
                        entry.getValue()));
            }
        } else if (firstKey instanceof LocalDate) {
            // Handle LocalDate -> BigDecimal maps (like sales by date)
            writer.write("Date,Amount\n");
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                LocalDate date = (LocalDate) entry.getKey();
                
                writer.write(String.format("%s,%s\n",
                        date.format(DATE_FORMATTER),
                        entry.getValue()));
            }
        } else {
            // Default map export
            writer.write("Data cannot be exported in CSV format");
        }
    }
    
    private void exportInvoiceToCSV(Invoice invoice, FileWriter writer) throws IOException {
        // Write invoice header
        writer.write("Invoice #: " + invoice.getInvoiceNumber() + "\n");
        writer.write("Date: " + invoice.getDate().format(DATE_FORMATTER) + "\n");
        writer.write("Customer: " + 
                (invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Walk-in Customer") + "\n");
        writer.write("Customer Type: " + invoice.getCustomerType() + "\n");
        writer.write("Payment Method: " + invoice.getPaymentMethod() + "\n\n");
        
        // Write items header
        writer.write("Product,Quantity,Unit Price,Total\n");
        
        // Write items
        for (var item : invoice.getItems()) {
            writer.write(String.format("%s,%s,%s,%s\n",
                    item.getProduct().getName().replace(",", " "),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotal()));
        }
        
        // Write totals
        writer.write("\nSubtotal: " + invoice.getTotalAmount() + "\n");
        writer.write("Discount: " + invoice.getDiscountAmount() + "\n");
        writer.write("Final Amount: " + invoice.getFinalAmount() + "\n");
    }
    
    private void exportListToExcel(List<?> data, Workbook workbook, Sheet sheet) {
        if (data.isEmpty()) {
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("No data available");
            return;
        }
        
        // Create header styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        Object firstItem = data.get(0);
        int rowNum = 0;
        
        if (firstItem instanceof Invoice) {
            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Invoice #", "Date", "Customer", "Customer Type", "Payment Method", "Amount"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            for (Object item : data) {
                Invoice invoice = (Invoice) item;
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(invoice.getInvoiceNumber());
                row.createCell(1).setCellValue(invoice.getDate().format(DATE_FORMATTER));
                row.createCell(2).setCellValue(invoice.getCustomer() != null ? 
                        invoice.getCustomer().getName() : "Walk-in Customer");
                row.createCell(3).setCellValue(invoice.getCustomerType());
                row.createCell(4).setCellValue(invoice.getPaymentMethod());
                row.createCell(5).setCellValue(invoice.getFinalAmount().doubleValue());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (firstItem instanceof Map.Entry) {
            // Handle Map.Entry lists
            exportMapEntryListToExcel(data, workbook, sheet, headerStyle);
        } else {
            // Default list export
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Data cannot be exported in Excel format");
        }
    }
    
    private void exportMapEntryListToExcel(List<?> data, Workbook workbook, Sheet sheet, CellStyle headerStyle) {
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) data.get(0);
        int rowNum = 0;
        
        if (entry.getKey() instanceof Product) {
            // Product sales report
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Product", "Category", "Quantity Sold", "Sales Value"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Object item : data) {
                @SuppressWarnings("unchecked")
                Map.Entry<Product, Map<String, Object>> productEntry = 
                        (Map.Entry<Product, Map<String, Object>>) item;
                
                Product product = productEntry.getKey();
                Map<String, Object> salesData = productEntry.getValue();
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getName());
                row.createCell(1).setCellValue(product.getCategory() != null ? 
                        product.getCategory().getName() : "Uncategorized");
                row.createCell(2).setCellValue(((BigDecimal) salesData.get("quantity")).doubleValue());
                row.createCell(3).setCellValue(((BigDecimal) salesData.get("salesValue")).doubleValue());
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (entry.getKey() instanceof Category) {
            // Category sales report
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Category", "Sales Value", "Percentage"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            BigDecimal totalSales = BigDecimal.ZERO;
            for (Object item : data) {
                @SuppressWarnings("unchecked")
                Map.Entry<Category, BigDecimal> categoryEntry = (Map.Entry<Category, BigDecimal>) item;
                totalSales = totalSales.add(categoryEntry.getValue());
            }
            
            for (Object item : data) {
                @SuppressWarnings("unchecked")
                Map.Entry<Category, BigDecimal> categoryEntry = (Map.Entry<Category, BigDecimal>) item;
                
                Category category = categoryEntry.getKey();
                BigDecimal salesValue = categoryEntry.getValue();
                BigDecimal percentage = salesValue.multiply(new BigDecimal("100"))
                        .divide(totalSales, 2, BigDecimal.ROUND_HALF_UP);
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category != null ? category.getName() : "Uncategorized");
                row.createCell(1).setCellValue(salesValue.doubleValue());
                row.createCell(2).setCellValue(percentage.doubleValue() + "%");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (entry.getKey() instanceof LocalDate) {
            // Daily sales report
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Date", "Sales Value"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Object item : data) {
                @SuppressWarnings("unchecked")
                Map.Entry<LocalDate, BigDecimal> dateEntry = (Map.Entry<LocalDate, BigDecimal>) item;
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dateEntry.getKey().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(dateEntry.getValue().doubleValue());
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (entry.getKey() instanceof Customer) {
            // Customer purchases report
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Customer", "Contact", "Purchase Value"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Object item : data) {
                @SuppressWarnings("unchecked")
                Map.Entry<Customer, BigDecimal> customerEntry = (Map.Entry<Customer, BigDecimal>) item;
                
                Customer customer = customerEntry.getKey();
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getName());
                row.createCell(1).setCellValue(customer.getContactNo());
                row.createCell(2).setCellValue(customerEntry.getValue().doubleValue());
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }
    
    private void exportMapToExcel(Map<?, ?> data, Workbook workbook, Sheet sheet) {
        if (data.isEmpty()) {
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("No data available");
            return;
        }
        
        // Create header styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        Object firstKey = data.keySet().iterator().next();
        int rowNum = 0;
        
        if (firstKey instanceof String) {
            // Handle String -> BigDecimal maps (like payment method analysis)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Type", "Amount"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey().toString());
                
                if (entry.getValue() instanceof BigDecimal) {
                    row.createCell(1).setCellValue(((BigDecimal) entry.getValue()).doubleValue());
                } else {
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (firstKey instanceof Product) {
            // Handle Product -> Double maps (like inventory turnover rate)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Product", "Category", "Rate"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                Product product = (Product) entry.getKey();
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getName());
                row.createCell(1).setCellValue(product.getCategory() != null ? 
                        product.getCategory().getName() : "Uncategorized");
                
                if (entry.getValue() instanceof Double) {
                    row.createCell(2).setCellValue((Double) entry.getValue());
                } else if (entry.getValue() instanceof BigDecimal) {
                    row.createCell(2).setCellValue(((BigDecimal) entry.getValue()).doubleValue());
                } else {
                    row.createCell(2).setCellValue(entry.getValue().toString());
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (firstKey instanceof Category) {
            // Handle Category -> BigDecimal maps (like inventory value by category)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Category", "Value"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                Category category = (Category) entry.getKey();
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category != null ? category.getName() : "Uncategorized");
                
                if (entry.getValue() instanceof BigDecimal) {
                    row.createCell(1).setCellValue(((BigDecimal) entry.getValue()).doubleValue());
                } else {
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else if (firstKey instanceof LocalDate) {
            // Handle LocalDate -> BigDecimal maps (like sales by date)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Date", "Amount"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                LocalDate date = (LocalDate) entry.getKey();
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(date.format(DATE_FORMATTER));
                
                if (entry.getValue() instanceof BigDecimal) {
                    row.createCell(1).setCellValue(((BigDecimal) entry.getValue()).doubleValue());
                } else {
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } else {
            // Default map export
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Data cannot be exported in Excel format");
        }
    }
    
    private void exportInvoiceToExcel(Invoice invoice, Workbook workbook, Sheet sheet) {
        int rowNum = 0;
        
        // Create header styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        
        // Invoice header
        Row headerRow1 = sheet.createRow(rowNum++);
        Cell cell1 = headerRow1.createCell(0);
        cell1.setCellValue("Invoice #:");
        cell1.setCellStyle(boldStyle);
        headerRow1.createCell(1).setCellValue(invoice.getInvoiceNumber());
        
        Row headerRow2 = sheet.createRow(rowNum++);
        Cell cell2 = headerRow2.createCell(0);
        cell2.setCellValue("Date:");
        cell2.setCellStyle(boldStyle);
        headerRow2.createCell(1).setCellValue(invoice.getDate().format(DATE_FORMATTER));
        
        Row headerRow3 = sheet.createRow(rowNum++);
        Cell cell3 = headerRow3.createCell(0);
        cell3.setCellValue("Customer:");
        cell3.setCellStyle(boldStyle);
        headerRow3.createCell(1).setCellValue(invoice.getCustomer() != null ? 
                invoice.getCustomer().getName() : "Walk-in Customer");
        
        Row headerRow4 = sheet.createRow(rowNum++);
        Cell cell4 = headerRow4.createCell(0);
        cell4.setCellValue("Customer Type:");
        cell4.setCellStyle(boldStyle);
        headerRow4.createCell(1).setCellValue(invoice.getCustomerType());
        
        Row headerRow5 = sheet.createRow(rowNum++);
        Cell cell5 = headerRow5.createCell(0);
        cell5.setCellValue("Payment Method:");
        cell5.setCellStyle(boldStyle);
        headerRow5.createCell(1).setCellValue(invoice.getPaymentMethod());
        
        // Skip a row
        rowNum++;
        
        // Items header
        Row itemsHeaderRow = sheet.createRow(rowNum++);
        String[] headers = {"Product", "Quantity", "Unit Price", "Total"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = itemsHeaderRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Items
        for (var item : invoice.getItems()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getProduct().getName());
            row.createCell(1).setCellValue(item.getQuantity().doubleValue());
            row.createCell(2).setCellValue(item.getUnitPrice().doubleValue());
            row.createCell(3).setCellValue(item.getTotal().doubleValue());
        }
        
        // Skip a row
        rowNum++;
        
        // Totals
        Row totalRow1 = sheet.createRow(rowNum++);
        Cell totalCell1 = totalRow1.createCell(2);
        totalCell1.setCellValue("Subtotal:");
        totalCell1.setCellStyle(boldStyle);
        totalRow1.createCell(3).setCellValue(invoice.getTotalAmount().doubleValue());
        
        Row totalRow2 = sheet.createRow(rowNum++);
        Cell totalCell2 = totalRow2.createCell(2);
        totalCell2.setCellValue("Discount:");
        totalCell2.setCellStyle(boldStyle);
        totalRow2.createCell(3).setCellValue(invoice.getDiscountAmount().doubleValue());
        
        Row totalRow3 = sheet.createRow(rowNum++);
        Cell totalCell3 = totalRow3.createCell(2);
        totalCell3.setCellValue("Final Amount:");
        totalCell3.setCellStyle(boldStyle);
        totalRow3.createCell(3).setCellValue(invoice.getFinalAmount().doubleValue());
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        return headerStyle;
    }
    
    // Additions to ExportUtil.java

/**
 * Export profit margin report to Excel
 */
public void exportProfitMarginReport(List<Map<String, Object>> profitData, String fileName) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Profit Margin Analysis");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Product", "Category", "Sales Value", "Cost", "Profit", "Margin %"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        int rowNum = 1;
        for (Map<String, Object> data : profitData) {
            Row row = sheet.createRow(rowNum++);
            
            Product product = (Product) data.get("product");
            row.createCell(0).setCellValue(product.getName());
            row.createCell(1).setCellValue(product.getCategory() != null ? 
                    product.getCategory().getName() : "Uncategorized");
            
            // Format currency values
            Cell salesCell = row.createCell(2);
            salesCell.setCellValue(((BigDecimal) data.get("sales")).doubleValue());
            
            Cell costCell = row.createCell(3);
            costCell.setCellValue(((BigDecimal) data.get("cost")).doubleValue());
            
            Cell profitCell = row.createCell(4);
            profitCell.setCellValue(((BigDecimal) data.get("profit")).doubleValue());
            
            // Format percentage
            Cell marginCell = row.createCell(5);
            marginCell.setCellValue(((BigDecimal) data.get("marginPercentage")).doubleValue());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        }
    }
}


/**
 * Export sales summary report to Excel
 */
public void exportSalesSummaryReport(Map<String, Object> reportData, String fileName) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        // Create the Sales Summary sheet
        Sheet summarySheet = workbook.createSheet("Sales Summary");
        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;
        
        // Add title
        Row titleRow = summarySheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Sales Summary Report");
        
        // Skip a row
        rowNum++;
        
        // Add totals
        List<Invoice> invoices = (List<Invoice>) reportData.get("invoices");
        
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal netSales = BigDecimal.ZERO;
        int totalInvoices = invoices.size();
        
        for (Invoice invoice : invoices) {
            totalSales = totalSales.add(invoice.getTotalAmount());
            if (invoice.getDiscountAmount() != null) {
                totalDiscount = totalDiscount.add(invoice.getDiscountAmount());
            }
            netSales = netSales.add(invoice.getFinalAmount());
        }
        
        Row totalSalesRow = summarySheet.createRow(rowNum++);
        totalSalesRow.createCell(0).setCellValue("Gross Sales:");
        totalSalesRow.createCell(1).setCellValue(totalSales.doubleValue());
        
        Row totalDiscountRow = summarySheet.createRow(rowNum++);
        totalDiscountRow.createCell(0).setCellValue("Total Discounts:");
        totalDiscountRow.createCell(1).setCellValue(totalDiscount.doubleValue());
        
        Row netSalesRow = summarySheet.createRow(rowNum++);
        netSalesRow.createCell(0).setCellValue("Net Sales:");
        netSalesRow.createCell(1).setCellValue(netSales.doubleValue());
        
        Row invoiceCountRow = summarySheet.createRow(rowNum++);
        invoiceCountRow.createCell(0).setCellValue("Total Invoices:");
        invoiceCountRow.createCell(1).setCellValue(totalInvoices);
        
        BigDecimal avgInvoice = totalInvoices > 0 
                ? netSales.divide(new BigDecimal(totalInvoices), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;
        
        Row avgInvoiceRow = summarySheet.createRow(rowNum++);
        avgInvoiceRow.createCell(0).setCellValue("Average Invoice Value:");
        avgInvoiceRow.createCell(1).setCellValue(avgInvoice.doubleValue());
        
        // Skip a row
        rowNum++;
        
        // Payment Method breakdown
        Map<String, BigDecimal> paymentMethodSales = 
                (Map<String, BigDecimal>) reportData.get("paymentMethodSales");
        
        Row paymentTitleRow = summarySheet.createRow(rowNum++);
        paymentTitleRow.createCell(0).setCellValue("Sales by Payment Method");
        
        Row paymentHeaderRow = summarySheet.createRow(rowNum++);
        paymentHeaderRow.createCell(0).setCellValue("Payment Method");
        paymentHeaderRow.createCell(1).setCellValue("Amount");
        paymentHeaderRow.createCell(2).setCellValue("Percentage");
        
        for (Map.Entry<String, BigDecimal> entry : paymentMethodSales.entrySet()) {
            Row row = summarySheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
            
            BigDecimal percentage = entry.getValue().multiply(new BigDecimal("100"))
                    .divide(netSales.equals(BigDecimal.ZERO) ? BigDecimal.ONE : netSales, 
                            2, RoundingMode.HALF_UP);
            
            row.createCell(2).setCellValue(percentage.doubleValue() + "%");
        }
        
        // Skip a row
        rowNum++;
        
        // Create Daily Sales sheet
        Sheet dailySheet = workbook.createSheet("Daily Sales");
        rowNum = 0;
        
        Row dailyHeaderRow = dailySheet.createRow(rowNum++);
        dailyHeaderRow.createCell(0).setCellValue("Date");
        dailyHeaderRow.createCell(1).setCellValue("Sales Amount");
        
        Map<LocalDate, BigDecimal> salesByDate = 
                (Map<LocalDate, BigDecimal>) reportData.get("salesByDate");
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        for (Map.Entry<LocalDate, BigDecimal> entry : salesByDate.entrySet()) {
            Row row = dailySheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().format(dateFormatter));
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
        }
        
        // Auto-size columns for each sheet
        for (int i = 0; i < 3; i++) {
            summarySheet.autoSizeColumn(i);
            if (i < 2) {
                dailySheet.autoSizeColumn(i);
            }
        }
        
        // Create invoices sheet
        Sheet invoicesSheet = workbook.createSheet("Invoices");
        rowNum = 0;
        
        Row invoicesHeaderRow = invoicesSheet.createRow(rowNum++);
        invoicesHeaderRow.createCell(0).setCellValue("Invoice #");
        invoicesHeaderRow.createCell(1).setCellValue("Date");
        invoicesHeaderRow.createCell(2).setCellValue("Customer");
        invoicesHeaderRow.createCell(3).setCellValue("Customer Type");
        invoicesHeaderRow.createCell(4).setCellValue("Payment Method");
        invoicesHeaderRow.createCell(5).setCellValue("Amount");
        
        for (Invoice invoice : invoices) {
            Row row = invoicesSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(invoice.getInvoiceNumber());
            row.createCell(1).setCellValue(invoice.getDate().format(dateFormatter));
            row.createCell(2).setCellValue(invoice.getCustomer() != null ? 
                    invoice.getCustomer().getName() : "Walk-in Customer");
            row.createCell(3).setCellValue(invoice.getCustomerType());
            row.createCell(4).setCellValue(invoice.getPaymentMethod());
            row.createCell(5).setCellValue(invoice.getFinalAmount().doubleValue());
        }
        
        for (int i = 0; i < 6; i++) {
            invoicesSheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
    }
}

/**
 * Export inventory stock report to Excel
 */
public void exportInventoryStockReport(List<Product> products, String fileName) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Stock Levels");
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Product Name", "Category", "Unit", "Current Stock", "Reorder Level", "Status"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add product data
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getCategory() != null ? 
                    product.getCategory().getName() : "Uncategorized");
            row.createCell(3).setCellValue(product.getUnit() != null ? 
                    product.getUnit().getName() : "");
            row.createCell(4).setCellValue(product.getCurrentStock().doubleValue());
            row.createCell(5).setCellValue(product.getReorderLevel().doubleValue());
            
            String status;
            if (product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
                status = "OUT OF STOCK";
            } else if (product.getCurrentStock().compareTo(product.getReorderLevel()) <= 0) {
                status = "LOW STOCK";
            } else {
                status = "NORMAL";
            }
            row.createCell(6).setCellValue(status);
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
    }
}

/**
 * Export customer credit report to Excel
 */
public void exportCustomerCreditReport(List<Customer> customers, String fileName) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Credit Status");
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Customer Name", "Contact", "Credit Balance", "Credit Limit", "Usage %", "Status"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add customer data
        int rowNum = 1;
        for (Customer customer : customers) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(customer.getId());
            row.createCell(1).setCellValue(customer.getName());
            row.createCell(2).setCellValue(customer.getContactNo());
            
            // Credit balance
            if (customer.getCreditAccount() != null) {
                row.createCell(3).setCellValue(customer.getCreditAccount().getBalance().doubleValue());
                
                // Credit limit
                if (customer.getCreditAccount().getCreditLimit() != null) {
                    row.createCell(4).setCellValue(customer.getCreditAccount().getCreditLimit().doubleValue());
                    
                    // Usage percentage
                    BigDecimal usage = customer.getCreditAccount().getBalance()
                        .multiply(new BigDecimal("100"))
                        .divide(customer.getCreditAccount().getCreditLimit(), 2, RoundingMode.HALF_UP);
                    row.createCell(5).setCellValue(usage.doubleValue() + "%");
                    
                    // Status
                    String status;
                    if (usage.compareTo(new BigDecimal("90")) > 0) {
                        status = "CRITICAL";
                    } else if (usage.compareTo(new BigDecimal("75")) > 0) {
                        status = "HIGH";
                    } else {
                        status = "NORMAL";
                    }
                    row.createCell(6).setCellValue(status);
                } else {
                    row.createCell(4).setCellValue("No Limit");
                    row.createCell(5).setCellValue("N/A");
                    row.createCell(6).setCellValue("ACTIVE");
                }
            } else {
                row.createCell(3).setCellValue(0);
                row.createCell(4).setCellValue("No Account");
                row.createCell(5).setCellValue("N/A");
                row.createCell(6).setCellValue("NO ACCOUNT");
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
    }
}

/**
 * Export profit and loss report to Excel
 */
public void exportProfitLossReport(List<Map<String, Object>> reportData, String fileName) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Profit & Loss");
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Revenue", "COGS", "Profit", "Margin %"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add daily profit data
        int rowNum = 1;
        
        // Calculate totals
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCOGS = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        
        for (Map<String, Object> row : reportData) {
            Row excelRow = sheet.createRow(rowNum++);
            
            excelRow.createCell(0).setCellValue((String) row.get("date"));
            
            BigDecimal revenue = (BigDecimal) row.get("revenue");
            BigDecimal cogs = (BigDecimal) row.get("cogs");
            BigDecimal profit = (BigDecimal) row.get("profit");
            
            excelRow.createCell(1).setCellValue(revenue.doubleValue());
            excelRow.createCell(2).setCellValue(cogs.doubleValue());
            excelRow.createCell(3).setCellValue(profit.doubleValue());
            excelRow.createCell(4).setCellValue((String) row.get("margin"));
            
            totalRevenue = totalRevenue.add(revenue);
            totalCOGS = totalCOGS.add(cogs);
            totalProfit = totalProfit.add(profit);
        }
        
        // Add totals row
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("TOTAL");
        totalRow.createCell(1).setCellValue(totalRevenue.doubleValue());
        totalRow.createCell(2).setCellValue(totalCOGS.doubleValue());
        totalRow.createCell(3).setCellValue(totalProfit.doubleValue());
        
        // Calculate overall margin
        String overallMargin = "N/A";
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            double margin = totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            overallMargin = String.format("%.2f%%", margin);
        }
        totalRow.createCell(4).setCellValue(overallMargin);
        
        // Apply bold style to totals row
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        
        for (int i = 0; i < 5; i++) {
            totalRow.getCell(i).setCellStyle(boldStyle);
        }
        
        // Format currency columns
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("\"LKR \"#,##0.00"));
        
        for (int row = 1; row < rowNum; row++) {
            for (int col = 1; col < 4; col++) {
                sheet.getRow(row).getCell(col).setCellStyle(currencyStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
    }
}


}

```
NimeshStore
├─ pom.xml
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ nimesh
│  │  │        ├─ config
│  │  │        │  ├─ JavaFxApplication.java
│  │  │        │  ├─ JpaConfig.java
│  │  │        │  └─ SecurityConfig.java
│  │  │        ├─ controller
│  │  │        │  ├─ BarcodeScannerDialogController.java
│  │  │        │  ├─ BarcodeScannerHandler.java
│  │  │        │  ├─ BatchManagementController.java
│  │  │        │  ├─ CustomerDialogController.java
│  │  │        │  ├─ CustomerManagementController.java
│  │  │        │  ├─ DashboardController.java
│  │  │        │  ├─ InventoryController.java
│  │  │        │  ├─ LoginController.java
│  │  │        │  ├─ NotificationController.java
│  │  │        │  ├─ OrderDetailsController.java
│  │  │        │  ├─ OrderDialogController.java
│  │  │        │  ├─ PaymentDialogController.java
│  │  │        │  ├─ POSController.java
│  │  │        │  ├─ ProductDialogController.java
│  │  │        │  ├─ ProductSearchDialogController.java
│  │  │        │  ├─ ReportingController.java
│  │  │        │  ├─ SettingsController.java
│  │  │        │  ├─ StockDialogController.java
│  │  │        │  ├─ SupplierDialogController.java
│  │  │        │  ├─ SupplierManagementController.java
│  │  │        │  ├─ TransactionHistoryController.java
│  │  │        │  └─ UserDialogController.java
│  │  │        ├─ JavaFxLauncher.java
│  │  │        ├─ model
│  │  │        │  ├─ ActivityLog.java
│  │  │        │  ├─ CartItem.java
│  │  │        │  ├─ Category.java
│  │  │        │  ├─ CreditAccount.java
│  │  │        │  ├─ Customer.java
│  │  │        │  ├─ Invoice.java
│  │  │        │  ├─ InvoiceItem.java
│  │  │        │  ├─ InvoiceItemBatch.java
│  │  │        │  ├─ OrderItem.java
│  │  │        │  ├─ Product.java
│  │  │        │  ├─ ProductBatch.java
│  │  │        │  ├─ PurchaseOrder.java
│  │  │        │  ├─ SMSNotification.java
│  │  │        │  ├─ Supplier.java
│  │  │        │  ├─ Unit.java
│  │  │        │  └─ User.java
│  │  │        ├─ NimeshStoreApplication.java
│  │  │        ├─ repository
│  │  │        │  ├─ ActivityLogRepository.java
│  │  │        │  ├─ CategoryRepository.java
│  │  │        │  ├─ CreditAccountRepository.java
│  │  │        │  ├─ CustomerRepository.java
│  │  │        │  ├─ InvoiceItemBatchRepository.java
│  │  │        │  ├─ InvoiceItemRepository.java
│  │  │        │  ├─ InvoiceRepository.java
│  │  │        │  ├─ OrderItemRepository.java
│  │  │        │  ├─ ProductBatchRepository.java
│  │  │        │  ├─ ProductRepository.java
│  │  │        │  ├─ PurchaseOrderRepository.java
│  │  │        │  ├─ SMSNotificationRepository.java
│  │  │        │  ├─ SupplierRepository.java
│  │  │        │  ├─ UnitRepository.java
│  │  │        │  └─ UserRepository.java
│  │  │        ├─ service
│  │  │        │  ├─ ActivityLogService.java
│  │  │        │  ├─ BatchReportingService.java
│  │  │        │  ├─ CategoryService.java
│  │  │        │  ├─ CustomerService.java
│  │  │        │  ├─ InvoiceService.java
│  │  │        │  ├─ LoginService.java
│  │  │        │  ├─ ProductBatchService.java
│  │  │        │  ├─ ProductService.java
│  │  │        │  ├─ PurchaseOrderService.java
│  │  │        │  ├─ ReportingService.java
│  │  │        │  ├─ SMSNotificationService.java
│  │  │        │  ├─ SMSService.java
│  │  │        │  ├─ SupplierService.java
│  │  │        │  ├─ SystemConfigService.java
│  │  │        │  ├─ UnitService.java
│  │  │        │  └─ UserService.java
│  │  │        └─ util
│  │  │           ├─ AlertHelper.java
│  │  │           ├─ DecimalFormatter.java
│  │  │           ├─ ExportUtil.java
│  │  │           ├─ ReceiptPrinter.java
│  │  │           ├─ SessionManager.java
│  │  │           ├─ SidebarStateManager.java
│  │  │           ├─ StageManager.java
│  │  │           └─ WebcamBarcodeScanner.java
│  │  └─ resources
│  │     ├─ application.properties
│  │     ├─ css
│  │     │  ├─ batch_management.css
│  │     │  ├─ customer.css
│  │     │  ├─ dashboard.css
│  │     │  ├─ dialog.css
│  │     │  ├─ inventory.css
│  │     │  ├─ notifications.css
│  │     │  ├─ pos.css
│  │     │  ├─ reports.css
│  │     │  ├─ settings.css
│  │     │  ├─ styles.css
│  │     │  └─ supplier.css
│  │     ├─ fonts
│  │     │  └─ fontawesome-webfont.ttf
│  │     ├─ fxml
│  │     │  ├─ barcode_scanner_dialog.fxml
│  │     │  ├─ batch_management.fxml
│  │     │  ├─ customer_dialog.fxml
│  │     │  ├─ customer_management.fxml
│  │     │  ├─ dashboard.fxml
│  │     │  ├─ employee_pos.fxml
│  │     │  ├─ inventory.fxml
│  │     │  ├─ login.fxml
│  │     │  ├─ notifications.fxml
│  │     │  ├─ order_details.fxml
│  │     │  ├─ order_dialog.fxml
│  │     │  ├─ payment_dialog.fxml
│  │     │  ├─ pos.fxml
│  │     │  ├─ product_dialog.fxml
│  │     │  ├─ product_search_dialog.fxml
│  │     │  ├─ reporting.fxml
│  │     │  ├─ settings.fxml
│  │     │  ├─ stock_dialog.fxml
│  │     │  ├─ supplier_dialog.fxml
│  │     │  ├─ supplier_management.fxml
│  │     │  ├─ transaction_history.fxml
│  │     │  └─ user_dialog.fxml
│  │     └─ images
│  │        └─ nimesh-store-icon.png
│  └─ test
│     └─ java
└─ target
   ├─ classes
   │  ├─ application.properties
   │  ├─ com
   │  │  └─ nimesh
   │  │     ├─ config
   │  │     │  ├─ JavaFxApplication.class
   │  │     │  ├─ JpaConfig.class
   │  │     │  └─ SecurityConfig.class
   │  │     ├─ controller
   │  │     │  ├─ BarcodeScannerDialogController.class
   │  │     │  ├─ BarcodeScannerHandler.class
   │  │     │  ├─ BatchManagementController$1.class
   │  │     │  ├─ BatchManagementController$2.class
   │  │     │  ├─ BatchManagementController$3.class
   │  │     │  ├─ BatchManagementController.class
   │  │     │  ├─ CustomerDialogController.class
   │  │     │  ├─ CustomerManagementController$1.class
   │  │     │  ├─ CustomerManagementController$2.class
   │  │     │  ├─ CustomerManagementController$3.class
   │  │     │  ├─ CustomerManagementController$4.class
   │  │     │  ├─ CustomerManagementController$5.class
   │  │     │  ├─ CustomerManagementController$6.class
   │  │     │  ├─ CustomerManagementController$7.class
   │  │     │  ├─ CustomerManagementController.class
   │  │     │  ├─ DashboardController.class
   │  │     │  ├─ InventoryController$1.class
   │  │     │  ├─ InventoryController$2.class
   │  │     │  ├─ InventoryController$3.class
   │  │     │  ├─ InventoryController$4.class
   │  │     │  ├─ InventoryController$5.class
   │  │     │  ├─ InventoryController.class
   │  │     │  ├─ LoginController.class
   │  │     │  ├─ NotificationController$1.class
   │  │     │  ├─ NotificationController.class
   │  │     │  ├─ OrderDetailsController$1.class
   │  │     │  ├─ OrderDetailsController$2.class
   │  │     │  ├─ OrderDetailsController.class
   │  │     │  ├─ OrderDialogController$1.class
   │  │     │  ├─ OrderDialogController$2.class
   │  │     │  ├─ OrderDialogController$3.class
   │  │     │  ├─ OrderDialogController.class
   │  │     │  ├─ PaymentDialogController.class
   │  │     │  ├─ POSController$1.class
   │  │     │  ├─ POSController$2.class
   │  │     │  ├─ POSController$3.class
   │  │     │  ├─ POSController$4.class
   │  │     │  ├─ POSController$5.class
   │  │     │  ├─ POSController$6.class
   │  │     │  ├─ POSController.class
   │  │     │  ├─ ProductDialogController$1.class
   │  │     │  ├─ ProductDialogController$2.class
   │  │     │  ├─ ProductDialogController.class
   │  │     │  ├─ ProductSearchDialogController$1.class
   │  │     │  ├─ ProductSearchDialogController.class
   │  │     │  ├─ ReportingController$1.class
   │  │     │  ├─ ReportingController$10.class
   │  │     │  ├─ ReportingController$11.class
   │  │     │  ├─ ReportingController$12.class
   │  │     │  ├─ ReportingController$13.class
   │  │     │  ├─ ReportingController$14.class
   │  │     │  ├─ ReportingController$15.class
   │  │     │  ├─ ReportingController$16.class
   │  │     │  ├─ ReportingController$17.class
   │  │     │  ├─ ReportingController$18.class
   │  │     │  ├─ ReportingController$19.class
   │  │     │  ├─ ReportingController$2.class
   │  │     │  ├─ ReportingController$20.class
   │  │     │  ├─ ReportingController$21.class
   │  │     │  ├─ ReportingController$22.class
   │  │     │  ├─ ReportingController$23.class
   │  │     │  ├─ ReportingController$24.class
   │  │     │  ├─ ReportingController$25.class
   │  │     │  ├─ ReportingController$26.class
   │  │     │  ├─ ReportingController$27.class
   │  │     │  ├─ ReportingController$28.class
   │  │     │  ├─ ReportingController$29.class
   │  │     │  ├─ ReportingController$3.class
   │  │     │  ├─ ReportingController$30.class
   │  │     │  ├─ ReportingController$31.class
   │  │     │  ├─ ReportingController$32.class
   │  │     │  ├─ ReportingController$33.class
   │  │     │  ├─ ReportingController$34.class
   │  │     │  ├─ ReportingController$35.class
   │  │     │  ├─ ReportingController$36.class
   │  │     │  ├─ ReportingController$37.class
   │  │     │  ├─ ReportingController$38.class
   │  │     │  ├─ ReportingController$39.class
   │  │     │  ├─ ReportingController$4.class
   │  │     │  ├─ ReportingController$40.class
   │  │     │  ├─ ReportingController$41.class
   │  │     │  ├─ ReportingController$42.class
   │  │     │  ├─ ReportingController$43.class
   │  │     │  ├─ ReportingController$44.class
   │  │     │  ├─ ReportingController$45.class
   │  │     │  ├─ ReportingController$46.class
   │  │     │  ├─ ReportingController$47.class
   │  │     │  ├─ ReportingController$48.class
   │  │     │  ├─ ReportingController$49.class
   │  │     │  ├─ ReportingController$5.class
   │  │     │  ├─ ReportingController$50.class
   │  │     │  ├─ ReportingController$51.class
   │  │     │  ├─ ReportingController$52.class
   │  │     │  ├─ ReportingController$53.class
   │  │     │  ├─ ReportingController$54.class
   │  │     │  ├─ ReportingController$55.class
   │  │     │  ├─ ReportingController$56.class
   │  │     │  ├─ ReportingController$57.class
   │  │     │  ├─ ReportingController$58.class
   │  │     │  ├─ ReportingController$59.class
   │  │     │  ├─ ReportingController$6.class
   │  │     │  ├─ ReportingController$7.class
   │  │     │  ├─ ReportingController$8.class
   │  │     │  ├─ ReportingController$9.class
   │  │     │  ├─ ReportingController.class
   │  │     │  ├─ SettingsController$1.class
   │  │     │  ├─ SettingsController.class
   │  │     │  ├─ StockDialogController.class
   │  │     │  ├─ SupplierDialogController.class
   │  │     │  ├─ SupplierManagementController$1.class
   │  │     │  ├─ SupplierManagementController$2.class
   │  │     │  ├─ SupplierManagementController$3.class
   │  │     │  ├─ SupplierManagementController$4.class
   │  │     │  ├─ SupplierManagementController$5.class
   │  │     │  ├─ SupplierManagementController$6.class
   │  │     │  ├─ SupplierManagementController.class
   │  │     │  ├─ TransactionHistoryController$1.class
   │  │     │  ├─ TransactionHistoryController$2.class
   │  │     │  ├─ TransactionHistoryController$3.class
   │  │     │  ├─ TransactionHistoryController.class
   │  │     │  └─ UserDialogController.class
   │  │     ├─ JavaFxLauncher.class
   │  │     ├─ model
   │  │     │  ├─ ActivityLog.class
   │  │     │  ├─ CartItem.class
   │  │     │  ├─ Category.class
   │  │     │  ├─ CreditAccount.class
   │  │     │  ├─ Customer.class
   │  │     │  ├─ Invoice.class
   │  │     │  ├─ InvoiceItem.class
   │  │     │  ├─ InvoiceItemBatch.class
   │  │     │  ├─ OrderItem.class
   │  │     │  ├─ Product.class
   │  │     │  ├─ ProductBatch.class
   │  │     │  ├─ PurchaseOrder.class
   │  │     │  ├─ SMSNotification.class
   │  │     │  ├─ Supplier.class
   │  │     │  ├─ Unit.class
   │  │     │  └─ User.class
   │  │     ├─ NimeshStoreApplication.class
   │  │     ├─ repository
   │  │     │  ├─ ActivityLogRepository.class
   │  │     │  ├─ CategoryRepository.class
   │  │     │  ├─ CreditAccountRepository.class
   │  │     │  ├─ CustomerRepository.class
   │  │     │  ├─ InvoiceItemBatchRepository.class
   │  │     │  ├─ InvoiceItemRepository.class
   │  │     │  ├─ InvoiceRepository.class
   │  │     │  ├─ OrderItemRepository.class
   │  │     │  ├─ ProductBatchRepository.class
   │  │     │  ├─ ProductRepository.class
   │  │     │  ├─ PurchaseOrderRepository.class
   │  │     │  ├─ SMSNotificationRepository.class
   │  │     │  ├─ SupplierRepository.class
   │  │     │  ├─ UnitRepository.class
   │  │     │  └─ UserRepository.class
   │  │     ├─ service
   │  │     │  ├─ ActivityLogService.class
   │  │     │  ├─ BatchReportingService.class
   │  │     │  ├─ CategoryService.class
   │  │     │  ├─ CustomerService.class
   │  │     │  ├─ InvoiceService.class
   │  │     │  ├─ LoginService.class
   │  │     │  ├─ ProductBatchService$BatchInfo.class
   │  │     │  ├─ ProductBatchService$BatchUsage.class
   │  │     │  ├─ ProductBatchService.class
   │  │     │  ├─ ProductService.class
   │  │     │  ├─ PurchaseOrderService.class
   │  │     │  ├─ ReportingService.class
   │  │     │  ├─ SMSNotificationService.class
   │  │     │  ├─ SMSService.class
   │  │     │  ├─ SupplierService.class
   │  │     │  ├─ SystemConfigService.class
   │  │     │  ├─ UnitService.class
   │  │     │  └─ UserService.class
   │  │     └─ util
   │  │        ├─ AlertHelper.class
   │  │        ├─ DecimalFormatter.class
   │  │        ├─ ExportUtil.class
   │  │        ├─ ReceiptPrinter.class
   │  │        ├─ SessionManager.class
   │  │        ├─ SidebarStateManager.class
   │  │        ├─ StageManager.class
   │  │        └─ WebcamBarcodeScanner.class
   │  ├─ css
   │  │  ├─ batch_management.css
   │  │  ├─ customer.css
   │  │  ├─ dashboard.css
   │  │  ├─ dialog.css
   │  │  ├─ inventory.css
   │  │  ├─ notifications.css
   │  │  ├─ pos.css
   │  │  ├─ reports.css
   │  │  ├─ settings.css
   │  │  ├─ styles.css
   │  │  └─ supplier.css
   │  ├─ fonts
   │  │  └─ fontawesome-webfont.ttf
   │  ├─ fxml
   │  │  ├─ barcode_scanner_dialog.fxml
   │  │  ├─ batch_management.fxml
   │  │  ├─ customer_dialog.fxml
   │  │  ├─ customer_management.fxml
   │  │  ├─ dashboard.fxml
   │  │  ├─ employee_pos.fxml
   │  │  ├─ inventory.fxml
   │  │  ├─ login.fxml
   │  │  ├─ notifications.fxml
   │  │  ├─ order_details.fxml
   │  │  ├─ order_dialog.fxml
   │  │  ├─ payment_dialog.fxml
   │  │  ├─ pos.fxml
   │  │  ├─ product_dialog.fxml
   │  │  ├─ product_search_dialog.fxml
   │  │  ├─ reporting.fxml
   │  │  ├─ settings.fxml
   │  │  ├─ stock_dialog.fxml
   │  │  ├─ supplier_dialog.fxml
   │  │  ├─ supplier_management.fxml
   │  │  ├─ transaction_history.fxml
   │  │  └─ user_dialog.fxml
   │  └─ images
   │     └─ nimesh-store-icon.png
   ├─ generated-sources
   │  └─ annotations
   ├─ maven-status
   │  └─ maven-compiler-plugin
   │     └─ compile
   │        └─ default-compile
   │           ├─ createdFiles.lst
   │           └─ inputFiles.lst
   └─ test-classes

```
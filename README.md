NimeshStore - Retail & Wholesale Management System

📋 Table of Contents

Overview
Features
Technology Stack
Prerequisites
Installation
Configuration
Usage
Project Structure
Database Schema
API Documentation
Screenshots
Contributing
License
Contact

🏪 Overview
NimeshStore is a comprehensive desktop-based Retail & Wholesale Management System designed to streamline business operations for small to medium-sized retail businesses. Built with Spring Boot and JavaFX, it offers a modern, user-friendly interface for managing inventory, sales, customers, and financial transactions.
The system addresses key challenges in retail operations including:

Manual billing inefficiencies
Inventory tracking difficulties
Customer credit management
Real-time reporting needs
Multi-pricing strategies for retail/wholesale

✨ Features
📊 Point of Sale (POS)

Barcode Scanning: Integrated barcode scanner support with webcam fallback
Quick Billing: Fast product search and cart management
Multiple Payment Methods: Cash, credit, and partial payments
Customer Type Pricing: Automatic retail/wholesale pricing
Receipt Printing: Customizable receipt formats

📦 Inventory Management

Batch Tracking: FIFO, LIFO, and Average costing methods
Real-time Stock Updates: Automatic inventory adjustments
Low Stock Alerts: Configurable reorder points
Product Categorization: Organized by categories and units
Expiry Management: Track batch expiry dates

👥 Customer Management

Customer Profiles: Detailed customer information
Credit Accounts: Credit limit management and tracking
Transaction History: Complete purchase history
SMS Notifications: Automated balance reminders via Twilio
Customer Types: Separate retail and wholesale customer management

📈 Reporting & Analytics

Sales Reports: Daily, monthly, and yearly analysis
Inventory Reports: Stock levels, movement, and valuation
Customer Analytics: Purchase patterns and credit analysis
Profit Analysis: Batch-wise profit calculations
Excel Export: Export reports to Excel format

🔐 Security & Administration

Role-Based Access: Admin and Employee roles
Activity Logging: Complete audit trail
Secure Authentication: BCrypt password encryption
Session Management: Secure user sessions

🏭 Supplier Management

Supplier Profiles: Contact and payment information
Purchase Orders: Create and track orders
Order History: Complete supplier transaction history

🛠️ Technology Stack
Backend

Framework: Spring Boot 3.1.5
Language: Java 21
Database: MySQL 8.0
ORM: Hibernate/JPA
Security: Spring Security

Frontend

UI Framework: JavaFX 21
Styling: CSS3
Layout: FXML

Libraries & Tools

Build Tool: Maven
Barcode: ZXing
Excel Export: Apache POI
SMS Service: Twilio SDK
Video Capture: JavaCV
IDE: Apache NetBeans / IntelliJ IDEA

📋 Prerequisites
Before installing NimeshStore, ensure you have:

Java Development Kit (JDK) 21 or higher
MySQL Server 8.0 or higher
Maven 3.8 or higher
Git (for cloning the repository)
4GB RAM minimum (8GB recommended)
Windows OS (primary support, Linux/Mac compatible)

🚀 Installation
1. Clone the Repository
bashgit clone https://github.com/yourusername/NimeshStore.git
cd NimeshStore
2. Set Up MySQL Database
sql-- Create database
CREATE DATABASE nimeshstore;

-- Create user (optional)
CREATE USER 'nimesh'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON nimeshstore.* TO 'nimesh'@'localhost';
FLUSH PRIVILEGES;

-- Import the schema
mysql -u root -p nimeshstore < nimeshstore_new.sql
3. Configure Application Properties
Edit src/main/resources/application.properties:
properties# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/nimeshstore
spring.datasource.username=your_username
spring.datasource.password=your_password

# SMS Configuration (Twilio)
sms.enabled=true
twilio.account.sid=your_twilio_sid
twilio.auth.token=your_twilio_token
twilio.phone.number=your_twilio_number

# Application Settings
app.currency=Rs.
app.store.name=Nimesh Store
app.store.address=Your Store Address
app.store.phone=+94XXXXXXXXX
4. Build the Project
bashmvn clean install
5. Run the Application
bashmvn spring-boot:run
Or run the main class NimeshStoreApplication.java from your IDE.
⚙️ Configuration
Pricing Strategy Configuration
The system supports three pricing strategies for batch management:

FIFO (First In, First Out)
LIFO (Last In, First Out)
Average Cost

Configure in Settings → System Configuration → Pricing Strategy
SMS Service Setup

Create a Twilio account at https://www.twilio.com
Get your Account SID, Auth Token, and Phone Number
Update application.properties with your credentials
Enable SMS notifications in Settings

Barcode Scanner Setup

USB barcode scanners are automatically detected
For webcam scanning, ensure camera permissions are granted
Supported formats: EAN-13, Code 128, QR Code

📖 Usage
Default Login Credentials
Admin User:
Username: admin
Password: admin123

Employee User:
Username: employee
Password: emp123
⚠️ Change default passwords after first login!
Quick Start Guide

Initial Setup

Add product categories and units
Create supplier profiles
Add products with barcodes


Daily Operations

Use POS for sales transactions
Monitor inventory levels
Process customer payments


Reporting

Generate daily sales reports
Check inventory status
Review customer balances



📁 Project Structure
NimeshStore/
├── src/
│   ├── main/
│   │   ├── java/com/nimesh/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # JavaFX controllers
│   │   │   ├── model/          # Entity classes
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Business logic
│   │   │   ├── util/           # Utility classes
│   │   │   └── NimeshStoreApplication.java
│   │   └── resources/
│   │       ├── fxml/           # JavaFX layouts
│   │       ├── css/            # Stylesheets
│   │       ├── images/         # Application images
│   │       └── application.properties
│   └── test/                   # Test classes
├── pom.xml                     # Maven configuration
├── README.md                   # This file
└── nimeshstore_new.sql        # Database schema
🗄️ Database Schema
Core Tables

products - Product catalog with pricing
product_batches - Batch-wise inventory tracking
customers - Customer profiles
credit_accounts - Customer credit management
invoices - Sales transactions
invoice_items - Invoice line items
suppliers - Supplier information
purchase_orders - Purchase order management
users - System users
activity_logs - Audit trail

Key Relationships

Products → Product Batches (1:N)
Customers → Credit Accounts (1:1)
Invoices → Invoice Items (1:N)
Invoice Items → Product Batches (N:N)

📸 Screenshots
Login Screen
<img src="docs/screenshots/login.png" alt="Login Screen" width="600"/>
Dashboard
<img src="docs/screenshots/dashboard.png" alt="Dashboard" width="800"/>
Point of Sale
<img src="docs/screenshots/pos.png" alt="POS Screen" width="800"/>
Inventory Management
<img src="docs/screenshots/inventory.png" alt="Inventory Management" width="800"/>
🤝 Contributing
We welcome contributions! Please follow these steps:

Fork the repository
Create a feature branch (git checkout -b feature/AmazingFeature)
Commit your changes (git commit -m 'Add some AmazingFeature')
Push to the branch (git push origin feature/AmazingFeature)
Open a Pull Request

Coding Standards

Follow Java naming conventions
Add JavaDoc comments for public methods
Write unit tests for new features
Ensure all tests pass before submitting PR

🐛 Known Issues

SMS notifications require active internet connection
Webcam barcode scanning may be slow on some systems
Report generation for large datasets may take time


👨‍💻 Author
Kaushalya Wickramasinghe - Initial work - GitHub Profile
📞 Contact
For support or queries:

Email: kaushalyawiki@gmail.com
Phone: +94701614804
Issues: GitHub Issues




<div align="center">
  Made with ❤️ in Sri Lanka
</div>

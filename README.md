# NimeshStore - Retail & Wholesale Management System

<div align="center">
  <img src="src/main/resources/images/nimesh-store-icon.png" alt="NimeshStore Logo" width="200"/>
  
  [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
  [![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Documentation](#api-documentation)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## 🏪 Overview

NimeshStore is a comprehensive desktop-based Retail & Wholesale Management System designed to streamline business operations for small to medium-sized retail businesses. Built with Spring Boot and JavaFX, it offers a modern, user-friendly interface for managing inventory, sales, customers, and financial transactions.

The system addresses key challenges in retail operations including:
- Manual billing inefficiencies
- Inventory tracking difficulties
- Customer credit management
- Real-time reporting needs
- Multi-pricing strategies for retail/wholesale

## ✨ Features

### 📊 Point of Sale (POS)
- **Barcode Scanning**: Integrated barcode scanner support with webcam fallback
- **Quick Billing**: Fast product search and cart management
- **Multiple Payment Methods**: Cash, credit, and partial payments
- **Customer Type Pricing**: Automatic retail/wholesale pricing
- **Receipt Printing**: Customizable receipt formats

### 📦 Inventory Management
- **Batch Tracking**: FIFO, LIFO, and Average costing methods
- **Real-time Stock Updates**: Automatic inventory adjustments
- **Low Stock Alerts**: Configurable reorder points
- **Product Categorization**: Organized by categories and units
- **Expiry Management**: Track batch expiry dates

### 👥 Customer Management
- **Customer Profiles**: Detailed customer information
- **Credit Accounts**: Credit limit management and tracking
- **Transaction History**: Complete purchase history
- **SMS Notifications**: Automated balance reminders via Twilio
- **Customer Types**: Separate retail and wholesale customer management

### 📈 Reporting & Analytics
- **Sales Reports**: Daily, monthly, and yearly analysis
- **Inventory Reports**: Stock levels, movement, and valuation
- **Customer Analytics**: Purchase patterns and credit analysis
- **Profit Analysis**: Batch-wise profit calculations
- **Excel Export**: Export reports to Excel format

### 🔐 Security & Administration
- **Role-Based Access**: Admin and Employee roles
- **Activity Logging**: Complete audit trail
- **Secure Authentication**: BCrypt password encryption
- **Session Management**: Secure user sessions

### 🏭 Supplier Management
- **Supplier Profiles**: Contact and payment information
- **Purchase Orders**: Create and track orders
- **Order History**: Complete supplier transaction history

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 21
- **Database**: MySQL 8.0
- **ORM**: Hibernate/JPA
- **Security**: Spring Security

### Frontend
- **UI Framework**: JavaFX 21
- **Styling**: CSS3
- **Layout**: FXML

### Libraries & Tools
- **Build Tool**: Maven
- **Barcode**: ZXing
- **Excel Export**: Apache POI
- **SMS Service**: Twilio SDK
- **Video Capture**: JavaCV
- **IDE**: Apache NetBeans / IntelliJ IDEA

## 📋 Prerequisites

Before installing NimeshStore, ensure you have:

- **Java Development Kit (JDK) 21** or higher
- **MySQL Server 8.0** or higher
- **Maven 3.8** or higher
- **Git** (for cloning the repository)
- **4GB RAM** minimum (8GB recommended)
- **Windows OS** (primary support, Linux/Mac compatible)

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/NimeshStore.git
cd NimeshStore
```

### 2. Set Up MySQL Database
```sql
-- Create database
CREATE DATABASE nimeshstore;

-- Create user (optional)
CREATE USER 'nimesh'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON nimeshstore.* TO 'nimesh'@'localhost';
FLUSH PRIVILEGES;

-- Import the schema
mysql -u root -p nimeshstore < nimeshstore_new.sql
```

### 3. Configure Application Properties
Edit `src/main/resources/application.properties`:
```properties
# Database Configuration
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
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

Or run the main class `NimeshStoreApplication.java` from your IDE.

## ⚙️ Configuration

### Pricing Strategy Configuration
The system supports three pricing strategies for batch management:
- **FIFO** (First In, First Out)
- **LIFO** (Last In, First Out)
- **Average Cost**

Configure in Settings → System Configuration → Pricing Strategy

### SMS Service Setup
1. Create a Twilio account at https://www.twilio.com
2. Get your Account SID, Auth Token, and Phone Number
3. Update `application.properties` with your credentials
4. Enable SMS notifications in Settings

### Barcode Scanner Setup
- USB barcode scanners are automatically detected
- For webcam scanning, ensure camera permissions are granted
- Supported formats: EAN-13, Code 128, QR Code

## 📖 Usage

### Default Login Credentials
```
Admin User:
Username: admin
Password: admin123

Employee User:
Username: employee
Password: emp123
```

**⚠️ Change default passwords after first login!**

### Quick Start Guide

1. **Initial Setup**
   - Add product categories and units
   - Create supplier profiles
   - Add products with barcodes

2. **Daily Operations**
   - Use POS for sales transactions
   - Monitor inventory levels
   - Process customer payments

3. **Reporting**
   - Generate daily sales reports
   - Check inventory status
   - Review customer balances

## 📁 Project Structure

```
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
```

## 🗄️ Database Schema

### Core Tables
- **products** - Product catalog with pricing
- **product_batches** - Batch-wise inventory tracking
- **customers** - Customer profiles
- **credit_accounts** - Customer credit management
- **invoices** - Sales transactions
- **invoice_items** - Invoice line items
- **suppliers** - Supplier information
- **purchase_orders** - Purchase order management
- **users** - System users
- **activity_logs** - Audit trail

### Key Relationships
- Products → Product Batches (1:N)
- Customers → Credit Accounts (1:1)
- Invoices → Invoice Items (1:N)
- Invoice Items → Product Batches (N:N)

## 📸 Screenshots

### Login Screen
<img src="https://wickra.dev/assets/nimeshstore/login.PNG" alt="Login Screen" width="600"/>

### Dashboard
<img src="https://wickra.dev/assets/nimeshstore/dashboard.PNG" alt="Dashboard" width="800"/>

### Point of Sale
<img src="https://wickra.dev/assets/nimeshstore/pos2.PNG" alt="POS Screen" width="800"/>

### Inventory Management
<img src="https://wickra.dev/assets/nimeshstore/inventory.PNG" alt="Inventory Management" width="800"/>
<img src="https://wickra.dev/assets/nimeshstore/inventory1.PNG" alt="Inventory Management" width="800"/>

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Write unit tests for new features
- Ensure all tests pass before submitting PR

## 🐛 Known Issues

- SMS notifications require active internet connection
- Webcam barcode scanning may be slow on some systems
- Report generation for large datasets may take time


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Kaushalya Wickramasinghe** - *Initial work* - [GitHub Profile](https://github.com/iamteki)

## 📞 Contact

For support or queries:
- web: https://wickra.dev
- Email: kaushalyawiki@gmail.com
- Phone: +94701614804
- Issues: [GitHub Issues](https://github.com/iamteki/NimeshStore/issues)


<div align="center">
  Made with ❤️ in Sri Lanka
</div>

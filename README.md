# PetPals Swing Application

A Java Swing desktop application for a pet adoption center that also allows users to purchase pet-related products.

## Features

### User Features

- **Authentication:** Secure user login and signup with password hashing (jBCrypt).
- **Pet Browsing:** View available pets for adoption with details and images.
- **Favorite Pets:** Mark pets as favorites for easy access.
- **Product Marketplace:** Browse and search for pet products (food, toys, accessories).
- **Shopping Cart:** Add products to a shopping cart.
- **Checkout:** Place orders for products with UPI payment integration.
- **Order History:** View past product orders.
- **Testimonials:** Read testimonials from other users and submit your own.
- **Support Queries:** Submit questions or support requests.

### Admin Features (Requires Admin Login)

- **Pet Management:** Add, update, and remove pet listings.
- **Product Management:** Add, update, and remove product listings.
- **Order Management:** View and manage customer product orders.
- **Testimonial Management:** Approve or reject user-submitted testimonials.

## Prerequisites (Windows)

- **Java Development Kit (JDK):** Version 17 or higher. Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/). Ensure `JAVA_HOME` environment variable is set and JDK's `bin` directory is in your `PATH`.
- **Apache Maven:** Build automation tool. Download from [Maven](https://maven.apache.org/download.cgi). Ensure `MAVEN_HOME` environment variable is set and Maven's `bin` directory is in your `PATH`.
- **MySQL Server:** Database for storing application data. Download the MySQL Installer for Windows from the [MySQL Community Downloads](https://dev.mysql.com/downloads/installer/).
- **MySQL Workbench:** GUI tool for MySQL. Usually included with the MySQL Installer, or download separately from [MySQL Community Downloads](https://dev.mysql.com/downloads/workbench/).

## Database Setup (Windows with MySQL Workbench)

3.  **Launch MySQL Workbench:** Open MySQL Workbench and connect to your local MySQL instance (usually `localhost` or `127.0.0.1`, port `3306`, user `root`, and the password you set during installation).
4.  **Create the Database Schema:**
    - In MySQL Workbench, open a new SQL query tab (File > New Query Tab).
    - Execute the following command:
      ```sql
      CREATE DATABASE IF NOT EXISTS petpals;
      ```
      (You can run this by clicking the lightning bolt icon).
5.  **Run the Setup Script:**
    - In MySQL Workbench, go to File > Open SQL Script...
    - Navigate to the project's root directory and select the `data-setup.sql` file.
    - Ensure the `petpals` schema is selected as the default schema in the toolbar above the script editor.
    - Execute the script by clicking the lightning bolt icon that runs the entire script. This will create the necessary tables and potentially insert initial data into the `petpals` database.
6.  **Verify Database Credentials:** The application connects using the following credentials defined in `src/main/java/org/petpals/db/DatabaseConnection.java`:
    - URL: `jdbc:mysql://localhost:3306/petpals`
    - User: `root`
    - Password: `root`
      If your MySQL root password is _not_ `root`, you will need to either:
      - Update the `DB_PASSWORD` constant in `DatabaseConnection.java` and rebuild the application (`mvn clean package`), OR
      - Create a dedicated MySQL user with the password `root` and grant it privileges on the `petpals` database.
7.  **Create Application Users:**
    - The application requires both regular users and an admin user to access all features.
    - To create a regular user, sign up through the application's signup form.
    - To create an admin user, you need to manually update the `is_admin` flag in the `users` table. Connect to the `petpals` database in MySQL Workbench and run the following query:
      ```sql
      UPDATE users SET is_admin = 1 WHERE username = 'your_admin_username';
      ```
      Replace `'your_admin_username'` with the username of the user you want to make an admin.

## Running the Application (Windows)

Open Command Prompt (cmd) or PowerShell in the project's root directory.

**1. Using Maven Exec Plugin (Development)**

This method compiles and runs the application directly using Maven.


```cmd
mvn clean install
```

```cmd
mvn compile exec:java
```

**2. Building and Running the Executable JAR (Recommended)**

This method packages the application and all its dependencies into a single executable JAR file.

a. **Build the JAR:**

```cmd
mvn clean package
```

This command cleans previous builds and creates the JAR file in the `target/` directory (e.g., `target/petpals-swing-1.0-SNAPSHOT.jar`).

b. **Run the JAR:**

```cmd
java -jar target\petpals-swing-1.0-SNAPSHOT.jar
```

_(Note the use of backslash `\` in the path for Windows Command Prompt. Replace `petpals-swing-1.0-SNAPSHOT.jar` with the actual name of the generated JAR file if it differs.)_

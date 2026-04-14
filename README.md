# PVT-15 Backend

Spring Boot backend for the PVT-15 project.

## Requirements

Before starting, make sure you have:

- IntelliJ IDEA installed
- Git installed
- Java 21 installed

## 1. Clone the repository

Open a terminal and run:

    git clone git@github.com:pvt-15/pvt-15-backend.git

Then move into the project folder:

    cd pvt-15-backend

## 2. Open the project in IntelliJ

1. Open IntelliJ IDEA
2. Press **Open**
3. Select the `pvt-15-backend` folder
4. Wait for IntelliJ to load the project

If IntelliJ asks about Maven, open it as a Maven project.

## 3. Reload Maven dependencies

When the project is open:

1. Open the **Maven** tool window in IntelliJ
2. Click **Reload All Maven Projects**

This makes IntelliJ download and load all dependencies from `pom.xml`.

## 4. Set environment variables

The application needs database credentials to start.

In IntelliJ:

1. Click the run configuration dropdown at the top
2. Select **Edit Configurations**
3. Open the configuration for `AccessingDataMysqlApplication`
4. Find the field **Environment variables**
5. Add:

    DB_URL=jdbc:mariadb://mysql.dsv.su.se:3306/YOUR_DATABASE;DB_USER=YOUR_USERNAME;DB_PASS=YOUR_PASSWORD

Replace:

- `YOUR_DATABASE` with your database name
- `YOUR_USERNAME` with your database username
- `YOUR_PASSWORD` with your database password

These are currently located in our discord server/backend sent from ludvig

## 5. Run the application

Open the file:

`src/main/java/com/example/accessingdatamysql/AccessingDataMysqlApplication.java`

Then press the green **Run** button next to the `main` method.

If everything works, IntelliJ should show that Spring Boot has started.

## 6. Verify that it works

When the application is running, open this in your browser:

    http://localhost:8080

    http://localhost:8080/hello

## Common issues

### Red imports in IntelliJ
Reload the Maven project again.

### MariaDB dependency is red
Run this in the terminal:

    mvnw -U clean package

Then reload the Maven project in IntelliJ.

### Database login fails
Double-check your `DB_URL`, `DB_USER`, and `DB_PASS`.

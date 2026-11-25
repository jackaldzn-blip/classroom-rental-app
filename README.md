# Classroom Rental App

A Java SE console application for managing 20 training classrooms, customer registration, availability searching and reservations.

## Project status

Phase 3: Maven and database foundation. The startup program checks the database connection and classroom count; the interactive menu and business logic are not yet implemented.

## Technology stack

Java 25 LTS, Maven, MySQL 8.4, native JDBC (MySQL Connector/J), JUnit 5, VS Code, Markdown and Mermaid, Git and GitHub.

## Documentation

- [Requirements and development plan](docs/REQUIREMENTS.md)
- [Architecture, UML and database design](docs/DESIGN.md)
- [Official assignment](https://github.com/Elnaz-Azizi/project-test-1/blob/main/ProjectTest_ClassRoomRentalApp.md)
- [Assessment guide](https://github.com/Elnaz-Azizi/project-test-1/blob/main/README.md)

## Setup (Windows PowerShell)

Install Java 25, Maven and MySQL 8.4. Verify the tools:

```powershell
java -version
mvn -version
mysql --version
```

From the repository root, open the MySQL client with a database administrator account:

```powershell
mysql -u root -p
```

Run these commands at the MySQL prompt, adjusting the absolute paths if the repository is stored elsewhere:

```sql
SOURCE E:/Projects/classroom-rental-app/sql/schema.sql;
SOURCE E:/Projects/classroom-rental-app/sql/seed.sql;
USE classroom_rental;
SHOW TABLES;
SELECT COUNT(*) AS classroom_count FROM classrooms;
```

Run seed.sql **only once on a fresh database**. Expected classroom count: 20. The sample customers and booking users are fictional.

For local development, create a dedicated account once and grant only the application's CRUD permissions. MySQL prints the randomly generated password; store it privately.

```sql
CREATE USER 'classroom_app'@'localhost' IDENTIFIED BY RANDOM PASSWORD;
GRANT SELECT, INSERT, UPDATE, DELETE ON classroom_rental.* TO 'classroom_app'@'localhost';
EXIT;
```

Back in PowerShell, configure the current terminal session. Enter the generated password at the masked prompt.

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/classroom_rental'
$env:DB_USER = 'classroom_app'
$credential = Get-Credential -UserName $env:DB_USER -Message 'Enter the MySQL application password'
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
Remove-Variable credential
```

The password is available to processes launched from this terminal. Do not commit credentials. Set these variables again after opening a new terminal.

## Build and test

```powershell
mvn clean test
```

The initial five unit tests validate the connection configuration without opening a database.

## Run

```powershell
mvn compile exec:java
```

Expected startup output:

```text
Classroom Rental App
Connected to MySQL successfully.
Database: classroom_rental
Classrooms: 20
```

The startup check verifies a live JDBC connection and a SQL query. The complete application will replace this check with a console menu in a later phase.

When finished, clear the password from your terminal environment:

```powershell
Remove-Item Env:DB_PASSWORD
```

## Git workflow

Use small commits and dedicated feature branches, then merge completed, locally verified work into main.

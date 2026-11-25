# Classroom Rental App

A Java SE console application for managing 20 training classrooms, customer registration, availability searching and reservations.

## Project status

Phase 4: Domain models and JDBC DAO classes are implemented. The startup program still checks the database connection and classroom count; service logic and the interactive menu are next.

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
USE classroom_rental;
SHOW TABLES;
```

Verify that schema.sql reports no errors and all five tables exist before proceeding.

```sql
SOURCE E:/Projects/classroom-rental-app/sql/seed.sql;
SELECT COUNT(*) AS classroom_count FROM classrooms;
```

Run seed.sql **only once on a fresh database**. If a setup attempt fails, do not rerun the seed against partially populated tables. Inspect the existing data first; reset the project database only if it contains no data you need to keep. Expected classroom count: 20. The sample customers and booking users are fictional.

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

The unit tests validate database configuration, customer types, classroom requirements and booking time rules without opening a database. The DAO integration test is skipped unless explicitly enabled.

To verify real JDBC CRUD operations, use the local development database only, with the connection variables configured as above:

```powershell
$env:RUN_DB_TESTS = 'true'
mvn clean test
Remove-Item Env:RUN_DB_TESTS
```

The integration test inserts temporary fictional customer, booking user, classroom and booking records. It verifies reads, classroom updates, conflict rejection, adjacent bookings and cancellation, then removes its own records in a finally block. Do not run it against production data. An interrupted test or failed cleanup may leave temporary records and incremented IDs.

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

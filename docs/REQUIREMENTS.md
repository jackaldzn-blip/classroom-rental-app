# Requirements and Development Plan

## Purpose

Build a Java console application for managing 20 training
classrooms, customers and room reservations.

## Functional Requirements

| ID | Requirement |
| --- | --- |
| FR01 | Register individual and company customers. |
| FR02 | Manage classrooms, capacity, equipment and accessibility. |
| FR03 | Search for available classrooms using requirements. |
| FR04 | Create bookings for customers and record the booking user. |
| FR05 | Support optional booking comments. |
| FR06 | Prevent overlapping bookings for the same classroom. |
| FR07 | View bookings by classroom. |
| FR08 | View bookings by customer. |
| FR09 | View all upcoming bookings. |
| FR10 | Store and retrieve data using SQL and JDBC. |
| FR11 | Provide a console-based user interface. |

## Business Rules

- Classroom capacity must be positive.
- Booking start time must precede end time.
- New bookings cannot start in the past.
- Classrooms must satisfy requested requirements.
- Overlapping bookings for the same room are prohibited.
- Adjacent bookings are permitted.
- Each booking identifies a customer, classroom and booking user.
- Booking comments are optional.

## Technical Approach

- Java 25 and Maven.
- MySQL and native JDBC.
- Simple UI, service, DAO and model packages.
- OOP, collections, streams and lambda expressions.
- Prepared SQL statements and exception handling.
- JUnit testing.
- Mermaid diagrams in Markdown.
- Incremental Git commits and feature branches.

## Scope Decisions

- One facility with 20 predefined classrooms.
- Identify booking users without a full authentication system.
- No payments, web interface or external integrations.
- No unnecessary frameworks or dependencies.
- Use fictional sample data.
- Never commit database credentials.

## Development Plan

1. Requirements and planning.
2. Architecture, UML and database design.
3. Maven and database setup.
4. Models, DAO and services.
5. Console interface.
6. Testing and final documentation.
7. Interview preparation.

## Acceptance Criteria

- All functional requirements work.
- Invalid input is handled correctly.
- Overlapping bookings are rejected.
- Adjacent bookings are accepted.
- Data persists after restarting the application.
- Automated tests pass.
- The implementation can be explained independently.

## Reference

[Official Assignment](https://github.com/Elnaz-Azizi/project-test-1/blob/main/ProjectTest_ClassRoomRentalApp.md)

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

## Acceptance Criteria

- All functional requirements work.
- Invalid input is handled correctly.
- Overlapping bookings are rejected.
- Adjacent bookings are accepted.
- Data persists after restarting the application.
- Automated tests pass.

## Reference

[Official Assignment](https://github.com/Elnaz-Azizi/project-test-1/blob/main/ProjectTest_ClassRoomRentalApp.md)

## Final acceptance checklist

Mark an item as passed only after executing it locally. Use a future date, fictional data, and the local development database.

| Requirement | Verification procedure | Expected result |
| --- | --- | --- |
| FR01 | Menu 1: register one individual and one company. | Both are saved with generated IDs and the correct types. |
| FR02 | Menu 3: list rooms; menu 4: update a room and list again. | 20 rooms; updated name, capacity, equipment and accessibility persist. Restore the original room details afterward. |
| FR03 | Menu 5: search with a future time range and minimum capacity, equipment and accessibility filters. | Only rooms satisfying every condition appear. |
| FR04 | Menu 2: register a booking user; menu 6: create a reservation using that customer and user. | A generated booking ID is displayed. |
| FR05 | Create a booking with comments and another with blank comments. | Comments are optional and saved when provided. |
| FR06 | Repeat the search for the booked interval, then try the room with an adjacent interval. | Booked room is excluded for overlapping times; adjacent interval is allowed. |
| FR07 | Menu 7: select the booked classroom ID. | Reservation is displayed. |
| FR08 | Menu 8: select the customer ID. | That customer's reservations are displayed. |
| FR09 | Menu 9: list upcoming bookings. | Future reservations are displayed in chronological order. |
| FR10 | Exit, restart and list the reservations again. | Records remain in MySQL. SQL CRUD is demonstrated by registration, listing, updating and cancellation. |
| FR11 | Try invalid menu options, impossible dates, invalid capacity and equipment, then choose 0. | Helpful errors; the application continues and exits normally. |

### Additional acceptance checks

- Cancel an upcoming test reservation using menu 10. Verify declining confirmation leaves it intact; confirming removes it and releases the classroom.
- Try a past date or an end time before the start time. Confirm the request is rejected.
- Run the full automated test suite with RUN_DB_TESTS=true on the local development database. Both integration tests must execute, not be skipped.
- Inspect the database after testing. Automated tests should remove their own temporary records; manual demonstration records may remain until explicitly removed.
- Verify that exiting via option 0 returns to PowerShell without the previous Connector/J cleanup warning.

### Reproducible manual demo

1. Launch the app and list classrooms (3).
2. Pick a future date, a two-hour interval and a suitable room; record its actual ID. Search (5) with capacity 10, equipment 2 (whiteboard) and accessibility as needed.
3. Register a fictional customer (1) and a linked booking user (2); record their IDs.
4. Create the booking (6) using the same search criteria and selected IDs, with a short comment.
5. Search again (5) and confirm that room is excluded for the original interval.
6. Search an interval starting exactly when the existing booking ends; the room should be offered if no other booking conflicts.
7. Show reports (7, 8, 9). Exit (0), restart and check that the reservation remains.
8. Cancel the demonstration booking (10), confirm, and search the original interval again.
9. Exit normally.

### Test evidence

The full local suite passed: **41 tests, 0 failures, 0 errors, 0 skipped**, including both MySQL integration tests. Application startup was verified and the user confirmed the manual console workflow works. Individual manual checklist results and the absence of the prior JDBC shutdown warning were not separately recorded.

All six development phases are complete and their branches have been merged into main. The checklist above remains available for repeatable verification.

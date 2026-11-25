-- Run once on a freshly initialized database. No real personal information.
USE classroom_rental;
START TRANSACTION;

INSERT INTO classrooms (name, capacity, accessible) VALUES
    ('Classroom 01', 10, TRUE),
    ('Classroom 02', 15, TRUE),
    ('Classroom 03', 20, TRUE),
    ('Classroom 04', 25, FALSE),
    ('Classroom 05', 30, TRUE),
    ('Classroom 06', 35, TRUE),
    ('Classroom 07', 40, TRUE),
    ('Classroom 08', 10, FALSE),
    ('Classroom 09', 15, TRUE),
    ('Classroom 10', 20, TRUE),
    ('Classroom 11', 25, TRUE),
    ('Classroom 12', 30, FALSE),
    ('Classroom 13', 35, TRUE),
    ('Classroom 14', 40, TRUE),
    ('Classroom 15', 10, TRUE),
    ('Classroom 16', 15, FALSE),
    ('Classroom 17', 20, TRUE),
    ('Classroom 18', 25, TRUE),
    ('Classroom 19', 30, TRUE),
    ('Classroom 20', 35, FALSE);

-- Look up generated room IDs rather than assuming their values.
INSERT INTO classroom_equipment (classroom_id, equipment)
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 01'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 01'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 02'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 02'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 03'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 04'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 04'
UNION ALL
SELECT id, 'VIDEO_CONFERENCE' FROM classrooms WHERE name = 'Classroom 04'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 05'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 05'
UNION ALL
SELECT id, 'COMPUTERS' FROM classrooms WHERE name = 'Classroom 05'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 06'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 07'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 07'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 08'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 08'
UNION ALL
SELECT id, 'VIDEO_CONFERENCE' FROM classrooms WHERE name = 'Classroom 08'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 09'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 10'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 10'
UNION ALL
SELECT id, 'COMPUTERS' FROM classrooms WHERE name = 'Classroom 10'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 11'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 11'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 12'
UNION ALL
SELECT id, 'VIDEO_CONFERENCE' FROM classrooms WHERE name = 'Classroom 12'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 13'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 13'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 14'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 14'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 15'
UNION ALL
SELECT id, 'COMPUTERS' FROM classrooms WHERE name = 'Classroom 15'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 16'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 16'
UNION ALL
SELECT id, 'VIDEO_CONFERENCE' FROM classrooms WHERE name = 'Classroom 16'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 17'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 17'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 18'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 19'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 19'
UNION ALL
SELECT id, 'WHITEBOARD' FROM classrooms WHERE name = 'Classroom 20'
UNION ALL
SELECT id, 'PROJECTOR' FROM classrooms WHERE name = 'Classroom 20'
UNION ALL
SELECT id, 'COMPUTERS' FROM classrooms WHERE name = 'Classroom 20'
UNION ALL
SELECT id, 'VIDEO_CONFERENCE' FROM classrooms WHERE name = 'Classroom 20';

INSERT INTO customers (name, email, customer_type)
VALUES ('Alex Example', 'alex@example.com', 'INDIVIDUAL');
SET @individual_id = LAST_INSERT_ID();

INSERT INTO customers (name, email, customer_type)
VALUES ('Example Training AB', 'office@example.org', 'COMPANY');
SET @company_id = LAST_INSERT_ID();

INSERT INTO booking_users (customer_id, name, email) VALUES
    (@individual_id, 'Alex Example', 'alex@example.com'),
    (@company_id, 'Sam Example', 'sam@example.org');

COMMIT;

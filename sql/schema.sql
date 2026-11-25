-- Run as a MySQL user with permission to create this database.
CREATE DATABASE IF NOT EXISTS classroom_rental
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE classroom_rental;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    customer_type VARCHAR(20) NOT NULL,
    CONSTRAINT chk_customer_type CHECK (customer_type IN ('INDIVIDUAL', 'COMPANY'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS booking_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    UNIQUE KEY uq_booking_user_customer_id (customer_id, id),
    CONSTRAINT fk_user_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS classrooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    is_accessible BOOLEAN NOT NULL,
    CONSTRAINT chk_classroom_capacity CHECK (capacity > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS classroom_equipment (
    classroom_id BIGINT NOT NULL,
    equipment VARCHAR(30) NOT NULL,
    PRIMARY KEY (classroom_id, equipment),
    CONSTRAINT fk_equipment_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    CONSTRAINT chk_equipment CHECK (equipment IN ('PROJECTOR', 'WHITEBOARD', 'COMPUTERS', 'VIDEO_CONFERENCE'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    booking_user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    comments VARCHAR(1000),
    CONSTRAINT chk_booking_time CHECK (end_time > start_time),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_booking_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    CONSTRAINT fk_booking_user_customer FOREIGN KEY (customer_id, booking_user_id)
        REFERENCES booking_users(customer_id, id),
    INDEX idx_booking_room_time (classroom_id, start_time, end_time),
    INDEX idx_booking_customer (customer_id, start_time),
    INDEX idx_booking_start (start_time)
) ENGINE=InnoDB;

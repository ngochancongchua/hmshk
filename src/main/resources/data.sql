-- Inserting sample staff members
INSERT INTO staff (name, username, password, email, phone_number, role, created_at)
VALUES
('Admin User', 'admin', 'admin123', 'admin@hotel.com', '1234567890', 'ADMIN', CURRENT_TIMESTAMP),
('Front Desk Staff', 'frontdesk', 'desk123', 'frontdesk@hotel.com', '2345678901', 'FRONT_DESK', CURRENT_TIMESTAMP),
('Housekeeping Staff', 'housekeeping', 'house123', 'housekeeping@hotel.com', '3456789012', 'HOUSEKEEPING', CURRENT_TIMESTAMP),
('Manager User', 'manager', 'manager123', 'manager@hotel.com', '4567890123', 'MANAGER', CURRENT_TIMESTAMP);

-- Inserting sample room types
INSERT INTO room (room_number, room_type, capacity, price_per_night, description, available, needs_cleaning)
VALUES
('101', 'SINGLE', 1, 100.00, 'Standard Single Room', true, false),
('102', 'SINGLE', 1, 100.00, 'Standard Single Room', true, false),
('201', 'DOUBLE', 2, 150.00, 'Standard Double Room', true, false),
('202', 'DOUBLE', 2, 150.00, 'Standard Double Room', true, false),
('301', 'TWIN', 2, 160.00, 'Twin Room with two single beds', true, false),
('302', 'TWIN', 2, 160.00, 'Twin Room with two single beds', true, false),
('401', 'SUITE', 4, 250.00, 'Luxury Suite with separate living area', true, false),
('501', 'DELUXE', 2, 200.00, 'Deluxe Room with premium amenities', true, false);

-- Inserting sample bookings
INSERT INTO booking (room_id, guest_name, guest_email, guest_phone, check_in_date, check_out_date, number_of_guests, total_price, status, created_by, created_at)
VALUES
(1, 'John Chan', 'john@example.com', '1234567890', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 1, 300.00, 'RESERVED', 1, CURRENT_TIMESTAMP),
(3, 'Jane Ng', 'jane@example.com', '2345678901', DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 2, 600.00, 'RESERVED', 2, CURRENT_TIMESTAMP),
(7, 'Robert Cheung', 'robert@example.com', '3456789012', DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), 3, 1250.00, 'RESERVED', 1, CURRENT_TIMESTAMP);

-- Update room availability based on bookings
UPDATE room SET available = false WHERE id IN (1, 3, 7);


-- Inserting sample facilities
INSERT INTO facility (name, description, cost_per_hour, capacity, location, available, created_at)
VALUES
('Conference Room A', 'Large conference room with projector and whiteboard', 50.00, 20, 'Ground Floor', true, CURRENT_TIMESTAMP()),
('Swimming Pool', 'Heated indoor swimming pool', 25.00, 30, 'Basement Level', true, CURRENT_TIMESTAMP()),
('Fitness Center', 'Fully equipped gym with cardio and weight equipment', 15.00, 25, 'Second Floor', true, CURRENT_TIMESTAMP()),
('Banquet Hall', 'Elegant banquet hall for events and celebrations', 100.00, 100, 'First Floor', true, CURRENT_TIMESTAMP());

-- Inserting sample customers
INSERT INTO customer (name, email, phone_number, address, id_type, id_number, date_of_birth, nationality, vip_status, active, special_requirements, created_at)
VALUES
('Chan Tai Man', 'chan@example.com', '9123-4567', '123 Nathan Rd, Tsim Sha Tsui, Kowloon, Hong Kong', 'Passport', 'P123456789', '1980-05-15', 'Hong Kong', 'Gold', true, 'Prefers quiet rooms', CURRENT_TIMESTAMP()),
('Lee Mei Ling', 'lee.meiling@example.com', '9234-5678', '456 Queen’s Rd, Central, Hong Kong', 'HKID', 'H123456789', '1985-10-20', 'Hong Kong', 'Regular', true, NULL, CURRENT_TIMESTAMP()),
('Wong Ka Wai', 'wong.ka.wai@example.com', '9345-6789', '789 King’s Rd, North Point, Hong Kong', 'National ID', 'ID12345678', '1975-03-25', 'Canada', 'Platinum', true, 'Allergic to feathers, needs hypoallergenic bedding', CURRENT_TIMESTAMP()),
('Ngai Siu Ping', 'ngai.siuping@example.com', '9456-7890', '101 Star St, Wan Chai, Hong Kong', 'Passport', 'P987654321', '1990-07-30', 'Spain', 'Silver', true, 'Early check-in when possible', CURRENT_TIMESTAMP());









INSERT INTO facility_booking (facility_id, customer_id, booker_name, booker_email, booker_phone, 
                             booking_date, start_time, end_time, number_of_people, total_cost, 
                             purpose, special_requirements, status, created_by, created_at)
VALUES 
-- Confirmed booking for Conference Room A by a regular customer
(1, 1, 'Chan Tai Man', 'chan@example.com', '9123-4567', 
 CURRENT_DATE, '10:00:00', '12:00:00', 10, 100.00, 
 'Business Meeting', 'Need projector setup', 'CONFIRMED', 1, CURRENT_TIMESTAMP),

-- Reserved booking for Swimming Pool by a hotel guest
(2, NULL, 'John Smith', 'john.smith@example.com', '8765-4321', 
 CURRENT_DATE, '14:00:00', '16:00:00', 4, 50.00, 
 'Family Recreation', 'Need towels', 'REQUESTED', 2, CURRENT_TIMESTAMP),

-- Completed booking for Fitness Center by a member customer
(3, 3, 'Wong Ka Wai', 'wong.ka.wai@example.com', '9345-6789', 
 DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '08:00:00', '09:00:00', 1, 15.00, 
 'Morning Workout', NULL, 'COMPLETED', 1, CURRENT_TIMESTAMP),

-- Cancelled booking for Banquet Hall
(4, 2, 'Lee Mei Ling', 'lee.meiling@example.com', '9234-5678', 
 DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), '18:00:00', '21:00:00', 50, 300.00, 
 'Birthday Party', 'Vegetarian catering required', 'CANCELLED', 1, CURRENT_TIMESTAMP),

-- Future booking for Conference Room A
(1, NULL, 'Sarah Johnson', 'sarah@example.com', '7654-3210', 
 DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '13:00:00', '15:00:00', 6, 100.00, 
 'Interview Session', 'Need quiet environment', 'REQUESTED', 2, CURRENT_TIMESTAMP),

-- Future booking for Swimming Pool
(2, 4, 'Ngai Siu Ping', 'ngai.siuping@example.com', '9456-7890', 
 DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '10:00:00', '11:00:00', 2, 25.00, 
 'Swimming Lesson', NULL, 'CONFIRMED', 1, CURRENT_TIMESTAMP);
 
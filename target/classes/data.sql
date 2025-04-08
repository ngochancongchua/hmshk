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

-- Inserting sample bookings for h2 database


INSERT INTO booking (room_id, guest_name, guest_email, guest_phone, check_in_date, check_out_date, number_of_guests, total_price, status, created_by, created_at) VALUES 
(1, 'John Chan', 'john@example.com', '1234567890', CURRENT_DATE, DATEADD('DAY', 3, CURRENT_DATE), 1, 300.00, 'RESERVED', 1, CURRENT_TIMESTAMP),
(3, 'Jane Ng', 'jane@example.com', '2345678901', DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 5, CURRENT_DATE), 2, 600.00, 'RESERVED', 2, CURRENT_TIMESTAMP),
(7, 'Robert Cheung', 'robert@example.com', '3456789012', DATEADD('DAY', 2, CURRENT_DATE), DATEADD('DAY', 7, CURRENT_DATE), 3, 1250.00, 'RESERVED', 1, CURRENT_TIMESTAMP);

-- for mysqwl
/*INSERT INTO booking (room_id, guest_name, guest_email, guest_phone, check_in_date, check_out_date, number_of_guests, total_price, status, created_by, created_at)
VALUES
(1, 'John Chan', 'john@example.com', '1234567890', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 1, 300.00, 'RESERVED', 1, CURRENT_TIMESTAMP),
(3, 'Jane Ng', 'jane@example.com', '2345678901', DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 2, 600.00, 'RESERVED', 2, CURRENT_TIMESTAMP),
(7, 'Robert Cheung', 'robert@example.com', '3456789012', DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), 3, 1250.00, 'RESERVED', 1, CURRENT_TIMESTAMP);
*/

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

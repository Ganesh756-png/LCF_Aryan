-- Local Service Finder Database Schema
-- Compatible with Supabase (PostgreSQL)

-- Enable UUID extension if not enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Clean up existing tables (Optional/For re-runs)
DROP TABLE IF EXISTS feedback CASCADE;
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS services CASCADE;
DROP TABLE IF EXISTS service_categories CASCADE;
DROP TABLE IF EXISTS service_providers CASCADE;
DROP TABLE IF EXISTS admin CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. Users Table (Core profiles)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Synchronized from Auth or used for manual JWT sign-in
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'USER', 'PROVIDER')),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    profile_image VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Service Providers Table (Extension of Users with role 'PROVIDER')
CREATE TABLE service_providers (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(255),
    bio TEXT,
    address VARCHAR(500),
    city VARCHAR(100),
    years_of_experience INT DEFAULT 0,
    whatsapp_number VARCHAR(50),
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Admins Table (Extension of Users with role 'ADMIN')
CREATE TABLE admin (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    is_super BOOLEAN DEFAULT FALSE
);

-- 4. Service Categories Table
CREATE TABLE service_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_class VARCHAR(100), -- CSS Class for icons (FontAwesome/Bootstrap Icons)
    image_url VARCHAR(500)
);

-- 5. Services Table
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES service_providers(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES service_categories(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_rate DECIMAL(10, 2) NOT NULL,
    duration VARCHAR(100) DEFAULT 'Per Hour', -- e.g., 'Per Hour', 'Per Service', 'Fixed'
    is_premium BOOLEAN DEFAULT FALSE
);

-- 6. Bookings Table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    booking_date TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    notes TEXT,
    total_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Ratings Table (Reviews for providers)
CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_id UUID NOT NULL REFERENCES service_providers(id) ON DELETE CASCADE,
    rating_value INT NOT NULL CHECK (rating_value BETWEEN 1 AND 5),
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Feedback Table (Support/Complaints to admin)
CREATE TABLE feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES for Search Optimization
CREATE INDEX idx_services_category ON services(category_id);
CREATE INDEX idx_providers_city ON service_providers(city);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_provider ON services(provider_id);

-- ==========================================
-- SEED DATA
-- ==========================================

-- Insert Service Categories
INSERT INTO service_categories (name, description, icon_class, image_url) VALUES
('Plumbing', 'Leaking taps, toilet repairs, pipe installations, and drainage solutions', 'bi bi-droplet-fill', 'https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=500&auto=format&fit=crop&q=60'),
('Electrical', 'Wiring, switch replacements, appliance installation, and safety checks', 'bi bi-lightning-charge-fill', 'https://images.unsplash.com/photo-1621905252507-b354bc25edac?w=500&auto=format&fit=crop&q=60'),
('Cleaning', 'Deep home cleaning, kitchen sanitization, sofa cleaning, and carpet washing', 'bi bi-stars', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=500&auto=format&fit=crop&q=60'),
('Carpentry', 'Furniture repairs, custom woodwork, modular kitchens, and door fixes', 'bi bi-tools', 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=500&auto=format&fit=crop&q=60'),
('Painting', 'Interior/exterior house painting, wall stenciling, and wallpaper fixes', 'bi bi-paint-bucket', 'https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=500&auto=format&fit=crop&q=60'),
('Tutors', 'Academic tutoring, music classes, languages, and technical training', 'bi bi-book-half', 'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=500&auto=format&fit=crop&q=60'),
('Mechanics', 'Car repairs, bike servicing, roadside assistance, and battery replacements', 'bi bi-wrench-adjustable', 'https://images.unsplash.com/photo-1486006920555-c77dce18193b?w=500&auto=format&fit=crop&q=60'),
('AC Repair', 'AC servicing, gas refilling, filter cleaning, and compressor repair', 'bi bi-wind', 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=500&auto=format&fit=crop&q=60');

-- Insert Mock Users (UUIDs pre-generated for seeding)
-- Password for mock users: '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq' (BCrypt hashed of 'password')
INSERT INTO users (id, email, password_hash, full_name, phone, role, status, profile_image) VALUES
('aa78e762-b2d9-48bb-bb83-8a39e9921f66', 'admin@servicefinder.com', '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq', 'Super Admin', '+911234567890', 'ADMIN', 'ACTIVE', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80'),
('bb78e762-b2d9-48bb-bb83-8a39e9921f67', 'provider.john@servicefinder.com', '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq', 'Rajesh Kumar (Plumber)', '+919876543210', 'PROVIDER', 'ACTIVE', 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=150&auto=format&fit=crop&q=80'),
('cc78e762-b2d9-48bb-bb83-8a39e9921f68', 'provider.alice@servicefinder.com', '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq', 'Amit Sharma (Electrician)', '+919876543220', 'PROVIDER', 'ACTIVE', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80'),
('dd78e762-b2d9-48bb-bb83-8a39e9921f70', 'provider.kiran@servicefinder.com', '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq', 'Kiran Rao (Cleaning)', '+919876543230', 'PROVIDER', 'ACTIVE', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80'),
('ee78e762-b2d9-48bb-bb83-8a39e9921f71', 'provider.sunita@servicefinder.com', '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq', 'Sunita Singh (Tutor)', '+919876543240', 'PROVIDER', 'ACTIVE', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80'),
('dd78e762-b2d9-48bb-bb83-8a39e9921f69', 'customer.bob@servicefinder.com', '$2a$10$tZ2zB.0R3w83PzC9u68Gcu1mN/c.nL20R.7oM.lK3wTj9hGvpe3Dq', 'Bob Smith', '+914443332220', 'USER', 'ACTIVE', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80');

-- Insert Admins
INSERT INTO admin (id, is_super) VALUES
('aa78e762-b2d9-48bb-bb83-8a39e9921f66', TRUE);

-- Insert Service Providers details
INSERT INTO service_providers (id, business_name, bio, address, city, years_of_experience, whatsapp_number, is_approved) VALUES
('bb78e762-b2d9-48bb-bb83-8a39e9921f67', 'Rajesh Plumbing Solutions', 'Experienced in residential and commercial plumbing repairs, leak detection, and installations across Delhi NCR.', 'A-Block, Connaught Place', 'Delhi', 8, '9876543210', TRUE),
('cc78e762-b2d9-48bb-bb83-8a39e9921f68', 'Sparky Electrical Solutions', 'Certified electrician specialized in house rewiring, smart home installations, and safety inspections.', 'Link Road, Bandra West', 'Mumbai', 5, '9876543220', FALSE), -- Pending approval
('dd78e762-b2d9-48bb-bb83-8a39e9921f70', 'Kiran Deep Cleaning Services', 'Professional deep home cleaning, sofa sanitization, modular kitchen cleaning and sanitization services.', '100 Feet Rd, Indiranagar', 'Bengaluru', 6, '9876543230', TRUE),
('ee78e762-b2d9-48bb-bb83-8a39e9921f71', 'Sunita Academy Home Tutors', 'Providing home tutoring services for school classes, college mathematics, physics and chemistry.', 'Anna Salai, T. Nagar', 'Chennai', 10, '9876543240', TRUE);

-- Insert Services for Providers
INSERT INTO services (provider_id, category_id, title, description, price_rate, duration, is_premium) VALUES
('bb78e762-b2d9-48bb-bb83-8a39e9921f67', 1, 'Emergency Leak Repair', 'Fixing burst pipes, toilet leaks, and tap leakages immediately.', 300.00, 'Per Hour', TRUE),
('bb78e762-b2d9-48bb-bb83-8a39e9921f67', 1, 'Water Heater Installation', 'Installation of modern energy-efficient instant and storage water heaters.', 1200.00, 'Fixed Price', FALSE),
('cc78e762-b2d9-48bb-bb83-8a39e9921f68', 2, 'House Electrical Inspection', 'Full inspection of outlets, breaker box, and main line with official report.', 800.00, 'Fixed Price', TRUE),
('dd78e762-b2d9-48bb-bb83-8a39e9921f70', 3, 'Full Home Deep Cleaning', 'Complete sanitization and deep cleaning of 2BHK/3BHK apartments using eco-friendly materials.', 3500.00, 'Fixed Price', TRUE),
('ee78e762-b2d9-48bb-bb83-8a39e9921f71', 6, 'Maths & Science Home Tuition', 'Regular tutoring classes for 8th to 12th grade students, preparing for exams.', 500.00, 'Per Hour', FALSE);

-- Insert Mock Booking
INSERT INTO bookings (id, user_id, service_id, booking_date, status, notes, total_price) VALUES
(1001, 'dd78e762-b2d9-48bb-bb83-8a39e9921f69', 1, '2026-06-25 10:00:00', 'PENDING', 'My kitchen faucet is leaking heavily. Need help quickly.', 300.00);

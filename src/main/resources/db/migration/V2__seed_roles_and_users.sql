-- Seed roles
insert into roles (id, name, description, created_at) values
    ('11111111-1111-1111-1111-111111111111', 'admin', 'Administrator', current_timestamp),
    ('22222222-2222-2222-2222-222222222222', 'user', 'Regular User', current_timestamp),
    ('33333333-3333-3333-3333-333333333333', 'super_root', 'Super Root', current_timestamp);

-- Seed users (root)
-- password=user123
insert into users (id, role_id, email, password, fullname, phone, address, remark, created_at) values
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'super@root.com', '$2a$10$MpqKG5E2eg0SZrhiYriGveYvoyVFlST6w0ZB5VdKHOSEbfeUyxMVO', 'Administrator', '081234567890', 'Jakarta, Indonesia', 'Super root', current_timestamp);

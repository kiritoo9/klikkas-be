-- Seed roles
insert into roles (id, name, description, created_at) values
    ('11111111-1111-1111-1111-111111111111', 'admin', 'Administrator', current_timestamp),
    ('22222222-2222-2222-2222-222222222222', 'user', 'Regular User', current_timestamp),
    ('33333333-3333-3333-3333-333333333333', 'super_root', 'Super Root', current_timestamp);

-- Seed users (root)
-- password=user123
insert into tenants (id, code, name, description, remark, is_active, created_at) values (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ROOTTENANT', 'RootTenant', 'Tenant for super root',
    'Tenant for super root', true, current_timestamp
);

insert into users (id, role_id, email, password, fullname, phone, address, remark, google_id, created_at) values (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'super@root.com', 
    '$2a$10$MpqKG5E2eg0SZrhiYriGveYvoyVFlST6w0ZB5VdKHOSEbfeUyxMVO', 'Administrator', '081234567890', 
    'Jakarta, Indonesia', 'Super root', null, current_timestamp
);

insert into user_tenants (id, user_id, tenant_id, remark, created_at) values (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'data_inject', current_timestamp
);
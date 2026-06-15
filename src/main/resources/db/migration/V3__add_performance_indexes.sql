-- Indexes for users table
create index idx_users_email on users(email);
create index idx_users_role_id on users(role_id);
create index idx_users_deleted_at on users(deleted_at);

-- Indexes for roles table
create index idx_roles_name on roles(name);
create index idx_roles_deleted_at on roles(deleted_at);

-- Indexes for orders table
create index idx_orders_category_id on orders(category_id);
create index idx_orders_deleted_at on orders(deleted_at);
create index idx_orders_status on orders(status);

-- Indexes for order_items table
create index idx_order_items_order_id on order_items(order_id);
create index idx_order_items_product_id on order_items(product_id);

-- Indexes for products table
create index idx_products_category_id on products(category_id);
create index idx_products_deleted_at on products(deleted_at);
create index idx_products_sku on products(sku);

-- Indexes for categories
create index idx_order_categories_deleted_at on order_categories(deleted_at);
create index idx_product_categories_deleted_at on product_categories(deleted_at);

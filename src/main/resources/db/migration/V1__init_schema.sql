-- Tables with no dependencies
create table roles (
    id uuid primary key,
    name varchar(50),
    description text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null
);

create table tenants (
    id uuid primary key,
    code varchar(50) unique,

    name varchar(150),
    description text,
    remark text,
    is_active boolean default false,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null
);

-- Depends on tenants
create table order_categories (
    id uuid primary key,
    tenant_id uuid,

    name varchar(150),
    description text,
    category_type varchar(50) check (
        category_type in ('kas_masuk', 'kas_keluar')
    ) default 'kas_masuk',
    is_active boolean default false,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (tenant_id) references tenants(id) on delete cascade
);

create table product_categories (
    id uuid primary key,
    tenant_id uuid,

    name varchar(150),
    description text,
    is_active boolean default false,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (tenant_id) references tenants(id) on delete cascade
);

-- Depends on roles
create table users (
    id uuid primary key,
    role_id uuid,

    email varchar(150) not null,
    password varchar(100) not null,
    fullname varchar(100) not null,

    phone varchar(20),
    address text,
    remark text,

    google_id text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (role_id) references roles(id) on delete cascade
);

-- Depends on users and tenants
create table user_tenants (
    id uuid primary key,
    user_id uuid,
    tenant_id uuid,

    remark text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (user_id) references users(id) on delete cascade,
    foreign key (tenant_id) references tenants(id) on delete cascade
);

-- Depends on product_categories
create table products (
    id uuid primary key,
    category_id uuid,
    tenant_id uuid,

    sku varchar(150),
    name varchar(150),
    description text,

    buy_price numeric default 0,
    sell_price numeric default 0,
    stock numeric default 0,

    img_name varchar(100),
    img_path varchar(150),
    is_active boolean default false,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (category_id) references product_categories(id) on delete cascade,
    foreign key (tenant_id) references tenants(id) on delete cascade
);

-- Depends on order_categories (and later products)
create table orders (
    id uuid primary key,
    category_id uuid,
    tenant_id uuid,
    
    no_order varchar(150),
    order_date timestamp default null,
    total_qty numeric default 0,
    total_price numeric default 0,
    tax_amount numeric default 0,
    discount_amount numeric default 0,
    grand_total numeric default 0,

    status varchar(50) check (
         status in (
            'pending', 'paid', 'canceled'
        )
    ),
    remark text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (category_id) references order_categories(id) on delete cascade,
    foreign key (tenant_id) references tenants(id) on delete cascade
);

-- Depends on orders and products
create table order_items (
    id uuid primary key,
    order_id uuid,
    product_id uuid default null,

    item_name text,
    item_desc text,
    price numeric default 0,
    qty numeric default 0,
    remark text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (order_id) references orders(id) on delete cascade,
    foreign key (product_id) references products(id) on delete cascade
);

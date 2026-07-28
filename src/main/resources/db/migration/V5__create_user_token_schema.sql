create table packages (
    id uuid primary key,
    code varchar(20),
    name varchar(100),

    limit_token numeric default 0,
    description text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null
);

insert into packages (id, code, name, limit_token, description, created_at) values
    ('11111111-1111-1111-1111-111111111111', 'STR1', 'Starter', 10000, 'Paket Starter', current_timestamp),
    ('22222222-2222-2222-2222-222222222222', 'PRE1', 'Premium', 1000000, 'Paket Premium', current_timestamp);

create table user_packages (
    id uuid primary key,
    user_id uuid,
    package_id uuid,

    start_at timestamp default null,
    end_at timestamp default null,
    is_active boolean default false,
    remark text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (user_id) references users(id) on delete cascade,
    foreign key (package_id) references packages(id) on delete cascade
);

create table user_agents (
    id uuid primary key,
    user_id uuid,

    name text,
    shortname varchar(100),
    lang varchar(20),
    tone varchar(50),
    base_knowledge text,
    icon varchar(50),

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null,

    foreign key (user_id) references users(id) on delete cascade
);

create table user_tokens (
    id uuid primary key,
    user_id uuid,

    limit_token numeric default 0,
    usage_token numeric default 0,
    reset_at timestamp default null,

    updated_at timestamp default null,
    foreign key (user_id) references users(id) on delete cascade
);

create table user_token_usages (
    id uuid primary key,
    user_id uuid,

    usage_title text,
    llm_model varchar(100),
    input_token numeric default 0,
    output_token numeric default 0,
    payload_body jsonb default null,
    response_body jsonb default null,
    
    is_failure boolean default false,
    failure_message text,

    created_at timestamp,
    foreign key (user_id) references users(id) on delete cascade
);
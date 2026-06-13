create table users (
    id uuid primary key,
    email varchar(150) not null,
    password varchar(100) not null,
    fullname varchar(100) not null,

    phone varchar(20),
    address text,
    remark text,

    created_at timestamp,
    updated_at timestamp default null,
    deleted_at timestamp default null
);
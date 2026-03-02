--liquibase formatted sql
--changeset Aravindh:VNG-01
create table company(
    id INTEGER PRIMARY KEY,
    company_name VARCHAR(255) UNIQUE NOT NULL,
    industry_type_id integer not null references industry_type,
    country_id INTEGER NOT NULL references country,
    state_id integer not null references state,
    address TEXT,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(255),
    tag_line VARCHAR(255)
);
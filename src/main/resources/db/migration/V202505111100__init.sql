create table notification_instance
(
    uuid uuid not null,
    name varchar(255) not null,
    -- webhook url
    url text,
    primary key (uuid)
);

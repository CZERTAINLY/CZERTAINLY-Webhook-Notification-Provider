create table notification_instance
(
    uuid uuid not null,
    name varchar(255) not null,
    -- webhook url
    url text not null,
    -- content type
    content_type varchar(255) not null,
    -- base64 encoded content template
    content_template text,
    attributes text,
    primary key (uuid)
);

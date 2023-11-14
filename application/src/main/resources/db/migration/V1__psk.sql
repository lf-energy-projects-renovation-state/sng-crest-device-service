create table psk
(
    revision_time_stamp timestamp(6) with time zone not null,
    identity            varchar(255)                not null,
    key                 varchar(255)                not null,
    primary key (revision_time_stamp, identity)
);

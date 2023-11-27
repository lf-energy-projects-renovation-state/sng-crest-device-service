create table pre_shared_key
(
    revision_time  timestamp(6) without time zone not null,
    identity       varchar(255)                   not null,
    pre_shared_key varchar(255)                   not null,
    primary key (revision_time, identity)
);

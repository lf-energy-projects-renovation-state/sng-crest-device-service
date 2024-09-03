create table command
(
    id              uuid            not null,
    device_id        varchar(255)    not null,
    correlation_id   uuid            not null,
    timestamp_issued timestamp       not null,
    type            varchar(255)    not null,
    command_value    varchar(255),
    status          varchar(255)    not null
);

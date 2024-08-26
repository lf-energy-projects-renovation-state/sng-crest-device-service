create table command
(
    id              uuid            not null,
    deviceId        varchar(255)    not null,
    correlationId   uuid            not null,
    timestampIssued timestamp       not null,
    type            varchar(255)    not null,
    commandValue    varchar(255)    not null,
    status          varchar(255)    not null
);

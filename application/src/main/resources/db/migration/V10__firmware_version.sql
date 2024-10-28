alter table firmware
    drop column hash;

alter table firmware
    add column version varchar(255) not null;
create table device (
    id varchar(16) not null,
    secret varchar(255) not null,

    primary key (id)
);

insert into device (id, secret)
select identity, secret
from pre_shared_key;

alter table pre_shared_key
drop column secret;

create table device (
    id varchar(15) not null,
    secret varchar(64) not null,

    primary key (id)
);

insert into device (id, secret)
select identity, secret
from pre_shared_key;

alter table pre_shared_key
drop column secret;

-- SPDX-FileCopyrightText: Contributors to the GXF project
--
-- SPDX-License-Identifier: Apache-2.0
alter table pre_shared_key
    add column status varchar(8) not null default 'ACTIVE';

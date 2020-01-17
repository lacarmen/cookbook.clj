create table recipe
(id serial primary key,
 tags jsonb,
 recipe jsonb,
 author varchar(20) references users(id),
 deleted boolean,
 created timestamp not null default (now()),
 last_updated timestamp not null default (now()));

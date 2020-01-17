create table users
(id varchar(20) primary key,
 first_name varchar(30),
 last_name varchar(30),
 email varchar(30),
 admin boolean,
 last_login timestamp,
 is_active boolean default true,
 pass varchar(300));

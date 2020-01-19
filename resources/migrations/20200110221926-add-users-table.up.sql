create table users
(id varchar(20) primary key,
 first_name varchar(30),
 last_name varchar(30),
 email varchar(30),
 admin boolean default false,
 last_login timestamp,
 active boolean default true,
 pass varchar(300));

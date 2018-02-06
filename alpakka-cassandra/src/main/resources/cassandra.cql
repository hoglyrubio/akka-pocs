CREATE KEYSPACE alpakka_tests WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

create table if not exists alpakka_tests.streaming (
    code int,
    name text,
    primary key (code));

insert into alpakka_tests.streaming (code, name) values (1, 'one');
insert into alpakka_tests.streaming (code, name) values (2, 'two');
insert into alpakka_tests.streaming (code, name) values (3, 'three');
insert into alpakka_tests.streaming (code, name) values (4, 'four');
insert into alpakka_tests.streaming (code, name) values (5, 'five');
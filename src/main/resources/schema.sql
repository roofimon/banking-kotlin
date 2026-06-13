drop table ACCOUNT_EVENT if exists;
drop table ACCOUNT if exists;
create table ACCOUNT (ID varchar(9) primary key, BALANCE double not null);
create table ACCOUNT_EVENT (
    ID bigint auto_increment primary key,
    ACCOUNT_ID varchar(9) not null,
    EVENT_TYPE varchar(10) not null,
    AMOUNT double not null,
    OCCURRED_AT timestamp not null
);

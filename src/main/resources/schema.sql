drop table CUSTOMER if exists;
drop table ONBOARDING if exists;
drop table TRANSFER_RECEIPT if exists;
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
create table TRANSFER_RECEIPT (
    ID bigint auto_increment primary key,
    TRANSFER_ID varchar(36) not null,
    SRC_ACCOUNT_ID varchar(9) not null,
    DST_ACCOUNT_ID varchar(9) not null,
    TRANSFER_AMOUNT double not null,
    FEE_AMOUNT double not null,
    SRC_FINAL_BALANCE double not null,
    DST_FINAL_BALANCE double not null,
    CREATED_AT timestamp not null
);
create table ONBOARDING (
    ID varchar(36) primary key,
    EMAIL varchar(255) not null,
    STATUS varchar(20) not null,
    EMAIL_CODE varchar(6) not null,
    NAME varchar(255),
    PHONE varchar(30),
    SESSION_TOKEN varchar(36),
    CREDIT_SCORE int,
    ACCOUNT_ID varchar(9),
    CREATED_AT timestamp not null
);
create table CUSTOMER (
    ACCOUNT_ID varchar(9) primary key,
    EMAIL varchar(255) not null,
    NAME varchar(255) not null,
    PHONE varchar(30) not null,
    PASSWORD varchar(5) not null,
    CREDIT_SCORE int not null,
    CREATED_AT timestamp not null
);

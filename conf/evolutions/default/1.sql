# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table environment (
  environment_id                integer,
  environment_key               varchar(255),
  create_date                   timestamptz
);


# --- !Downs

drop table if exists environment cascade;


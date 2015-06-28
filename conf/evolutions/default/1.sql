# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table media_file (
  id                        bigint not null,
  checksum                  varchar(255),
  filename                  varchar(255),
  constraint pk_media_file primary key (id))
;

create sequence media_file_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists media_file;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists media_file_seq;


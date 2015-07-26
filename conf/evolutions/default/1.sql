# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table media_file (
  id                        bigint not null,
  checksum                  varchar(255),
  filename                  varchar(255),
  path                      varchar(255),
  constraint pk_media_file primary key (id))
;

create table property (
  id                        bigint not null,
  k                         varchar(255),
  v                         TEXT,
  media_file_id             bigint,
  constraint pk_property primary key (id))
;

create table tag (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_tag primary key (id))
;


create table media_file_tag (
  media_file_id                  bigint not null,
  tag_id                         bigint not null,
  constraint pk_media_file_tag primary key (media_file_id, tag_id))
;
create sequence media_file_seq;

create sequence property_seq;

create sequence tag_seq;

alter table property add constraint fk_property_mediaFile_1 foreign key (media_file_id) references media_file (id) on delete restrict on update restrict;
create index ix_property_mediaFile_1 on property (media_file_id);



alter table media_file_tag add constraint fk_media_file_tag_media_file_01 foreign key (media_file_id) references media_file (id) on delete restrict on update restrict;

alter table media_file_tag add constraint fk_media_file_tag_tag_02 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists media_file;

drop table if exists media_file_tag;

drop table if exists property;

drop table if exists tag;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists media_file_seq;

drop sequence if exists property_seq;

drop sequence if exists tag_seq;


# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table configuration (
  id                        bigint auto_increment not null,
  k                         varchar(255),
  v                         TEXT,
  hostname                  varchar(255),
  constraint pk_configuration primary key (id))
;

create table media_file (
  id                        bigint auto_increment not null,
  filepath                  varchar(255),
  checksum                  varchar(255),
  filename                  varchar(255),
  views                     integer,
  filesize                  bigint,
  mime_type                 varchar(255),
  last_check                timestamp,
  created                   timestamp,
  cover_id                  bigint,
  folder_id                 bigint,
  constraint pk_media_file primary key (id))
;

create table media_folder (
  id                        bigint auto_increment not null,
  path                      TEXT,
  name                      varchar(255),
  parent_id                 bigint,
  constraint pk_media_folder primary key (id))
;

create table property (
  id                        bigint auto_increment not null,
  k                         varchar(255),
  v                         TEXT,
  media_file_id             bigint,
  constraint pk_property primary key (id))
;

create table tag (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  constraint pk_tag primary key (id))
;

create table thumbnail (
  id                        bigint auto_increment not null,
  filepath                  varchar(255),
  checksum                  varchar(255),
  media_file_id             bigint,
  constraint pk_thumbnail primary key (id))
;


create table media_file_tag (
  media_file_id                  bigint not null,
  tag_id                         bigint not null,
  constraint pk_media_file_tag primary key (media_file_id, tag_id))
;
alter table media_file add constraint fk_media_file_cover_1 foreign key (cover_id) references thumbnail (id) on delete restrict on update restrict;
create index ix_media_file_cover_1 on media_file (cover_id);
alter table media_file add constraint fk_media_file_folder_2 foreign key (folder_id) references media_folder (id) on delete restrict on update restrict;
create index ix_media_file_folder_2 on media_file (folder_id);
alter table media_folder add constraint fk_media_folder_parent_3 foreign key (parent_id) references media_folder (id) on delete restrict on update restrict;
create index ix_media_folder_parent_3 on media_folder (parent_id);
alter table property add constraint fk_property_mediaFile_4 foreign key (media_file_id) references media_file (id) on delete restrict on update restrict;
create index ix_property_mediaFile_4 on property (media_file_id);
alter table thumbnail add constraint fk_thumbnail_mediaFile_5 foreign key (media_file_id) references media_file (id) on delete restrict on update restrict;
create index ix_thumbnail_mediaFile_5 on thumbnail (media_file_id);



alter table media_file_tag add constraint fk_media_file_tag_media_file_01 foreign key (media_file_id) references media_file (id) on delete restrict on update restrict;

alter table media_file_tag add constraint fk_media_file_tag_tag_02 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists configuration;

drop table if exists media_file;

drop table if exists media_file_tag;

drop table if exists media_folder;

drop table if exists property;

drop table if exists tag;

drop table if exists thumbnail;

SET REFERENTIAL_INTEGRITY TRUE;


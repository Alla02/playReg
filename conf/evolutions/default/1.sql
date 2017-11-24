# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table feature (
  id                            bigint auto_increment not null,
  title                         varchar(255),
  description                   varchar(255),
  image_url                     varchar(255),
  constraint pk_feature primary key (id)
);


# --- !Downs

drop table if exists feature;


# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table environment (
  env_id                        integer,
  environment_key               varchar(255),
  create_date                   timestamptz
);

create table inventory (
  inventory_id                  serial not null,
  env_id                        varchar(255),
  create_date                   timestamptz,
  update_date                   timestamptz,
  inventory_key                 varchar(255),
  inventory_alias               varchar(255),
  current_snapshot              varchar(255),
  current_transaction           varchar(255),
  constraint pk_inventory primary key (inventory_id)
);

create table inventory_item (
  inventory_item_id             serial not null,
  env_id                        varchar(255),
  create_date                   timestamptz,
  update_date                   timestamptz,
  inventory_key                 varchar(255),
  product_id                    integer,
  count                         float,
  constraint pk_inventory_item primary key (inventory_item_id)
);

create table inventory_snapshot (
  inventory_snapshot_id         serial not null,
  env_id                        varchar(255),
  create_date                   timestamptz,
  update_date                   timestamptz,
  snapshot_key                  varchar(255),
  constraint pk_inventory_snapshot primary key (inventory_snapshot_id)
);

create table inventory_transaction (
  transaction_id                serial not null,
  env_id                        varchar(255),
  create_date                   timestamptz,
  update_date                   timestamptz,
  transaction_key               varchar(255),
  product_id                    integer,
  difference                    float,
  inventory_key                 varchar(255),
  constraint pk_inventory_transaction primary key (transaction_id)
);

create table product (
  product_id                    serial not null,
  env_id                        varchar(255),
  create_date                   timestamptz,
  update_date                   timestamptz,
  product_name                  varchar(255),
  product_description           varchar(255),
  product_uom                   varchar(255),
  constraint pk_product primary key (product_id)
);

create table uom (
  uom_name                      varchar(255) not null,
  env_id                        varchar(255),
  create_date                   timestamptz,
  update_date                   timestamptz,
  uom_type                      varchar(255),
  constraint pk_uom primary key (uom_name)
);


# --- !Downs

drop table if exists environment cascade;

drop table if exists inventory cascade;

drop table if exists inventory_item cascade;

drop table if exists inventory_snapshot cascade;

drop table if exists inventory_transaction cascade;

drop table if exists product cascade;

drop table if exists uom cascade;


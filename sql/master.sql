create table environment
(
  environment_id  serial,
  environment_key text,
  create_date     timestamp default now()
);


create table product
(
  product_id          serial,
  product_name        text,
  product_description text,
  product_uom         text, -- foreign key
  env_id              text,
  create_date         timestamp default now(),
  update_date         timestamp,
  constraint product_pkey primary key (product_id, env_id),
  constraint unq_prod unique (product_name, env_id)
);


create table uom
(
  uom_name    text,
  uom_type    text,
  env_id      text,
  create_date timestamp default now(),
  update_date timestamp default now(),
  constraint uom_pkey primary key (uom_name, env_id)
);


create table inventory
(
  inventory_id        serial,
  inventory_alias     text,
  inventory_key       text,
  current_snapshot    text,
  current_transaction text,
  env_id              text,
  create_date         timestamp default now(),
  update_date         timestamp
);


create table inventory_snapshot
(
  inventory_snapshot_id serial,
  snapshot_key          text,
  env_id                text,
  create_date           timestamp default now(),
  update_date           timestamp,
  constraint unq_snap_key UNIQUE (snapshot_key)
);


create table inventory_transaction
(
  transaction_id  serial,
  transaction_key text,
  product_id      int,
  difference      decimal,
  inventory_key   text,
  env_id         text,
  create_date     timestamp default now(),
  update_date timestamp
);

create table inventory_item
(
  inventory_item_id serial,
  inventory_key     text,
  product_id        int,
  count             decimal,
  env_id            text,
  create_date       timestamp default now(),
  update_date       timestamp,
  constraint inventory_item_pkey primary key (inventory_item_id)
);

create table environment
(
environment_id serial,
environment_key text,
create_date timestamp default now()
);


create table product
(
  product_id serial,
  product_name text,
  product_description text,
  product_uom text references uom(uom_name), -- foreign key
  env_id text,
  create_date timestamp default now(),
  update_date timestamp,
  constraint product_pkey primary key (product_id, env_id)
);


create table uom
(
  uom_name text,
  uom_type text,
  env_id text,
  create_date timestamp default now(),
  constraint uom_pkey primary key (uom_name, env_id)
);


create table inventory
(
inventory_id serial,
inventory_alias text,
current_version int default 0,
create_date timestamp default now(),
update_date timestamp,
);


create table inventory_item
(
inventory_item_id serial,
inventory_id int references inventory(inventory_id),
product_id int references product(product_id),
count decimal,
create_date timestamp default now(),
constraint inventory_item_pkey primary key (inventory_id)
);
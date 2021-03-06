[one-to-many]
create table Product (
	id serial not null,
	title varchar(100) not null,
	primary key(id)
)
;
create table Price (
	currency varchar(3) not null,
	unitprice decimal(6,3),
	saleprice decimal(6,3),
	product_id bigint unsigned not null,
	primary key (product_id,currency,unitprice),
	foreign key (product_id) references Product(id)
)
;

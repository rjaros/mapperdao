Assuming:

```
create schema test

create table test.Product (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)

create table test.Attribute (
	id serial not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)

create table test.Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key (product_id,attribute_id),
	foreign key (product_id) references test.Product(id) on delete cascade on update cascade,
	foreign key (attribute_id) references test.Attribute(id) on delete cascade on update cascade
)
```

Override databaseSchema :

```
	case class Product(name: String, attributes: Set[Attribute])

	case class Attribute(name: String, value: String)

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		override val databaseSchema = Some(Schema("test"))

		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m: ValuesMap) = new Product(name, attributes) with Stored
		{
			val id: Int = ProductEntity.id
		}
	}

	object AttributeEntity extends Entity[Int, SurrogateIntId, Attribute]
	{
		override val databaseSchema = Some(Schema("test"))

		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m: ValuesMap) = new Attribute(name, value) with Stored
		{
			val id: Int = AttributeEntity.id
		}
	}
```

Note: for many-to-many, you might want to declare the schema of the intermediate table (if it differs from the main tables):

```
		val attributes = manytomany(AttributeEntity) schema(Schema("other")) to (_.attributes)
```
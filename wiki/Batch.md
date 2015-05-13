MapperDao uses batch inserts and updates most of the time (depending on the capabilities
of the jdbc driver) even when 1 entity is inserted/updated. But the most efficient way to
insert or update a lot of entities in one go is via the insertBatch and updateBatch
methods of MapperDao and CRUD traits.

```
	val List(p1,p2,p3)=mapperDao.insertBatch(PersonEntity,List(person1,person2,person3))
```

The updateBatch method has 2 variations, one for mutable and one for immutable entities.

```
	// immutable
	val List(up1,up2)=mapperDao.updateBatch(PersonEntity,List((oldValue1,newValue1),(oldValue2,newValue2)))

	// mutable
	val List(up1,up2)=mapperDao.updateBatchMutable(PersonEntity,List(person1,person2))
```


Driver compatibility:

Not all drivers support batch inserts or updates always. It depends on jdbc driver support for batches when
inserting data with autogenerated columns. Here is the compatibility list:

```
Database		Batch support
-----------------------------------
Derby			inserts in batches only if no values are autogenerated.
			Updates are in batches always

H2			inserts in batches only if no values are autogenerated
			Updates are in batches always

MySql			always

Oracle			inserts in batches only if no values are autogenerated (via sequences)
			Updates are in batches always

PostgreSql		always

SqlServer		inserts in batches only if no values are autogenerated
			Updates are in batches always
```

So for example in H2 database, if a table has a surrogate int id and we insert entities
using insertBatch, then those will not be inserted in a batch because the driver doesn't
support reading the autogenerated keys via batch inserts. But many-to-many intermediate
table data will be batch inserted, along with any entities with natural or no keys.

Updating those entities is different. No data are autogenerated during updates and hence
all updates use batches.

MySql and PostgreSql always use batch inserts and updates.
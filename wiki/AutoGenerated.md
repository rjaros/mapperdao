# Auto Generated Primary Keys #

The auto-generated key way of handling is a nice feature of the library. Lets map a simple class with an auto-generated primary key

## Domain Class ##

```
class JobPosition(val name: String, val rank: Int)
```

Please notice that there is no id. This is correct cause the id is a property of the persisted entities but not of the non-persisted entities. But don't worry, the id will be there as soon as the entity is inserted,updated or selected.

## Table ##
The table ddl for postgresql:
```
create table JobPosition (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
```

Nothing special here, serial is an autoincrement int data type.

## Mapping ##

Here is the mapping for the entity:

```
object JobPositionEntity extends Entity[Int,SurrogateIntId, JobPosition] {
	val id = key("id") autogenerated(_.id) // this is the primary key
	val name = column("name") to (_.name) 
	val rank = column("rank") to ( _.rank)

	def constructor(implicit m:ValuesMap) = new JobPosition(name, rank) 
	with Stored {
		// JobPositionEntity.id will implicitly be converted to Int via the implicit variable m
		val id:Int = JobPositionEntity.id
	}
}
```

The first thing to notice is that `JobPositionEntity extends Entity[Int,SurrogateIntId, JobPosition]`. This is because the non-persisted entity
is JobPosition but the persisted entity is JobPosition with SurrogateIntId. This means that before persisting the entity, it doesn't have an id.
But as soon as you persist it, it obtains an id which was autogenerated (via autoincrement or sequences) by the database.

IntId is a simple trait that is mixed into persisted entities. It holds the id of the entity:

```
trait SurrogateIntId {
	val id: Int
}
```

(there are similar traits for different id types, i.e. SurrogateLongId)

Now, the next interesting thing is the mapping to this id:

```
	val id = key("id") autogenerated(_.id) // this is the primary key
```

This informs mapperdao that the "id" is autogenerated by the database.

Finally, the constructor needs to construct an instance which has this id:

```
def constructor(implicit m) = new JobPosition(name, rank) 
with Stored {
	val id:Int = JobPositionEntity.id
}
```

The id now is accessible after persisting the entity:

```
val inserted=mapperDao.insert(JobPositionEntity, new JobPosition("Scala Developer", 10))
// inserted=JobPosition("Scala Developer", 10) with IntId
println(inserted.id) 

val updated=mapperDao.update(JobPositionEntity,inserted, new JobPosition("New Scala Developer", 25))

// updated=JobPosition("New Scala Developer", 25) with IntId
println(updated.id)

val selected=mapperDao.select(JobPositionEntity,inserted.id)
// selected=JobPosition("New Scala Developer", 25) with IntId

```

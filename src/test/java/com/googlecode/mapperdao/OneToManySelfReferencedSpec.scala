package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup

/**
 * tests one-to-many references to self
 *
 * @author kostantinos.kougios
 *
 * 5 Aug 2011
 */
class OneToManySelfReferencedSpec extends SpecificationWithJUnit {
	import OneToManySelfReferencedSpec._

	val (jdbc, mapperDao) = setup

	"insert" in {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)
		inserted must_== person
	}

	"update, remove from traversable" in {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)

		val modified = new Person("main-changed", inserted.friends.filterNot(_.name == "friend1"))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated must_== modified

		mapperDao.select(PersonEntity, updated.id).get must_== updated
	}

	"update, add to traversable" in {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)
		var friends = inserted.friends
		val modified = new Person("main-changed", friends + new Person("friend3", Set()))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated must_== modified

		mapperDao.select(PersonEntity, updated.id).get must_== updated
	}

	"3 levels deep" in {
		createTables

		val person = Person("level1", Set(Person("level2-friend1", Set(Person("level3-friend1-1", Set()), Person("level3-friend1-2", Set()))), Person("level2-friend2", Set(Person("level3-friend2-1", Set())))))
		val inserted = mapperDao.insert(PersonEntity, person)

		val modified = Person("main-changed", inserted.friends + Person("friend3", Set(Person("level3-friend3-1", Set()))))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated must_== modified

		mapperDao.select(PersonEntity, updated.id).get must_== updated
	}

	"use already persisted friends" in {
		val level3 = Set(Person("level3-friend1-1", Set()), Person("level3-friend1-2", Set()))
		val level3Inserted = level3.map(mapperDao.insert(PersonEntity, _)).toSet[Person]
		val person = Person("level1", Set(Person("level2-friend1", level3Inserted), Person("level2-friend2", Set(Person("level3-friend2-1", Set())))))
		val inserted = mapperDao.insert(PersonEntity, person)

		val modified = Person("main-changed", inserted.friends + Person("friend3", Set(Person("level3-friend3-1", Set()))))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated must_== modified

		mapperDao.select(PersonEntity, updated.id).get must_== updated
	}
	def setup =
		{

			val typeRegistry = TypeRegistry(PersonEntity)

			Setup.setupMapperDao(typeRegistry)
		}

	def createTables {
		Setup.dropAllTables(jdbc)
		Setup.database match {
			case "postgresql" | "mysql" =>
				jdbc.update("""
			create table Person (
				id serial not null,
				name varchar(100) not null,
				friend_id int,
				primary key (id),
				foreign key (friend_id) references Person(id) on delete cascade
			)
		""")
			case "oracle" =>
				Setup.createMySeq(jdbc)
				jdbc.update("""
			create table Person (
				id int not null,
				name varchar(100) not null,
				friend_id int,
				primary key (id),
				foreign key (friend_id) references Person(id) on delete cascade
			)
		""")
		}
	}
}

object OneToManySelfReferencedSpec {

	case class Person(val name: String, val friends: Set[Person])

	object PersonEntity extends Entity[IntId, Person]("Person", classOf[Person]) {
		val aid = Setup.database match {
			case "oracle" => intAutoGeneratedPK("id", "myseq", _.id)
			case _ => intAutoGeneratedPK("id", _.id)
		}
		val name = string("name", _.name)
		val friends = oneToMany(classOf[Person], "friend_id", _.friends)

		def constructor(implicit m: ValuesMap) = new Person(name, friends) with Persisted with IntId {
			val valuesMap = m
			val id: Int = aid
		}
	}
}
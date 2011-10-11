package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
class ManyToOneMutableAutoGeneratedSpec extends SpecificationWithJUnit {
	import ManyToOneMutableAutoGeneratedSpec._
	val (jdbc, driver, mapperDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity, HouseEntity))

	import mapperDao._

	"update to null both FK" in {
		createTables

		val company1 = insert(CompanyEntity, Company("Coders limited"))
		val house = House("Rhodes,Greece")
		val person = Person("Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person

		inserted.name = "changed"
		inserted.company = null
		inserted.lives = null
		val updated = update(PersonEntity, inserted)
		updated must_== inserted

		val selected = select(PersonEntity, inserted.id).get
		selected must_== updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) must beNone
	}

	"update to null" in {
		createTables

		val company1 = insert(CompanyEntity, Company("Coders limited"))
		val house = House("Rhodes,Greece")
		val person = Person("Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person

		inserted.name = "changed"
		inserted.company = null

		val updated = update(PersonEntity, inserted)
		updated must_== inserted

		val selected = select(PersonEntity, updated.id).get
		selected must_== updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) must beNone
	}

	"update" in {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company("Coders limited"))
		val company2 = insert(CompanyEntity, Company("Scala Inc"))
		val house = House("Rhodes,Greece")
		val person = Person("Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person
		inserted.company = company2
		val updated = update(PersonEntity, inserted)
		updated must_== inserted

		val selected = select(PersonEntity, updated.id).get
		selected must_== updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) must beNone
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.database match {
				case "postgresql" =>
					jdbc.update("""
					create table Company (
						id serial not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table House (
						id serial not null,
						address varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Person (
						id serial not null,
						name varchar(100) not null,
						company_id int,
						house_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade,
						foreign key (house_id) references House(id) on delete cascade
					)
			""")
				case "oracle" =>
					jdbc.update("""
					create table Company (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table House (
						id int not null,
						address varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Person (
						id int not null,
						name varchar(100) not null,
						company_id int,
						house_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade,
						foreign key (house_id) references House(id) on delete cascade
					)
			""")
					Setup.createSeq(jdbc, "CompanySeq")
					Setup.createSeq(jdbc, "HouseSeq")
					Setup.createSeq(jdbc, "PersonSeq")
				case "mysql" =>
					jdbc.update("""
					create table Company (
						id serial not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table House (
						id serial not null,
						address varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Person (
						id serial not null,
						name varchar(100) not null,
						company_id int,
						house_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade,
						foreign key (house_id) references House(id) on delete cascade
					)
			""")
				case "derby" =>
					jdbc.update("""
					create table Company (
						id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table House (
						id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
						address varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Person (
						id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
						name varchar(100) not null,
						company_id int,
						house_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade,
						foreign key (house_id) references House(id) on delete cascade
					)
			""")
			}
		}
}

object ManyToOneMutableAutoGeneratedSpec {
	case class Person(var name: String, var company: Company, var lives: House)
	case class Company(var name: String)
	case class House(var address: String)

	object PersonEntity extends Entity[IntId, Person](classOf[Person]) {
		val id = Setup.database match {
			case "oracle" => intAutoGeneratedPK("id", "PersonSeq", _.id)
			case _ => intAutoGeneratedPK("id", _.id)
		}
		val name = string("name", _.name)
		val company = manyToOne(classOf[Company], _.company)
		val lives = manyToOne(classOf[House], _.lives)

		def constructor(implicit m: ValuesMap) = new Person(name, company, lives) with IntId with Persisted {
			val id: Int = PersonEntity.id
		}
	}

	object CompanyEntity extends Entity[IntId, Company](classOf[Company]) {
		val id = Setup.database match {
			case "oracle" => intAutoGeneratedPK("id", "CompanySeq", _.id)
			case _ => intAutoGeneratedPK("id", _.id)
		}
		val name = string("name", _.name)

		def constructor(implicit m: ValuesMap) = new Company(name) with IntId with Persisted {
			val id: Int = CompanyEntity.id
		}
	}

	object HouseEntity extends Entity[IntId, House](classOf[House]) {
		val id = Setup.database match {
			case "oracle" => intAutoGeneratedPK("id", "HouseSeq", _.id)
			case _ => intAutoGeneratedPK("id", _.id)
		}
		val address = string("address", _.address)
		def constructor(implicit m: ValuesMap) = new House(address) with IntId with Persisted {
			val id: Int = HouseEntity.id
		}
	}
}
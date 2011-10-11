package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 8 Aug 2011
 */
class ManyToManyNonRecursiveSpec extends SpecificationWithJUnit {
	import ManyToManyNonRecursiveSpec._

	val (jdbc, driver, mapperDao) = setup

	"insert tree of entities" in {
		createTables
		val product = Product("blue jean", Set(Attribute("colour", "blue"), Attribute("size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product

		// due to cyclic reference, the attributes set contains "mock" products which have empty traversables.
		// it is not possible to create cyclic-depended immutable instances.
		mapperDao.select(ProductEntity, inserted.id).get must_== inserted

		// attributes->product should also work
		val colour = inserted.attributes.toList.filter(_.name == "colour").head
		val loadedAttribute = mapperDao.select(AttributeEntity, mapperDao.intIdOf(colour)).get
		loadedAttribute must_== Attribute("colour", "blue")
	}

	"insert tree of entities with persisted leaf entities" in {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "medium"))
		val product = Product("blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product

		// due to cyclic reference, the attributes collection contains "mock" products which have empty traversables
		mapperDao.select(ProductEntity, inserted.id).get must_== inserted
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.database match {
				case "postgresql" | "mysql" =>
					jdbc.update("""
					create table Product (
						id serial not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Attribute (
						id serial not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id)
					)
			""")
				case "oracle" =>
					jdbc.update("""
					create table Product (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Attribute (
						id int not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id)
					)
			""")
					Setup.createSeq(jdbc, "ProductSeq")
					Setup.createSeq(jdbc, "AttributeSeq")
				case "derby" =>
					jdbc.update("""
					create table Product (
						id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
						name varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Attribute (
						id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id)
					)
			""")
			}
		}
	def setup =
		{
			val typeRegistry = TypeRegistry(ProductEntity, AttributeEntity)

			Setup.setupMapperDao(typeRegistry)
		}
}

object ManyToManyNonRecursiveSpec {
	case class Product(val name: String, val attributes: Set[Attribute])
	case class Attribute(val name: String, val value: String)

	object ProductEntity extends Entity[IntId, Product]("Product", classOf[Product]) {
		val id = Setup.database match {
			case "oracle" => intAutoGeneratedPK("id", "ProductSeq", _.id)
			case _ => intAutoGeneratedPK("id", _.id)
		}
		val name = string("name", _.name)
		val attributes = manyToMany(classOf[Attribute], _.attributes)

		def constructor(implicit m: ValuesMap) = new Product(name, attributes) with Persisted with IntId {
			val id: Int = ProductEntity.id // we explicitly convert this to an int because mysql serial values are always BigInteger (a bug maybe?)
		}
	}
	object AttributeEntity extends Entity[IntId, Attribute]("Attribute", classOf[Attribute]) {
		val id = Setup.database match {
			case "oracle" => intAutoGeneratedPK("id", "AttributeSeq", _.id)
			case _ => intAutoGeneratedPK("id", _.id)
		}
		val name = string("name", _.name)
		val value = string("value", _.value)

		def constructor(implicit m: ValuesMap) = new Attribute(name, value) with Persisted with IntId {
			val id: Int = AttributeEntity.id // we explicitly convert this to an int because mysql serial values are always BigInteger (a bug maybe?)
		}
	}
}
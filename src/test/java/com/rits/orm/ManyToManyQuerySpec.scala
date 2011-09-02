package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
class ManyToManyQuerySpec extends SpecificationWithJUnit {
	import ManyToManyQuerySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(ProductEntity, AttributeEntity))

	import mapperDao._
	import queryDao._
	import TestQueries._

	"join, 1 condition" in {
		createTables
		val a0 = insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		val p3 = insert(ProductEntity, Product(4, "TV 3", Set(a1, a3)))

		query(q0).toSet must_== Set(p0, p2)
	}

	"join, 2 condition" in {
		createTables
		val a0 = insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		val p3 = insert(ProductEntity, Product(4, "TV 3", Set(a1, a3)))

		query(q1).toSet must_== Set(p0, p1, p3)
	}

	def createTables =
		{
			jdbc.update("drop table if exists Product_Attribute cascade")
			jdbc.update("drop table if exists Product cascade")
			jdbc.update("drop table if exists Attribute cascade")

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
		}
}

object ManyToManyQuerySpec {
	object TestQueries {
		val p = ProductEntity
		val a = AttributeEntity

		import Query._

		def q0 = select from p join (p, p.attributes, a) where a.value === "46'"

		def q1 = select from p join (p, p.attributes, a) where a.value === "50'" or a.value === "black"
	}

	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity("Product", classOf[Product]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val attributes = manyToMany("Product_Attribute", "product_id", "attribute_id", classOf[Attribute], _.attributes)

		val constructor = (m: ValuesMap) => new Product(m(id), m(name), m(attributes).toSet) with Persisted {
			val valuesMap = m
		}
	}

	object AttributeEntity extends SimpleEntity("Attribute", classOf[Attribute]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val value = string("value", _.value)

		val constructor = (m: ValuesMap) => new Attribute(m(id), m(name), m(value)) with Persisted {
			val valuesMap = m
		}
	}
}
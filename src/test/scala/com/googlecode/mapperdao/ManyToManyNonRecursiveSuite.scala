package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 *
 *         8 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyNonRecursiveSuite extends FunSuite with ShouldMatchers {
	val typeRegistry = TypeRegistry(ProductEntity, AttributeEntity)

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(typeRegistry)

	test("update, add with new set") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "medium"))
		val a1l = mapperDao.select(AttributeEntity, a1.id).get

		val product = Product("blue jean", Set(a1))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product

		val selected = mapperDao.select(ProductEntity, inserted.id).get
		selected should be === inserted

		val up = Product("red jean", Set(a1l, a2))
		val updated = mapperDao.update(ProductEntity, selected, up)
		updated should be === up

		val reloaded = mapperDao.select(ProductEntity, inserted.id).get
		reloaded should be === updated
	}

	test("update, remove with new set") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "medium"))
		val a1l = mapperDao.select(AttributeEntity, a1.id).get

		val product = Product("blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product

		val selected = mapperDao.select(ProductEntity, inserted.id).get
		selected should be === inserted

		val up = Product("red jean", Set(a1l))
		val updated = mapperDao.update(ProductEntity, selected, up)
		updated should be === up

		val reloaded = mapperDao.select(ProductEntity, inserted.id).get
		reloaded should be === updated
	}

	test("insert tree of entities") {
		createTables
		val product = Product("blue jean", Set(Attribute("colour", "blue"), Attribute("size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product

		mapperDao.select(ProductEntity, inserted.id).get should be === inserted

		// attributes->product should also work
		val colour = inserted.attributes.toList.filter(_.name == "colour").head
		val loadedAttribute = mapperDao.select(AttributeEntity, Helpers.intIdOf(colour)).get
		loadedAttribute should be === Attribute("colour", "blue")
	}

	test("insert tree of entities  leaf entities") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "medium"))
		val product = Product("blue jean", Set(a1, a2))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product

		mapperDao.select(ProductEntity, inserted.id).get should be === inserted
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
		Setup.database match {
			case "oracle" =>
				Setup.createSeq(jdbc, "ProductSeq")
				Setup.createSeq(jdbc, "AttributeSeq")
			case _ =>
		}
	}

	case class Product(val name: String, val attributes: Set[Attribute])

	case class Attribute(val name: String, val value: String)

	object ProductEntity extends Entity[Int, Product] {
		type Stored = SurrogateIntId
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("ProductSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m) = new Product(name, attributes) with Stored {
			val id: Int = ProductEntity.id // we explicitly convert this to an int because mysql serial values are always BigInteger (a bug maybe?)
		}
	}

	object AttributeEntity extends Entity[Int, Attribute] {
		type Stored = SurrogateIntId
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("AttributeSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(name, value) with Stored {
			val id: Int = AttributeEntity.id // we explicitly convert this to an int because mysql serial values are always BigInteger (a bug maybe?)
		}
	}

}
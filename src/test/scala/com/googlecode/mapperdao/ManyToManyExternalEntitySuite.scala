package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 *         Jan 18, 2012
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyExternalEntitySuite extends FunSuite with ShouldMatchers {
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	if (Setup.database == "h2") {

		test("delete without propagation") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10"), Attribute(20, "x20")))
			val inserted = mapperDao.insert(ProductEntity, product)
			mapperDao.delete(ProductEntity, inserted)
			mapperDao.select(ProductEntity, inserted.id) should be(None)
			AttributeEntity.onDeleteCalled should be === 0
		}

		test("delete with propagation") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10"), Attribute(20, "x20")))
			val inserted = mapperDao.insert(ProductEntity, product)
			mapperDao.delete(DeleteConfig(propagate = true), ProductEntity, inserted)
			mapperDao.select(ProductEntity, inserted.id) should be(None)
			AttributeEntity.onDeleteCalled should be === 2
		}

		test("persists and select") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10"), Attribute(20, "x20")))
			val inserted = mapperDao.insert(ProductEntity, product)
			inserted should be === product
			mapperDao.select(ProductEntity, inserted.id).get should be === inserted
		}

		test("updates/select, remove item") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10"), Attribute(20, "x20")))
			val inserted = mapperDao.insert(ProductEntity, product)
			val toUpdate = Product("p2", inserted.attributes.filterNot(_.id == 10))
			val updated = mapperDao.update(ProductEntity, inserted, toUpdate)
			updated should be === toUpdate
			mapperDao.select(ProductEntity, inserted.id).get should be === updated
		}
		test("updates/select, add item") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10")))
			val inserted = mapperDao.insert(ProductEntity, product)
			val toUpdate = Product("p2", inserted.attributes + Attribute(20, "x20"))
			val updated = mapperDao.update(ProductEntity, inserted, toUpdate)
			updated should be === toUpdate
			mapperDao.select(ProductEntity, inserted.id).get should be === updated
		}

		test("added valid when inserting") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10"), Attribute(20, "x20")))
			mapperDao.insert(ProductEntity, product)
			AttributeEntity.added.toSet should be(product.attributes)
		}

		test("added valid when updating") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10")))
			val inserted = mapperDao.insert(ProductEntity, product)
			resetAE()
			val newA1 = Attribute(11, "x11")
			val newA2 = Attribute(12, "x12")
			mapperDao.update(ProductEntity, inserted, product.copy(attributes = inserted.attributes + newA1 + newA2))
			AttributeEntity.added.toSet should be(Set(newA1, newA2))
		}

		test("updated valid when updating") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10")))
			val inserted = mapperDao.insert(ProductEntity, product)
			resetAE()
			val newA1 = Attribute(11, "x11")
			mapperDao.update(ProductEntity, inserted, product.copy(attributes = inserted.attributes + newA1))
			AttributeEntity.updated.toSet should be(product.attributes)
		}

		test("removed valid when updating") {
			createTables

			val product = Product("p1", Set(Attribute(10, "x10"), Attribute(11, "x11")))
			val inserted = mapperDao.insert(ProductEntity, product)
			resetAE()
			mapperDao.update(ProductEntity, inserted, product.copy(attributes = inserted.attributes.filter(_.id == 10)))
			AttributeEntity.removed.toSet should be(Set(Attribute(11, "x11")))
		}
	}

	def createTables {
		resetAE()
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	def resetAE() {
		AttributeEntity.onDeleteCalled = 0
		AttributeEntity.added = Nil
		AttributeEntity.updated = Nil
		AttributeEntity.removed = Nil
	}

	case class Product(val name: String, val attributes: Set[Attribute])

	case class Attribute(val id: Int, val name: String)

	object ProductEntity extends Entity[Int, Product] {
		type Stored = SurrogateIntId
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m) = new Product(name, attributes) with Stored {
			val id: Int = ProductEntity.id
		}
	}

	object AttributeEntity extends ExternalEntity[Int, Attribute] {
		val id = key("id") to (_.id)
		var added = List[Attribute]()
		var updated = List[Attribute]()
		var removed = List[Attribute]()

		onSelectManyToMany(ProductEntity.attributes) {
			s =>
				s.foreignIds.map {
					case (id: Int) :: Nil =>
						Attribute(id, "x" + id)
					case _ => throw new RuntimeException
				}
		}

		onUpdateManyToMany(ProductEntity.attributes) {
			u =>
				val attribute = u.foreign
				u.operation match {
					case UpdateExternalManyToMany.Operation.Add =>
						added = attribute :: added
					case UpdateExternalManyToMany.Operation.Remove =>
						removed = attribute :: removed
					case UpdateExternalManyToMany.Operation.Update =>
						updated = attribute :: updated
				}
				PrimaryKeysValues(attribute.id)
		}

		var onDeleteCalled = 0
		onDeleteManyToMany {
			d =>
				onDeleteCalled += 1
		}
	}

}
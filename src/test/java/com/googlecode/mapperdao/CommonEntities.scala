package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.jdbc.Jdbc

/**
 * @author kostantinos.kougios
 *
 * 22 May 2012
 */
object CommonEntities {

	def createProductAttribute(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		Setup.commonEntitiesQueries(jdbc).update("product-attribute")
	}
	/**
	 * many to many
	 */
	case class Product(val name: String, val attributes: Set[Attribute])
	case class Attribute(val name: String, val value: String)

	object ProductEntity extends Entity[IntId, Product]("Product", classOf[Product]) {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) getter ("attributes") to (_.attributes)

		def constructor(implicit m) = new Product(name, attributes) with Persisted with IntId {
			val id: Int = ProductEntity.id
		}
	}
	object AttributeEntity extends Entity[IntId, Attribute]("Attribute", classOf[Attribute]) {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)
		val product = manytomanyreverse(ProductEntity) forQueryOnly () to (p => Nil)

		def constructor(implicit m) = new Attribute(name, value) with Persisted with IntId {
			val id: Int = AttributeEntity.id
		}
	}

	/**
	 * many to one
	 */
	def createPersonCompany(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		Setup.commonEntitiesQueries(jdbc).update("person-company")
	}

	case class Person(val name: String, val company: Company)
	case class Company(val name: String)

	object PersonEntity extends Entity[IntId, Person] {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val company = manytoone(CompanyEntity) to (_.company)

		def constructor(implicit m) = new Person(name, company) with IntId with Persisted {
			val id: Int = PersonEntity.id
		}
	}

	object CompanyEntity extends Entity[IntId, Company] {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)

		def constructor(implicit m) = new Company(name) with IntId with Persisted {
			val id: Int = CompanyEntity.id
		}
	}

	/**
	 * one to many
	 */
	case class Owner(var name: String, owns: Set[House])
	case class House(val address: String)

	object HouseEntity extends Entity[IntId, House] {
		val id = key("id") autogenerated (_.id)
		val address = column("address") to (_.address)

		def constructor(implicit m) = new House(address) with Persisted with IntId {
			val id: Int = HouseEntity.id
		}
	}

	object OwnerEntity extends Entity[IntId, Owner] {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val owns = onetomany(HouseEntity) to (_.owns)

		def constructor(implicit m) = new Owner(name, owns) with Persisted with IntId {
			val id: Int = OwnerEntity.id
		}
	}

	/**
	 * one to one
	 */
	def createHusbandWife(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		Setup.commonEntitiesQueries(jdbc).update("husband-wife")
	}
	case class Husband(name: String, age: Int, wife: Wife)
	case class Wife(name: String, age: Int)

	object HusbandEntity extends SimpleEntity[Husband] {
		val name = column("name") to (_.name)
		val age = column("age") to (_.age)
		val wife = onetoone(WifeEntity) to (_.wife)

		declarePrimaryKey(wife)

		def constructor(implicit m) = new Husband(name, age, wife) with Persisted
	}

	object WifeEntity extends Entity[IntId, Wife] {
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val age = column("age") to (_.age)
		def constructor(implicit m) = new Wife(name, age) with Persisted with IntId {
			val id: Int = WifeEntity.id
		}
	}

	/**
	 * blob
	 */
	def createImage(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		if (Setup.database == "oracle")
			Setup.createSeq(jdbc, "ImageSeq")

		Setup.commonEntitiesQueries(jdbc).update("image")
	}
	case class Image(name: String, data: Array[Byte])

	val ieSequence = Setup.database match {
		case "oracle" => Some("ImageSeq")
		case _ => None
	}
	object ImageEntity extends Entity[IntId, Image] {
		val id = key("id") sequence (ieSequence) autogenerated (_.id)
		val name = column("name") to (_.name)
		val data = column("data") to (_.data)

		def constructor(implicit m) = new Image(name, data) with Persisted with IntId {
			val id: Int = ImageEntity.id
		}
	}
}
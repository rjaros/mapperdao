package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.jdbc.Jdbc

/**
 * @author kostantinos.kougios
 *
 *         22 May 2012
 */
object CommonEntities
{
	val AllEntities: List[EntityBase[_, _]] = List(ProductEntity, AttributeEntity, PersonEntity, CompanyEntity, OwnerEntity, HouseEntity, HusbandEntity, WifeEntity, ImageEntity)

	def createProductAttribute(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		if (Setup.database == "oracle") {
			Setup.createSeq(jdbc, "ProductSeq")
			Setup.createSeq(jdbc, "AttributeSeq")
		}
		Setup.commonEntitiesQueries(jdbc).update("product-attribute")
	}

	/**
	 * many to many
	 */
	case class Product(name: String, attributes: Set[Attribute])

	case class Attribute(name: String, value: String)

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("ProductSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) getter ("attributes") to (_.attributes)

		def constructor(implicit m: ValuesMap) = new Product(name, attributes) with Stored
		{
			val id: Int = ProductEntity.id
		}
	}

	object AttributeEntity extends Entity[Int, SurrogateIntId, Attribute]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("AttributeSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)
		val product = manytomanyreverse(ProductEntity) forQueryOnly() to (p => Nil)

		def constructor(implicit m: ValuesMap) = new Attribute(name, value) with Stored
		{
			val id: Int = AttributeEntity.id
		}
	}

	/**
	 * many to one
	 */
	def createPersonCompany(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		if (Setup.database == "oracle") {
			Setup.createSeq(jdbc, "PersonSeq")
			Setup.createSeq(jdbc, "CompanySeq")
		}
		Setup.commonEntitiesQueries(jdbc).update("person-company")
	}

	case class Person(name: String, company: Company)

	case class Company(name: String)

	object PersonEntity extends Entity[Int, SurrogateIntId, Person]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("PersonSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val company = manytoone(CompanyEntity) to (_.company)

		def constructor(implicit m: ValuesMap) = new Person(name, company) with Stored
		{
			val id: Int = PersonEntity.id
		}
	}

	object CompanyEntity extends Entity[Int, SurrogateIntId, Company]
	{

		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("CompanySeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)

		def constructor(implicit m: ValuesMap) = new Company(name) with Stored
		{
			val id: Int = CompanyEntity.id
		}
	}

	/**
	 * one to many
	 */
	def createOwnerHouse(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		if (Setup.database == "oracle") {
			Setup.createSeq(jdbc, "HouseSeq")
			Setup.createSeq(jdbc, "OwnerSeq")
		}
		Setup.commonEntitiesQueries(jdbc).update("owner-house")
	}

	case class Owner(var name: String, owns: Set[House])

	case class House(address: String)

	object HouseEntity extends Entity[Int, SurrogateIntId, House]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("HouseSeq")
			case _ => None
		}) autogenerated (_.id)
		val address = column("address") to (_.address)

		def constructor(implicit m: ValuesMap) = new House(address) with Stored
		{
			val id: Int = HouseEntity.id
		}
	}

	object OwnerEntity extends Entity[Int, SurrogateIntId, Owner]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("OwnerSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val owns = onetomany(HouseEntity) to (_.owns)

		def constructor(implicit m: ValuesMap) = new Owner(name, owns) with Stored
		{
			val id: Int = OwnerEntity.id
		}
	}

	/**
	 * one to one
	 */
	def createHusbandWife(jdbc: Jdbc) = {
		Setup.dropAllTables(jdbc)
		if (Setup.database == "oracle") {
			Setup.createSeq(jdbc, "WifeSeq")
		}
		Setup.commonEntitiesQueries(jdbc).update("husband-wife")
	}

	case class Husband(name: String, age: Int, wife: Wife)

	case class Wife(name: String, age: Int)

	object HusbandEntity extends Entity[Unit, NoId, Husband]
	{
		val name = column("name") to (_.name)
		val age = column("age") to (_.age)
		val wife = onetoone(WifeEntity) to (_.wife)

		declarePrimaryKey(wife)

		def constructor(implicit m) = new Husband(name, age, wife) with Stored
	}

	object WifeEntity extends Entity[Int, SurrogateIntId, Wife]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("WifeSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val age = column("age") to (_.age)

		def constructor(implicit m) = new Wife(name, age) with Stored
		{
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

	object ImageEntity extends Entity[Int, SurrogateIntId, Image]
	{
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("ImageSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val data = column("data") to (_.data)

		def constructor(implicit m) = new Image(name, data) with Stored
		{
			val id: Int = ImageEntity.id
		}
	}

}
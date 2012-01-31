package com.googlecode.mapperdao

/**
 * int simple type
 */
case class IntValue(val value: Int) extends SimpleTypeValue[Int, IntValue] {
	def compareTo(o: IntValue): Int = value.compareTo(o.value)
}

protected class IntEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[IntValue](table, classOf[IntValue]) {
	val value = column(soleColumn) to (_.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new IntValue(value) with Persisted
}

abstract class IntEntityManyToManyBase[PC](table: String, soleColumn: String) extends Entity[PC, IntValue](table, classOf[IntValue]) {
	val value = column(soleColumn) to (_.value)
}
class IntEntityManyToManyAutoGenerated(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) extends IntEntityManyToManyBase[IntId](table, soleColumn) {
	val id = key(pkColumn) sequence (sequence) autogenerated (_.id)
	def constructor(implicit m: ValuesMap) = new IntValue(value) with Persisted with IntId {
		val id: Int = IntEntityManyToManyAutoGenerated.this.id
	}
}

object IntEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new IntEntityOTM(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new IntEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}
package com.googlecode.mapperdao

/**
 * mapping simple type values to tables. These classes provide easy integration with
 * tables holding 1 simple type.
 *
 * @author kostantinos.kougios
 *
 * 5 Nov 2011
 */

trait SimpleTypeValue[T, E] extends Comparable[E] {
	val value: T
}

/**
 * string simple type
 */
case class StringValue(val value: String) extends SimpleTypeValue[String, StringValue] {
	def compareTo(o: StringValue): Int = value.compareTo(o.value)
}

protected class StringEntityOneToMany(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[StringValue](table, classOf[StringValue]) {
	val value = column(soleColumn) to (_.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new StringValue(value) with Persisted
}

abstract class StringEntityManyToManyBase[PC](table: String, soleColumn: String) extends Entity[PC, StringValue](table, classOf[StringValue]) {
	val value = column(soleColumn) to (_.value)
}
class StringEntityManyToManyAutoGenerated(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) extends StringEntityManyToManyBase[IntId](table, soleColumn) {
	val id = intAutoGeneratedPK(pkColumn, sequence.getOrElse(null), _.id)
	def constructor(implicit m: ValuesMap) = new StringValue(value) with Persisted with IntId {
		val id: Int = StringEntityManyToManyAutoGenerated.this.id
	}
}

object StringEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new StringEntityOneToMany(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new StringEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}

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
	val id = intAutoGeneratedPK(pkColumn, sequence.getOrElse(null), _.id)
	def constructor(implicit m: ValuesMap) = new IntValue(value) with Persisted with IntId {
		val id: Int = IntEntityManyToManyAutoGenerated.this.id
	}
}

object IntEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new IntEntityOTM(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new IntEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}
/**
 * long simple type
 */
case class LongValue(val value: Long) extends SimpleTypeValue[Long, LongValue] {
	def compareTo(o: LongValue): Int = value.compare(o.value)
}

protected class LongEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[LongValue](table, classOf[LongValue]) {
	val value = column(soleColumn) to (_.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new LongValue(value) with Persisted
}

abstract class LongEntityManyToManyBase[PC](table: String, soleColumn: String) extends Entity[PC, LongValue](table, classOf[LongValue]) {
	val value = column(soleColumn) to (_.value)
}
class LongEntityManyToManyAutoGenerated(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) extends LongEntityManyToManyBase[IntId](table, soleColumn) {
	val id = intAutoGeneratedPK(pkColumn, sequence.getOrElse(null), _.id)
	def constructor(implicit m: ValuesMap) = new LongValue(value) with Persisted with IntId {
		val id: Int = LongEntityManyToManyAutoGenerated.this.id
	}
}

object LongEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new LongEntityOTM(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new LongEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}

/**
 * float simple type
 */
case class FloatValue(val value: Float) extends SimpleTypeValue[Float, FloatValue] {
	def compareTo(o: FloatValue): Int = value.compare(o.value)
}

protected class FloatEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[FloatValue](table, classOf[FloatValue]) {
	val value = column(soleColumn) to (_.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new FloatValue(value) with Persisted
}

abstract class FloatEntityManyToManyBase[PC](table: String, soleColumn: String) extends Entity[PC, FloatValue](table, classOf[FloatValue]) {
	val value = column(soleColumn) to (_.value)
}
class FloatEntityManyToManyAutoGenerated(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) extends FloatEntityManyToManyBase[IntId](table, soleColumn) {
	val id = intAutoGeneratedPK(pkColumn, sequence.getOrElse(null), _.id)
	def constructor(implicit m: ValuesMap) = new FloatValue(value) with Persisted with IntId {
		val id: Int = FloatEntityManyToManyAutoGenerated.this.id
	}
}

object FloatEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new FloatEntityOTM(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new FloatEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}

/**
 * double simple type
 */
case class DoubleValue(val value: Double) extends SimpleTypeValue[Double, DoubleValue] {
	def compareTo(o: DoubleValue): Int = value.compare(o.value)
}

protected class DoubleEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[DoubleValue](table, classOf[DoubleValue]) {
	val value = column(soleColumn) to (_.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new DoubleValue(value) with Persisted
}

abstract class DoubleEntityManyToManyBase[PC](table: String, soleColumn: String) extends Entity[PC, DoubleValue](table, classOf[DoubleValue]) {
	val value = column(soleColumn) to (_.value)
}
class DoubleEntityManyToManyAutoGenerated(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) extends DoubleEntityManyToManyBase[IntId](table, soleColumn) {
	val id = intAutoGeneratedPK(pkColumn, sequence.getOrElse(null), _.id)
	def constructor(implicit m: ValuesMap) = new DoubleValue(value) with Persisted with IntId {
		val id: Int = DoubleEntityManyToManyAutoGenerated.this.id
	}
}

object DoubleEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new DoubleEntityOTM(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new DoubleEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}

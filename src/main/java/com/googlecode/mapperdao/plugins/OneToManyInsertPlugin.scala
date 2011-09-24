package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.OneToMany
import com.googlecode.mapperdao.TypeRef
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.LowerCaseMutableMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyInsertPlugin(mapperDao: MapperDao) extends BeforeInsert with PostInsert {
	val typeRegistry = mapperDao.typeRegistry
	val driver = mapperDao.driver

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo) = updateInfo
			if (parent != null) {
				val parentColumn = parentColumnInfo.column
				parentColumn match {
					case otm: OneToMany[_] =>
						val foreignKeyColumns = otm.foreignColumns.filterNot(tpe.table.primaryKeyColumns.contains(_))
						if (!foreignKeyColumns.isEmpty) {
							val parentEntity = typeRegistry.entityOfObject[Any, Any](parent)
							val parentTpe = typeRegistry.typeOf(parentEntity)
							val parentTable = parentTpe.table
							val parentKeysAndValues = parent.asInstanceOf[Persisted].valuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
							val foreignKeys = parentKeysAndValues.map(_._2)
							if (foreignKeys.size != foreignKeyColumns.size) throw new IllegalArgumentException("mappings of one-to-many from " + parent + " to " + o + " is invalid. Number of FK columns doesn't match primary keys. columns: " + foreignKeyColumns + " , primary key values " + foreignKeys);
							foreignKeyColumns zip foreignKeys
						} else Nil
					case _ => Nil
				}
			} else Nil
		}

	override def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOfObject[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (mapperDao.isPersisted(nested)) {
							val OneToMany(foreign: TypeRef[_], foreignColumns: List[Column]) = cis.column
							// update
							val keyArgs = nestedTpe.table.toListOfColumnAndValueTuples(nestedTpe.table.primaryKeys, nested)
							driver.doUpdateOneToManyRef(nestedTpe, foreignColumns zip newKeyValues, keyArgs)
							nested
						} else {
							// insert
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(nestedEntity, nested, entityMap)
							entityMap.up
							inserted
						}
						val cName = cis.column.alias
						modifiedTraversables(cName) = newO
					}
				}
			}
		}
}
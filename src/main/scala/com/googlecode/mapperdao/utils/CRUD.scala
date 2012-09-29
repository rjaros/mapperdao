package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.QueryDao
import com.googlecode.mapperdao.Query
import org.springframework.transaction.PlatformTransactionManager
import com.googlecode.mapperdao.jdbc.Transaction
import Transaction._
import com.googlecode.mapperdao.MemoryMapperDao
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.jdbc.MockTransaction
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.DeclaredIds

/**
 * mixin to add CRUD methods to a dao
 *
 * https://code.google.com/p/mapperdao/wiki/CRUDDaos
 *
 * T is the entity type, i.e. Product
 * T with PC is the persisted type, i.e. Product with IntId. PC can be AnyRef
 * 		if T's type doesn't change when persisted.
 * PK is the type of the key, i.e. Int or String
 *
 * @author kostantinos.kougios
 *
 * 30 Aug 2011
 */
trait CRUD[ID, PC <: DeclaredIds[ID], T] {
	protected val mapperDao: MapperDao
	protected val entity: Entity[PC, T]

	// override these to customise them
	protected val selectConfig = SelectConfig.default
	protected val updateConfig = UpdateConfig.default
	protected val deleteConfig = DeleteConfig.default

	/**
	 * insert an entity into the database
	 */
	def create(t: T): T with PC = mapperDao.insert(updateConfig, entity, t)

	/**
	 * update an entity. The entity must have been retrieved from the database and then
	 * changed prior to calling this method.
	 * The whole tree will be updated (if necessary).
	 * The method heavily relies on object equality to assess which entities will be updated.
	 */
	def update(t: T with PC): T with PC = mapperDao.update(updateConfig, entity, t)
	/**
	 * update an immutable entity. The entity must have been retrieved from the database. Because immutables can't change, a new instance
	 * of the entity must be created with the new values prior to calling this method. Values that didn't change should be copied from o.
	 * The method heavily relies on object equality to assess which entities will be updated.
	 * The whole tree will be updated (if necessary).
	 */
	def update(oldValue: T with PC, newValue: T): T with PC = mapperDao.update(updateConfig, entity, oldValue, newValue)
	/**
	 * select an entity by it's primary key
	 *
	 * @param clz		Class[T], classOf[Entity]
	 * @param id		the id
	 * @return			Option[T] or None
	 */
	def retrieve(pk: ID): Option[T with PC] = mapperDao.select(selectConfig, entity, pk)

	/**
	 * delete a persisted entity
	 */
	def delete(t: T with PC): T = mapperDao.delete(deleteConfig, entity, t)

	/**
	 * this will delete an entity based on it's id.
	 *
	 * The delete will cascade to related entities only if there are cascade constraints
	 * on the foreign keys in the database.
	 */
	def delete(id: ID): Unit = mapperDao.delete(entity, id)

	/**
	 * unlinks an entity from mapperdao. The entity is not tracked for changes and can't
	 * be used in updates or deletes. This can free up some memory
	 */
	def unlink(o: T): T = mapperDao.unlink(entity, o)
}
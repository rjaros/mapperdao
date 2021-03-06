package com.googlecode.mapperdao.jdbc

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl


/**
 * default, single-threaded, transaction safe query run strategy
 *
 * @author kostantinos.kougios
 *
 *         6 May 2012
 */
private[mapperdao] class DefaultQueryRunStrategy extends QueryRunStrategy
{

	override def run[ID, T](
		mapperDao: MapperDaoImpl,
		entity: EntityBase[ID, T],
		queryConfig: QueryConfig,
		lm: List[DatabaseValues]
		) = {
		val entityMap = new EntityMap
		val selectConfig = SelectConfig.from(queryConfig)
		val v = mapperDao.toEntities(lm, entity, selectConfig, entityMap)
		v
	}
}
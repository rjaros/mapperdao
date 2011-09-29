package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.jdbc.UpdateResultWithGeneratedKeys
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.Query

/**
 * @author kostantinos.kougios
 *
 * 2 Sep 2011
 */
class Mysql(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {

	override protected def sequenceSelectNextSql(sequenceColumn: ColumnBase): String = throw new IllegalStateException("MySql doesn't support sequences")

	override protected def insertSql[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): String =
		{
			val sql = super.insertSql(tpe, args)
			if (args.isEmpty) {
				sql + "\nvalues()"
			} else sql
		}

	protected[mapperdao] override def getAutoGenerated(ur: UpdateResultWithGeneratedKeys, column: ColumnBase): Any = ur.keys.get("GENERATED_KEY").get

	override def endOfQuery[PC, T](queryConfig: QueryConfig, qe: Query.QueryEntity[PC, T], sql: StringBuilder): Unit =
		if (queryConfig.offset.isDefined || queryConfig.limit.isDefined) {
			val offset = queryConfig.offset.getOrElse(0)
			val limit = queryConfig.limit.getOrElse(Long.MaxValue)
			sql append "\nLIMIT " append offset append "," append limit
		}
}
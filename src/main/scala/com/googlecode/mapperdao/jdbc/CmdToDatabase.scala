package com.googlecode.mapperdao.jdbc

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.persistcmds._
import com.googlecode.mapperdao.drivers.Driver
import org.springframework.jdbc.core.SqlParameterValue
import com.googlecode.mapperdao.state.persisted._
import com.googlecode.mapperdao.state.persistcmds.PersistCmd
import com.googlecode.mapperdao.state.persistcmds.InsertCmd

/**
 * converts commands to database operations, executes
 * them and returns the resulting persisted nodes.
 *
 * @author kostantinos.kougios
 *
 * 22 Nov 2012
 */
class CmdToDatabase(
		updateConfig: UpdateConfig,
		driver: Driver,
		typeManager: TypeManager) {

	private val jdbc = driver.jdbc

	def insert[ID, PC <: DeclaredIds[ID], T](
		cmds: List[PersistCmd[ID, PC, T]]): List[T with PC] = {
		// collect the sql and values
		cmds.map { cmd =>
			val sql = toSql(cmd)
			(sql, cmd)
		}.groupBy {
			case (sql, cmd) => sql.sql
		}.foreach {
			case (sql, nodes) =>
				val entity = nodes.head._2.entity
				val table = entity.tpe.table
				val autoGeneratedColumnNames = table.autoGeneratedColumnNamesArray
				val bo = BatchOptions(driver.batchStrategy, autoGeneratedColumnNames)
				val args = nodes.map {
					case (sql, _) =>
						sql.values.toArray
				}.toArray

				// do the batch update
				val br = jdbc.batchUpdate(bo, sql, args)

				// now extract the keys and set them into the nodes
				val keys = br.keys map { m =>
					table.autoGeneratedColumns.map { column =>
						(column, driver.getAutoGenerated(m, column))
					}
				}
				(nodes zip keys) foreach {
					case ((_, cmd), key) =>
						PersistedNode(cmd.entity, cmd.o, Nil, key.toList)
				}
		}

		// reconstruct the persisted entities
		Nil
	}

	private def toSql(cmd: PersistCmd[_, _, _]) = cmd match {
		case InsertCmd(entity, o, columns, commands) =>
			driver.insertSql(entity.tpe, columns).result
	}
}
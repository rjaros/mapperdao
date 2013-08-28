package com.googlecode.mapperdao.queryphase

import com.googlecode.mapperdao.queryphase.model._
import com.googlecode.mapperdao.schema.Type
import com.googlecode.mapperdao.schema.ManyToMany
import com.googlecode.mapperdao.{Query, QueryBuilder, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 13/08/13
 */
class QueryPhase
{
	private val alreadyDone = collection.mutable.Set.empty[Type[_, _]]

	def toQuery[ID, PC <: Persisted, T](q: QueryBuilder[ID, PC, T]): Select = {
		val tpe = q.entity.tpe
		val iqt = InQueryTable(Table(tpe.table), "maint")
		val from = From(iqt)
		Select(from, joins(tpe, iqt), where(tpe, iqt, q))
	}

	private def joins[ID, T](tpe: Type[ID, T], iqt: InQueryTable): List[Join] = {
		// make sure we don't process the same type twice
		if (alreadyDone.contains(tpe))
			Nil
		else {
			alreadyDone += tpe
			tpe.table.relationshipColumns.map {
				case ManyToMany(e, linkTable, foreign) =>
					val linkT = Table(linkTable)
					val linkIQT = InQueryTable(linkT, alias)
					val ftpe = foreign.entity.tpe
					val rightT = Table(ftpe.table)
					val rightIQT = InQueryTable(rightT, alias)
					Join(
						linkIQT,
						OnClause(
							tpe.table.primaryKeys.map {
								pk =>
									Column(iqt, pk.name)
							},
							linkTable.left.map {
								c =>
									Column(linkIQT, c.name)
							}
						)
					) :: Join(
						rightIQT,
						OnClause(
							ftpe.table.primaryKeys.map {
								pk =>
									Column(rightIQT, pk.name)
							},
							linkTable.right.map {
								c =>
									Column(linkIQT, c.name)
							}
						)
					) :: joins(ftpe, rightIQT)
			}.flatten
		}
	}

	private def where[ID, PC <: Persisted, T](tpe: Type[ID, T], iqt: InQueryTable, q: QueryBuilder[ID, PC, T]): Clause =
		q matches {
			case wc: Query.Where[ID, PC, T] =>
				where(tpe, iqt, wc)
			case _ => NoClause
		}

	private def where[ID, T](tpe: Type[ID, T], iqt: InQueryTable, q: Query.Where[ID, T]): Clause = {}

	private var aliasCnt = 0

	private def alias = {
		aliasCnt += 1
		"a" + aliasCnt
	}
}

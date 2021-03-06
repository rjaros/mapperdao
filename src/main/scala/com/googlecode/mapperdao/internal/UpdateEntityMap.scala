package com.googlecode.mapperdao.internal

import scala.collection.immutable.Stack
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase
import com.googlecode.mapperdao.{Entity, Persisted}
import java.util

protected[mapperdao] class UpdateEntityMap
{
	private val m = new util.IdentityHashMap[Any, Any]
	private var stack = Stack[UpdateInfo[_, _, _, _, _]]()

	def put[T](o: Any, mock: Persisted with T) {
		m.put(o, mock)
	}

	def get[T](o: Any): Option[Persisted with T] = {
		val g = m.get(o)
		if (g == null) None else Some(g.asInstanceOf[Persisted with T])
	}

	def down[PID, PT, V, FID, F](
		o: PT,
		ci: ColumnInfoRelationshipBase[PT, V, FID, F],
		parentEntity: Entity[PID, Persisted, PT]
		) {
		stack = stack.push(UpdateInfo(o, ci, parentEntity))
	}

	def peek[PID, PT, V, FID, F] =
		(if (stack.isEmpty) UpdateInfo(null, null, null) else stack.top).asInstanceOf[UpdateInfo[PID, PT, V, FID, F]]

	def up() {
		stack = stack.pop
	}

	def done() {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}

	def toErrorStr = {
		val sb = new StringBuilder
		stack.foreach {
			u =>
				sb append u.o append ('\n')
		}
		sb.toString()
	}
}

protected[mapperdao] case class UpdateInfo[PID, PT, V, FID, F](
	o: PT,
	ci: ColumnInfoRelationshipBase[PT, V, FID, F],
	parentEntity: Entity[PID, Persisted, PT]
	)

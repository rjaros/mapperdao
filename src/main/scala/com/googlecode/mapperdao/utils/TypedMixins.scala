package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.SurrogateIntId
import com.googlecode.mapperdao.SurrogateLongId
import com.googlecode.mapperdao.NaturalStringId

/**
 * provides CRUD methods for entities with IntId
 *
 * @see CRUD
 */
trait SurrogateIntIdCRUD[T] extends CRUD[Int, SurrogateIntId, T]

/**
 * provides CRUD methods for entities with LongId
 *
 * @see CRUD
 */
trait SurrogateLongIdCRUD[T] extends CRUD[Long, SurrogateLongId, T]

trait NaturalStringIdCRUD[T] extends CRUD[String, NaturalStringId, T]

/**
 * these mixin traits add querying methods to a dao. Please see the All trait
 */
trait SurrogateIntIdAll[T] extends All[SurrogateIntId, T]
trait SurrogateLongIdAll[T] extends All[SurrogateLongId, T]
trait NaturalStringIdAll[T] extends All[NaturalStringId, T]

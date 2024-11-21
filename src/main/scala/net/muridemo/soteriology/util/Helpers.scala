package net.muridemo.soteriology.util

import net.muridemo.soteriology.util.MemberHandle
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Constructor
import scala.reflect.ClassTag
import com.google.gson.JsonObject

import scala.jdk.CollectionConverters.*
import cats.Functor
import scala.collection.immutable.ArraySeq

import scala.util.boundary, boundary.{Label, break}

object Helpers {
  // enum Member[T] {
  //   case Field(field: Field)
  //   case Method(method: Method)
  //   case Constructor(constructor: Constructor[T])
  // }
  extension (obj: JsonObject)
    def getAsScalaArray(key: String) = {
      obj.getAsJsonArray(key).asList().asScala.toArray
    }

    def getAsImmutableArray(key: String) = {
      ArraySeq.unsafeWrapArray(obj.getAsJsonArray(key).asList().asScala.toArray)
    }

  extension (cls: Class[?])
    def getGeneralizedFields(): Array[Field | Method] = {
      cls.getDeclaredFields().asInstanceOf[Array[Field | Method]] ++ 
      cls.getDeclaredMethods().filter(_.getParameterCount() == 0).asInstanceOf[Array[Field | Method]]
    }
  
  given iSeqFunctor: Functor[IndexedSeq] with {
    def map[A, B](fa: IndexedSeq[A])(f: A => B): IndexedSeq[B] = fa.map(f)
  }

  object nullable:
    inline def apply[T](inline body: Label[Null] ?=> T): T | Null =
      boundary(body)

    extension [T](r: T | Null)
      /** Exits with null to next enclosing `nullable` boundary */
      transparent inline def ? (using Label[Null]): T =
        if r == null then break(null) else r
    extension [T](r: => T | Null)
      /** Tests r in a nullable context, returning alt if null */  
      transparent inline def ?? (inline alt: => T): T =
        val eval = nullable(r)
        if eval == null then alt else eval
}

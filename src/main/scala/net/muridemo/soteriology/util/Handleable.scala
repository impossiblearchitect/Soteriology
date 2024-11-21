package net.muridemo.soteriology.util

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Member
import java.lang.reflect.Type
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Constructor


/**
 * A typeclass for members that can be represented by a `MethodHandle`.
 */
trait Handleable[-T <: Member] {
  def handle(t: T): Option[(String, Array[Type], Type, MethodHandle)]
}

given FieldHandle: Handleable[Field] with
  def handle(field: Field) = {
    val lookup = MethodHandles.lookup()
    field.trySetAccessible()
    Some((field.getName(), Array.empty, field.getType(), lookup.unreflectGetter(field)))
  }


given MethodHandle: Handleable[Method] with
  def handle(method: Method) = {
    val lookup = MethodHandles.lookup()
    method.trySetAccessible()
    Some((method.getName(), method.getGenericParameterTypes(), method.getReturnType(), lookup.unreflect(method)))
  }


given ConstructorHandle: Handleable[Constructor[?]] with
  def handle(constructor: Constructor[?]) = {
    val lookup = MethodHandles.lookup()
    constructor.trySetAccessible()
    Some((constructor.getName(), constructor.getGenericParameterTypes(), constructor.getDeclaringClass(), lookup.unreflectConstructor(constructor)))
  }

given GeneralizedFieldHandle: Handleable[Field | Method] with
  def handle(member: Field | Method) = member match {
    case field: Field => FieldHandle.handle(field)
    case method: Method => MethodHandle.handle(method)
  }


given UnifiedMemberHandle: Handleable[Field | Method | Constructor[?]] with
  def handle(member: Field | Method | Constructor[?]) = member match {
    case field: Field => FieldHandle.handle(field)
    case method: Method => MethodHandle.handle(method)
    case constructor: Constructor[?] => ConstructorHandle.handle(constructor)
  }

object MemberHandle {
  def unapply[T <: Member](t: T)(using handleable: Handleable[T]) = handleable.handle(t)
}



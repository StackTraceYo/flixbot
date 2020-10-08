package org.stacktrace.yo.flixbot.util

import scala.language.implicitConversions

case class Ternary(b: Boolean) {
  def ?[T](t: => T) = new {
    def |(f: => T): T = if (b) t else f
  }
}



object Ternary {
  implicit def BooleanCondition(b: Boolean): Ternary = Ternary(b)

  implicit def StringCondition(b: String): Ternary = Ternary(b != null && b.nonEmpty)

  implicit def SeqCondition[A](b: Seq[A]): Ternary = Ternary(b != null && b.nonEmpty)

  implicit def ArrayCondition[A](b: Array[A]): Ternary = Ternary(b != null && b.nonEmpty)

  implicit def OptionCondition[A](b: Option[A]): Ternary = Ternary(b.isDefined)
}

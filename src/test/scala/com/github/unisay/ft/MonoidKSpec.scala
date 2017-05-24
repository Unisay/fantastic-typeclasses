package com.github.unisay.ft

import cats.MonoidK
import cats.implicits._
import cats.kernel.Monoid
import org.specs2.Specification

class MonoidKSpec extends Specification { def is = s2"""
 Monoid[List[?]].empty    $emptyListMonoid
 MonoidK[List].empty      $emptyListMonoidK
 Monoid[List[?]].combine  $combineListMonoid
 MonoidK[List].combine    $combineListMonoidK
 """

  object Foo

  // Monoid[A] means there is an “empty” A value that functions as an identity
  def emptyListMonoid = {
    val res: List[Foo.type] = Monoid[List[Foo.type]].empty
    res must_=== Nil
  }

  // A MonoidK[F] can produce a Monoid[F[A]] for any type A
  // The empty value just depend on the structure of F, but not on the structure of A.
  def emptyListMonoidK = {
    val res1: List[Foo.type] = MonoidK[List].empty[Foo.type]
    val res2: List[Foo.type] = MonoidK[List].empty // type parameter can be inferred
    (res1 must_=== Nil) and (res2 must_=== Nil)
  }

  // Monoid[A] allows A values to be combined
  def combineListMonoid = {
    val res: List[String] = Monoid[List[String]].combine(List("hello", "world"), List("goodbye", "moon"))
    res must_=== List("hello", "world", "goodbye", "moon")
  }

  // MonoidK[F] allows two F[A] values to be combined, for any A.
  // The combination operation just depend on the structure of F, but not on the structure of A.
  def combineListMonoidK = {
    val res: List[String] = MonoidK[List].combineK[String](List("hello", "world"), List("goodbye", "moon"))
    res must_=== List("hello", "world", "goodbye", "moon")
  }
}

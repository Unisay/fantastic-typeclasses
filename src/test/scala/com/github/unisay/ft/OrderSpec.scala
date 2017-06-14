package com.github.unisay.ft

import cats.{Order, Semigroup}
import cats.implicits._
import org.specs2._

class OrderSpec extends Specification { def is = s2"""
  testOrderComposition (combineAll)   $testOrderComposition
  testOrderComposition (semigroup)    $testOrderCompositionSemigroup
  """

  val elements = List((0, 1), (2, 2), (1, 0), (2, 1), (0, 0))
  val expected = List((0, 0), (1, 0), (0, 1), (2, 1), (2, 2))

  val orderByFirst: Order[(Int, Int)] = Order.by(_._1)
  val orderBySecond: Order[(Int, Int)] = Order.by(_._2)

  def testOrderCompositionSemigroup = {
    implicit val orderSemigroup: Semigroup[Order[(Int, Int)]] = Order.whenEqualMonoid
    implicit val ordering = (orderBySecond |+| orderByFirst).toOrdering
    elements.sorted must_=== expected
  }

  def testOrderComposition = {
    implicit val orderTuples: Ordering[(Int, Int)] =
      List(orderBySecond, orderByFirst)
        .combineAll(Order.whenEqualMonoid)
        .toOrdering

    elements.sorted must_=== expected
  }

}

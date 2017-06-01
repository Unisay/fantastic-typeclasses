package com.github.unisay.ft

import cats.Order
import cats.implicits._
import org.specs2._

class OrderSpec extends Specification { def is = s2"""
  testOrderComposition                $testOrderComposition
  """

  def testOrderComposition = {
    val elements = List(
      (0, 1),
      (2, 2),
      (1, 0),
      (2, 1),
      (0, 0)
    )

    val orderByFirst: Order[(Int, Int)] = Order.by(_._1)
    val orderBySecond: Order[(Int, Int)] = Order.by(_._2)
    val orders: List[Order[(Int, Int)]] = List(orderBySecond, orderByFirst)
    implicit val orderTuples: Ordering[(Int, Int)] = orders.combineAll(Order.whenEqualMonoid).toOrdering

    elements.sorted must_=== List(
      (0, 0),
      (1, 0),
      (0, 1),
      (2, 1),
      (2, 2)
    )
  }

}

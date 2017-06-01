package com.github.unisay.ft

import cats.kernel.{Order, PartialOrder}
import cats.syntax.partialOrder._
import com.github.unisay.ft.EqFixture._
import org.specs2._

class PartialOrderSpec extends Specification { def is = s2"""
  testPartialCompare                         $testPartialCompare
  testTryCompare                             $testTryCompare
  testPartialComparison                      $testPartialComparison
  testPMin                                   $testPMin
  testPMax                                   $testPMax
  testReverse                                $testReverse
  """

  implicit val partialOrderOfStrings: PartialOrder[String] = Order.fromComparable
  implicit val partialOrderOfNames: PartialOrder[Name] = PartialOrder.by(_.value)

  implicit val partialOrderOfThings: PartialOrder[Thing] = PartialOrder.from {
    case (a: MadeInChina, b: MadeInChina) => a.code partialCompare b.code
    case (a: MadeInGermany, b: MadeInGermany) => a.name partialCompare b.name
    case _ => Double.NaN
  }

  val a: Thing = MadeInChina(code = "a", Name("Fidget spinner"))
  val a2: Thing = MadeInChina(code = "a2", Name("Fidget spinner (rev. 2)"))
  val b: Thing = MadeInGermany(code = "b", Name("Fidget spinner"))

  def testPartialCompare = (a partialCompare b).toString must_=== "NaN"

  def testTryCompare = a tryCompare b must beNone

  def testPartialComparison = PartialOrder[Thing].partialComparison(a, b) must beNone // no syntax

  def testPMin = (a pmin b must beNone) and (a pmin a2 must beSome(a))

  def testPMax = (a pmax b must beNone) and (a pmax a2 must beSome(a2))

  def testReverse = PartialOrder[Thing].pmin(a, b) must_=== PartialOrder[Thing].reverse.pmax(a, b)
}

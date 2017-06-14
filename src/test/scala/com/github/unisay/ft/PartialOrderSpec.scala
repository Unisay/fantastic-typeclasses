package com.github.unisay.ft

import cats.implicits._
import cats.kernel.{Order, PartialOrder}
import com.github.unisay.ft.EqFixture._
import org.specs2._

import scala.Function.unlift
import scala.annotation.tailrec

class PartialOrderSpec extends Specification { def is = s2"""
  testPartialCompare                         $testPartialCompare
  testTryCompare                             $testTryCompare
  testPartialComparison                      $testPartialComparison
  testPMin                                   $testPMin
  testPMax                                   $testPMax
  testReverse                                $testReverse
  testTopoSort                               $testTopoSort
  """

  implicit val partialOrderOfStrings: PartialOrder[String] = Order.fromComparable
  implicit val partialOrderOfNames: PartialOrder[Name] = PartialOrder.by(_.value)

  implicit val partialOrderOfThings: PartialOrder[Thing] = PartialOrder.from {
    case (a: MadeInChina, b: MadeInChina) => a.code partialCompare b.code
    case (a: MadeInGermany, b: MadeInGermany) => a.name partialCompare b.name
    case _ => Double.NaN
  }

  val a: Thing = MadeInChina(code = 1, Name("Fidget spinner"))
  val a2: Thing = MadeInChina(code = 11, Name("Fidget spinner (rev. 2)"))
  val b: Thing = MadeInGermany(code = 2, Name("Fidget spinner"))

  def testPartialCompare = (a partialCompare b).toString must_=== "NaN"

  def testTryCompare = a tryCompare b must beNone

  def testPartialComparison = PartialOrder[Thing].partialComparison(a, b) must beNone // no syntax

  def testPMin = (a pmin b must beNone) and (a pmin a2 must beSome(a))

  def testPMax = (a pmax b must beNone) and (a pmax a2 must beSome(a2))

  def testReverse = PartialOrder[Thing].pmin(a, b) must_=== PartialOrder[Thing].reverse.pmax(a, b)

  @tailrec
  final def smin[A](as: List[A])(implicit PO: PartialOrder[A]): List[A] =
    if (as.size < 2) as else smin {
      as
        .combinations(2)
        .map(pair => pair.head pmin pair.last)
        .collect(unlift(identity))
        .toList
    }

  def topoSort[A](as: List[A])(implicit PO: PartialOrder[A]): List[A] = {
    if (as.size < 2) as else {
      val minimal = smin(as)
      val tail = if (minimal.isEmpty) as else topoSort(as diff minimal)
      minimal ++ tail
    }
  }

  def testTopoSort = {

    sealed trait Action
    object RemoveFurniture extends Action
    object PaintWalls extends Action
    object ReplaceWindows extends Action
    object RefinishFloors extends Action
    object AssignOffices extends Action
    object MoveFurniture extends Action

    implicit val actionPartialOrder: PartialOrder[Action] =
      (x: Action, y: Action) => (x, y) match {
        case (RemoveFurniture, _) => -1.0
        case (_, RemoveFurniture) => +1.0
        case (PaintWalls, RefinishFloors) => -1.0
        case (RefinishFloors, PaintWalls) => +1.0
        case _ => Double.NaN
      }

    val actions = List(
      RefinishFloors,
      PaintWalls,
      ReplaceWindows,
      RemoveFurniture,
      AssignOffices,
      MoveFurniture
    )

    val sorted: List[Action] = topoSort(actions)

    sorted must contain(actions)

    // Furniture had to be removed before anything
    sorted.head must_=== RemoveFurniture

    // painting had to be done before the floors
    sorted.indexOf(PaintWalls) must be_< (sorted.indexOf(RefinishFloors))
  }
}

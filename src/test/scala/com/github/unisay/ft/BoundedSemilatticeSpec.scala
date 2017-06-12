package com.github.unisay.ft

import cats.kernel.BoundedSemilattice
import org.specs2._

import scala.collection.immutable.IntMap
import scala.math.max

class BoundedSemilatticeSpec extends Specification { def is = s2"""
  clusterCounterTest                               $clusterCounterTest
  """

  case class Node(id: Int) {
    var counter = PNCounter()
    def increment(): Unit =
      counter = counter increment this
    def decrement(): Unit =
      counter = counter decrement this
    def ->:(other: Node)(implicit S: BoundedSemilattice[PNCounter]): Unit =
      counter = S.combine(counter, other.counter)
  }

  case class GCounter(state: IntMap[Int] = IntMap.empty) {
    def increment(node: Node): GCounter =
      copy(state = state.updated(node.id, state.getOrElse(node.id, 0) + 1))
    def value: Int =
      state.values.sum
  }

  case class PNCounter(pos: GCounter = GCounter(), neg: GCounter = GCounter()) {
    def increment(node: Node): PNCounter = copy(pos = pos.increment(node))
    def decrement(node: Node): PNCounter = copy(neg = neg.increment(node))
    def value: Int = pos.value - neg.value
  }

  implicit val gCounterBoundedSemilattice: BoundedSemilattice[GCounter] =
    new BoundedSemilattice[GCounter] {
      def empty: GCounter = GCounter()
      def combine(x: GCounter, y: GCounter): GCounter =
        GCounter(x.state.unionWith(y.state, (_, v1, v2) => max(v1, v2)))
    }

  implicit val pnCounterBoundedSemilattice: BoundedSemilattice[PNCounter] =
    new BoundedSemilattice[PNCounter] {
      def empty: PNCounter = PNCounter()
      def combine(x: PNCounter, y: PNCounter): PNCounter =
        PNCounter(pos = BoundedSemilattice[GCounter].combine(x.pos, y.pos),
                  neg = BoundedSemilattice[GCounter].combine(x.neg, y.neg))
    }

  def clusterCounterTest = {
    val node1 = Node(id = 1)
    val node2 = Node(id = 2)
    val node3 = Node(id = 3)

    node2 increment()
    node3 increment()
    node3 increment()
    node3 decrement()

    node3 ->: node2
              node2 ->: node1
              node2 ->: node1 // demonstrating idempotency

    node1.counter.value must_=== 2
  }

}

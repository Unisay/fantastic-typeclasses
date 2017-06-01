package com.github.unisay.ft

import cats.implicits._
import cats.kernel.BoundedSemilattice
import org.specs2._

import scala.collection.immutable.IntMap
import scala.math.max

class BoundedSemilatticeSpec extends Specification { def is = s2"""
  clusterCounterTest                               $clusterCounterTest
  """

  case class Node(id: Int) {
    val counter = new GCounter()
    def increment(): Unit = counter.increment(this)
  }

  case class Cluster(nodes: List[Node]) {
    def counter: Int = nodes.foldMap(_.counter).value
  }

  class GCounter(var state: IntMap[Int] = IntMap.empty) {
    def increment(node: Node): Unit = state = state.updated(node.id, state.getOrElse(node.id, 0) + 1)
    def value: Int = state.values.sum
  }

  implicit val gCounterBoundedSemilattice: BoundedSemilattice[GCounter] =
    new BoundedSemilattice[GCounter] {
      def empty: GCounter = new GCounter()
      def combine(x: GCounter, y: GCounter): GCounter =
        new GCounter(x.state.unionWith(y.state, (_, v1, v2) => max(v1, v2)))
    }

  def clusterCounterTest = {
    val node1 = Node(id = 1)
    val node2 = Node(id = 2)
    val node3 = Node(id = 3)
    val nodes = List(node1, node2, node3)
    val cluster = Cluster(nodes)
    node2.increment()
    node3.increment()
    cluster.counter must_=== 2
  }

}

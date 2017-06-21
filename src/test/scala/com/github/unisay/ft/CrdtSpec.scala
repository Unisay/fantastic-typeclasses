package com.github.unisay.ft

import cats.kernel.{BoundedSemilattice, CommutativeMonoid, Monoid}
import org.specs2._

import scala.collection.immutable.IntMap
import scala.collection.mutable.ListBuffer
import scala.math.max

class CrdtSpec extends Specification { def is = s2"""
  testCvRDT                          $testCvRDT
  testCmRDT                          $testCmRDT
  testCmRDT with multiplication      $testCmRDT2
  """

  def testCvRDT = {
    class Node(val id: Int) {
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


    val node1 = new Node(id = 1)
    val node2 = new Node(id = 2)
    val node3 = new Node(id = 3)

    node2 increment()
    node3 increment()
    node3 increment()
    node3 decrement()

    node3 ->: node2
              node2 ->: node1
              node2 ->: node1 // demonstrating idempotency

    node1.counter.value must_=== 2
  }

  def testCmRDT = {

    sealed trait Operation {
      def apply(state: Int): Int
    }
    case class Increment(value: Int) extends Operation {
      override def apply(state: Int): Int = state + value
    }
    case class Decrement(value: Int) extends Operation {
      override def apply(state: Int): Int = state - value
    }

    class Node(val id: Int) {
      private var _counter: Int = 0
      def counter: Int = _counter
      private val _nodes: ListBuffer[Node] = ListBuffer.empty
      def broadcastTo(nodes: Node*): Unit = _nodes ++= nodes
      def increment(value: Int): Unit = apply(Increment(value))
      def decrement(value: Int): Unit = apply(Decrement(value))
      def apply(operation: Operation): Unit = {
        local(operation)
        downstream(operation)
      }
      def local(operation: Operation): Unit = {
        _counter = operation(_counter)
        println(s"Node $id applied $operation locally, result = ${_counter}")
      }
      def downstream(operation: Operation): Unit = _nodes.foreach(_.local(operation))
    }

    val node1 = new Node(id = 1)
    val node2 = new Node(id = 2)
    val node3 = new Node(id = 3)

    node1 broadcastTo (node2, node3)
    node2 broadcastTo (node1, node3)
    node3 broadcastTo (node1, node2)

    node2 increment 2
    node3 increment 3
    node3 increment 0
    node3 decrement 4

    node1.counter must_=== 1
  }

  def testCmRDT2 = {

    case class Operation(inc: Int, dec: Int, mul: Int) {
      def apply(value: Int): Int = (value + inc - dec) * mul
    }

    def inc(value: Int): Operation = Operation(inc = value, dec = 0, mul = 1)
    def dec(value: Int): Operation = Operation(inc = 0, dec = value, mul = 1)
    def mul(value: Int): Operation = Operation(inc = 0, dec = 0, mul = value)

    implicit val operationCM: CommutativeMonoid[Operation] =
      new CommutativeMonoid[Operation] {
        def empty: Operation =
          Operation(inc = 0, dec = 0, mul = 1)
        def combine(x: Operation, y: Operation): Operation =
          Operation(inc = x.inc + y.inc, dec = x.dec + y.dec, mul = x.mul * y.mul)
      }


    class Node(val id: Int) {
      private val operations = ListBuffer[Operation]()
      def counter: Int = {
        val operation = Monoid[Operation].combineAll(operations)
        operation.apply(0)
      }
      private val _nodes: ListBuffer[Node] = ListBuffer.empty
      def broadcastTo(nodes: Node*): Unit = _nodes ++= nodes
      def apply(operation: Operation): Unit = {
        local(operation)
        downstream(operation)
      }
      def local(operation: Operation): Unit = {
        operations += operation
        println(s"Node $id has ${operations.mkString(", ")}, result = $counter")
      }
      def downstream(operation: Operation): Unit = _nodes.foreach(_.local(operation))
    }

    val node1 = new Node(id = 1)
    val node2 = new Node(id = 2)
    val node3 = new Node(id = 3)

    node1 broadcastTo (node2, node3)
    node2 broadcastTo (node1, node3)
    node3 broadcastTo (node1, node2)

    node2 apply inc(1)
    node3 apply inc(3)
    node3 apply mul(2)
    node3 apply dec(4)

    node1.counter must_=== 0 // TODO
  }
}

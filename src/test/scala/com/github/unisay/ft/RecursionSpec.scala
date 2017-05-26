package com.github.unisay.ft

import cats.Eval
import cats.Eval.always
import org.specs2._

class RecursionSpec extends Specification { def is = s2"""
 Stream[Int]           $testStream
 Eval[Stream[Int]]     $testEvalStream
 """

  def testStream = {
    def one: Stream[Int] = 1 #:: zer // 1 #:: 0 #:: 1 #:: ⊥
    def zer: Stream[Int] = 0 #:: one // 0 #:: 1 #:: 0 #:: ⊥
    one.take(3).toList must_=== List(1, 0, 1)
  }

  def testEvalStream = {
    def one: Eval[Stream[Int]] = always(1 #:: zer.value)
    def zer: Eval[Stream[Int]] = always(0 #:: one.value)
    one.map(_.take(3).toList).value must_=== List(1, 0, 1)
  }
}

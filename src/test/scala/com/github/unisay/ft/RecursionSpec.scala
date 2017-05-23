package com.github.unisay.ft

import cats.Eval
import org.specs2._

class RecursionSpec extends Specification { def is = s2"""
 stackOverflow              $stackOverflow
 noStackOverflow            $noStackOverflow
 """

  def noStackOverflow = {
    def one: Stream[Int] = 1 #:: zer // 1 #:: 0 #:: 1 #:: ⊥
    def zer: Stream[Int] = 0 #:: one // 0 #:: 1 #:: 0 #:: ⊥
    one.take(3).toList must_=== List(1, 0, 1)
  }

  def stackOverflow = {
    def one: Eval[Stream[Int]] = zer.map(0 #:: _)
    def zer: Eval[Stream[Int]] = one.map(1 #:: _)
    one.map(_.take(3).toList).value must_=== List(1, 0, 1)
  }
}

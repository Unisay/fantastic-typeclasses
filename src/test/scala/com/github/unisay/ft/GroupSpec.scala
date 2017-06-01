package com.github.unisay.ft

import cats.implicits._
import org.specs2._

class GroupSpec extends Specification { def is = s2"""
  inverse        $testInverse
  remove         $testRemove
  combineN n=0   $testCombineN_Zero
  combineN n>0   $testCombineN_Positive
  combineN n<0   $testCombineN_Negative
  """

  def testInverse = {
    -42.inverse must_=== 42
  }

  def testRemove = {
    100 |-| 58 must_=== 42
  }

  def testCombineN_Zero = {
    100 combineN 0 must_=== 0
  }

  def testCombineN_Positive = {
    100 combineN 2 must_=== 200
  }

  def testCombineN_Negative = {
    100 combineN -2 must_=== -200
  }

}

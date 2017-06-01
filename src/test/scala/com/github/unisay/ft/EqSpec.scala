package com.github.unisay.ft

import cats.Monoid
import cats.implicits._
import cats.kernel.Eq
import com.github.unisay.ft.EqFixture._
import org.specs2._

class EqSpec extends Specification { def is = s2"""
  test eqv for strings                   $testEqStrings
  test eqv for names                     $testEqNames
  test eqv for some things (nominal)     $testNominalEquality
  test eqv for some things (composite)   $testCompositeEquality
  test eqv for some things (combined)    $testCombinedEquality
  """

  implicit val stringEquality: Eq[String] = Eq.fromUniversalEquals

  implicit val caseInsensitiveNameEquality: Eq[Name] = Eq.by(_.value.toLowerCase)

  implicit val qualitativeEquality: Eq[Quality] = Eq.fromUniversalEquals

  val equalityByName: Eq[Thing]    = Eq[Name].on(_.name) // contramap
  val equalityByCode: Eq[Thing]    = Eq[String].on(_.code)
  val equalityByQuality: Eq[Thing] = Eq[Quality].on(_.quality)
  val compositeEquality: Eq[Thing] = equalityByCode or (equalityByName and equalityByQuality) // <|>

  def combinedEquality: Eq[Thing] = {
    // Note: Eq.allEquals is an identity element under the 'and' composition
    implicit val equalityMonoid: Monoid[Eq[Thing]] = Eq.allEqualBoundedSemilattice
    List(equalityByCode, equalityByName, equalityByQuality).combineAll(equalityMonoid)
  }

  def testEqStrings = {
    (Eq[String].eqv("foo", "foo") must_=== true) and
    (Eq[String].neqv("foo", "Foo") must_=== true)
  }

  def testEqNames = {
    (Eq[Name].eqv(Name("foo"), Name("foo")) must_=== true) and
    (Eq[Name].eqv(Name("foo"), Name("Foo")) must_=== true)
  }

  def testNominalEquality = {
    (equalityByName.eqv(MadeInGermany(code = "1", Name("f")), MadeInGermany(code = "2", Name("F"))) must_=== true) and
    (equalityByName.eqv(MadeInGermany(code = "1", Name("f")), MadeInChina(code = "1", Name("f"))) must_=== true)
  }

  def testCompositeEquality = {
    (compositeEquality.eqv(MadeInGermany(code = "1", Name("f")), MadeInGermany(code = "1", Name("Z"))) must_=== true) and
    (compositeEquality.eqv(MadeInGermany(code = "1", Name("f")), MadeInGermany(code = "2", Name("F"))) must_=== true) and
    (compositeEquality.eqv(MadeInGermany(code = "1", Name("f")), MadeInChina(code = "2", Name("f"))) must_=== false)
  }

  def testCombinedEquality =
    combinedEquality.eqv(MadeInGermany(code = "1", Name("f")), MadeInGermany(code = "1", Name("F"))) must_=== true

}

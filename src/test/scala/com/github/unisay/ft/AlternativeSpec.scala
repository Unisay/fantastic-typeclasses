package com.github.unisay.ft

import cats.{Alternative, Applicative, Apply, Eval, MonoidK}
import org.specs2._
import cats.implicits._
import cats.syntax.eq._
import cats.kernel.Monoid

class MonoidKSpec extends Specification { def is = s2"""
 Monoid[List[?]].empty    $emptyListMonoid
 MonoidK[List].empty      $emptyListMonoidK
 Monoid[List[?]].combine  $combineListMonoid
 MonoidK[List].combine    $combineListMonoidK
 """

  object Foo

  // Monoid[A] means there is an “empty” A value that functions as an identity
  def emptyListMonoid = {
    val res: List[Foo.type] = Monoid[List[Foo.type]].empty
    res must_=== Nil
  }

  // A MonoidK[F] can produce a Monoid[F[A]] for any type A
  // The empty value just depend on the structure of F, but not on the structure of A.
  def emptyListMonoidK = {
    val res1: List[Foo.type] = MonoidK[List].empty[Foo.type]
    val res2: List[Foo.type] = MonoidK[List].empty // type parameter can be inferred
    (res1 must_=== Nil) and (res2 must_=== Nil)
  }

  // Monoid[A] allows A values to be combined
  def combineListMonoid = {
    val res: List[String] = Monoid[List[String]].combine(List("hello", "world"), List("goodbye", "moon"))
    res must_=== List("hello", "world", "goodbye", "moon")
  }

  // MonoidK[F] allows two F[A] values to be combined, for any A.
  // The combination operation just depend on the structure of F, but not on the structure of A.
  def combineListMonoidK = {
    val res: List[String] = MonoidK[List].combineK[String](List("hello", "world"), List("goodbye", "moon"))
    res must_=== List("hello", "world", "goodbye", "moon")
  }
}

// https://wiki.haskell.org/Typeclassopedia#Failure_and_choice:_Alternative.2C_MonadPlus.2C_ArrowPlus
class AlternativeSpec extends Specification { def is = s2"""
  parse binary zero (positive)   $binZeroPositive
  parse binary zero (negative)   $binZeroNegative
  parse binary digit             $parseBinDigit
  parse binary digits            $parseBinDigits
  """
  implicit val listOfOptions: Alternative[λ[α => List[Option[α]]]] = Alternative[List].compose[Option]
  implicit val optionOfLists: Alternative[λ[α => Option[List[α]]]] = Alternative[Option].compose[List]

  type Digit = Int
  type Parser[A] = String => Option[A]

  trait MyAlternative[A[_]] extends Alternative[A] {
    def <|>[F](l: A[F], r: => A[F]): A[F]
  }

  implicit class AlternativeSyntax[F, A[_]: MyAlternative](val a: A[F]) {
    def <|>(o: => A[F]): A[F] = implicitly[MyAlternative[A]].<|>(a, o)
    def many: A[List[F]] = some <|> Applicative[A].pure(List.empty[F])
    def some: A[List[F]] = Apply[A].map2Eval(a, Eval.later(many))(_ :: _).value
  }

  implicit val alternativeParser: MyAlternative[Parser] = new MyAlternative[Parser] {
    def <|>[F](l: Parser[F], r: => Parser[F]): Parser[F] = (s: String) => l(s).orElse(r(s))
    def empty[A]: Parser[A] = _ => None
    def pure[A](x: A): Parser[A] = _ => Some(x)
    def ap[A, B](ff: Parser[A => B])(fa: Parser[A]): Parser[B] = s => (ff(s), fa(s)) match {
      case (Some(f), sa @ Some(_)) => sa map f
      case _ => None
    }
    def combineK[A](x: Parser[A], y: Parser[A]): Parser[A] = s => x(s) orElse y(s)
  }

  def digit(i: Digit): Parser[Digit] = {
    assert (i <= 9 && i >= 0, "Digit 0-9 is expected")
    _.headOption.map(_.asDigit).filter(_ === i)
  }

//  val binDigit: Parser[Digit] = digit(0) |@| digit(1) map (_ orElse _)
  def binDigit: Parser[Digit] = digit(0) <|> digit(1)
  def binDigits: Parser[List[Digit]] = binDigit.some

  def binZeroPositive = digit(0).apply("0") must beSome(0)
  def binZeroNegative = digit(0).apply("1") must beNone

  def parseBinDigit = {
    binDigit.apply("0").must(beSome(0)) and
    binDigit.apply("1").must(beSome(1))
  }

  def parseBinDigits =
    binDigits("010101111") must beSome(List(0, 1, 0, 1, 0, 1, 1, 1, 1))
}

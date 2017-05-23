package com.github.unisay.ft

import cats.Eval.{always, now}
import cats.data.EitherT
import cats.{Alternative, Eval, MonoidK}
import org.specs2._
import cats.implicits._
import cats.kernel.Monoid
import com.github.unisay.ft.Alt._


trait Alt[A[_]] extends Alternative[A] {
  def alt[F](l: A[F], r: => A[F]): A[F]
}

object Alt {
  def apply [A[_]: Alt]: Alt[A] = implicitly[Alt[A]]
  def alt   [A[_]: Alt, B](l: A[B], r: => A[B]): A[B] = Alt[A].alt(l, r)
  def some  [A[_]: Alt, B](a: A[B]): A[Stream[B]] = Alt[A].map2Eval(a, always(many(a)))(_ #:: _).value
  def many  [A[_]: Alt, B](a: A[B]): A[Stream[B]] = alt(some(a), Stream.empty[B].pure[A])
}

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
  test1                          $stackOverflow
  test2                          $noStackOverflow
  """
/*  implicit val listOfOptions: Alternative[λ[α => List[Option[α]]]] = Alternative[List].compose[Option]
  implicit val optionOfLists: Alternative[λ[α => Option[List[α]]]] = Alternative[Option].compose[List]*/

  type Digit = Int
  type Offset = Int
  type Error = String

  trait Parser[A] {
    def apply(s: String, o: Offset): EitherT[Eval, Error, (Int, A)]
  }

  def parse[A](parser: Parser[A], s: String): Either[Error, A] =
    parser(s, 0).map(_._2).value.value

  implicit val alternativeParser: Alt[Parser] = new Alt[Parser] {
    def alt[F](l: Parser[F], r: => Parser[F]): Parser[F] =
      (s: String, o: Offset) => l(s, o).recoverWith { case (_: Error) => r(s, o) }
    def combineK[A](x: Parser[A], y: Parser[A]): Parser[A] =
      alt(x, y)
    def empty[A]: Parser[A] =
      (_, _) => EitherT.left(now("Error"))
    def pure[A](a: A): Parser[A] =
      (_, o) => EitherT.right(always((o, a)))
    def ap[A, B](ff: Parser[A => B])(fa: Parser[A]): Parser[B] =
      (s, o) => ff(s, o).flatMap {
        case (o1, f) => fa(s, o1).map {
          case (o2, a) => (o2, f(a))
        }
      }

    override def map[A, B](fa: Parser[A])(f: A => B): Parser[B] =
      (s: String, o: Offset) => fa(s, o).map { case (o1, a) => (o1, f(a)) }

    override def map2Eval[A, B, Z](fa: Parser[A], efb: Eval[Parser[B]])(f: (A, B) => Z): Eval[Parser[Z]] =
      efb.map { fb =>
        (s: String, o: Offset) => {
          for {
            ra <- fa(s, o)
            (oa, a) = ra
            rb <- fb(s, o)
            (ob, b) = rb
          } yield (oa + ob, f(a, b))
        }
      }
  }

  def digit(i: Digit): Parser[Digit] = {
    assert (i <= 9 && i >= 0, "Digit 0-9 is expected")
    (s: String, o: Offset) => {
      println(s.substring(0, o) + "|" + s.substring(o))
      EitherT {
        Eval.now {
          s.headOption
           .map(_.asDigit)
           .filter(_ === i)
           .map((d: Digit) => (o + 1, d))
           .toRight("Unexpected digit occurred")
        }
      }
    }
  }

//  val binDigit: Parser[Digit] = digit(0) |@| digit(1) map (_ orElse _)
  def binDigit: Parser[Digit] = alt(digit(0), digit(1))
  def binDigits: Parser[Stream[Digit]] = Alt.some(binDigit)

  def binZeroPositive = parse(digit(0), "0") must beRight(0)
  def binZeroNegative = parse(digit(0), "1") must beLeft

  def parseBinDigit = {
    parse(binDigit, "0").must(beRight(0)) and
    parse(binDigit, "1").must(beRight(1))
  }

  def parseBinDigits =
    parse(binDigits, "010").map(_.toList) must beRight(List(0, 1, 0))

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

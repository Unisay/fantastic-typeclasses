package com.github.unisay.ft

import cats.Alternative
import cats.implicits._
import com.github.unisay.ft.Alt._
import org.specs2._


trait Alt[A[_]] extends Alternative[A] {
  def alt[F](l: => A[F], r: => A[F]): A[F]
}

object Alt {
  def apply [A[_]: Alt]: Alt[A] = implicitly[Alt[A]]
  def alt   [A[_]: Alt, B](l: => A[B], r: => A[B]): A[B] = Alt[A].alt(l, r)
  def some  [A[_]: Alt, B](a: A[B]): A[Stream[B]] = Alt[A].map2(a, many(a))(_ #:: _)
  def many  [A[_]: Alt, B](a: A[B]): A[Stream[B]] = alt(some(a), Stream.empty[B].pure[A])
}

// https://wiki.haskell.org/Typeclassopedia#Failure_and_choice:_Alternative.2C_MonadPlus.2C_ArrowPlus
class AlternativeSpec extends Specification { def is = s2"""
  parse binary zero (positive)   $binZeroPositive
  parse binary zero (negative)   $binZeroNegative
  parse binary digit             $parseBinDigit
  parse some binary digits       $parseSomeBinDigits
  parse many binary digits       $parseManyBinDigits
  """

  /*
  implicit val listOfOptions: Alternative[λ[α => List[Option[α]]]] = Alternative[List].compose[Option]
  implicit val optionOfLists: Alternative[λ[α => Option[List[α]]]] = Alternative[Option].compose[List]
  */

  type Digit = Int
  type Offset = Int
  type Error = String

  type Parser[A] = (String, Offset) => Either[Error, (Offset, A)]

  implicit val alternativeParser: Alt[Parser] = new Alt[Parser] {
    def alt[F](l: => Parser[F], r: => Parser[F]): Parser[F] =
      (s: String, o: Offset) => l(s, o).recoverWith { case (_: Error) => r(s, o) }
    def combineK[A](x: Parser[A], y: Parser[A]): Parser[A] =
      alt(x, y)
    def empty[A]: Parser[A] =
      (_, _) => Either.left("Error")
    def pure[A](a: A): Parser[A] =
      (_, o) => Either.right((o, a))
    def ap[A, B](ff: Parser[A => B])(fa: Parser[A]): Parser[B] =
      (s, o) => ff(s, o).flatMap {
        case (o1, f) => fa(s, o1).map {
          case (o2, a) => (o2, f(a))
        }
      }

    override def map[A, B](fa: Parser[A])(f: A => B): Parser[B] =
      (s: String, o: Offset) => fa(s, o).map { case (o1, a) => (o1, f(a)) }
  }

  def digit(i: Digit): Parser[Digit] = {
    assert (i <= 9 && i >= 0, "Digit 0-9 is expected")
    (s: String, o: Offset) => {
      if (s.length <= o)
        Either.left(s"No input at offset $o")
      else {
        s.drop(o)
          .headOption
          .map(_.asDigit)
          .filter(_ === i)
          .map((d: Digit) => (o + 1, d))
          .toRight("Expected: digit")
      }
    }
  }

  def binDigit: Parser[Digit] = alt(digit(0), digit(1))
  def someBinDigits: Parser[Stream[Digit]] = Alt.some(binDigit)
  def manyBinDigits: Parser[Stream[Digit]] = many(binDigit)

  // tests

  def binZeroPositive =
    digit(0).apply("0", 0) must beRight((1, 0))

  def binZeroNegative =
    digit(0).apply("1", 0) must beLeft("Expected: digit")

  def parseBinDigit =
    binDigit("0", 0).must(beRight((1, 0))) and
    binDigit("1", 0).must(beRight((1, 1)))

  def parseSomeBinDigits =
    someBinDigits("010110", 1).must(beRight((6, Stream(1, 0, 1, 1, 0)))) and
    someBinDigits("no digits", 0).must(beLeft("Expected: digit"))

  def parseManyBinDigits =
    manyBinDigits("no digits", 0).must(beRight((0, Stream()))) and
    manyBinDigits("10end", 0).must(beRight((2, Stream(1, 0))))

}

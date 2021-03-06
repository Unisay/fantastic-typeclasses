package com.github.unisay.ft

object EqFixture {

  case class Name(value: String)

  sealed trait Quality
  object Good extends Quality
  object Bad extends Quality

  trait Thing {
    def name: Name
    def code: Int
    def quality: Quality
  }

  case class MadeInGermany(code: Int, name: Name) extends Thing {
    def quality: Quality = Good
  }

  case class MadeInChina(code: Int, name: Name) extends Thing {
    def quality: Quality = Bad
  }

}

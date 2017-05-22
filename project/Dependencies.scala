import sbt._

object Dependencies {
  lazy val cats = Seq("org.typelevel" %% "cats" % "0.9.0")
  lazy val specs2 = Seq("org.specs2" %% "specs2-core" % "3.8.9" % "test")
}

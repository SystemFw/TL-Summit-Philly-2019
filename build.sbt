lazy val root = (project in file(".")).settings(
  commonSettings,
  consoleSettings,
  compilerOptions,
  typeSystemEnhancements,
  dependencies
)

lazy val commonSettings = Seq(
  name := "examples",
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.11.12", "2.12.5")
)

lazy val consoleSettings = Seq(
  initialCommands := s"import Examples._",
  scalacOptions in (Compile, console) -= "-Ywarn-unused-import"
)

lazy val compilerOptions =
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-target:jvm-1.8",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-Ypartial-unification",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )

lazy val typeSystemEnhancements =
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

def dep(org: String)(version: String)(modules: String*) =
  Seq(modules: _*) map { name =>
    org %% name % version
  }

lazy val dependencies = {
  val fs2 = dep("co.fs2")("1.0.4")(
    "fs2-core",
    "fs2-io"
  )

  val http4s = dep("org.http4s")("0.20.0-M4")(
    "http4s-dsl",
    "http4s-blaze-server",
    "http4s-blaze-client",
    "http4s-circe",
    "http4s-scala-xml"
  )

  val circe = dep("io.circe")("0.11.0")(
    "circe-generic",
    "circe-literal",
    "circe-parser"
  )

  val mixed = Seq(
    "org.typelevel" %% "mouse" % "0.20",
    "org.typelevel" %% "kittens" % "1.2.0",
    "com.chuusai" %% "shapeless" % "2.3.3"
  )

  def extraResolvers =
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    )

  val deps =
    libraryDependencies ++= Seq(
      fs2,
      http4s,
      circe,
      mixed
    ).flatten

  Seq(deps, extraResolvers)
}

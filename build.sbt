import java.nio.file.Files

name := "codacy-scalastyle"

ThisBuild / scalaVersion := "2.13.5"

val scalastyleVersion = "1.5.1"

val commonDeps = Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.beautiful-scala" %% "scalastyle" % scalastyleVersion,
  "com.codacy" %% "codacy-engine-scala-seed" % "5.0.3"
)

libraryDependencies ++= commonDeps

val `doc-generator` = project
  .settings(libraryDependencies ++= commonDeps, Compile / sourceGenerators += Def.task {
    val file = (Compile / sourceManaged).value / "Versions.scala"
    IO.write(file, s"""object Versions { val scalastyle = "$scalastyleVersion" }""")
    Seq(file)
  }.taskValue)

enablePlugins(JavaAppPackaging)

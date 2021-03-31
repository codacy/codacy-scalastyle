import java.nio.file.Files

name := "codacy-scalastyle"

ThisBuild / scalaVersion := "2.13.5"

val scalastyleVersion = "1.5.0"

val commonDeps = Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "com.beautiful-scala" %% "scalastyle" % scalastyleVersion,
  "com.codacy" %% "codacy-engine-scala-seed" % "5.0.3"
)

libraryDependencies ++= commonDeps

val `doc-generator` = project
  .settings(
    libraryDependencies ++= commonDeps ++ Seq("com.lihaoyi" %% "os-lib" % "0.7.3"),
    Compile / sourceGenerators += Def.task {
      val file = (Compile / sourceManaged).value / "Versions.scala"
      IO.write(file, s"""object Versions { val scalastyle = "$scalastyleVersion" }""")
      Seq(file)
    }.taskValue
  )

enablePlugins(JavaAppPackaging)

import java.nio.file.Files

name := "codacy-scalastyle"

scalaVersion := "2.13.5"

val scalastyleVersion = IO.read(file(".scalastyle-version")).trim

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "com.beautiful-scala" %% "scalastyle" % scalastyleVersion,
  "com.codacy" %% "codacy-engine-scala-seed" % "5.0.3"
)

enablePlugins(JavaAppPackaging)

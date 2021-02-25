import java.nio.file.Files

name := "codacy-scalastyle"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "com.beautiful-scala" %% "scalastyle" % "1.5.0",
  "com.codacy" %% "codacy-engine-scala-seed" % "5.0.3"
)

enablePlugins(JavaAppPackaging)

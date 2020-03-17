import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

organization := "codacy"

name := "codacy-scalastyle"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.codacy" %% "codacy-engine-scala-seed" % "4.0.2"
)

enablePlugins(AshScriptPlugin)

enablePlugins(DockerPlugin)

version in Docker := "1.0"

mappings in Universal ++= {
  (resourceDirectory in Compile) map { (resourceDir: File) =>
    val src = resourceDir / "docs"
    val dest = "/docs"

    for {
      path <- src.allPaths.get if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  }
}.value

mappings in Universal ++= {
  (baseDirectory in Compile) map { (directory: File) =>
    val src = directory / "jar"

    for {
      path <- src.allPaths.get
      if !path.isDirectory
    } yield path -> src.toPath.relativize(path.toPath).toString
  }
}.value

val dockerUser = "docker"
val dockerGroup = "docker"

daemonUser in Docker := dockerUser

daemonGroup in Docker := dockerGroup

dockerBaseImage := "openjdk:8-jre-alpine"

dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("ADD", _) =>
    List(
      Cmd("RUN", s"adduser -u 2004 -D $dockerUser"),
      cmd,
      Cmd("RUN", "mv /opt/docker/docs /docs"),
      Cmd("RUN", "mv /opt/docker/scalastyle-1.0.0-with-id.jar /opt/docker/scalastyle.jar"),
      ExecCmd("RUN", Seq("chown", "-R", s"$dockerUser:$dockerGroup", "/docs"): _*)
    )
  case other => List(other)
}

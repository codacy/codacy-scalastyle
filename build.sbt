import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

name := """codacy-engine-scalastyle"""

version := "1.0-SNAPSHOT"

val languageVersion = "2.11.7"

scalaVersion := languageVersion

resolvers ++= Seq(
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.3.10" withSources(),
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5" withSources(),
  "com.codacy" %% "codacy-engine-scala-seed" % "2.7.1",
  "com.github.pathikrit" %% "better-files" % "2.16.0"
)

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

version in Docker := "1.0"

val installAll =
  s"""apk --no-cache add bash
      |&& rm -rf /var/cache/apk/*""".stripMargin.replaceAll(System.lineSeparator(), " ")

mappings in Universal <++= (resourceDirectory in Compile) map { (resourceDir: File) =>
  val src = resourceDir / "docs"
  val dest = "/docs"

  for {
    path <- (src ***).get
    if !path.isDirectory
  } yield path -> path.toString.replaceFirst(src.toString, dest)
}

mappings in Universal <++= (baseDirectory in Compile) map { (directory: File) =>
  val src = directory / "jar"

  for {
    path <- (src ***).get
    if !path.isDirectory
  } yield path -> src.toPath.relativize(path.toPath).toString
}

val dockerUser = "docker"
val dockerGroup = "docker"

daemonUser in Docker := dockerUser

daemonGroup in Docker := dockerGroup

dockerBaseImage := "openjdk:8-jre-alpine"

dockerCommands := dockerCommands.value.flatMap {
  case cmd@Cmd("ADD", _) => List(
    Cmd("RUN", s"adduser -u 2004 -D $dockerUser"),
    cmd,
    Cmd("RUN", installAll),
    Cmd("RUN", "mv /opt/docker/docs /docs"),
    Cmd("RUN", "mv /opt/docker/scalastyle-1.0.0-with-id.jar /opt/docker/scalastyle.jar"),
    ExecCmd("RUN", Seq("chown", "-R", s"$dockerUser:$dockerGroup", "/docs"): _*)
  )
  case other => List(other)
}

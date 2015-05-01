val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"

val dispatch = "net.databinder" %% "dispatch-http" % "0.8.10"

val dispatchJson = "net.databinder" %% "dispatch-json" % "0.8.10"

val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

val mainClassName = "tumblishr.TumblishrMain"

lazy val commonSettings = Seq(
  organization := "org.behaghel",
  version := "0.1.0",
  scalaVersion := "2.11.6"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "tumblishr",
    libraryDependencies += scalatest,
    libraryDependencies += typesafeConfig,
    libraryDependencies += dispatch,
    libraryDependencies += dispatchJson,
    mainClass := Some(mainClassName),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript)),
    assemblyJarName in assembly := "tum"
  )


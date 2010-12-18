import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  
  val javaNetRepo = "Java.net Repository for Maven" at
    "http://download.java.net/maven/2"
  val newReleaseToolsRepository = ScalaToolsSnapshots

  val scalaTestDep = "org.scalatest" % "scalatest" % "1.2.1-SNAPSHOT" % "test"

  val dispatchDep = "net.databinder" %% "dispatch" % "0.7.8"
  val jerseyClientDep = "com.sun.jersey" % "jersey-client" % "1.4"
}

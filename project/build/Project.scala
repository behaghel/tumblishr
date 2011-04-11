import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with ProguardProject {

  val javaNetRepo = "Java.net Repository for Maven" at
    "http://download.java.net/maven/2"
  val newReleaseToolsRepository = ScalaToolsSnapshots

  val scalaTestDep = "org.scalatest" % "scalatest" % "1.2.1-SNAPSHOT" % "test"

  val dispatchDep = "net.databinder" %% "dispatch" % "0.7.8"

  // sadly, the published configgy is pulling in an inexistent vscaladoc version
  // hence intransitive()
  val configgyDep = "net.lag" % "configgy" % "2.0.0" intransitive()

  val mainClassName = "tumblishr.TumblishrMain"
  override def mainClass = Some(mainClassName)

  // Proguard config
  val javaHome = System.getProperty("java.home")
  val osXJdkJarsDir = Path.fromFile(new java.io.File(javaHome).getParent()) / "Classes"
  val jceLocation = osXJdkJarsDir / "jce.jar"
  val jsseLocation = osXJdkJarsDir / "jsse.jar"
  override def proguardLibraryJars = super.proguardLibraryJars +++ jceLocation +++ jsseLocation
  override def makeInJarFilter(file: String) = "!META-INF/**"
  override def proguardInJars = super.proguardInJars +++ scalaLibraryPath

  // config borrowed from http://proguard.sourceforge.net/manual/examples.html#scala
  val scalaKeep = List(
    "-dontwarn **$$anonfun$*",
    "-dontwarn scala.collection.immutable.RedBlack$Empty",
    "-dontwarn scala.tools.**,plugintemplate.**",
    """-keepclasseswithmembers public class * {
        public static void main(java.lang.String[]);
    }""",
    "-keep class * implements org.xml.sax.EntityResolver",
    """-keepclassmembers class * {
        ** MODULE$;
    }""",
    """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
        long eventCount;
        int  workerCounts;
        int  runControl;
        scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
        scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
    }""",
    """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
        int base;
        int sp;
        int runState;
    }""",
    """-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
        int status;
    }""",
    """-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
        scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
        scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
        scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
    }"""
    )

  // following lines about commons-logging are precious: were found the hard way...
  val jclKeep = List(
    "-dontwarn org.apache.commons.logging.impl.**",
    "-keep class org.apache.commons.logging.**",
    "-keep class org.apache.commons.logging.LogFactory",
    "-keep class org.apache.commons.logging.impl.LogFactoryImpl",
    """-keep class org.apache.commons.logging.impl.SimpleLog {
      public SimpleLog(java.lang.String);
    }"""
  )

  override def proguardOptions = scalaKeep :::
    jclKeep :::
    """-keepclassmembers class org.apache.http.client.utils.JdkIdn {
      java.lang.String toUnicode(java.lang.String);
    }""" ::
    """-keepclassmembers class com.thoughtworks.paranamer.DefaultParanamer { 
      java.lang.String __PARANAMER_DATA; 
    }""" ::
    proguardKeepMain(mainClassName) ::
    Nil
    

}

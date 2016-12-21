name := """mediaDB"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "commons-io" % "commons-io" % "2.4",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.4",
  "org.webjars" % "video-js" % "4.11.4",
  "org.webjars" % "d3js" % "3.5.3"  
)

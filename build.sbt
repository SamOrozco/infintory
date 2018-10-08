name := "inventory_cloud"

version := "1.0"

lazy val `inventory_cloud` = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalaVersion := "2.11.11"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.7"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212.jre7"
libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.9.5"
libraryDependencies ++= Seq(javaJdbc, cache, javaWs)


      
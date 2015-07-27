name := "samanthaplay"

version := "1.0"

lazy val `samanthaplay` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Scalaz Bintray Repository" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq( jdbc , cache , ws )

libraryDependencies += "com.ivona" % "ivona-speechcloud-sdk-java" % "0.3.0"

libraryDependencies += "com.typesafe.play" %% "anorm" % "2.4.0"

libraryDependencies += specs2 % Test

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
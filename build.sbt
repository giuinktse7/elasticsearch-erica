name := "untitled"

version := "0.1"

scalaVersion := "2.12.4"

// major.minor are in sync with the elasticsearch releases
val elastic4sVersion = "6.1.4"
libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,

  // for the http client
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,

  //for elasticsearch client
  "org.elasticsearch" % "elasticsearch" % "6.2.1"
)
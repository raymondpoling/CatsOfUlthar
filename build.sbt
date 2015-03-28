name := "CatsOfUlthar"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "org.fusesource" % "sigar" % "1.6.4"

parallelExecution in Test := false

resolvers += "Spray" at "http://repo.spray.io"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.6"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "io.spray" %%  "spray-can" % "1.3.1"

libraryDependencies += "io.spray" %%  "spray-httpx" % "1.3.1"

libraryDependencies += "io.spray" %%  "spray-http" % "1.3.1"

libraryDependencies += "io.spray" %%  "spray-routing" % "1.3.1"

libraryDependencies += "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M2"

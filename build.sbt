name := "siigna-main"

version := "0.1"

organization := "com.siigna"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.1", "2.9.2")

scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src" / "main")

mainClass in (Compile, run) := Some("com.siigna.app.SiignaApplication")

mainClass in (Compile, packageBin) := Some("com.siigna.app.SiignaApplication")

javaOptions += "-Xss1m -server"

publishTo := Some(Resolver.file("file",  new File( "../rls" )) )

resolvers += "Siigna" at "http://siigna.com/rls"

libraryDependencies ++= Seq(
  "com.siigna" %% "siigna-module" % "0.1"
)

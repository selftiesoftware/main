name := "siigna-main"

version := "preAlpha"

organization := "com.siigna"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.1", "2.9.2")

scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src" / "main")

mainClass in (Compile, run) := Some("com.siigna.app.SiignaApplication")

mainClass in (Compile, packageBin) := Some("com.siigna.app.SiignaApplication")

javaOptions += "-Xss1m -server"

publishTo := Some(Resolver.file("file",  new File( "../rls/base" )) )

resolvers += "Siigna" at "http://siigna.com/rls/base"

libraryDependencies ++= Seq(
  "com.siigna" %% "siigna-module" % "0.1"
)

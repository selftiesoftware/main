name := "siigna-main"

version := "preAlpha"

organization := "com.siigna"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2")

scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src" / "main")

scalaSource in Test <<= (baseDirectory in Compile)(_ / "src" / "test")

mainClass in (Compile, run) := Some("com.siigna.app.SiignaApplication")

mainClass in (Compile, packageBin) := Some("com.siigna.app.SiignaApplication")

publishTo := Some(Resolver.file("file",  new File( "../rls/base" )) )

resolvers += "Siigna" at "http://rls.siigna.com/base"

fork in run := true // Do this. All day everyday

libraryDependencies ++= Seq(
  "com.siigna" %% "siigna-module" % "preAlpha",
  "org.scala-lang" % "scala-library" % "2.9.2"
)

name := "siigna-main"

version := "nightly"

organization := "com.siigna"

scalaVersion := "2.10.0"

//crossScalaVersions := Seq("2.10.0", "2.9.2")

scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src" / "main")

scalaSource in Test <<= (baseDirectory in Compile)(_ / "src" / "test")

mainClass in (run) := Some("com.siigna.app.SiignaApplication")

mainClass in (Compile, packageBin) := Some("com.siigna.app.SiignaApplication")

//publishTo := Some(Resolver.file("file",  new File( "../rls" )) )
publishTo := Some(Resolver.sftp("Siigna rls", "rls.siigna.com", 22, "/srv/rls") as ("siigna", new File("../budapest/jenkins.rsa")))

resolvers += "Siigna" at "http://rls.siigna.com"

fork in run := true // Do this. All day everyday

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-actors" % "2.10.0",
  "org.scala-lang" % "scala-library" % "2.10.0",
  "org.scala-lang" % "scala-reflect" % "2.10.0",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

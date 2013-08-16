name := "siigna-main"

version := "stable"

organization := "com.siigna"

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.10.0", "2.9.2")

mainClass in (run) := Some("com.siigna.app.SiignaApplication")

mainClass in (Compile, packageBin) := Some("com.siigna.app.SiignaApplication")

//publishTo := Some(Resolver.file("file",  new File( "../rls" )) )
publishTo := Some(Resolver.sftp("Siigna rls", "80.71.132.98", 12022, "/var/www/public_html") as ("www-data", new File("../budapest/jenkins.rsa")))

resolvers += "Siigna" at "http://rls.siigna.com"

fork in run := true // Do this. All day everyday

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-actors" % "2.10.0",
  "org.scala-lang" % "scala-library" % "2.10.0",
  "org.scala-lang" % "scala-reflect" % "2.10.0",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.itextpdf" % "itextpdf" % "5.4.2" % "runtime",
  "com.itextpdf" % "itextpdf" % "5.4.2" % "compile"
)

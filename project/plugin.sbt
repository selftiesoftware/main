resolvers += Classpaths.typesafeResolver

//addSbtPlugin("com.typesafe.startscript" % "xsbt-start-script-plugin" % "0.5.2")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0-SNAPSHOT")

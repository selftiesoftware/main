import sbt._
import Keys._

object MyBuild extends Build { 
    val buildOrganization  = "com.siigna"
    val buildVersion       = "0.1.0-SNAPSHOT"
    val buildScalaVersion  = "2.10.3"

    val rootProjectId = "siigna"

    val lwjglVersion = "2.9.0"

    object Settings {
        lazy val base = Defaults.defaultSettings ++ Seq(
            organization    := buildOrganization,
            version         := buildVersion,
            scalaVersion    := buildScalaVersion
        )
        
        lazy val lwjgl = LWJGLPlugin.lwjglSettings ++ Seq(
            LWJGLPlugin.lwjgl.version := lwjglVersion
        )
        
        lazy val rootProject = base ++ lwjgl
    }

    lazy val broken = Project(
        id = rootProjectId,
        base = file("."),
        settings = Defaults.defaultSettings ++ Seq(
            scalaVersion := buildScalaVersion,
            target := new File("target/broken"),
            libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl-platform" % lwjglVersion classifier "natives-windows",
            libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl-platform" % lwjglVersion classifier "natives-linux",
            libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl-platform" % lwjglVersion classifier "natives-osx"
        )
    )

    lazy val root = Project(id=rootProjectId, base=file("."), settings=Settings.rootProject)
}

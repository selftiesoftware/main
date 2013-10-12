import sbt._
import Keys._

object MyBuild extends Build { 
    val buildOrganization  = "com.example"
    val buildVersion       = "0.1.0-SNAPSHOT"
    val buildScalaVersion  = "2.10.3"

    val rootProjectId = "myproject"

    val lwjglVersion = "2.8.2"

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
        id = "broken",
        base = file("."),
        settings = Defaults.defaultSettings ++ Seq(
            scalaVersion := "2.9.1",
            target := new File("target/broken"),
            libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl-platform" % lwjglVersion classifier "natives-windows",
            libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl-platform" % lwjglVersion classifier "natives-linux",
            libraryDependencies += "org.lwjgl.lwjgl" % "lwjgl-platform" % lwjglVersion classifier "natives-osx"
        )
    )

    lazy val root = Project(id=rootProjectId, base=file("."), settings=Settings.rootProject)
}

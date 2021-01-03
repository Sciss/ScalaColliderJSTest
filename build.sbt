lazy val root = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "ScalaCollider JS Test",
    scalaVersion := "2.13.4",
    // scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "de.sciss" %%% "scalacollider" % "2.5.0-SNAPSHOT",
    ),
  )

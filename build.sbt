lazy val root = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name                  := "ScalaCollider JS Test",
    scalaVersion          := "2.13.4",
    libraryDependencies  ++= Seq(
      "de.sciss" %%% "scalacollider" % "2.5.0",
    ),
    artifactPath in(Compile, fastOptJS) := baseDirectory.value / "lib" / "main.js",
  )

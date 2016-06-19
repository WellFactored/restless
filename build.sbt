
name := "restless"

organization in ThisBuild := "com.wellfactored"

scalaVersion in ThisBuild := "2.11.8"

lazy val restless = project.in(file("."))
  .dependsOn(core).aggregate(core)
  .dependsOn(`play-json`).aggregate(`play-json`)
  .dependsOn(`play-actions`).aggregate(`play-actions`)
  .enablePlugins(GitVersioning)
  .enablePlugins(GitBranchPrompt)

git.useGitDescribe in ThisBuild := true

lazy val core = project.in(file("core"))
lazy val `play-json` = project.in(file("play-json")).dependsOn(core)
lazy val `play-actions` = project.in(file("play-actions")).dependsOn(`play-json`)




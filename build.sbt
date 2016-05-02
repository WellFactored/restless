
name := "restless"

organization in ThisBuild := "com.wellfactored"

scalaVersion in ThisBuild := "2.11.8"

enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

git.useGitDescribe in ThisBuild := true

val core = project.in(file("core"))
val `play-json` = project.in(file("play-json")).dependsOn(core)

val restless = project.in(file("."))
  .dependsOn(core).aggregate(core)
  .dependsOn(`play-json`).aggregate(`play-json`)


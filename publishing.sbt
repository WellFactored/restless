publishMavenStyle in ThisBuild := true

useGpg := true

usePgpKeyHex("46C41F3C")

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository in ThisBuild := { _ => false }

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/wellfactored/restless"))

scmInfo := Some(ScmInfo(url("http://github.com/wellfactored/restless"), "scm:git@github.com:wellfactored/restless.git"))

pomExtra in ThisBuild := (
  <developers>
    <developer>
      <id>dclinton</id>
      <name>Doug Clinton</name>
    </developer>
  </developers>
  )
name := "akka-http-crypto"

organization := "com.github.dnvriend"

version := "1.0.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"

libraryDependencies ++= {
  val akkaVersion = "2.4.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.apache.shiro" % "shiro-all" % "1.2.4",
    "de.svenkubiak" % "jBCrypt" % "0.4.1",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.scalatest" %% "scalatest" % "2.2.4" % Test,
    "org.scalacheck" %% "scalacheck" % "1.12.5" % Test
  )
}

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbt.Keys._

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

// enable scala code formatting //

import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)

// enable sbt-revolver
import spray.revolver.RevolverPlugin.Revolver

Revolver.settings ++ Seq(
  Revolver.enableDebugging(port = 5050, suspend = false),
  mainClass in Revolver.reStart := Some("com.github.dnvriend.Main")
)

// configure code lint //
//wartremoverWarnings ++= Seq(Wart.Any, Wart.Serializable)
wartremoverWarnings ++= Warts.unsafe

// configure build info //
// build info configuration //
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "com.github.dnvriend"

// enable plugins //
enablePlugins(AutomateHeaderPlugin, BuildInfoPlugin)
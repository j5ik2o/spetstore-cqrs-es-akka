import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

val formatPreferences = FormattingPreferences()
  .setPreference(RewriteArrowSymbols, false)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(SpacesAroundMultiImports, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(AlignArguments, true)

val commonSettings = Seq(
  organization := "com.github.j5ik2o",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-encoding", "UTF-8", "-language:existentials", "-language:implicitConversions", "-language:postfixOps")
) ++ SbtScalariform.scalariformSettings ++ Seq(
  ScalariformKeys.preferences in Compile := formatPreferences,
  ScalariformKeys.preferences in Test := formatPreferences)

val infrastructure = (project in file("infrastructure"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.2",
      "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.2",
      "com.typesafe.akka" %% "akka-actor" % "2.4.2",
      "com.typesafe.akka" %% "akka-persistence" % "2.4.2",
      "com.typesafe.akka" %% "akka-stream" % "2.4.2",
      "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.2",
      "com.typesafe.akka" %% "akka-http-experimental" % "2.4.2",
      "com.typesafe.akka" %% "akka-http-testkit" % "2.4.2" % "test",
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.2",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.2",
      "io.spray" %% "spray-json" % "1.3.2",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.4.2" % "test",
      "com.github.nscala-time" %% "nscala-time" % "2.10.0",
      "org.skinny-framework" %% "skinny-orm"      % "2.0.7",
      "com.h2database"       %  "h2"              % "1.4.+",
      "de.knutwalker" %% "typed-actors" % "1.6.0-a24",
      "de.knutwalker" %% "typed-actors-creator" % "1.6.0-a24"
    )
  )

val domain = (project in file("domain"))
  .dependsOn(infrastructure)
  .settings(commonSettings: _*)

val usecase = (project in file("usecase"))
  .dependsOn(domain, infrastructure)
  .settings(commonSettings: _*).dependsOn(domain)

val interface = (project in file("interface"))
  .dependsOn(usecase, infrastructure)
  .settings(commonSettings: _*).dependsOn(usecase)

val application = (project in file("application"))
  .dependsOn(usecase, interface)
  .settings(commonSettings: _*).dependsOn(interface)

val root = (project in file("."))
  .aggregate(infrastructure, domain, usecase, interface, application)
  .settings(commonSettings: _*).settings(
    name := "spetstore-cqrs-es-akka"
  )

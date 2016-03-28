logLevel := Level.Warn

resolvers ++= Seq(
  Classpaths.typesafeReleases,
  Classpaths.typesafeSnapshots,
  "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Staging Repository" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
)

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")
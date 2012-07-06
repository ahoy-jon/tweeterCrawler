

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "2.2.5"

libraryDependencies += "org.w3" %% "banana-jena" % "0.3-SNAPSHOT"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

scalaSource in Test <<= baseDirectory(_ / "test")

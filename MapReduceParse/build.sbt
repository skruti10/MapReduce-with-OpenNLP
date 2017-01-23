name := "MapReduceParse"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.hadoop" % "hadoop-core" % "0.20.2"

libraryDependencies += "org.apache.opennlp" % "opennlp-tools" % "1.6.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "2.2.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.6.0"

//resolvers += "Hortonworks Releases" at "[http://repo.hortonworks.com/content/repositories/releases/](http://repo.hortonworks.com/content/repositories/releases/)"
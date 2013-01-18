/**
 * Author: robin
 * Date: 11/25/12
 * Time: 4:52 AM
 *
 */

import sbt._
import sbt.Keys._


trait PublishSettings {

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )

  lazy val withPublishing = seq(
    	publishMavenStyle := true,

      	publishTo <<= version { (v: String) =>
      	  val nexus = "http://scala.repo.ansvia.com/nexus/"
      	  if (v.trim.endsWith("SNAPSHOT")) 
      	    Some("snapshots" at nexus + "content/repositories/snapshots") 
      	  else
      		Some("releases" at nexus + "content/repositories/releases")
      	},

      	credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      	publishArtifact in Test := false,

      	pomIncludeRepository := { _ => false },
      	
      	crossPaths := false
  )
}
package models

import org.joda.time.Duration

/**
 * @author: mgibowski
 */
object TrackFactory {

  val defaultTrackLength = 3000

  def createTrack(lengthMillis: Long = defaultTrackLength) = Track(
        duration = new Duration(lengthMillis), soundCloudId = 666,
        title = "Oops I did it again", soundCloudUsername = "Britney",
        permalinkUrl = "spermalink-url", artworkUrl = "nowork-url")
}

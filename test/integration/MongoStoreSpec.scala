package integration

import org.specs2.mutable.Specification
import scala.concurrent.{duration, Await}
import play.api.test.WithApplication
import org.joda.time.DateTime
import play.api.mvc.Controller

import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import models.{MongoPlaylistStore, PlaylistItem, TrackFactory}

// Reactive Mongo plugin
import play.modules.reactivemongo._


/**
* @author: mgibowski
*/
class MongoStoreSpec extends Specification{

  val atMostDuration = duration.Duration(1, duration.SECONDS)
  val playlistId = "516ee1274fdc459e3c9b2c71"

  val track = TrackFactory.createTrack()

  "Mongo Store" should {
    "work as expected" in new WithApplication {
      // test persisting new item
      val playlistItem = PlaylistItem(playlistId, DateTime.now(), track)
      val futureOk = MongoPlaylistStore.addItem(playlistItem)
      val ok = Await.result(futureOk, atMostDuration)
      ok must beTrue

      // test fetching items
      val future = MongoPlaylistStore.findItems(playlistId)
      val results = Await.result(future, atMostDuration)
      results must not beEmpty

      // clean up
      Cleaner.cleanUp(playlistId)
    }
  }
}

object Cleaner extends Controller with MongoController{
  def cleanUp(playlistId: String) =
    MongoPlaylistStore.playListItems.remove[BSONDocument](BSONDocument("playlistId" -> BSONObjectID(playlistId)))
}

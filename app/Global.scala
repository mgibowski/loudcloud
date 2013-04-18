
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
/**
 * @author: mgibowski
 */
object Global extends GlobalSettings {

  override def onHandlerNotFound(request: RequestHeader) = NotFound(views.html.notFound())

}

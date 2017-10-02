package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scalikejdbc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def init(team: String) = Action { implicit request: Request[AnyContent] =>
    import models.Database._

    sql"TRUNCATE results, ratings RESTART IDENTITY;".execute().apply()

    val fullTeam = if (team == "ds") "Data Science" else "Data Engineering"
    val ratings = for (dim <- MatchController.dsDims; name <- MatchController.dsPeople) yield Seq(fullTeam, dim, name, 100)
    sql"insert into ratings (team, dimension, name, rating) values (?, ?, ?, ?)".batch(ratings : _*).apply()

    Ok
  }
}

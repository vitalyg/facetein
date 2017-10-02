package controllers

import javax.inject._

import models.Rating
import play.api.libs.json._
import play.api.mvc._
import scalikejdbc._

case class Score(name: String, score: Int)

object WinnerController {
  import models.Database._

  def getTop10(team: String, dim: String): Seq[Score] = {
    val r = Rating.syntax("rating")
    val topRatings = withSQL {
      select.from(Rating as r).where.eq(r.dimension, dim).and.eq(r.team, team).orderBy(r.rating).desc.limit(10)
    }.map(res => Score(res.string(4), res.int(5))).list().apply()
    topRatings
  }

  def getRating(team: String, dimension: String, name: String): Option[Rating] = {
    val r = Rating.syntax("rating")
    withSQL {
      select.from(Rating as r).where.eq(r.name, name).and.eq(r.dimension, dimension).and.eq(r.team, team)
    }.map(Rating(_)).single().apply()
  }

  implicit val scoreWrites: Writes[Score] = new Writes[Score] {
    override def writes(score: Score): JsObject = Json.obj(
      "name" -> score.name,
      "score" -> score.score
    )
  }
}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class WinnerController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  import play.api.libs.json.Json
  import WinnerController._
  import models.Database._

  def saveWinner() = Action { implicit request: Request[AnyContent] =>
    def writeWinner(team: String, dimension: String, winner: String, loser: String) = {
      sql"insert into results (team, dimension, winner_name, loser_name) values ($team, $dimension, $winner, $loser)".update.apply()
    }
    def updateRating(team: String, dimension: String, name: String, newRating: Int) = {

      getRating(team, dimension, name) match {
        case None => sql"insert into ratings (team, dimension, name, rating) values ($team, $dimension, $name, $newRating)".update().apply()
        case Some(old) => sql"update ratings set rating = $newRating where id = ${old.id}".update().apply()
      }
    }

    val json = request.body.asJson.get.as[JsObject]
    val dim = json("dim").as[JsString].value
    val left = json("left").as[JsString].value
    val leftRating = json("leftRating").as[JsNumber].value.toInt
    val right = json("right").as[JsString].value
    val rightRating = json("rightRating").as[JsNumber].value.toInt
    val winner = json("winner").as[JsString].value
    val (isLeftWinner, loser, winnerRating, loserRating) =
      if (left == winner)
        (true, right, leftRating, rightRating)
      else
        (false, left, rightRating, leftRating)

    val ratingDiff = math.abs(leftRating - rightRating)
    val ratingModifier = (ratingDiff / 20 + 10) min 20
    val isWinnerHigher = (isLeftWinner && leftRating > rightRating) || (!isLeftWinner && rightRating > leftRating)
    val finalModifier = if (isWinnerHigher) 20 - ratingModifier else ratingModifier

    writeWinner("Data Science", dim, winner, loser)
    updateRating("Data Science", dim, winner, winnerRating + finalModifier)
    updateRating("Data Science", dim, loser, loserRating - finalModifier)

    Ok(Json.toJson("{}"))
  }

  def getHighScores(team: String, dim: String) = Action { implicit request: Request[AnyContent] =>
    import WinnerController.scoreWrites

    val teamFullName = if (team == "ds") "Data Science" else "Data Engineering"

    Ok(Json.toJson(WinnerController.getTop10(teamFullName, dim)))
  }
}

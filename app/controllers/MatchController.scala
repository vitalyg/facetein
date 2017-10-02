package controllers

import java.io.File
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._

object MatchController {
  val dsPeople: Array[String] = new File("public/images/data-science/")
    .listFiles()
    .filter(_.getName.endsWith("jpg"))
    .map(_.getName.split('.').head)

  val dsDims: List[String] = List("machine learning", "coding", "data acumen", "business acumen", "getting things done", "communicating")
}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class MatchController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  import MatchController._

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  val sequenceSize = 10
  private val atomicCounter = new AtomicInteger()
  def getDim: String = {
    val num = atomicCounter.getAndIncrement()
    dsDims((num / sequenceSize) % dsDims.length)
  }
  def index() = Action { implicit request: Request[AnyContent] =>
    val rand = new Random()
    val dim = getDim
    val leftPerson = dsPeople(rand.nextInt(dsPeople.length))
    val leftRating = WinnerController.getRating("Data Science", dim, leftPerson).map(_.rating).getOrElse(100)
    var rightPerson = dsPeople(rand.nextInt(dsPeople.length))
    val rightRating = WinnerController.getRating("Data Science", dim, rightPerson).map(_.rating).getOrElse(100)
    while (leftPerson == rightPerson)
      rightPerson = dsPeople(rand.nextInt(dsPeople.length))

    val result = Json.toJson(Map(
      "dim" -> dim,
      "left" -> leftPerson,
      "leftRating" -> leftRating.toString,
      "right" -> rightPerson,
      "rightRating" -> rightRating.toString))
    Ok(result)
  }
}

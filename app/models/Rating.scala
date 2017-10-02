package models
import scalikejdbc._
import scalikejdbc.config.DBs

object Database {
  Class.forName("org.postgresql.Driver")
  DBs.setupAll
  implicit val session = AutoSession
}

case class Rating(id: Long, team: String, dimension: String, name: String, rating: Int)

object Rating extends SQLSyntaxSupport[Rating] {
  override val tableName = "ratings"
  def apply(rs: WrappedResultSet) = {
    new Rating(
      rs.long(1), rs.string(2), rs.string(3), rs.string(4), rs.int(5))
  }
}

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import dispatch.{:/, Http}
import net.liftweb.json.{JsonAST, Formats}
import util.parsing.json.JSON

case class TerrastoreClient (hostName: String, port: Int) {
  val http = new Http
  val req = :/(hostName, port)

  implicit val formats = net.liftweb.json.DefaultFormats

  def getBucketNames : List[String] = {
    val res = http(req as_str)
    JSON.parse(res) match {
      case Some(list) => list map {_.toString} 
      case None => List.empty
    }
  }

  def removeBucket (bucketName:String) = {
    http(req.DELETE / bucketName >|)
  }

  def putValue (bucket:String, key: String, content:String) = {
    http(req / bucket / key <<< content <:< Map("Content-Type" -> "application/json") >|)
  }

  def removeValue (bucket:String, key:String) = {
    http(req.DELETE / bucket / key >|)
  }

  def getValue[T](bucket: String, key: String)(implicit formats: Formats, mf: scala.reflect.Manifest[T]) : T = {
    val res = http(req / bucket / key as_str)
    parse(res).extract[T]
  }

  def getAllValues[T](bucket:String, limit:Int)(implicit formats: Formats, mf: scala.reflect.Manifest[T]) : Map[String, T] = {
    val res = http(req / bucket <<? Map("limit" -> limit) as_str)
    Map() ++ parse(res).children.map {
      case f: JField => (f.name, f.extract[T])
    }
  }
}
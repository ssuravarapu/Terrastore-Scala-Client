import dispatch.json.{JsValue, JsArray, Js, JsonParser}
import net.liftweb.json.Formats
import net.liftweb.json.JsonParser._
import dispatch.{Request, :/, Http}
import util.parsing.input.CharSequenceReader
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

  def getBuckets : List[Any] = {
    val allBuckets = for {
      bucket <- getBucketNames
    } yield getAllValues(bucket)
    allBuckets
  }

  def removeBucket (bucketName:String) = {
    http(req.DELETE / bucketName >|)
  }

  def putValue (bucket:String, key: String, content:String) = {
    http(req / bucket / key <<< content <:< Map("Content-Type"-> "application/json") >|)
  }

  def removeValue (bucket:String, key:String) = {
    http(req.DELETE / bucket / key >|)
  }

  def getValue[T](bucket: String, key: String)(implicit formats: Formats, mf: scala.reflect.Manifest[T]) : T = {
    val res = http(req / bucket / key as_str)
    println("res: " + res)
    parse(res).extract[T]
  }

  def getAllValues (bucket:String):List[Any] = {
    val res = http(req / bucket as_str)
    JSON.parse(res) match {
      case Some(s) => s
      case None => List.empty
    }
  }
}
import dispatch.json.{JsValue, JsArray, Js, JsonParser}
import net.liftweb.json.Formats
import net.liftweb.json.JsonParser._
import dispatch.{Request, :/, Http}
import util.parsing.input.CharSequenceReader
import util.parsing.json.JSON

class TerrastoreClient (hostName: String, port: Int) {

  def getBucketNames : List[String] = {
    val http = new Http
    val req = :/(hostName, port)
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
    val http = new Http
    val req = :/(hostName, port)
    http(req.DELETE / bucketName >|)
  }

  def putValue (bucket:String, key: String, content:String) = {
      val http = new Http
      val req = :/(hostName, port) / bucket / key <<< content <:< Map("Content-Type"-> "application/json")
      val res = http (req >|)
  }

  def removeValue (bucket:String, key:String) = {
    val http = new Http
    val req = :/(hostName, port)
    http(req.DELETE / bucket / key >|)  
  }

  def getValue[T](bucket: String, key: String)(implicit formats: Formats, mf: scala.reflect.Manifest[T]) : T = {
    val http = new Http
    val req = :/(hostName, port) / bucket / key
    val res = http(req as_str)
    println("res: " + res)
    implicit val formats = net.liftweb.json.DefaultFormats
    parse(res).extract[T]
  }

  def getValue2 (bucket: String, key: String) : Any = {
    val http = new Http
    val req = :/(hostName, port) / bucket / key
    val res = http(req as_str)
    JsonParser (new CharSequenceReader(res))
  }

  def getAllValues (bucket:String):List[Any] = {
    val http = new Http
    val req = :/(hostName, port) / bucket
    val res = http(req as_str)
    JSON.parse(res) match {
      case Some(s) => s
      case None => List.empty
    }
  }
}
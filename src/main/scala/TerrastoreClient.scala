import dispatch.json.{JsValue, JsArray, Js, JsonParser}
import dispatch.{Request, :/, Http}
import util.parsing.input.CharSequenceReader
import util.parsing.json.JSON

/**
 * Created by IntelliJ IDEA.
 * User: surya.suravarapu
 * Date: May 5, 2010
 * Time: 11:48:52 AM
 * To change this template use File | Settings | File Templates.
 */

class TerrastoreClient (hostName: String, port: Int) {
  def getBucketNames : List[Any] = {
    val http = new Http
    val req = :/(hostName, port)
    val res = http(req as_str)
    JSON.parse(res) match {
      case Some(s) => s
      case None => List.empty
    }
  }

  def removeBucket (bucketName:String) = {
    val http = new Http
    val req = :/(hostName, port)
    http(req.DELETE / bucketName as_str)
  }

  def putDocument (bucket:String, key: String, content:String) = {
    try {
      val http = new Http
      val req = :/(hostName, port) / bucket / key <<< content <:< Map("Content-Type"-> "application/json")
      val res = http (req as_str)
      println (res)
    } catch {
       case e if (e.getMessage.startsWith("response has no entity")) => println ("Ignore this error")
    }
  }

  def getValue (bucket: String, key: String) : Any = {
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
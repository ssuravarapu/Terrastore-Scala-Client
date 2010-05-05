import dispatch.json.JsonParser
import dispatch.{:/, Http}
import util.parsing.input.CharSequenceReader

/**
 * Created by IntelliJ IDEA.
 * User: surya.suravarapu
 * Date: May 5, 2010
 * Time: 11:48:52 AM
 * To change this template use File | Settings | File Templates.
 */

class TerrastoreClient (hostName: String, port: Int) {
  def getBuckets : Any = {
    val http = new Http
    val req = :/(hostName, port)
    val res = http(req as_str)
    JsonParser (new CharSequenceReader(res))
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
}
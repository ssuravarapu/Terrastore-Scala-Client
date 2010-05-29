package terrastore.client

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.{Formats}
import util.parsing.json.JSON
import dispatch._

case class TerrastoreClient (hostName: String, port: Int) {
  val http = new Http
  val req = :/(hostName, port)

  implicit val formats = net.liftweb.json.DefaultFormats

  def getBucketNames : List[String] = {
    val res = httpExecute[String](req as_str)
    JSON.parse(res) match {
      case Some(list) => list map {_.toString}
      case None => List.empty
    }
  }

  def removeBucket (bucketName:String) = {
    httpExecute[Unit](req.DELETE / bucketName >|)
  }

  def putDocument (bucket:String, key: String, content:String) = {
    httpExecute[Unit](req / bucket / key <<< content <:< Map("Content-Type" -> "application/json") >|)
  }

  def removeDocument (bucket:String, key:String) = {
    httpExecute[Unit](req.DELETE / bucket / key >|)
  }

  def getDocument[T](bucket: String, key: String)(implicit formats: Formats, mf: scala.reflect.Manifest[T]) : T = {
    val res = httpExecute[String](req / bucket / key as_str)
    parse(res).extract[T]
  }

  def getAllDocuments[T](bucket:String, limit:Int)(implicit formats: Formats, mf: scala.reflect.Manifest[T]) : Map[String, T] = {
    val res = httpExecute[String](req / bucket <<? Map("limit" -> limit) as_str)
    parseJsonToMap[T](res)(formats, mf)
  }

  def doRangeQuery[T](bucket:String, startKey:String, endKey:String, limit:Int, comparator:String, predicate:String,
          timeToLive:Long)(implicit formats: Formats, mf: scala.reflect.Manifest[T]):Map[String, T] = {
    val params = Map ("startKey" -> startKey, "endKey" -> endKey, "limit" -> limit)
    val res = httpExecute[String](req / bucket / "range" <<? params as_str)
    parseJsonToMap[T](res)(formats, mf)
  }

  def doPredicateQuery[T](bucket:String, predicate:String)
    (implicit formats: Formats, mf: scala.reflect.Manifest[T]):Map[String, T] = {
    val res = httpExecute[String](req / bucket / "predicate" <<? Map("predicate" -> predicate) as_str)
    parseJsonToMap[T](res)(formats, mf)
  }

  def exportBackup(bucket:String, destination:String, secret:String) = {
    httpExecute[Unit](req.POST / bucket / "export" <<? Map("destination" -> destination, "secret" -> secret) <:<
            Map("Content-Type" -> "application/json") >|)
  }

  def importBackup(bucket:String, source:String, secret:String) = {
    httpExecute[Unit](req.POST / bucket / "import" <<? Map("source" -> source, "secret" -> secret) <:<
            Map("Content-Type" -> "application/json") >|)
  }

  private def httpExecute[T](handler:Handler[T]):http.HttpPackage[T] = {
    try {
      http(handler)
    } catch {
      case e: StatusCode => {
        val error = parse(e.contents).extract[Error]
        throw new TerrastoreException(error)
      }
    }
  }

  private def parseJsonToMap[T](json:String)
    (implicit formats: Formats, mf: scala.reflect.Manifest[T]):Map[String, T] = {
   Map() ++ parse(json).children.map {
      case f: JField => (f.name, f.extract[T])
      case _ => throw new RuntimeException("Wrong field")
    }
  }
}

case class Error(message:String, code:Int)
case class TerrastoreException(error:Error)
        extends Exception("Error in response with code: " + error.code + " and message: " + error.message)

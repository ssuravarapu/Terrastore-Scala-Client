import dispatch.{:/, Request, Http}

/**
 * @author Surya Suravarapu
 */

object Test {
  def main(args: Array[String]) {
    val http = new Http
    val req = :/("suryasuravarapu.com")
    val response = http(req as_str)
    println(response.toString)
  }
}
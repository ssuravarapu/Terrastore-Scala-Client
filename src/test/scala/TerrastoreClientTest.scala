
/**
 * Created by IntelliJ IDEA.
 * User: surya.suravarapu
 * Date: May 5, 2010
 * Time: 12:04:32 PM
 * To change this template use File | Settings | File Templates.
 */

object TerrastoreClientTest {
  def main(args: Array[String]) {
    val client = new TerrastoreClient("localhost", 8010)
    println(client.getBucketNames)
    client.removeBucket("bucket")
    println(client.getBucketNames)
//    client.putDocument("bucket", "abc", "{\"bucket-test1\":\"test1\"}")
//    println(client.getValue("bucket", ""))
//    println(client.getAllValues("bucket"))
  }
}
import org.specs._
import runner.ScalaTest

/**
 * Created by IntelliJ IDEA.
 * User: surya.suravarapu
 * Date: May 12, 2010
 * Time: 3:24:03 PM
 * To change this template use File | Settings | File Templates.
 */

class TerrastoreClientSpecTest extends SpecificationWithJUnit {

  "Put a new document" should {
    "add the document specified in the bucket specified" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      client.putDocument("SPS2", "some-key", "{\"color\":\"red\", \"value\":\"#f00\"}")
    }
  }

  "Get the bucket list" should {
    "have SPS1 in it" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      val bucketNames = client.getBucketNames
      bucketNames must contain("SPS2")
    }
  }
}
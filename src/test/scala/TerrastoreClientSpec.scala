import org.specs._

/**
 * Created by IntelliJ IDEA.
 * User: surya.suravarapu
 * Date: May 12, 2010
 * Time: 3:24:03 PM
 * To change this template use File | Settings | File Templates.
 */

object TerrastoreClientSpec extends Specification {

  "Put a new document" should {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      client.putDocument("SPS1", "key1", "{\"value1\"")
    }
}
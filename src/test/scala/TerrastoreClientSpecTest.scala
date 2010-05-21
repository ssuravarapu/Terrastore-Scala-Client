import dispatch.StatusCode
import java.lang.String
import net.liftweb.json.JsonParser._
import org.specs._
import reflect.BeanInfo

class TerrastoreClientSpecTest extends SpecificationWithJUnit {
  val bucketName = "XYZ1234"
  val key = "person1"
  val value = "{\"color\" : \"red\", \"value\" : \"#f00\"}"
  val jsonStr = "{\"name\": \"Surya Suravarapu\", \"address\": {\"street\": \"622 Sunderland\",\"city\": \"Chester Springs\"}}"

  "Put a new document" should {
    "add the document specified in the bucket specified" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      client.putValue(bucketName, key, jsonStr)
      client.getBucketNames must contain(bucketName)
    }
  }

  "Get a document" should {
    "have the value specified" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      implicit val formats = net.liftweb.json.DefaultFormats
      val person = client.getValue[Person](bucketName, key)
      person.name must equalIgnoreSpace("Surya Suravarapu")
      person.address.street must equalIgnoreSpace("622 Sunderland")
      person.address.city must equalIgnoreSpace("Chester Springs")
    }
  }

  "Get the bucket list" should {
    "have SPS1 in it" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      val bucketNames = client.getBucketNames
      bucketNames must contain(bucketName)
    }
  }

  "Delete a value" should {
    "remove the value specified" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      client.removeValue(bucketName, key)
      implicit val formats = net.liftweb.json.DefaultFormats
      client.getValue[Person](bucketName, key) must throwA[StatusCode]
    }
  }

  "Delete a bucket" should {
    "remove the bucket specified" in {
      val client:TerrastoreClient = new TerrastoreClient("localhost", 8010)
      client.removeBucket(bucketName)
      client.getBucketNames must not contain bucketName
    }
  }
}

case class Child(name: String, age: Int, birthdate: Option[java.util.Date])
case class Address(street: String, city: String)
case class Person(name: String, address: Address, children: List[Child])


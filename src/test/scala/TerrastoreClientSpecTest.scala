import dispatch.StatusCode
import java.lang.String
import org.specs._

class TerrastoreClientSpecTest extends SpecificationWithJUnit {
  implicit val formats = net.liftweb.json.DefaultFormats
  val client = TerrastoreClient("localhost", 8010)
  val bucketName = "XYZ1234"
  val key = "person1"
  val jsonStr = "{\"name\": \"Surya Suravarapu\", \"address\": {\"street\": \"622 Sunderland\",\"city\": \"Chester Springs\"}}"

  "Put a new document" should {
    "add the document specified in the bucket specified" in {
      client.putValue(bucketName, key, jsonStr)
      client.getBucketNames must contain(bucketName)
    }
  }

  "Get a document" should {
    "have the value specified" in {
      val person = client.getValue[Person](bucketName, key)
      person.name must equalIgnoreSpace("Surya Suravarapu")
      person.address.street must equalIgnoreSpace("622 Sunderland")
      person.address.city must equalIgnoreSpace("Chester Springs")
    }
  }

  "Get the bucket list" should {
    "have SPS1 in it" in {
      val bucketNames = client.getBucketNames
      bucketNames must contain(bucketName)
    }
  }

  "Delete a value" should {
    "remove the value specified" in {
      client.removeValue(bucketName, key)
      client.getValue[Person](bucketName, key) must throwA[StatusCode]
    }
  }

  "Delete a bucket" should {
    "remove the bucket specified" in {
      client.removeBucket(bucketName)
      client.getBucketNames must not contain bucketName
    }
  }
}

case class Child(name: String, age: Int, birthdate: Option[java.util.Date])
case class Address(street: String, city: String)
case class Person(name: String, address: Address, children: List[Child])


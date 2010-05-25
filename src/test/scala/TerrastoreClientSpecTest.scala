import dispatch.StatusCode
import java.lang.String
import org.specs._

class TerrastoreClientSpecTest extends SpecificationWithJUnit {
  implicit val formats = net.liftweb.json.DefaultFormats
  val client = TerrastoreClient("localhost", 8010)
  val bucketName = "XYZ1234"
  val key1 = "person1"
  val jsonStr1 = """{"name": "Surya Suravarapu", "address": {"street": "622 Sunderland","city": "Chester Springs"}}"""
  val key2 = "person2"
  val jsonStr2 = """{"name": "Joe Schmo", "address": {"street": "Some Street","city": "Some City"}}"""

  "Delete a bucket" should {
    "remove the bucket specified" in {
      client.removeBucket(bucketName)
      client.getBucketNames must not contain bucketName
    }
  }

  "Put a new value" should {
    "add/update the value specified in the bucket and key specified" in {
      client.putDocument(bucketName, key1, jsonStr1)
      client.getBucketNames must contain(bucketName)
      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Surya Suravarapu",Address("622 Sunderland","Chester Springs")))
    }
    "add another value" in {
      client.putDocument(bucketName, key2, jsonStr2)
      client.getDocument[Person](bucketName, key2) must beEqualTo(Person("Joe Schmo",Address("Some Street","Some City")))
    }
  }

  "Get a value" should {
    "have the value specified" in {
      val person = client.getDocument[Person](bucketName, key1)
      person.name must equalIgnoreSpace("Surya Suravarapu")
      person.address.street must equalIgnoreSpace("622 Sunderland")
      person.address.city must equalIgnoreSpace("Chester Springs")
    }
  }

  "Get all values" should {
    "returns all values when no limit" in {
      val personMap = client.getAllDocuments[Person](bucketName, 0)
      personMap must have size(2)
    }
    "returns only one value with limit one" in {
      val personMap = client.getAllDocuments[Person](bucketName, 1)
      personMap must have size(1)
    }
  }

  "Get the bucket list" should {
    "have SPS1 in it" in {
      val bucketNames = client.getBucketNames
      bucketNames must contain(bucketName)
    }
  }

  "Export and import backup" should {
    "export all the documents in a bucket to a specified destination" in {
      val file = bucketName + ".bak"
      client.exportBackup(bucketName, file, "SECRET-KEY")
      
    }
    "restore/import a backup of documents for a give bucket" in {
      client.removeDocument(bucketName, key1)
      client.getDocument[Person](bucketName, key1) must throwA[StatusCode]
      client.importBackup(bucketName, bucketName + ".bak", "SECRET-KEY")
      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Surya Suravarapu",Address("622 Sunderland","Chester Springs")))
    }
  }

  "Delete a value" should {
    "remove the value specified" in {
      client.removeDocument(bucketName, key1)
      client.getDocument[Person](bucketName, key1) must throwA[StatusCode]
    }
  }

//  "Import backup" should {
//    "restore a backup of documents for a give bucket" in {
//      client.getDocument[Person](bucketName, key1) must throwA[StatusCode]
//      "C:\\terrastore\\0.5.0\\server1\\backups\\" + bucketName + ".bak" must beAFilePath
//      client.importBackup(bucketName, bucketName + ".bak", "SECRET-KEY")
//      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Surya Suravarapu",Address("622 Sunderland","Chester Springs")))
//    }
//  }
}

case class Address(street: String, city: String)
case class Person(name: String, address: Address)

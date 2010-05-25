package terrastore.client

import dispatch.StatusCode
import org.specs._

class TerrastoreClientSpecTest extends SpecificationWithJUnit {
  implicit val formats = net.liftweb.json.DefaultFormats
  val client = TerrastoreClient("localhost", 8010)
  val bucketName = "BucketOne"
  val key1 = "person1"
  val jsonStr1 = """{"name": "Name One", "address": {"street": "Street One","city": "City One"}}"""
  val key2 = "person2"
  val jsonStr2 = """{"name": "Name Two", "address": {"street": "Street Two","city": "City Two"}}"""
  val key3 = "person3"
  val jsonStr3 = """{"name": "Name Three", "address": {"street": "Street Three","city": "City Three"}}"""

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
      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Name One",Address("Street One","City One")))
    }
    "add another value" in {
      client.putDocument(bucketName, key2, jsonStr2)
      client.getDocument[Person](bucketName, key2) must beEqualTo(Person("Name Two",Address("Street Two","City Two")))
    }
  }

  "Get a value" should {
    "have the value specified" in {
      val person = client.getDocument[Person](bucketName, key1)
      person.name must equalIgnoreSpace("Name One")
      person.address.street must equalIgnoreSpace("Street One")
      person.address.city must equalIgnoreSpace("City One")
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
    "have an already created bucket in it" in {
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
      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Name One",Address("Street One","City One")))
    }
  }

  "Do a range query" should {
    "get all the documents with the given range of keys" in {
      client.putDocument(bucketName, key1, jsonStr1)
      client.putDocument(bucketName, key2, jsonStr2)
      client.putDocument(bucketName, key3, jsonStr3)
      client.getAllDocuments[Person](bucketName, 0) must have size(3)
      val personMap = client.doRangeQuery[Person](bucketName, key2, key3, 0, "", "", 0)
      personMap must have size(2)
    }
  }

  "Delete a value" should {
    "remove the value specified" in {
      client.removeDocument(bucketName, key1)
      client.getDocument[Person](bucketName, key1) must throwA[StatusCode]
    }
  }
}

case class Address(street: String, city: String)
case class Person(name: String, address: Address)

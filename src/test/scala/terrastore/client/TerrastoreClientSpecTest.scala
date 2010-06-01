package terrastore.client

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

  "Add a new document" should {
    "add the document specified in the bucket and key specified" in {
      client.putDocument(bucketName, key1, jsonStr1)
      client.getBucketNames must contain(bucketName)
      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Name One",Address("Street One","City One")))
    }
    "add another document" in {
      client.putDocument(bucketName, key2, jsonStr2)
      client.getDocument[Person](bucketName, key2) must beEqualTo(Person("Name Two",Address("Street Two","City Two")))
    }
  }

  "Get a document" should {
    "retrieve the document for a given bucket and key" in {
      val person = client.getDocument[Person](bucketName, key1)
      person.name must equalIgnoreSpace("Name One")
      person.address.street must equalIgnoreSpace("Street One")
      person.address.city must equalIgnoreSpace("City One")
    }
  }

  "Get all documents" should {
    "returns all the documents when no limit" in {
      val personMap = client.getAllDocuments[Person](bucketName, 0)
      personMap must have size(2)
    }
    "returns only one document with limit one" in {
      val personMap = client.getAllDocuments[Person](bucketName, 1)
      personMap must have size(1)
    }
  }

  "Get the bucket list" should {
    "returns the names of all the buckets" in {
      val bucketNames = client.getBucketNames
      bucketNames must contain(bucketName)
    }
  }

  "Server side update function" should {
    "use the function provided to perform update" in {
      client.putDocument(bucketName, key2, jsonStr2)
      client.getDocument[Person](bucketName, key2) must beEqualTo(Person("Name Two",Address("Street Two","City Two")))
      client.update(bucketName, key2, "replace", 2000, jsonStr3)
      client.getDocument[Person](bucketName, key2) must beEqualTo(Person("Name Three",Address("Street Three","City Three")))
    }
  }

  "Export and import backup" should {
    "export the bucket specified and then restore" in {
      val file = bucketName + ".bak"
      client.exportBackup(bucketName, file, "SECRET-KEY")
      client.removeDocument(bucketName, key1)
      client.getDocument[Person](bucketName, key1) must throwA[TerrastoreException]
      client.importBackup(bucketName, bucketName + ".bak", "SECRET-KEY")
      client.getDocument[Person](bucketName, key1) must beEqualTo(Person("Name One",Address("Street One","City One")))
    }
  }

  "A range query" should {
    "get all the documents with the given range of keys" in {
      client.putDocument(bucketName, key1, jsonStr1)
      client.putDocument(bucketName, key2, jsonStr2)
      client.putDocument(bucketName, key3, jsonStr3)
      client.getAllDocuments[Person](bucketName, 0) must have size(3)
      val personMap = client.doRangeQuery[Person](bucketName, RangeQueryParam(key2, key3, 0, "lexical-asc", 0))
      personMap must have size(2)
      personMap.keysIterator.next must equalIgnoreCase(key2)
    }
  }

  "A predicate query" should {
    "get only those documents that match the predicate" in {
      val personMap = client.doPredicateQuery[Person](bucketName, "jxpath:/name[.='Name Two']")
      personMap("person2") must beEqualTo (Person("Name Two",Address("Street Two","City Two")))
    }
  }

  "Delete a document" should {
    "remove the document specified for a give bucket and key" in {
      client.removeDocument(bucketName, key1)
      client.getDocument[Person](bucketName, key1) must throwA[TerrastoreException]
    }
  }
}

case class Address(street: String, city: String)
case class Person(name: String, address: Address)

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.RefreshPolicy

object Main extends App {

  def test(args: (String, String)*) = {
    args foreach { x => println(x._1) }
  }

  val k = List("hej" -> "a", "hej2" -> "b")
  test(k: _*)

  def serializeEvent(event: EricaEvent) = (
    "careContactId" -> event.CareContactId,
    "category" -> event.Category,
    "start" -> event.Start,
    "end" -> event.End,
    "title" -> event.Title,
    "type" -> event.Type,
    "value" -> event.Value,
    "visitId" -> event.VisitId,
    "timeEvent" -> event.TimeEvent
  )

  // you must import the DSL to use the syntax helpers
  import com.sksamuel.elastic4s.http.ElasticDsl._

  val client = HttpClient(ElasticsearchClientUri("198.211.120.44", 8080))

  client.execute {
    createIndex("events").mappings(
      mapping("erica-event").fields(
        textField("reasonForVisit") analyzer NotAnalyzed,
        intField("patientId"),
        dateField("start"),
        dateField("end"),
        textField("title"),
        textField("type"),
        textField("value"),
        intField("visitId"),
        dateField("timeEvent")
      )
    )
  }.await
  

  def sendToElasticSearch(event: List[(String, Any)]) = client.execute {
    (indexInto("events" / "erica-event") fields event).refresh(RefreshPolicy.IMMEDIATE)
  }.await

  def newEvent(reason: String, id: Int) = List("reasonForVisit" -> reason, "patientId" -> id)

  sendToElasticSearch(newEvent("None", 2))
  sendToElasticSearch(newEvent("None", 8))
  sendToElasticSearch(newEvent("Party", 10))


  val resp = client.execute {
    search("events") query "None OR Party"
  }.await

  resp match {
    case Left(failure) => println("haha oh no")
    case Right(results) => println(results.result.hits.total)
  }

  client.close()

}
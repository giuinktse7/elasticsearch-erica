import java.util.Date

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.RefreshPolicy

object Main extends App {
  val formatString = "yyyy-MM-dd'T'HH:mm:ss.SSS"
  val format = new java.text.SimpleDateFormat(formatString)
  val now = format.format(new Date())

  def serializeEvent(event: EricaEvent) = List(
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

  /*client.execute {
    deleteIndex("events")
  }.await
*/
/*  client.execute {
    createIndex("events").mappings(
      mapping("erica-event").fields(
        textField("reasonForVisit") analyzer NotAnalyzed,
        intField("patientId"),
        dateField("start") format formatString,
        dateField("end") format formatString,
        textField("title") analyzer NotAnalyzed,
        textField("type") analyzer NotAnalyzed,
        textField("value") analyzer NotAnalyzed,
        intField("visitId"),
        dateField("timeEvent") format formatString
      )
    )
  }.await
  */

  def sendToElasticSearch(event: List[(String, Any)], eventId: Int) = client.execute {
    (indexInto("events" / "erica-event") fields event).id(eventId.toString).refresh(RefreshPolicy.IMMEDIATE)
  }.await

  val ericaEvents = List(
    EricaEvent(1, "Some category", now, now, "Hello im an event", "MyType", "Value", 1, now),
    EricaEvent(2, "Some category", now, now, "Hello im an event", "MyType", "Value", 2, now)
  )

  println(now)

  ericaEvents foreach { event => sendToElasticSearch(serializeEvent(event), event.CareContactId) }


  val resp = client.execute {
    search("events") query "Hello"
  }.await

  resp match {
    case Left(failure) => println("haha oh no")
    case Right(results) => println(results.result.totalHits)
  }

  client.close()

}
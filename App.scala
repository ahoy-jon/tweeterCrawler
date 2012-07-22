import com.hp.hpl.jena.tdb.TDBFactory
import java.io.File
import java.util
import org.w3.banana.jena.{Jena, JenaStore}
import twitter4j.conf._
import twitter4j._
import scala.collection.JavaConversions._

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory
import com.hp.hpl.jena.tdb.TDBFactory
import org.w3.banana.Diesel
import org.w3.banana.GraphStore
import org.w3.banana.jena.{JenaRDFBlockingWriter, JenaRDFReader, Jena, JenaStore}
import org.w3.banana.RDF
import org.w3.banana.RDFReader
import org.w3.banana.RDFXML

import Jena._
import JenaRDFReader._
import org.w3.banana._


object MyApp extends App {

  import TweeterToRdf._


  def printTime(s: String = "") : Unit = println((new java.util.Date()) +":" +s)

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }


  val confb = new ConfigurationBuilder()

    confb.setOAuthConsumerKey("7Kg8uFnxrY0qgZNjStIMw")
    .setOAuthConsumerSecret("KZphiWF9g2KfGl3M8TTSQdmczIIAk4oRHXPc3EkEOU")
    .setOAuthAccessToken("14733354-YB3fiqkelx19AVPGqu2jorbHkHtQ78m62cLhd8cWl")
    .setOAuthAccessTokenSecret("lEJMuI55Llbf9cfYVXKKz86hd6SPFNfDarJabudKLY4")

  val tdbDataset =  TDBFactory.createDataset("tdb")
  val jenas = JenaStore(tdbDataset)

  val twitterRdfStore = new TwitterRdfStore[Jena](jenas)

  import twitterRdfStore.diesel._

  val tf = new TwitterFactory(confb.build())

  val twitter = tf.getInstance()

  val a = (1 to 2).flatMap(i => {
    val page = new Paging(i, 200)
    twitter.getUserTimeline(page).toList
  })

  println(a.length)

  printTime()

  printToFile(new File("tweeter.txt"))(p => {
    a.foreach(s => p.println(s))

  })

  printTime("list")


  val listg = a.flatMap({s:Status => jenaTweeterToRdf(s)})


  printTime("union")

   val uniongraph = diesel.ops.union(listg)


  printTime("put into the store")

  twitterRdfStore.appendToTwitter(uniongraph)

  // g.map(g => twitterRdfStore.appendToTwitter(g))

  printTime()

  def tidy(s:String): String = s.replace('\u00a0',' ')

  printTime ("get the graph")
  val graph = twitterRdfStore.twitter

  printTime("to RXML")

  JenaRDFBlockingWriter.RDFXMLWriter.asString(graph, "").map(s => printToFile(new File("tweeter.rdf"))(_.println(tidy(s)) ) )

  printTime("fin")

}

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory
import com.hp.hpl.jena.tdb.TDBFactory
import org.w3.banana.Diesel
import org.w3.banana.GraphStore
import org.w3.banana.jena.{JenaRDFBlockingWriter, JenaRDFReader, Jena, JenaStore}
import org.w3.banana.RDF
import org.w3.banana.RDFReader
import org.w3.banana.RDFXML

import Jena._
import JenaRDFReader._
import org.w3.banana._




class TwitterRdfStore[Rdf <: RDF](val jenaStore : GraphStore[Rdf])
                                 (implicit val diesel: Diesel[Rdf], val reader: RDFReader[Rdf, RDFXML]) {

  import diesel._
  import ops._

  val myGraph = uri("http://twitter.com/un_jon")

  def appendToTwitter(graph: Rdf#Graph) = jenaStore.appendToNamedGraph(myGraph, graph)

  def twitter:Rdf#Graph = jenaStore.getNamedGraph(myGraph)

}


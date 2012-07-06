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


object MyApp2 extends App {

  val tdbDataset = TDBFactory.createDataset("tdb")
  val jenas = JenaStore(tdbDataset)

  val twitterRdfStore = new TwitterRdfStore[Jena](jenas)

}



class TwitterRdfStore[Rdf <: RDF](val jenaStore : GraphStore[Rdf])
                                 (implicit val diesel: Diesel[Rdf], val reader: RDFReader[Rdf, RDFXML]) {

  import diesel._
  import ops._

  val myGraph = uri("http://twitter.com/un_jon")

  jenaStore.removeGraph(myGraph)

  def appendToTwitter(graph: Rdf#Graph) = jenaStore.appendToNamedGraph(myGraph, graph)

  def twitter:Rdf#Graph = jenaStore.getNamedGraph(myGraph)

}
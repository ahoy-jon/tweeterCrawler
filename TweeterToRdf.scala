import org.joda.time.DateTime
import org.w3.banana._
import twitter4j.{EntitySupport, User, Status}
import jena._

class TweeterToRdf[Rdf <: RDF](implicit diesel: Diesel[Rdf]) extends (Status => List[Rdf#Graph]) {

  import diesel._

  val sioc =  SIOCPrefix(diesel.ops)
  val twt = TwitterPrefix(diesel.ops)

  def twitterUri(s:Status): Rdf#URI = twitterUri(s.getUser.getScreenName,s.getId)

  def userUri(u: User): Rdf#URI = userUri(u.getScreenName)
  def userUri(screenName: String): Rdf#URI =  uri("https://twitter.com/"+screenName)

  def twitterUri(screenName: String,  id:Long): Rdf#URI = uri("https://twitter.com/" + screenName + "/status/" + id)

  def cleanApiOutput(id: Long): Option[Long] = Option(id).filter(_ != -1)


  def entityToList(e: EntitySupport, entityUri: Rdf#URI): List[Rdf#Graph]   = {
    def cleanOutput[A](a: Array[A])  : List[A] = Option(a).map(_.toList).toList.flatten

    List(cleanOutput(e.getUserMentionEntities).map(u => (entityUri -- twt.mention ->- userUri(u.getScreenName)).graph),
      cleanOutput(e.getURLEntities).map(u => (entityUri -- sioc.links_to ->- uri(u.getExpandedURL.toExternalForm)).graph),
      cleanOutput(e.getMediaEntities).map(m => (entityUri -- twt.has_media ->- uri(m.getMediaURL.toExternalForm)).graph)).flatten
  }

  def apply(s: Status): List[Rdf#Graph] =  {
    val toMerge: List[List[Rdf#Graph]] = List(
      List((twitterUri(s)
        -- sioc.content ->- s.getText
        -- sioc.creator_of ->- userUri(s.getUser)
        -- sioc.last_activity_date ->- new DateTime(s.getCreatedAt)
        ).graph),

      entityToList(s, twitterUri(s)),

      cleanApiOutput(s.getInReplyToStatusId).map(id =>
        (twitterUri(s) -- sioc.reply_of ->- twitterUri(s.getInReplyToScreenName, id)).graph).toList,

      Option(s.getRetweetedStatus).map(rt => (twitterUri(s) -- twt.retweet_of ->- twitterUri(rt)).graph :: this.apply(rt)).toList.flatten
    )

    toMerge.flatten

  }

}

object TweeterToRdf {
  val jenaTweeterToRdf = new TweeterToRdf[Jena]()

}




object TwitterPrefix {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]) = new TwitterPrefix[Rdf](ops)
}


class TwitterPrefix[Rdf <: RDF](ops : RDFOperations[Rdf]) extends PrefixBuilder("twitter", "http://example.com/twitter/ns#", ops) {
  val retweet_of = apply("retweet_of")
  val mention    = apply("mention")
  val has_media  = apply("has_media")
}

object SIOCPrefix {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]) = new SIOCPrefix(ops)
}



class SIOCPrefix[Rdf <: RDF](ops: RDFOperations[Rdf]) extends PrefixBuilder("sioc", "http://rdfs.org/sioc/ns#", ops) {
  val content = apply("content")
  val creator_of = apply("creator_of")
  val last_activity_date = apply("last_activity_date")
  val reply_of = apply("reply_of")
  val links_to = apply("links_to")


  /*

  public static final Resource NAMESPACE = m_model.createResource( NS );
	public static final Property  about = m_model.createProperty( "http://rdfs.org/sioc/ns#about" );
	public static final Property  account_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#account_of" );
	public static final Property  addressed_to = m_model.createProperty( "http://rdfs.org/sioc/ns#addressed_to" );
	public static final Property  administrator_of = m_model.createProperty( "http://rdfs.org/sioc/ns#administrator_of" );
	public static final Property  attachment = m_model.createProperty( "http://rdfs.org/sioc/ns#attachment" );
	public static final Property  avatar = m_model.createProperty( "http://rdfs.org/sioc/ns#avatar" );
	public static final Property  container_of = m_model.createProperty( "http://rdfs.org/sioc/ns#container_of" );
	public static final Property  content  = m_model.createProperty( "http://rdfs.org/sioc/ns#content" );
	public static final Property  creator_of = m_model.createProperty( "http://rdfs.org/sioc/ns#creator_of" );
	public static final Property  earlier_version = m_model.createProperty( "http://rdfs.org/sioc/ns#earlier_version" );
	public static final Property  email  = m_model.createProperty( "http://rdfs.org/sioc/ns#email" );
	public static final Property  email_sha1  = m_model.createProperty( "http://rdfs.org/sioc/ns#email_sha1" );
	public static final Property  embeds_knowledge  = m_model.createProperty( "http://rdfs.org/sioc/ns#embeds_knowledge" );
	public static final Property  feed  = m_model.createProperty( "http://rdfs.org/sioc/ns#feed" );
	public static final Property  follows  = m_model.createProperty( "http://rdfs.org/sioc/ns#follows" );
	public static final Property  function_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#function_of" );
	public static final Property  has_administrator  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_administrator" );
	public static final Property  has_container  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_container" );
	public static final Property  has_creator  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_creator" );
	public static final Property  has_discussion  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_discussion" );
	public static final Property  has_function  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_function" );
	public static final Property  has_host  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_host" );
	public static final Property  has_member = m_model.createProperty( "http://rdfs.org/sioc/ns#has_member" );
	public static final Property  has_moderator = m_model.createProperty( "http://rdfs.org/sioc/ns#has_moderator" );
	public static final Property  has_modifier  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_modifier" );
	public static final Property  has_owner  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_owner" );
	public static final Property  has_parent  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_parent" );
	public static final Property  has_reply  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_reply" );
	public static final Property  has_scope  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_scope" );
	public static final Property  has_space  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_space" );
	public static final Property  has_subscriber  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_subscriber" );
	public static final Property  has_usergroup  = m_model.createProperty( "http://rdfs.org/sioc/ns#has_usergroup" );
	public static final Property  host_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#host_of" );
	public static final Property  id  = m_model.createProperty( "http://rdfs.org/sioc/ns#id" );
	public static final Property  ip_address  = m_model.createProperty( "http://rdfs.org/sioc/ns#ip_address" );
	public static final Property  last_activity_date  = m_model.createProperty( "http://rdfs.org/sioc/ns#last_activity_date" );
	public static final Property  last_item_date  = m_model.createProperty( "http://rdfs.org/sioc/ns#last_item_date" );
	public static final Property  last_reply_date  = m_model.createProperty( "http://rdfs.org/sioc/ns#last_reply_date" );
	public static final Property  later_version  = m_model.createProperty( "http://rdfs.org/sioc/ns#later_version" );
	public static final Property  latest_version  = m_model.createProperty( "http://rdfs.org/sioc/ns#latest_version" );
	public static final Property  link  = m_model.createProperty( "http://rdfs.org/sioc/ns#link" );
	public static final Property  links_to  = m_model.createProperty( "http://rdfs.org/sioc/ns#links_to" );
	public static final Property  member_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#member_of" );
	public static final Property  moderator_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#moderator_of" );
	public static final Property  modifier_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#modifier_of" );
	public static final Property  name  = m_model.createProperty( "http://rdfs.org/sioc/ns#name" );
	public static final Property  next_by_date  = m_model.createProperty( "http://rdfs.org/sioc/ns#next_by_date" );
	public static final Property  next_version  = m_model.createProperty( "http://rdfs.org/sioc/ns#next_version" );
	public static final Property  note  = m_model.createProperty( "http://rdfs.org/sioc/ns#note" );
	public static final Property  num_authors  = m_model.createProperty( "http://rdfs.org/sioc/ns#num_authors" );
	public static final Property  num_items  = m_model.createProperty( "http://rdfs.org/sioc/ns#num_items" );
	public static final Property  num_replies = m_model.createProperty( "http://rdfs.org/sioc/ns#num_replies" );
	public static final Property  num_threads  = m_model.createProperty( "http://rdfs.org/sioc/ns#num_threads" );
	public static final Property  num_views  = m_model.createProperty( "http://rdfs.org/sioc/ns#num_views" );
	public static final Property  owner_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#owner_of" );
	public static final Property  parent_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#parent_of" );
	public static final Property  previous_by_date  = m_model.createProperty( "http://rdfs.org/sioc/ns#previous_by_date" );
	public static final Property  previous_version  = m_model.createProperty( "http://rdfs.org/sioc/ns#previous_version" );
	public static final Property  related_to  = m_model.createProperty( "http://rdfs.org/sioc/ns#related_to" );
	public static final Property  reply_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#reply_of" );
	public static final Property  scope_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#scope_of" );
	public static final Property  sibling  = m_model.createProperty( "http://rdfs.org/sioc/ns#sibling" );
	public static final Property  space_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#space_of" );
	public static final Property  subscriber_of   = m_model.createProperty( "http://rdfs.org/sioc/ns#subscriber_of" );
	public static final Property  topic  = m_model.createProperty( "http://rdfs.org/sioc/ns#topic" );
	public static final Property  usergroup_of  = m_model.createProperty( "http://rdfs.org/sioc/ns#usergroup_of" );
	public static final Resource Community = m_model.createResource( "http://rdfs.org/sioc/ns#Community" );
	public static final Resource Container  = m_model.createResource( "http://rdfs.org/sioc/ns#Container" );
	public static final Resource Forum  = m_model.createResource( "http://rdfs.org/sioc/ns#Forum" );
	public static final Resource Item  = m_model.createResource( "http://rdfs.org/sioc/ns#Item" );
	public static final Resource Post  = m_model.createResource( "http://rdfs.org/sioc/ns#Post" );
	public static final Resource Role  = m_model.createResource( "http://rdfs.org/sioc/ns#Role" );
	public static final Resource Site  = m_model.createResource( "http://rdfs.org/sioc/ns#Site" );
	public static final Resource Space  = m_model.createResource( "http://rdfs.org/sioc/ns#Space" );
	public static final Resource Thread  = m_model.createResource( "http://rdfs.org/sioc/ns#Thread" );
	public static final Resource UserAccount  = m_model.createResource( "http://rdfs.org/sioc/ns#UserAccount" );
	public static final Resource Usergroup  = m_model.createResource( "http://rdfs.org/sioc/ns#Usergroup" );

   */

}

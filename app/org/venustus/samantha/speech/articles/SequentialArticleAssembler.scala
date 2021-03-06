package org.venustus.samantha.speech.articles

import java.util.Date

import akka.actor.{Cancellable, ActorRef, Actor}
import akka.event.Logging
import akka.routing.{RoundRobinRoutingLogic, Router, ActorRefRoutee}
import com.google.inject.name.Named
import com.google.inject.Inject
import org.venustus.samantha.speech.articles.ArticleAssembler.AssembleArticleFromUrl
import org.venustus.samantha.speech.articles.ArticleExtractor.{ExtractedContent, ExtractContentFromPage}
import org.venustus.samantha.speech.articles.SequentialArticleAssembler.ExtractorTimedOut
import org.venustus.samantha.speech.articles.components.{Speakables, PublishedDate, Title, Author}
import play.api.libs.ws.{WS, WSClient}
import play.api.Play.current
import play.api.libs.concurrent.Akka

import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.duration._

/**
 * Created by venkat on 16/08/15.
 */
class SequentialArticleAssembler @Inject() (wsClient: WSClient,
                                            gooseFactory: GooseArticleExtractor.Factory,
                                            customFactory: PublisherGuidanceBasedExtractor.Factory,
                                            readabilityFactory: ReadabilityExtractor.Factory,
                                            embedlyFactory: EmbedlyArticleExtractor.Factory,
                                            @Named("extractorCount") extractorRouteeCount: Int) extends Actor
        with InjectedActorSupport {

    val numExtractors = 3
    val log = Logging getLogger (context.system, this)

    override def preStart() = log.debug("Starting")


    override def preRestart(reason: Throwable, message: Option[Any]) = {
        log error (reason, "Restarting due to [{}] when processing [{}]",
            reason getMessage, if(message.isDefined) message.get else "")
    }

    var currentSender: Option[ActorRef] = None
    var title: Option[(String, Int)] = None
    var author: Option[(String, Int)] = None
    var publishedDate: Option[(Date, Int)] = None
    var allSpeakables: Option[(List[Speakable], Int)] = None
    var extractorTimeoutCancellable: Option[Cancellable] = None

    def getRouter(actors: Seq[ActorRef]): Router = {
        val routees = actors map { case r =>
            context watch r
            ActorRefRoutee(r)
        }
        Router(RoundRobinRoutingLogic(), routees.toVector)
    }

    val routers: List[Router] = List(
        getRouter((1 to extractorRouteeCount) map { case i =>
            injectedChild(gooseFactory(), "goose-" + i )
        }),
        getRouter((1 to extractorRouteeCount) map { case i =>
            injectedChild(customFactory(), "custom-" + i )
        }),
        getRouter((1 to extractorRouteeCount) map { case i =>
            injectedChild(readabilityFactory(), "readability-" + i )
        }),
        getRouter((1 to extractorRouteeCount) map { case i =>
            injectedChild(embedlyFactory(), "embedly-" + i)
        })
    )


    def receive = listenForNewRequests

    def listenForNewRequests: Receive = {
        case AssembleArticleFromUrl(url: String) =>
            val wsRequestHolder = WS url url
            implicit val ec = context.dispatcher
            (wsRequestHolder get()) map {
                case response =>
                    routers.zipWithIndex foreach {
                        case (r, i: Int) => r route (ExtractContentFromPage(url, response.body.toString, i), self)
                    }
            }
            currentSender = Some(sender())
            log info "Sent extraction commands to all routees"
            log info "now listening for responses"
            extractorTimeoutCancellable = Some(Akka.system.scheduler.scheduleOnce(5 seconds, self, ExtractorTimedOut))
            context become listenForResponses
    }

    def listenForResponses: Receive = {
        case ExtractorTimedOut =>
            log info "Time out while waiting for responses from all extractors"
            wrapUpAndListenForRequests()
        case ExtractedContent(components, priority) =>
            log info "Received response from " + sender()
            components foreach {
                case Title(t) =>
                    if(title.isEmpty || title.get._2 > priority) title = Some(t, priority)
                case Author(a, _) =>
                    if(author.isEmpty || author.get._2 > priority) author = Some(a, priority)
                case PublishedDate(d) =>
                    if(publishedDate.isEmpty || publishedDate.get._2 > priority) publishedDate = Some(d, priority)
                case Speakables(speakables) =>
                    if(allSpeakables.isEmpty || allSpeakables.get._2 > priority) allSpeakables = Some(speakables, priority)
            }
            if(allSpeakables.isDefined && title.isDefined && currentSender.isDefined) {
                wrapUpAndListenForRequests()
            }
    }

    private def wrapUpAndListenForRequests() = {
        if(extractorTimeoutCancellable.isDefined && !extractorTimeoutCancellable.get.isCancelled) extractorTimeoutCancellable.get cancel()
        if(allSpeakables.isDefined) {
            currentSender.get ! Article(title map (_._1),
                publishedDate map (_._1),
                author map (_._1),
                allSpeakables.get._1)
        }
        else {
            currentSender.get ! EmptyArticle
        }
        currentSender = None
        title = None
        author = None
        publishedDate = None
        allSpeakables = None
        extractorTimeoutCancellable = None
        context become listenForNewRequests
    }
}

object SequentialArticleAssembler {
    trait Factory {
        def apply(): Actor
    }
    case object ExtractorTimedOut
}

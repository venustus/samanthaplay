package org.venustus.samantha.speech.cache

import akka.actor.Actor
import akka.routing.{RoundRobinRoutingLogic, Router, ActorRefRoutee}
import com.google.inject.Inject
import play.api.libs.concurrent.InjectedActorSupport

/**
 * Created by venkat on 14/09/15.
 */
class TranscriptStoreRouter @Inject() (storeFactory: TranscriptStore.Factory) extends Actor with InjectedActorSupport {
    val router = {
        val routees = ((1 to 5) map { case i => injectedChild(storeFactory(), "store-" + i)}) map { case r =>
            context watch r
            ActorRefRoutee(r)
        }
        Router(RoundRobinRoutingLogic(), routees.toVector)
    }

    def receive = {
        case x => router route (x, sender())
    }
}

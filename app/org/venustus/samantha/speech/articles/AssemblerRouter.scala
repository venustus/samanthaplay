package org.venustus.samantha.speech.articles

import akka.actor.Actor
import akka.routing.{RoundRobinRoutingLogic, Router, ActorRefRoutee}
import com.google.inject.{Singleton, Inject}
import play.api.libs.concurrent.InjectedActorSupport

/**
 * Created by venkat on 25/08/15.
 */
@Singleton
class AssemblerRouter @Inject() (assemblerFactory: SequentialArticleAssembler.Factory) extends Actor with InjectedActorSupport {

    val router = {
        val routees = ((1 to 5) map { case i => injectedChild(assemblerFactory(), "assembler-" + i)}) map { case r =>
            context watch r
            ActorRefRoutee(r)
        }
        Router(RoundRobinRoutingLogic(), routees.toVector)
    }

    def receive = {
        case x => router route (x, sender())
    }
}

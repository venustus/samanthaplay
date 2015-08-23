package modules

import akka.actor.Props
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.google.inject.name.{Names, Named}
import com.google.inject.{Singleton, Provides, Inject, AbstractModule}
import com.gravity.goose.{Goose, Configuration}
import com.ivona.services.tts.IvonaSpeechCloudClient
import org.venustus.samantha.speech.articles.{SequentialArticleAssembler, PublisherGuidanceBasedExtractor, GooseArticleExtractor}
import play.api.libs.concurrent.AkkaGuiceSupport


/**
 * Created by venkat on 31/07/15.
 */
class SamanthaPlayModule extends AbstractModule with AkkaGuiceSupport {

    override def configure() = {
        bindActor[SequentialArticleAssembler]("assembler")
        val speechCloudClient = new IvonaSpeechCloudClient(
            new ClasspathPropertiesFileCredentialsProvider("ivona/IvonaCredentials.properties"))
        speechCloudClient setEndpoint "https://tts.eu-west-1.ivonacloud.com"
        bind(classOf[IvonaSpeechCloudClient]) toInstance speechCloudClient
    }

    @Provides
    @Singleton
    @Named("extractors")
    def provideExtractors: List[Props] = {
        List(Props[GooseArticleExtractor], Props[PublisherGuidanceBasedExtractor])
    }
}

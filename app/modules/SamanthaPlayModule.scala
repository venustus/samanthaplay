package modules

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.google.inject.AbstractModule
import com.gravity.goose.Configuration
import com.ivona.services.tts.IvonaSpeechCloudClient
import org.venustus.samantha.speech.articles.{GooseArticleExtractor, ArticleExtractor}
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * Created by venkat on 31/07/15.
 */
class SamanthaPlayModule extends AbstractModule with AkkaGuiceSupport {

    val configuration = new Configuration
    configuration setEnableImageFetching (false)

    override def configure = {
        bind(classOf[Configuration]) toInstance (configuration)
        bind(classOf[ArticleExtractor]) to (classOf[GooseArticleExtractor])
        val speechCloudClient = new IvonaSpeechCloudClient(
            new ClasspathPropertiesFileCredentialsProvider("ivona/IvonaCredentials.properties"))
        speechCloudClient setEndpoint ("https://tts.eu-west-1.ivonacloud.com")
        bind(classOf[IvonaSpeechCloudClient]) toInstance (speechCloudClient)
    }

}

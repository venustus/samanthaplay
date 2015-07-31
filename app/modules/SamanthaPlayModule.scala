package modules

import com.google.inject.AbstractModule
import com.gravity.goose.{Goose, Configuration}
import org.venustus.samantha.speech.articles.{GooseArticleExtractor, ArticleExtractor}

/**
 * Created by venkat on 31/07/15.
 */
class SamanthaPlayModule extends AbstractModule {

    val configuration = new Configuration();
    configuration setEnableImageFetching (false)


    override def configure = {
        bind(classOf[Configuration]).toInstance(configuration)
        bind(classOf[ArticleExtractor]).to(classOf[GooseArticleExtractor])
    }

}

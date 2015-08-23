package org.venustus.samantha.speech.articles

import scala.concurrent.Future

/**
 * Created by venkat on 17/08/15.
 */
trait ArticleAssembler {

    def assembleArticle(url: String)

}

object ArticleAssembler {
    case class AssembleArticleFromUrl(url: String)
}

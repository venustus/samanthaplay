package org.venustus.samantha.speech.articles

import com.gravity.goose.Article

/**
 * Created by venkat on 31/07/15.
 */
trait ArticleExtractor {

    def extractContent(url: String): Article

}

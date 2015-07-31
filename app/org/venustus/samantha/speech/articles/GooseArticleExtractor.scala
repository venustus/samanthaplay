package org.venustus.samantha.speech.articles

import com.google.inject.{Inject, Singleton}
import com.gravity.goose.{Configuration, Goose}

/**
 * Created by venkat on 31/07/15.
 */
@Singleton
class GooseArticleExtractor @Inject() (configuration: Configuration) extends Goose(configuration) with ArticleExtractor

package org.venustus.samantha.speech.articles

import java.util.Date

/**
 * Created by venkat on 16/08/15.
 */
case class Article(title: Option[String], publishedDate: Option[Date], author: Option[String], speakables: List[Speakable])
case object EmptyArticle

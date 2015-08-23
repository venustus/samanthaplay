package org.venustus.samantha.speech.articles

import java.util.Date

/**
 * Created by venkat on 16/08/15.
 */
case class Article(title: String, publishedDate: Date, author: String, speakables: List[Speakable])

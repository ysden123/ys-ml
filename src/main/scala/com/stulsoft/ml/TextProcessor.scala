/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import scala.io.Source
import com.typesafe.scalalogging.LazyLogging

object TextProcessor extends LazyLogging:
  def extractWordsFromSentence(text: String): Array[String] =
    logger.debug("extractWordsFromSentence text: {}", text)
    text.split("""[ ,()\[\];:&".#-]""").toList
      .map(w => w.replaceAll(""""""", ""))
      .map(w => w.replaceAll("""«""", ""))
      .map(w => w.replaceAll("""»""", ""))
      .map(w => w.replaceAll("""“""", ""))
      .map(w => w.replaceAll("""”""", ""))
      .filter(i => !i.isBlank && i.nonEmpty)
      .filter(i => !i.forall(Character.isDigit))
      .toArray

  def processSentence(sentence:String):Unit=
    logger.debug("Processing sentence: {}", sentence)
    val words = extractWordsFromSentence(sentence)
    if words.length > 1 then
      for (i <- 0 until words.length - 1){
        DBService.addWordPair(Word(words(i)), Word(words(i + 1)))
      }

  def processText(filename:String):Unit=
    logger.info("==>processText {}", filename)
    val source = Source.fromFile(filename)
    val lines = source.getLines().toSeq
    source.close()

    val text = lines.mkString(" ")

    // Заменяем символы переноса строки на пробелы перед точкой, следующей за буквой
    val processedText = text.replaceAll("(?<=\\p{L})\\.(?=\\p{L})", " ")

    // Разделяем текст на предложения, используя точку как разделитель
    processedText
      .split("\\.")
      .foreach(sentence => processSentence(sentence))

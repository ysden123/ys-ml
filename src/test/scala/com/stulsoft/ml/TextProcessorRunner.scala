/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import com.typesafe.scalalogging.LazyLogging

object TextProcessorRunner extends LazyLogging:
  def main(args: Array[String]): Unit =
    logger.info("==>main")
    DBService.createDB()

    for (index <- 1 to 4)
      TextProcessor.processText(s"src/test/resources/text$index.txt")

    DBService.findHighestScore() match
      case Some((word1:Word, following:Following, word2:Word)) =>
        logger.info("Pairs with highest score {} -> {} -> {}", word1.name, following.score, word2.name)
      case _ =>
        logger.info("Nothing was found")

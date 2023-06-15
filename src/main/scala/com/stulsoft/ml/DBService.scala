/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import com.typesafe.scalalogging.StrictLogging

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Using}

object DBService extends StrictLogging:
  def clearDB(): Unit =
    logger.info("==>clearDB")
    Using(SessionManager.session()) {
      session => {
        session.executeWrite(tc => {
          val clearQuery = "MATCH(n) DETACH DELETE(n)"
          logger.info("Clearing DB (delete all nodes")
          val result = tc.run(clearQuery)
        })
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)

  def createDB(): Unit =
    logger.info("==>createDB")
    clearDB()
    Using(SessionManager.session()) {
      session => {
        session.executeWrite(tc => {
          logger.info("Creating constraint")
          val createConstrainQuery = "CREATE CONSTRAINT word_name IF NOT EXISTS FOR (w:Word) REQUIRE w.name IS UNIQUE"
          tc.run(createConstrainQuery)
        })
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)

  def addWordPair(word1: Word, word2: Word): Unit =
    logger.info("==>addWordPair")
    logger.info("Adding words: {} -> {}", word1, word2)
    Using(SessionManager.session()) {
      session => {
        session.executeWrite(tc => {
          val connectQuery =
            s"""
               |MERGE (w1: $word1)
               |MERGE (w2: $word2)
               |""".stripMargin
          tc.run(connectQuery)
        })

        session.executeWrite(tc =>
          val queryToFindFollowing = s"MATCH (w1:Word {name: '${word1.name}'}) -[r:Following] ->(w2:Word {name: '${word2.name}'}) RETURN r"
          val searchResult = tc.run(queryToFindFollowing)
          if searchResult.hasNext then
            val query =
              s"""
                 |MATCH (w1:Word) -[r:Following] ->(w2:Word)
                 |WHERE w1.name = '${word1.name}' AND w2.name = '${word2.name}'
                 |SET r.score = r.score + 1
                 |""".stripMargin
            tc.run(query)
          else
            val query =
              s"""
                 |MATCH
                 |   (w1:Word),
                 |   (w2:Word)
                 |WHERE w1.name = '${word1.name}' AND w2.name = '${word2.name}'
                 |CREATE (w1) -[:Following {score: 1}]->(w2)
                 |""".stripMargin
            tc.run(query)
        )

        /*
                session.executeRead(tc => {
                  val query = s"MATCH (w1:Word {name: '${word1.name}'}) -[r:Following] ->(w2:Word {name: '${word2.name}'}) RETURN r"
                  tc.run(query)
                    .list()
                    .asScala
                    .map(record => Following.fromValue(record.get("r")))
                    .foreach(following => logger.info("{}", following))
                })
        */
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)

  def findBestWord(word: Word): Option[Word] =
    logger.info("==>findBestWord")
    logger.info("Find best word for {}", word.name)
    var bestWord: Option[Word] = None
    Using(SessionManager.session()) {
      session => {
        session.executeRead(tc => {
          val query =
            s"""
               |MATCH (w1:Word) -[r:Following] ->(w2:Word)
               |WHERE w1.name ='${word.name}'
               |RETURN r,w2
               |""".stripMargin
          val words = tc.run(query).list().asScala.map(record => {
            val following = Following.fromValue(record.get("r"))
            val word2 = Word.fromValue(record.get("w2"))
            (following, word2)
          })
          if words.nonEmpty then
            bestWord = Some(words.maxBy(item => item._1.score)._2)
        })
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)
    bestWord

  def findHighestScore(): Option[(Word,Following,Word)] =
    logger.info("==>findHighestScore")
    var highestPair: Option[(Word,Following,Word)] = None
    Using(SessionManager.session()) {
      session => {
        session.executeRead(tc => {
          var query =
            s"""
               |MATCH (w1:Word) -[r:Following] ->(w2:Word)
               |RETURN r, w1, w2
               |""".stripMargin
          val pairs = tc.run(query)
            .list()
            .asScala
            .map(record => {
              val following = Following.fromValue(record.get("r"))
              val word1 = Word.fromValue(record.get("w1"))
              val word2 = Word.fromValue(record.get("w2"))
              (word1, following, word2)
            })
          if pairs.nonEmpty then
            highestPair = Some(pairs.maxBy(item => item._2.score))
        })
      }
    }
    highestPair
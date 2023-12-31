/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import com.typesafe.scalalogging.StrictLogging

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Using}

object DBService extends StrictLogging:
  def clearDB(): Unit =
    Using(SessionManager.session()) {
      session => {
        session.executeWrite(tc => {
          val clearQuery = "MATCH(n) DETACH DELETE(n)"
          logger.info("Clearing DB (delete all nodes")
          val result = tc.run(clearQuery).consume()
        })
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)

  def createDB(): Unit =
    clearDB()
    Using(SessionManager.session()) {
      session => {
        session.executeWrite(tc => {
          logger.info("Creating constraint")
          val createConstrainQuery = "CREATE CONSTRAINT word_name IF NOT EXISTS FOR (w:Word) REQUIRE w.name IS UNIQUE"
          tc.run(createConstrainQuery).consume()
        })
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)

  def addWordPair(word1: Word, word2: Word): Unit =
    Using(SessionManager.session()) {
      session => {
        session.executeWrite(tc => {
          val connectQuery =
            s"""
               |MERGE (w1: $word1)
               |MERGE (w2: $word2)
               |""".stripMargin
          tc.run(connectQuery).consume()
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
            tc.run(query).consume()
          else
            val query =
              s"""
                 |MATCH
                 |   (w1:Word),
                 |   (w2:Word)
                 |WHERE w1.name = '${word1.name}' AND w2.name = '${word2.name}'
                 |CREATE (w1) -[:Following {score: 1}]->(w2)
                 |""".stripMargin
            tc.run(query).consume()
        )
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)

  def findBestWord(word: Word): Option[Word] =
    var bestWord: Option[Word] = None
    Using(SessionManager.session()) {
      session => {
        session.executeRead(tc => {
          val query =
            s"""
               |MATCH (w1:Word) -[r:Following] ->(w2:Word)
               |WHERE w1.name ='${word.name}'
               |RETURN w2 AS w
               |ORDER BY r.score DESC
               |LIMIT 1
               |""".stripMargin
          val result = tc.run(query)
          if result.hasNext then
            bestWord = Some(Word.fromValue(result.next().get("w")))
        })
      }
    } match
      case Success(_) =>
      case Failure(exception) => logger.error(exception.getMessage, exception)
    bestWord

  def findHighestScore(): Option[(Word, Following, Word)] =
    var highestPair: Option[(Word, Following, Word)] = None
    Using(SessionManager.session()) {
      session => {
        session.executeRead(tc => {
          val query =
            s"""
               |MATCH (w1:Word) -[r:Following] ->(w2:Word)
               |RETURN r, w1, w2
               |ORDER BY r.score DESC
               |LIMIT 1""".stripMargin
          val pairs = tc.run(query)
          if pairs.hasNext then
            val record = pairs.next()
            val following = Following.fromValue(record.get("r"))
            val word1 = Word.fromValue(record.get("w1"))
            val word2 = Word.fromValue(record.get("w2"))
            highestPair = Some((word1, following, word2))
        })
      }
    }
    highestPair
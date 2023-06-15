/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import org.neo4j.driver.{Record, Value}

case class Following(score:Int):
  override def toString: String =
    s"Following {score: $score}"

object Following extends Neo4jDataObject[Following]:
  override def fromValue(aValue: Value): Following =
    Following(aValue.get("score").asInt())

  override def fromRecord(record: Record, valueName:String): Following =
    fromValue(record.get(valueName))

/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import org.neo4j.driver.{Value, Record}

case class Word(name:String):
  override def toString: String =
    s"Word {name: '$name'}"

object Word extends Neo4jDataObject[Word]:
  override def fromValue(aValue: Value): Word =
    Word(aValue.get("name").asString())

  override def fromRecord(record: Record, valueName:String): Word =
    fromValue(record.get(valueName))

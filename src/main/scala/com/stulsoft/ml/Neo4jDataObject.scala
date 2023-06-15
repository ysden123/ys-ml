/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import org.neo4j.driver.{Record, Value}

trait Neo4jDataObject[T]:
  def fromValue(aValue: Value):T
  def fromRecord(record: Record, valueName:String):T


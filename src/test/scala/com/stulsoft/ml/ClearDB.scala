/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

object ClearDB:
  def main(args: Array[String]): Unit =
    DBService.clearDB()

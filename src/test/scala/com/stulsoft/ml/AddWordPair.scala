/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

object AddWordPair:
  def main(args: Array[String]): Unit =
    DBService.createDB()
    DBService.addWordPair(Word("Theword1"),Word("Theword2"))
    DBService.addWordPair(Word("Theword1"),Word("Theword2"))
    DBService.addWordPair(Word("Theword1"),Word("Theword2"))
    DBService.addWordPair(Word("Theword1"),Word("Theword2"))
    DBService.addWordPair(Word("Theword1"),Word("Theword3"))
    DBService.addWordPair(Word("Theword4"),Word("Theword2"))
    DBService.findBestWord(Word("Theword1")).foreach(word => println(s"Theword1->$word"))
    DBService.findBestWord(Word("Theword2")).foreach(word => println(s"Theword2->$word"))
    DBService.findBestWord(Word("Theword4")).foreach(word => println(s"Theword4->$word"))

/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuiteLike

class TextProcessorTest extends AnyFlatSpec:
  "TextProcessor" should "extract words 1" in {
    val text = "This is a simple test."
    val words = TextProcessor.extractWordsFromSentence(text)
    assert(5 == words.length)

    assert("test" == words(4))
  }

  it should "extract words from empty text" in {
    assert(TextProcessor.extractWordsFromSentence("").isEmpty)
    assert(TextProcessor.extractWordsFromSentence(".").isEmpty)
    assert(TextProcessor.extractWordsFromSentence("...").isEmpty)
  }

  it should "process sentence 1" in {
    try
      TextProcessor.processSentence("This is a simple test.")
    catch
      case exception: Exception =>
        fail(exception)
  }

  it should "process sentence 2" in {
    try
      TextProcessor.processSentence("This is a more complicated test.")
    catch
      case exception: Exception =>
        fail(exception)
  }
package org.opencompare.formalizer.interpreters

import java.util.regex.Matcher
import org.diverse.pcm.api.java.Value
import org.opencompare.api.java.{Value, Product, Feature}

import scala.collection.immutable.List

class UnknownPatternInterpreter (
    validHeaders : List[String],
    regex : String,
    parameters : List[String],
    confident : Boolean)
    extends PatternInterpreter(validHeaders, regex, parameters, confident) {

  override def createValue(s: String, matcher : Matcher, parameters : List[String], product : Product, feature : Feature) : Option[Value] = {
		 val value = factory.createNotAvailable()
		 Some(value)
  }

}
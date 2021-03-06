/*
 * Copyright (c) 2015-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.tools.tck

import org.opencypher.tools.tck.values._
import org.scalatest.{FunSuite, Matchers}

class CypherValueVisitorTest extends FunSuite with Matchers {

  test("unlabelled node") {
    val string = "()"
    val parsed = CypherValue(string)
    val expected = CypherNode(Set.empty, CypherPropertyMap(Map.empty))
    parsed should equal(expected)
  }

  test("node") {
    val string = "(:A {name: 'Hans'})"
    val parsed = CypherValue(string)
    val expected = CypherNode(Set("A"), CypherPropertyMap(Map("name" -> CypherString("Hans"))))
    parsed should equal(expected)
  }

  test("relationship") {
    val string = "[:A {since: 1920}]"
    val parsed = CypherValue(string)
    val expected = CypherRelationship("A", CypherPropertyMap(Map("since" -> CypherInteger(1920))))
    parsed should equal(expected)
  }

  test("scalars") {
    CypherValue("true") should equal(CypherBoolean(true))
    CypherValue("false") should equal(CypherBoolean(false))
    CypherValue("-1") should equal(CypherInteger(-1))
    CypherValue("-1.0") should equal(CypherFloat(-1.0))
    CypherValue("'true'") should equal(CypherString("true"))
    CypherValue("''") should equal(CypherString(""))
    CypherValue("'-1'") should equal(CypherString("-1"))
    CypherValue("null") should equal(CypherNull)
  }

  test("path with a single node") {
    val string = "<()>"
    val parsed = CypherValue(string)
    val expected = CypherPath(CypherNode())
    parsed should equal(expected)
  }

  test("complex path") {
    val string = "<({a: true})-[:R]->(:A)<-[:T {b: 'true'}]-()>"
    val parsed = CypherValue(string)
    val expected = CypherPath(
      CypherNode(properties = CypherPropertyMap(Map("a" -> CypherBoolean(true)))),
      List(
        Forward(
          CypherRelationship("R"),
          CypherNode(Set("A"))),
        Backward(
          CypherRelationship("T", CypherPropertyMap(Map("b" -> CypherString("true")))),
          CypherNode())
      )
    )
    parsed should equal(expected)
  }

  test("map") {
    CypherValue("{}") should equal(CypherPropertyMap())
    CypherValue("{name: 'Hello', foo: true}") should equal(
      CypherPropertyMap(Map("name" -> CypherString("Hello"), "foo" -> CypherBoolean(true))))
  }

  test("list") {
    CypherValue("[]") should equal(CypherOrderedList())
    CypherValue("[1, 2, null]") should equal(
      CypherOrderedList(List(CypherInteger(1), CypherInteger(2), CypherNull)))
    CypherValue("[]", orderedLists = false) should equal(CypherUnorderedList())
    CypherValue("[1, 2, null]", orderedLists = false) should equal(
      CypherUnorderedList(List(CypherInteger(1), CypherInteger(2), CypherNull).sorted(CypherValue.ordering)))
  }

}

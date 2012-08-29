package com.typesafe.slick.testkit.tests

import org.junit.Assert._
import scala.slick.lifted._
import java.sql.{Time, Date, Timestamp}
import scala.slick.testutil.TestDB
import com.typesafe.slick.testkit.util.TestkitTest

class ScalarFunctionTest(val tdb: TestDB) extends TestkitTest {
  import tdb.profile.Implicit._

  def test {
    def check[T](q: Query[ColumnBase[T], T], exp: T*) = {
      println("Executing: " + q.selectStatement)
      assertEquals(exp.toSet, q.list.toSet)
    }
    def checkIn[T](q: Query[ColumnBase[T], T], exp: T*) = {
      println("Executing: " + q.selectStatement)
      val found = q.list.toSet
      assert(found.forall(exp contains _), "all of result "+found+" should be in expected "+exp)
    }

    // Literals
    def checkLit[T : TypeMapper](v: T) = check(Query(ConstColumn(v)), v)
    checkLit(false)
    checkLit(true)
    checkLit(42: Byte)
    checkLit(-42: Byte)
    checkLit(Date.valueOf("2011-07-15"))
    checkLit(Time.valueOf("15:53:21"))
    checkLit(Timestamp.valueOf("2011-07-15 15:53:21"))
    checkLit(42)
    checkLit(-42)
    checkLit(17.5)
    checkLit(-17.5)
    checkLit(17.5f)
    checkLit(-17.5f)
    checkLit(42l)
    checkLit(-42l)
    checkLit("foo")

    check(Query("42".asColumnOf[Int]), 42)
    check(Query(ConstColumn("foo").length), 3)
    check(Query(ConstColumn("foo") ++ "bar"), "foobar")
    check(Query(ConstColumn(1) ifNull 42), 1)
    check(Query(ConstColumn[Option[Int]](None) ifNull 42), 42)
    check(Query(ConstColumn("Foo").toUpperCase), "FOO")
    check(Query(ConstColumn("Foo").toLowerCase), "foo")
    check(Query(ConstColumn("  foo  ").ltrim), "foo  ")
    check(Query(ConstColumn("  foo  ").rtrim), "  foo")
    check(Query(ConstColumn("  foo  ").trim), "foo")
    assertEquals(1, Query(Functions.database.toLowerCase).list.length)
    assertEquals(1, Query(Functions.user.toLowerCase).list.length)
    check(Query(ConstColumn(8) % 3 ), 2)
    check(Query(ConstColumn(-12.5).abs), 12.5)
    check(Query(ConstColumn(1.9).ceil), 2.0)
    check(Query(ConstColumn(1.5).ceil), 2.0)
    check(Query(ConstColumn(1.4).ceil), 2.0)
    check(Query(ConstColumn(-1.9).ceil), -1.0)
    check(Query(ConstColumn(-1.5).ceil), -1.0)
    check(Query(ConstColumn(-1.4).ceil), -1.0)
    check(Query(ConstColumn(1.5).floor), 1.0)
    check(Query(ConstColumn(1.4).floor), 1.0)
    check(Query(ConstColumn(-1.5).floor), -2.0)
    check(Query(ConstColumn(-10.0).sign), -1)
    check(Query(Functions.pi.toDegrees), 180.0)
    check(Query(Functions.pi.toDegrees.toRadians is Functions.pi), true)

    val myExpr = SimpleExpression.binary[Int, Int, Int] { (l, r, qb) =>
      qb.sqlBuilder += '('
      qb.expr(l)
      qb.sqlBuilder += '+'
      qb.expr(r)
      qb.sqlBuilder += "+1)"
    }

    check(Query(myExpr(4, 5)), 10)
  }
}

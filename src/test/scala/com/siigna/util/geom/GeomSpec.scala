/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.util.geom

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests the geom package object.
 */
class GeomSpec extends FunSpec with ShouldMatchers {

  describe ("The geom package") {

    it ("can normalize degrees") {
      normalizeDegrees(0) should equal(0)
      normalizeDegrees(90) should equal(90)
      normalizeDegrees(270) should equal(270)
      normalizeDegrees(359) should equal(359)
      normalizeDegrees(360) should equal(0)
      normalizeDegrees(361) should equal(1)
      normalizeDegrees(350 + 20) should equal(10)
      normalizeDegrees(-1) should equal(359)
      normalizeDegrees(-180) should equal(180)
      normalizeDegrees(36000000000000.0) should equal(0)
      normalizeDegrees(-36000000000000.0) should equal(0)
    }

  }

}

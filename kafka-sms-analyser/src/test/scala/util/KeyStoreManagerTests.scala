package io.github.malyszaryczlowiek
package util
import io.github.malyszaryczlowiek.util.KeyStoreManager.{Done, Error}

import org.scalatest.funsuite.AnyFunSuite
import scala.sys.process._

class KeyStoreManagerTests extends AnyFunSuite {

  test("testing creation of trustStore.jks and inserting cert to them") {

    val keyM = new KeyStoreManager


    // required download sample certificate first
//    val res = keyM.createTrustStore("trustStore.jks","password".toCharArray,"superuser.com.cer", "superuser.com")
//    res match {
//      case Done => assert(true)
//      case Error => assert(false)
//    }


    // we remove create trustStore
    s"rm -rf trustStore.jks".!!
//    // and cert
//    s"rm- rf superuser.com.cer".!!

  }

}

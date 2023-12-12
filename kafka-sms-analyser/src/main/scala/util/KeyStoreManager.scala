package io.github.malyszaryczlowiek
package util

import config.AppConfig._



import java.io.{File, FileInputStream, FileOutputStream, InputStream}
import java.security.KeyStore
import java.security.cert.{Certificate, CertificateFactory, X509Certificate}
import scala.util.{Failure, Success, Using}


class KeyStoreManager {



  import io.github.malyszaryczlowiek.util.KeyStoreManager.{Done, Error, StoreCreation}
  /**
   * this method creates required for SSL keyStore
   *
   * @return
   */
  def createKeyStore(keystoreLocation: String      = KafkaSSLConfig.SSL_KEYSTORE_LOCATION_CONFIG,
                     keyStorePassword: Array[Char] = KafkaSSLConfig.SSL_KEYSTORE_PASSWORD_CONFIG.toCharArray): StoreCreation = {

    val keyStoreFile: File = new File( keystoreLocation )
    // if keyStrore file does not exists we need create them
    if ( ! keyStoreFile.exists() ) {
      val ks: KeyStore = KeyStore.getInstance( KeyStore.getDefaultType )
      ks.load(null, keyStorePassword)
      Using(new FileOutputStream( keystoreLocation )) {
        (fos: FileOutputStream) => ks.store(fos, keyStorePassword)
      } match {
        case Failure(exception) => Error
        case Success(value) => Done
      }
    } else Error
  }


  /**
   * this method creates required for SSL trust store file and put into it
   * security certificate
   *
   * @return
   */
  def createTrustStore( storeLocation: String      = KafkaSSLConfig.SSL_TRUSTSTORE_LOCATION_CONFIG,
                        storePassword: Array[Char] = KafkaSSLConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG.toCharArray,
                        certFile: String           = KafkaSSLConfig.SSL_CERTIFICATE,
                        certAlias: String          = KafkaSSLConfig.SSL_CERTIFICATE_ALIAS  ): StoreCreation = {

    val trustStore: File = new File( storeLocation )
    // if truststore file does not exists we need create them
    if (!trustStore.exists()) {
      val ks: KeyStore = KeyStore.getInstance( KeyStore.getDefaultType ) //  or "pkcs12"
      ks.load(null, storePassword)

      Using(new FileInputStream( certFile )) {
        (is: InputStream) => {
          val cf: CertificateFactory = CertificateFactory.getInstance("X.509");
          val cert: X509Certificate = cf.generateCertificate(is).asInstanceOf[X509Certificate]
          ks.setCertificateEntry( certAlias, cert)
        }
      } match {
        case Failure(exception) =>
          // logger
          println(s"$exception")
          Error
        case Success(value) =>
          // after successfully creating certificate
          // we create the keystore file
          Using(new FileOutputStream( storeLocation )) {
            (fos: FileOutputStream) => ks.store(fos, storePassword)
          } match {
            case Failure(exception) => Error
            case Success(value) => Done
          }
      }
    } else Error
  }

}
object KeyStoreManager {

  sealed trait StoreCreation
  case object Done extends StoreCreation
  case object Error extends StoreCreation

}


package io.github.malyszaryczlowiek
package util

import config.AppConfig._

import java.io.{File, FileInputStream, FileOutputStream, InputStream}
import java.security.KeyStore
import java.security.cert.{Certificate, CertificateFactory, X509Certificate}
import scala.util.{Failure, Success, Using}


class KeyStoreManager {

  sealed trait StoreCreation
  case object Done  extends StoreCreation
  case object Error extends StoreCreation


  /**
   * this method creates required for SSL keyStore
   *
   * @return
   */
  def createKeyStore(): StoreCreation = {

    val keystoreLocation = KafkaSSLConfig.SSL_KEYSTORE_LOCATION_CONFIG
    val keyStorePassword = KafkaSSLConfig.SSL_KEYSTORE_PASSWORD_CONFIG.toCharArray


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
  def createTrustStore(): StoreCreation = {

    val storeLocation    = KafkaSSLConfig.SSL_TRUSTSTORE_LOCATION_CONFIG
    val storePassword    = KafkaSSLConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG.toCharArray


    val keyStoreFile: File = new File( storeLocation )
    // if truststore file does not exists we need create them
    if (!keyStoreFile.exists()) {
      val ks: KeyStore = KeyStore.getInstance( KeyStore.getDefaultType ) //  or "pkcs12"
      ks.load(null, storePassword)

      Using(new FileInputStream( KafkaSSLConfig.SSL_CERTIFICATE )) {
        (is: InputStream) => {
          val cf: CertificateFactory = CertificateFactory.getInstance("X.509");
          val cert: X509Certificate = cf.generateCertificate(is).asInstanceOf[X509Certificate]
          ks.setCertificateEntry(KafkaSSLConfig.SSL_CERTIFICATE_ALIAS , cert)
        }
      } match {
        case Failure(exception) =>
          // logger
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

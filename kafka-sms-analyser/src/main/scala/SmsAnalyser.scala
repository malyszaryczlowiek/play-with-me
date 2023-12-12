package io.github.malyszaryczlowiek

import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Branched, Consumed, KStream, Materialized, Produced}
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde
import org.apache.kafka.streams.kstream.{GlobalKTable, Named}

import java.util.Properties
import config.AppConfig._
import config.AppConfig.Topics._
import model.{ConfidenceAndUriList, Sms, UriList}
import model.ConfidenceLevel._
import serdes.CustomSerdes._
import util.{KeyStoreManager, PhishingApiCaller, TopicCreator, UriSearcher}
import util.TopicCreator.{Done, Error}
//import play.api.Configuration.logger.logger



class SmsAnalyser
object SmsAnalyser {


  def main(args: Array[String]): Unit = {


    // define types for easier managing streams
    type Uri              = String
    type Nulll            = String    // this type means string is null for sure
    type UserNum          = String
    type UserStatus       = String
    type UserStatusOrNull = String
    type Confidence       = String
    type ConfidenceOrNull = String    // may be confidence or null string


    // uncomment to create both files required for SSL communication

//    val keyManager = new KeyStoreManager
//    val keyRes = keyManager.createKeyStore()
//    val trsRes = keyManager.createTrustStore()
//    // if creating keyStore and/or trustStore impossible then close app
//    if (keyRes == Error || trsRes == Error ) System.exit(555)



    // Define properties for KafkaStreams object
    val properties: Properties = new Properties()
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG,    appId)
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.servers)
    properties.put(StreamsConfig.STATE_DIR_CONFIG,         kafkaConfig.fileStore)

    // SSL security configs uncomment when all SSL Settings done, but check first SSL notes in README.md

//    properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, KafkaSSLConfig.SECURITY_PROTOCOL_CONFIG)
//    properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,    KafkaSSLConfig.SSL_TRUSTSTORE_LOCATION_CONFIG)
//    properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,    KafkaSSLConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG)
//    properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,      KafkaSSLConfig.SSL_KEYSTORE_LOCATION_CONFIG)
//    properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,      KafkaSSLConfig.SSL_KEYSTORE_PASSWORD_CONFIG)
//    properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,           KafkaSSLConfig.SSL_KEY_PASSWORD_CONFIG)


    /*
    Create required topics to save analyses.
     */
    val err = createKafkaTopic()


    // If required topics are  not created or present in broker we cannot start application
    if (err > 0) {
      // logger.error(s"Not all topic created or already exists.")
      System.exit( err )
    }


    // define builder
    val builder: StreamsBuilder = new StreamsBuilder()



    /*
    Building topology
     */



    // pool stream with sms. key is null
    val smsStream: KStream[Nulll, Sms] = builder.stream( smsInputTopicName )(Consumed `with` (stringSerde, smsSerde))




    // pool data with users and his status
    // if user has "false" as a value this means protecting service is inactive
    // if user has null or user is not present then service is active
    val userTable: GlobalKTable[UserNum, UserStatusOrNull] = builder.globalTable(
      userStatusTopicName,
      Materialized.as("user_table")(stringSerde,stringSerde)
    )(Consumed `with`(stringSerde, stringSerde))




    // pool data with uri and its confidence level
    val uriTable: GlobalKTable[Uri, Confidence] =
      builder.globalTable(
        uriConfidenceTopicName,
        Materialized.as("uri_table")(stringSerde,stringSerde)
      )(Consumed `with`(stringSerde, stringSerde))


    val uriToCheckStream: KStream[Uri, Uri] = builder.stream( uriToCheckTopicName )(Consumed `with` (stringSerde, stringSerde))


    val smsWithManyUriStream: KStream[Sms, UriList] = builder.stream( smsWithManyUriTopicName )(Consumed `with` (smsSerde, uriListSerde))


    val uriSearcher = new UriSearcher



    /*
    All split() methods must be replaced by filters due to exception:

    Exception in thread "main" java.util.NoSuchElementException: key not found: sms_with_uri
     */

//    val splitSmsWithUriOrNot = smsStream.split( Named.as("split_sms_uri_or_not") ) // Named.as("split_sms_uri_or_not")
//      .branch((nulll, sms) => {
//        val uriList = uriSearcher.search(sms.message)
//        uriList.nonEmpty
//      }, Branched.as("sms_with_uri"))
//      .branch((nulll, sms) => {
//        val uriList = uriSearcher.search(sms.message)
//        uriList.isEmpty
//      }, Branched.as("sms_without_uri"))
//      .noDefaultBranch()
//    val smsWithUri: KStream[Nulll, Sms] = splitSmsWithUriOrNot.apply("sms_with_uri")
//    val smsWithoutUri: KStream[Nulll, Sms] = splitSmsWithUriOrNot.apply("sms_without_uri")


    val smsWithUri: KStream[Nulll, Sms] = smsStream.filter((nulll, sms) => {
      val uriList = uriSearcher.search(sms.message)
      uriList.nonEmpty
    }, Named.as("sms_with_uri"))


    val smsWithoutUri: KStream[Nulll, Sms] = smsStream.filter((nulll, sms) => {
      val uriList = uriSearcher.search(sms.message)
      uriList.isEmpty
    }, Named.as("sms_without_uri"))




//    val splitPhishingService = smsWithoutUri.split( Named.as( "split_phishing_service_turn_on_off_or_do_nothing") )
//      .branch((nulll, sms) => {
//        // recipient must be different from defined in conf
//        sms.recipient != serviceNumber
//      }, Branched.as("service_no_change"))
//      .branch((nulll, sms) => {
//        // recipient number must be equal to service turn on/off number
//        // and message must be stop or start
//        sms.recipient == serviceNumber && (sms.message == "STOP" || sms.message == "START")
//      }, Branched.as("change_service"))
//      .noDefaultBranch()
//
//    val serviceNoChange: KStream[Nulll, Sms] = splitPhishingService.apply("service_no_change")
//    val changeService: KStream[Nulll, Sms] = splitPhishingService.apply("change_service")


    val serviceNoChange: KStream[Nulll, Sms] = smsWithoutUri.filter((nulll, sms) => {
      // recipient must be different from defined in conf
      sms.recipient != serviceNumber
    }, Named.as("service_no_change"))


    val changeService: KStream[Nulll, Sms] = smsWithoutUri.filter((nulll, sms) => {
      // recipient number must be equal to service turn on/off number
      // and message must be stop or start
      sms.recipient == serviceNumber && (sms.message == "STOP" || sms.message == "START")
    }, Named.as("change_service"))



    // we map our sms to user number who want to change service status.
    changeService.map( (nulll,sms) => {
      // null string in value means we activate service and null value delete user from GlobalKTable
      val status: String = if (sms.message == "STOP") "false" else null
      (sms.sender, status)
    }, Named.as("user_status_changes"))
      // and we save changes to user_status topic
      .to( userStatusTopicName )(Produced `with` (stringSerde, stringSerde))



    // back to sms with uri
    val smsUserNum: KStream[Sms, UserNum] = smsWithUri.map( (nulll,sms) => (sms, sms.sender), Named.as("sms_user_num"))



    val checkingUserService: KStream[Sms, UserStatusOrNull] = smsUserNum.leftJoin( userTable )(
      // join using userNum
      (sms, userNum) => userNum,
      // if userStatus is null this means there is no user in table
      // and user has active protection, if user status is "false",
      // this means user has protection turned off
      (userNum, userStatus) => userStatus
    )



//    val splitSmsProtectedOrNot = checkingUserService.split(Named.as( "split_sms_protected_or_not"))
//      .branch((sms, userStatus) => {
//        // if userStatus is null this means user is protected
//        userStatus == null
//      }, Branched.as("protected"))
//      .branch((sms, userStatus) => {
//        // if userNum is not null and is false then user is not protected
//        // remember we only put "false" or null as value to user_status topic
//        userStatus != null && userStatus == "false"
//      }, Branched.as("not_protected"))
//      .noDefaultBranch()
//
//    val smsWithUriProtected: KStream[Sms, Nulll] = splitSmsProtectedOrNot.apply("protected")
//    val smsWithUriNotProtected1: KStream[Sms, UserNum] = splitSmsProtectedOrNot.apply("not_protected")



    val smsWithUriProtected: KStream[Sms, Nulll] = checkingUserService.filter((sms, userStatus) => {
      // if userStatus is null this means user is protected
      userStatus == null
    }, Named.as("protected"))



    val smsWithUriNotProtected1: KStream[Sms, UserStatus] = checkingUserService.filter((sms, userStatus) => {
      // if userNum is not null and is false then user is not protected
      // remember we only put "false" or null as value to user_status topic
      userStatus != null && userStatus == "false"
    }, Named.as("not_protected"))



    val smsWithUriNotProtected2: KStream[Nulll, Sms] = smsWithUriNotProtected1.map(
      (sms, userNum) => (null, sms),
      Named.as("sms_with_uri_not_protected_2")
    )


    val smsNotProtectedAndNoUri: KStream[Nulll, Sms] = smsWithUriNotProtected2.merge( serviceNoChange, Named.as("merge_sms_not_protected_and_no_uri") )


    val uriListInSms: KStream[Sms, UriList] = smsWithUriProtected.map((sms, nulll) => {
      val list: List[String] = uriSearcher.search( sms.message )
      val uriList: UriList = UriList(list)
      (sms, uriList )
    }, Named.as("uri_list_in_sms"))


    val uriStream: KStream[Uri, Uri] = smsUserNum.flatMap(
      (sms, userNumm) => uriSearcher.search(sms.message).map(uri => (uri,uri)),
      Named.as("uri_stream")
    )


    val mergedUriStream: KStream[Uri, Uri] = uriToCheckStream.merge( uriStream, Named.as("merge_uri_stream") )

    val serviceCaller = new PhishingApiCaller

    val uriWithConfidenceOrNull: KStream[Uri, ConfidenceOrNull]  = mergedUriStream.leftJoin( uriTable )(
      (uri,urii) => uri,
      (uri, confidenceOrNulll) => confidenceOrNulll
    )
      // we filter only uri which are not contained in our uriTable (GlobalKTable)
      .filter((uri, confidenceOrNull) => confidenceOrNull == null, Named.as("sms_filtered_before_api_call"))
      // most blocking operation. We call phishingApi for Confidence level of uri
      .map((uri, nulll) => {
        // val serviceCaller = new PhishingApiCaller
        val confidenceOrNull = serviceCaller.check(uri, phishingService)
        (uri, confidenceOrNull)
      }, Named.as("uri_with_confidence_or_not"))


    val mergedSmsWithUriList: KStream[Sms, UriList] = smsWithManyUriStream.merge(uriListInSms, Named.as("merged_sms_with_uri_list"))


    val smsConfidenceUriList: KStream[Sms, ConfidenceAndUriList] = mergedSmsWithUriList.leftJoin( uriTable )(
      // we join via uri so this is why we extract head of list.
      (sms, uriList) => uriList.list.head,
      // confidence below may be null if head of uri list is absent in uriTable
      // this means that we need to check the confidence of this uri in future
      (uriList, confidence) => ConfidenceAndUriList(confidence, uriList)
    )


    // now i split if confidence level is secure, unsecure or null
//    val splitSmsConfidenceUriList = smsConfidenceUriList.split(Named.as("split_sms_confidence_uri_list"))
//      .branch((sms, confidenceAndUriList) => {
//        // we check if confidence of uriList is in uriTable
//        // if is null we need to call phishing APi
//        confidenceAndUriList.confidence == null || confidenceAndUriList.confidence == CONFIDENCE_LEVEL_UNSPECIFIED
//      }, Branched.as("confidence_is_null"))
//      .branch((sms, confidenceAndUriList) => {
//        // we check if confidence level is secure
//        secureConfidenceLevels.contains( confidenceAndUriList.confidence )
//      }, Branched.as("confidence_is_secure"))
//      .branch((sms, confidenceAndUriList) => {
//        // we check if confidence level is not secure
//        notSecureConfidenceLevels.contains( confidenceAndUriList.confidence )
//      }, Branched.as("confidence_is_not_secure"))
//      .noDefaultBranch()
//
//    val nullConfidence: KStream[Sms, ConfidenceAndUriList] = splitSmsConfidenceUriList.apply("confidence_is_null")
//    val secureConfidence: KStream[Sms, ConfidenceAndUriList] = splitSmsConfidenceUriList.apply("confidence_is_secure")
//    val notSecureConfidence: KStream[Sms, ConfidenceAndUriList] = splitSmsConfidenceUriList.apply("confidence_is_not_secure")



    // now i split if confidence level is secure, unsecure or null

    val nullConfidence: KStream[Sms, ConfidenceAndUriList] = smsConfidenceUriList.filter((sms, confidenceAndUriList) => {
      // we check if confidence of uriList is in uriTable
      // if is null we need to call phishing APi
      confidenceAndUriList.confidence == null || confidenceAndUriList.confidence == CONFIDENCE_LEVEL_UNSPECIFIED
    }, Named.as("confidence_is_null"))


    val secureConfidence: KStream[Sms, ConfidenceAndUriList] = smsConfidenceUriList.filter((sms, confidenceAndUriList) => {
      // we check if confidence level is secure
      secureConfidenceLevels.contains( confidenceAndUriList.confidence )
    }, Named.as("confidence_is_secure"))


    val notSecureConfidence: KStream[Sms, ConfidenceAndUriList] = smsConfidenceUriList.filter((sms, confidenceAndUriList) => {
      // we check if confidence level is not secure
      notSecureConfidenceLevels.contains(confidenceAndUriList.confidence)
    }, Named.as("confidence_is_not_secure"))



    val uriChecked: KStream[Sms, ConfidenceAndUriList] = nullConfidence.map((sms, confidenceAndUriList) => {
       // val serviceCaller = new PhishingApiCaller
      val confidenceOrNull = serviceCaller.check( confidenceAndUriList.uriList.list.head, phishingService)
      (sms, confidenceAndUriList.copy(confidence = confidenceOrNull) )
    }, Named.as("uri_checked"))


//    val splitCheckedUri = uriChecked.split( Named.as("split_uri_checked") )
//      .branch((sms, confidenceAndUriList) => {
//        // we check if confidence of head of uriList is secure or null if service is unavailable
//        confidenceAndUriList.confidence == null || confidenceAndUriList.confidence == CONFIDENCE_LEVEL_UNSPECIFIED || secureConfidenceLevels.contains(confidenceAndUriList.confidence)
//      }, Branched.as("confidence_is_secure_or_null"))
//      .branch((sms, confidenceAndUriList) => {
//        // we check if confidence level is not secure
//        notSecureConfidenceLevels.contains(confidenceAndUriList.confidence)
//      }, Branched.as("confidence_is_not_secure"))
//      .noDefaultBranch()

//    val uriCheckedButNotSecure: KStream[Sms, ConfidenceAndUriList] = splitCheckedUri.apply("confidence_is_not_secure")
//    val checkedUriIsSecureOrNull: KStream[Sms, ConfidenceAndUriList] = splitCheckedUri.apply("confidence_is_secure_or_null")



    // this means that such sms is dangerous and because user has active protection
    // we cannot process further this sms to end user but we can check rest of uri from list if any
    // and build our uriTable further (but only if urilist has more then one uri, otherwise
    // we do not have what to process because head of list was processed)
    val checkedUriIsSecureOrNull: KStream[Sms, ConfidenceAndUriList] = uriChecked.filter((sms, confidenceAndUriList) => {
      // we check if confidence of head of uriList is secure or null if service is unavailable
      confidenceAndUriList.confidence == null || confidenceAndUriList.confidence == CONFIDENCE_LEVEL_UNSPECIFIED || secureConfidenceLevels.contains(confidenceAndUriList.confidence)
    }, Named.as("confidence_is_secure_or_null"))


    val uriCheckedButNotSecure: KStream[Sms, ConfidenceAndUriList] = uriChecked.filter((sms, confidenceAndUriList) => {
      // we check if confidence level is not secure
      notSecureConfidenceLevels.contains(confidenceAndUriList.confidence)
    }, Named.as("sms_confidence_is_not_secure"))



    val mergedNotSecureConfidence: KStream[Sms, ConfidenceAndUriList] = uriCheckedButNotSecure.merge( notSecureConfidence,
      Named.as("merged_not_secure_confidence")
    ) // OK


    // here we remove head of list because was checked
    val uriListToCheck: KStream[Uri, Uri] = mergedNotSecureConfidence.flatMap(
      (sms, confidenceAndUriList) => confidenceAndUriList.uriList.list.tail.map(uri => (uri,uri)),
      Named.as("uri_list_to_check")
    ) // OK

    // and subsequently send it to uri_to_check topic
    uriListToCheck.to( uriToCheckTopicName ) (Produced.`with`(stringSerde, stringSerde))


    // merging sms with secure first uri
    val checkedUriSecured: KStream[Sms, ConfidenceAndUriList] = secureConfidence.merge(checkedUriIsSecureOrNull, Named.as("checked_secure_uri")) //ok

    val checkedUriDeleted:  KStream[Sms, UriList] = checkedUriSecured.mapValues(
      (sms, confidenceAndUriList) => UriList( confidenceAndUriList.uriList.list.tail ),
      Named.as("checked_uri_deleted")
    ) //ok


//    val splitUriListEmptyOrNot = checkedUriDeleted.split( Named.as("uri_list_empty_or_not") )
//      .branch((sms, uriList) => uriList.list.nonEmpty , Branched.as("non_empty_uri_list"))
//      .branch((sms, uriList) => uriList.list.isEmpty  , Branched.as("empty_uri_list"))
//      .noDefaultBranch()
//
//    val nonEmptyUriList: KStream[Sms, UriList] = splitUriListEmptyOrNot.apply("non_empty_uri_list")
//    val emptyUriList: KStream[Sms, UriList]    = splitUriListEmptyOrNot.apply("empty_uri_list")


    val nonEmptyUriList: KStream[Sms, UriList] = checkedUriDeleted.filter((sms, uriList) => uriList.list.nonEmpty, Named.as("non_empty_uri_list")) //ok
    val emptyUriList: KStream[Sms, UriList] = checkedUriDeleted.filter((sms, uriList) => uriList.list.isEmpty, Named.as("empty_uri_list")) //ok


    // here we send sms with protection with more than one uri to process rest of uri
    // from uriList (head is processed now)
    nonEmptyUriList.to( smsWithManyUriTopicName )(Produced.`with`(smsSerde, uriListSerde))


    // sms with checked all uri (all safe) and ready to save to sms_output_topic
    val processedSafeSms: KStream[Nulll, Sms] = emptyUriList.map((sms,_) => (null, sms), Named.as("processed_safe_sms"))


    // merging processed sms (with safe uri) with sms not protected and sms without uri.
    val allSmsToSave: KStream[Nulll, Sms] = processedSafeSms.merge( smsNotProtectedAndNoUri , Named.as("all_sms_to_save"))


    // sending processed sms to sms_output_topic
    allSmsToSave.to( smsOutputTopicName )(Produced.`with`(stringSerde,smsSerde))


    val checkedUriWithConfidence: KStream[Uri, Confidence] = checkedUriIsSecureOrNull.map(
      (sms, confidenceAndUriList) => (confidenceAndUriList.uriList.list.head, confidenceAndUriList.confidence),
      Named.as("checked_uri_with_confidence")
    )

    val uriConfidenceToSave: KStream[Uri, Confidence] = checkedUriWithConfidence.merge( uriWithConfidenceOrNull,
      Named.as("merged_uri_confidence_to_save")
    )
    // remove all with null, we do not need them in topic
    .filter((uri, confidence) => confidence != null, Named.as("uri_confidence_to_save_without_null"))


    // and we saving all new checked uris to topic for uriTable
    uriConfidenceToSave.to( uriConfidenceTopicName )(Produced.`with`(stringSerde, stringSerde))




    // we build topology of the streams and tables
    val topology: Topology = builder.build()


    /*
      Main loop of program
    */
    var continue = true

    while (continue) {

      // create KafkaStreams object
      val streams: KafkaStreams = new KafkaStreams(topology, properties)


      // we initialize shutdownhook only once.
      // if initializeShutDownHook then
      Runtime.getRuntime.addShutdownHook(new Thread("closing_stream_thread") {
        override
        def run(): Unit =
          streams.close()
        // logger.warn(s"Streams closed from ShutdownHook.")
      })


      // we starting streams
      try {
        streams.start()
        // logger.trace(s"Streams started.")
      } catch {
        case e: Throwable =>
          // logger.error(s"Error during Streams starting: ${e.toString}.")
          continue = false
          System.exit(333)
      }
      // logger.trace(s"SmsAnalyser started.")
      Thread.sleep(120_000) // two minutes sleep.
      streams.close()
      // logger.trace(s"Streams stopped.")
    }

  }





  /**
   *
   */
  private def createKafkaTopic(): Int = {

    var err = 0

    TopicCreator.createTopic( smsInputTopic ) match {
      case Done(msg) =>
      // logger.info(s"Topic '$userStatusTopicName' created")
      case Error(error) =>
        err += 1
      // logger.error(s"Creation topic '$userStatusTopicName' failed with error: $error")
    }

    TopicCreator.createTopic( userStatusTopic) match {
      case Done(msg) =>
      // logger.info(s"Topic '$userStatusTopicName' created")
      case Error(error) =>
        err += 1
      // logger.error(s"Creation topic '$userStatusTopicName' failed with error: $error")
    }

    TopicCreator.createTopic( uriConfidenceLevelTopic ) match {
      case Done(msg) =>
        // logger.info(s"Topic '$uriConfidenceTopicName' created")
      case Error(error) =>
        err += 1
        // logger.error(s"Creation topic '$uriConfidenceTopicName' failed with error: $error")
    }

    TopicCreator.createTopic( uriToCheckTopic ) match {
      case Done(msg) =>
      // logger.info(s"Topic '$uriConfidenceTopicName' created")
      case Error(error) =>
        err += 1
      // logger.error(s"Creation topic '$uriConfidenceTopicName' failed with error: $error")
    }

    TopicCreator.createTopic( smsWithManyUriTopic ) match {
      case Done(msg) =>
      // logger.info(s"Topic '$userStatusTopicName' created")
      case Error(error) =>
        err += 1
      // logger.error(s"Creation topic '$userStatusTopicName' failed with error: $error")
    }

    TopicCreator.createTopic( smsOutputTopic ) match {
      case Done(msg) =>
      // logger.info(s"Topic '$userStatusTopicName' created")
      case Error(error) =>
        err += 1
      // logger.error(s"Creation topic '$userStatusTopicName' failed with error: $error")
    }

    err
  }



}




/*
all Named.as() names: --> no duplicates

user_table
uri_table
split_sms_uri_or_not
split_phishing_service_turn_on_off_or_do_nothing
user_status_changes
sms_user_num
split_sms_protected_or_not
sms_with_uri_not_protected_2
merge_sms_not_protected_and_no_uri
uri_list_in_sms
uri_stream
merge_uri_stream
uri_with_confidence_or_not
merged_sms_with_uri_list
split_sms_confidence_uri_list
uri_checked
split_uri_checked
merged_not_secure_confidence
merged_uri_confidence_to_save
uri_confidence_to_save_without_null
uri_list_to_check
checked_secure_uri
checked_uri_deleted
uri_list_empty_or_not
processed_safe_sms
all_sms_to_save
checked_uri_with_confidence
uri_confidence_to_save
filtered_before_api_call
 */

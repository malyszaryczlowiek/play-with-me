package io.github.malyszaryczlowiek
package util

class UriSearcher {

  /**
   * regex do dzielenia stringa po whitespace
   */
  private val regexSplitter = s"\\s".r



  /**
   *  można dodać więcej albo zdefiniować wczytywanie ich z application.conf
   */
  private val protocols = List("http", "https")



  /**
   * metoda pobiera jako argument wiadomość sms i wyszukuje w jej treści linki,
   * następnie znalezione linki są zwracane w postaci listy
   *
   * @param smsMessage
   * @return lista linków do weryfikacji, jeśli nie ma, to list będzie pusta
   */
  def search(smsMessage: String): List[String] = {
    val split: List[String] = regexSplitter.split(smsMessage).toList
    protocols.flatMap(protocol => split.filter(s => s.contains(protocol)).map(str => {
      val index = str.indexOf(protocol)
      str.substring(index)
    })
    ).distinct
  }



}

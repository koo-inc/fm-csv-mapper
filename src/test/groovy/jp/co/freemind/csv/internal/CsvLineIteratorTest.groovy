package jp.co.freemind.csv.internal

import spock.lang.Specification

import java.nio.charset.Charset
/**
 * Created by kakusuke on 15/07/24.
 */
class CsvLineIteratorTest extends Specification {
  def "test iterator"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","bbb"\n"ccc","ddd"'.bytes)
    def itr = new CsvLineIterator(new CsvScanner(testData, Charset.forName("UTF-8"), '"' as char, '\\' as char))
    expect:
    assert itr.hasNext()
    assert itr.next() == '"aaa","bbb"'
    and:
    assert itr.hasNext()
    assert itr.next() == '"ccc","ddd"'
    and:
    assert !itr.hasNext()
  }
}

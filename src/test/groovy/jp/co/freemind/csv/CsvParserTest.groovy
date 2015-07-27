package jp.co.freemind.csv
import jp.co.freemind.csv.data.Sample
import jp.co.freemind.csv.internal.CsvParser
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
/**
 * Created by kakusuke on 15/07/24.
 */
class CsvParserTest extends Specification {
  @Shared
  def parser = new CsvParser(CsvFormatter.builder(Sample).with(Sample.CsvFormat).build())

  def "test formatter"() {
    when:
    def parsed = parser.parse(new ByteArrayInputStream('a,1,true'.getBytes("MS932"))).collect(Collectors.toList())

    then:
    assert parsed == [new Sample(a: "a", b: true, c: 1)]
  }
}

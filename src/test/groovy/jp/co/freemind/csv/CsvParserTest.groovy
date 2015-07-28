package jp.co.freemind.csv
import jp.co.freemind.csv.data.Sample
import jp.co.freemind.csv.exception.FieldFormatException
import jp.co.freemind.csv.internal.CsvParser
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
/**
 * Created by kakusuke on 15/07/24.
 */
class CsvParserTest extends Specification {
  @Shared
  def CsvParser<Sample> parser = new CsvParser<Sample>(CsvFormatter.builder(Sample).with(Sample.CsvFormat).build())

  def "test formatter"() {
    when:
    def parsed = parser.parse(new ByteArrayInputStream('a,1,true'.getBytes("MS932"))).collect(Collectors.toList())

    then:
    assert parsed == [new Sample(a: "a", b: true, c: 1)]
  }

  def "test one error for each lines"() {
    when:
    def stream = parser.parse(new ByteArrayInputStream('a,a,true\nb,1,b'.getBytes("MS932")))
    def parsed = stream.collect(Collectors.toList())
    stream.close()

    then:
    def e = thrown(FieldFormatException)
    assert e.locations == [new Location(1, OptionalInt.of(2)), new Location(2, OptionalInt.of(3))]
    assert parsed == [new Sample(a: "a", b: true, c: null), new Sample(a: "b", b: null, c: 1)]
  }

  def "test two error on one line"() {
    when:
    def stream = parser.parse(new ByteArrayInputStream('a,a,a'.getBytes("MS932")))
    def parsed = stream.collect(Collectors.toList())
    stream.close()

    then:
    def e = thrown(FieldFormatException)
    assert e.locations == [new Location(1, OptionalInt.of(2)), new Location(1, OptionalInt.of(3))]
    assert parsed == [new Sample(a: "a", b: null, c: null)]
  }
}

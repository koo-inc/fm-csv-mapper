package jp.co.freemind.csv.internal
import jp.co.freemind.csv.CsvFormatter
import jp.co.freemind.csv.Location
import jp.co.freemind.csv.data.Sample
import jp.co.freemind.csv.data.SampleNestedObject
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
    def sniffer = new CsvErrorSniffer()
    def parsed = parser.parse(new ByteArrayInputStream('a,1,true'.getBytes("UTF-8")), sniffer).collect(Collectors.toList())

    then:
    assert !sniffer.hasError()
    assert parsed == [new Sample(a: "a", b: true, c: 1)]
  }

  def "test one error for each lines"() {
    when:
    def sniffer = new CsvErrorSniffer()
    def stream = parser.parse(new ByteArrayInputStream('a,a,true\nb,1,b'.getBytes("UTF-8")), sniffer)
    def parsed = stream.collect(Collectors.toList())
    stream.close()

    then:
    assert sniffer.locations == [new Location(1, OptionalInt.of(2)), new Location(2, OptionalInt.of(3))] as Set
    assert sniffer.hasError()
    assert parsed == [new Sample(a: "a", b: true, c: null), new Sample(a: "b", b: null, c: 1)]
  }

  def "test two error on one line"() {
    when:
    def sniffer = new CsvErrorSniffer()
    def stream = parser.parse(new ByteArrayInputStream('a,a,a'.getBytes("UTF-8")), sniffer)
    def parsed = stream.collect(Collectors.toList())
    stream.close()

    then:
    assert sniffer.locations == [new Location(1, OptionalInt.of(2)), new Location(1, OptionalInt.of(3))] as Set
    assert sniffer.hasError()
    assert parsed == [new Sample(a: "a", b: null, c: null)]
  }

  def "test with headers"() {
    given:
    def CsvParser<Sample> parser = new CsvParser<Sample>(CsvFormatter.builder(Sample).with(Sample.CsvFormat).withHeaders().build())

    when:
    def sniffer = new CsvErrorSniffer()
    def parsed = parser.parse(new ByteArrayInputStream('foo,bar,buz\r\na,a,a'.getBytes("UTF-8")), sniffer).collect(Collectors.toList())

    then:
    assert sniffer.locations == [new Location(2, OptionalInt.of(2)), new Location(2, OptionalInt.of(3))] as Set
    assert sniffer.hasError()
    assert parsed == [new Sample(a: "a", b: null, c: null)]
  }

  def "test with nullValue"() {
    given:
    def CsvParser<Sample> parser = new CsvParser<Sample>(CsvFormatter.builder(Sample).with(Sample.CsvFormat).nullValue('NULL').build())

    when:
    def sniffer = new CsvErrorSniffer()
    def parsed = parser.parse(new ByteArrayInputStream('NULL,NULL,NULL'.getBytes("UTF-8")), sniffer).collect(Collectors.toList())

    then:
    assert parsed == [new Sample(a: null, b: null, c: null)]
    assert !sniffer.hasError()
  }

  def "test with linebreaks"() {
    given:
    def CsvParser<Sample> parser = new CsvParser<Sample>(CsvFormatter.builder(Sample).with(Sample.CsvFormat).build())

    when:
    def sniffer = new CsvErrorSniffer()
    def parsed = parser.parse(new ByteArrayInputStream('"a\na",,\r\n"b\r\nb",,\r\n"c\rc",,'.getBytes("UTF-8")), sniffer).collect(Collectors.toList())

    then:
    assert parsed == [new Sample(a: "a\na", b: null, c:null), new Sample(a: "b\r\nb", b: null, c: null), new Sample(a: "c\rc", b: null, c: null)]
    assert !sniffer.hasError()
  }

  def "test with orderBy"() {
    given:
    def CsvParser<SampleNestedObject> parser = new CsvParser<SampleNestedObject>(CsvFormatter.builder(SampleNestedObject)
      .orderBy("a.c", "a.d", "b[0].c", "b[0].d", "b[1].c", "b[1].d").build())

    when:
    def sniffer = new CsvErrorSniffer()
    def parsed = parser.parse(new ByteArrayInputStream('1,2,3,4,5,6'.getBytes("UTF-8")), sniffer).collect(Collectors.toList())

    then:
    assert parsed == [new SampleNestedObject(
      a: new SampleNestedObject.Nested(c: '1', d: '2'),
      b: [new SampleNestedObject.Nested(c: '3', d: '4'), new SampleNestedObject.Nested(c: '5', d: '6')]
    )]
    assert !sniffer.hasError()
  }

}

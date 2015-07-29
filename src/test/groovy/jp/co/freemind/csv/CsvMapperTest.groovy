package jp.co.freemind.csv
import jp.co.freemind.csv.data.Sample
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Created by kakusuke on 15/07/29.
 */
class CsvMapperTest extends Specification {
  @Shared
  def mapper = new CsvMapper<Sample>(Sample, Sample.CsvFormat)

  def "test reader"() {
    given:
    def reader = mapper.createReader()

    when:
    def data = reader.read(new ByteArrayInputStream('"あああ",1,true\r\n"いいい","ううう","えええ"\r\n,,'.getBytes("MS932"))).collect(Collectors.toList())

    then:
    assert data == [new Sample(a: "あああ", b: true, c: 1), new Sample(a: "いいい", b: null, c: null), new Sample(a: null, b: null, c: null)]
    and:
    assert reader.errorLocations == [new Location(2, OptionalInt.of(2)), new Location(2, OptionalInt.of(3))] as Set
  }

  def "CreateWriter"() {
    given:
    def writer = mapper.createWriter()

    when:
    def os = new ByteArrayOutputStream()
    Stream.of(new Sample(a: "あああ", b: true, c: 1)).forEach(writer.writeTo(os))

    then:
    assert os.toString("MS932") == '"あああ","1","true"'
  }
}

package jp.co.freemind.csv.internal

import jp.co.freemind.csv.CsvFormatter
import jp.co.freemind.csv.data.Sample
import spock.lang.Specification

import java.util.stream.Stream
/**
 * Created by kakusuke on 15/07/28.
 */
class CsvBuilderTest extends Specification {
  def "test builder"() {
    given:
    def builder = new CsvBuilder<Sample>(Sample, Sample.CsvFormat)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あ\"ああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"a","1","true"\r\n"あ\\"ああ",,'
  }

  def "test builder with another column separator"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).columnSeparator('\t' as char).build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あ\"ああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"a"\t"1"\t"true"\r\n"あ\\"ああ"\t\t'
  }

  def "test builder with another quote char"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).quoteChar('\'' as char).build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: null, b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("MS932") == "'a','1','true'\r\n,,"
  }

  def "test builder with another line break"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).lineBreak(CsvFormatter.LineBreak.CR).build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: null, b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("MS932") == '"a","1","true"\r,,'
  }

  def "test builder with another charset"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).charset("EUC-JP").build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("EUC-JP") == '"a","1","true"\r\n"あああ",,'
  }

  def "test builder with header"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).withHeaders().build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"foo","bar","buz"\r\n"a","1","true"\r\n"あああ",,'
  }

  def "test builder with custom header"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).withHeaders().build()
    def builder = new CsvBuilder<Sample>(formatter).withHeader("hoge", "fuga")
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"hoge","fuga"\r\n"a","1","true"\r\n"あああ",,'
  }

  def "test builder without header and with custom header"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).build()
    def builder = new CsvBuilder<Sample>(formatter).withHeader("hoge", "fuga")
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"hoge","fuga"\r\n"a","1","true"\r\n"あああ",,'
  }

  def "test builder with header and no data"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).withHeaders().build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.empty()

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"foo","bar","buz"'
  }

  def "test builder with custom header and no data"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).withHeaders().build()
    def builder = new CsvBuilder<Sample>(formatter).withHeader("hoge", "fuga")
    def os = new ByteArrayOutputStream()
    def stream = Stream.empty()

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"hoge","fuga"'
  }

  def "test builder with nullValue"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).nullValue("NULL").build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"a","1","true"\r\n"あああ",NULL,NULL'
  }

  def "test builder with another escape"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).escapeChar('"' as char).build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あ\"ああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '"a","1","true"\r\n"あ""ああ",,'
  }

  def "test builder with another escape and another quote"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).escapeChar("'" as char).quoteChar("'" as char).build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あ'ああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == "'a','1','true'\r\n'あ''ああ',,"
  }

  def "test builder with BOM and utf-8"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).withBom().charset("UTF-8").build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("UTF-8") == '\uFEFF"a","1","true"\r\n"あああ",,'
  }

  def "test builder with BOM and not uft-8"() {
    given:
    def CsvFormatter<Sample> formatter = CsvFormatter.builder(Sample).with(Sample.CsvFormat).withBom().charset("EUC-JP").build()
    def builder = new CsvBuilder<Sample>(formatter)
    def os = new ByteArrayOutputStream()
    def stream = Stream.of(new Sample(a: "a", b: true, c: 1), new Sample(a: "あああ", b: null, c: null))

    when:
    stream.forEach(builder.writeTo(os))

    then:
    assert os.toString("EUC-JP") == '"a","1","true"\r\n"あああ",,'
  }

}

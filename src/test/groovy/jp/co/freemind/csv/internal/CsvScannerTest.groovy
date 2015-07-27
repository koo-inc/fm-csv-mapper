package jp.co.freemind.csv.internal
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.Charset
/**
 * Created by kakusuke on 15/07/23.
 */
class CsvScannerTest extends Specification {
  @Shared
  def UTF_8 = Charset.forName("UTF-8")

  def "test nextLine with line feed"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","bbb"\n"ccc","ddd"\n"ee\ne","f\nff"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"aaa","bbb"';
    and:
    assert scanner.nextLine() == '"ccc","ddd"';
    and:
    assert scanner.nextLine() == '"ee\ne","f\nff"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with carriage return"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","bbb"\r"ccc","ddd"\r"ee\re","f\rff"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"aaa","bbb"';
    and:
    assert scanner.nextLine() == '"ccc","ddd"';
    and:
    assert scanner.nextLine() == '"ee\re","f\rff"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with CRLF"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","bbb"\r\n"ccc","ddd"\r\n"ee\r\ne","f\r\nff"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"aaa","bbb"';
    and:
    assert scanner.nextLine() == '"ccc","ddd"';
    and:
    assert scanner.nextLine() == '"ee\r\ne","f\r\nff"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with single quote"() {
    given:
    def testData = new ByteArrayInputStream("'aaa','bbb'\n'ccc','ddd'\n'ee\ne','f\nff'".bytes)
    def scanner = new CsvScanner(testData, UTF_8, "'" as char, '\\' as char)

    expect:
    assert scanner.nextLine() == "'aaa','bbb'";
    and:
    assert scanner.nextLine() == "'ccc','ddd'";
    and:
    assert scanner.nextLine() == "'ee\ne','f\nff'";
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with normal escaped quote"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","b\\"bb"\n"ccc","ddd"\n"ee\ne","f\nff"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"aaa","b\\"bb"';
    and:
    assert scanner.nextLine() == '"ccc","ddd"';
    and:
    assert scanner.nextLine() == '"ee\ne","f\nff"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with special escaped quote"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","b/"bb"\n"ccc","ddd"\n"ee\ne","f\nff"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '//' as char)

    expect:
    assert scanner.nextLine() == '"aaa","b/"bb"';
    and:
    assert scanner.nextLine() == '"ccc","ddd"';
    and:
    assert scanner.nextLine() == '"ee\ne","f\nff"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine over lines with last line break"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","bbb"\n'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"aaa","bbb"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with multi byte chars"() {
    given:
    def testData = new ByteArrayInputStream('"あああ","ｧｧｧ"\nいい'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"あああ","ｧｧｧ"';
    and:
    assert scanner.nextLine() == 'いい';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with MS932 chars"() {
    given:
    def charset = Charset.forName("MS932")
    def testData = new ByteArrayInputStream('"あああ","ｧｧｧ"\r\nいい'.getBytes(charset))
    def scanner = new CsvScanner(testData, charset, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"あああ","ｧｧｧ"';
    and:
    assert scanner.nextLine() == 'いい';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with UTF-8 chars with BOM"() {
    given:
    def charset = Charset.forName("UTF-8")
    def bom = [0xEF, 0xBB, 0xBF] as byte[]
    def bytes = '"あああ","ｧｧｧ"\r\nいい'.getBytes(charset)
    def bytesWithBOM = new byte[bom.length + bytes.length]
    System.arraycopy(bom, 0, bytesWithBOM, 0, bom.length);
    System.arraycopy(bytes, 0, bytesWithBOM, bom.length, bytes.length);
    def testData = new ByteArrayInputStream(bytesWithBOM)
    def scanner = new CsvScanner(testData, charset, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"あああ","ｧｧｧ"';
    and:
    assert scanner.nextLine() == 'いい';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with empty line"() {
    given:
    def testData = new ByteArrayInputStream('"aaa","bbb"\n\n"ccc","ddd"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"aaa","bbb"';
    and:
    assert scanner.nextLine() == '';
    and:
    assert scanner.nextLine() == '"ccc","ddd"';
    and:
    assert scanner.nextLine() == null;
  }

  def "test nextLine with escaped char"() {
    given:
    def testData = new ByteArrayInputStream('"\\n","\\\\n","\\\\\\"","\\"bbb"\n"\\\r\n","ddd"'.bytes)
    def scanner = new CsvScanner(testData, UTF_8, '"' as char, '\\' as char)

    expect:
    assert scanner.nextLine() == '"\\n","\\\\n","\\\\\\"","\\"bbb"';
    and:
    assert scanner.nextLine() == '"\\\r\n","ddd"';
    and:
    assert scanner.nextLine() == null;
  }
}

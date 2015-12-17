package jp.co.freemind.csv.internal

import spock.lang.Specification
/**
 * Created by kakusuke on 15/07/24.
 */
class CsvLineParserTest extends Specification {

  def "test parse line"() {
    given:
    def parser = new CsvLineParser('"' as char, '\\' as char, ',' as char)

    when:
    def actual = parser.parse(line)
    then:
    assert actual == expected

    where:
    line             | expected
    '"aaa"'          | ["aaa"]
    '"aaa","bbb"'    | ["aaa", "bbb"]
    '"aaa" , "bbb"'  | ["aaa", "bbb"]
    '"aa\na","bbb"'  | ["aa\na", "bbb"]
    'aaa,"bbb"'      | ["aaa", "bbb"]
    '"aaa",bbb'      | ["aaa", "bbb"]
    '"a,a,a",bbb'    | ["a,a,a", "bbb"]
    '"\\"","\\\\"'   | ['"', '\\']
  }

  def "test parse line with alternative quote char"() {
    given:
    def parser = new CsvLineParser("^" as char, "\\" as char, ',' as char)

    when:
    def actual = parser.parse(line)
    then:
    assert actual == expected

    where:
    line             | expected
    "^aaa^"          | ['aaa']
    "^aaa^,^bbb^"    | ['aaa', 'bbb']
    "^aaa^ , ^bbb^"  | ['aaa', 'bbb']
    "^aa\na^,^bbb^"  | ['aa\na', 'bbb']
    "aaa,^bbb^"      | ['aaa', 'bbb']
    "^aaa^,bbb"      | ['aaa', 'bbb']
    "^a,a,a^,bbb"    | ['a,a,a', 'bbb']
    "^\\^^,^\\\\^"   | ["^", "\\"]
  }

  def "test parse line with alternative escape char"() {
    given:
    def parser = new CsvLineParser('"' as char, '|' as char, ',' as char)

    when:
    def actual = parser.parse(line)
    then:
    assert actual == expected

    where:
    line             | expected
    '"aaa"'          | ["aaa"]
    '"aaa","bbb"'    | ["aaa", "bbb"]
    '"aaa" , "bbb"'  | ["aaa", "bbb"]
    '"aa\na","bbb"'  | ["aa\na", "bbb"]
    'aaa,"bbb"'      | ["aaa", "bbb"]
    '"aaa",bbb'      | ["aaa", "bbb"]
    '"a,a,a",bbb'    | ["a,a,a", "bbb"]
    '"|"","||"'      | ['"', '|']
  }

  def "test parse line with other field separator"() {
    given:
    def parser = new CsvLineParser('"' as char, '\\' as char, '\t' as char)

    when:
    def actual = parser.parse(line)
    then:
    assert actual == expected

    where:
    line             | expected
    '"aaa"'          | ["aaa"]
    '"aaa"\t"bbb"'   | ["aaa", "bbb"]
    '"aaa" \t "bbb"' | ["aaa", "bbb"]
    '"aa\na"\t"bbb"' | ["aa\na", "bbb"]
    'aaa\t"bbb"'     | ["aaa", "bbb"]
    '"aaa"\tbbb'     | ["aaa", "bbb"]
    '"a\ta\ta"\tbbb' | ["a\ta\ta", "bbb"]
    '"\\""\t"\\\\"'  | ['"', '\\']
  }

  def "test parse line with double quote escape char"() {
    given:
    def parser = new CsvLineParser('"' as char, '"' as char, ',' as char)

    when:
    def actual = parser.parse(line)
    then:
    assert actual == expected

    where:
    line             | expected
    '"aaa"'          | ["aaa"]
    '"aaa","bbb"'    | ["aaa", "bbb"]
    '"aaa" , "bbb"'  | ["aaa", "bbb"]
    '"aa\na","bbb"'  | ["aa\na", "bbb"]
    'aaa,"bbb"'      | ["aaa", "bbb"]
    '"aaa",bbb'      | ["aaa", "bbb"]
    '"a,a,a",bbb'    | ["a,a,a", "bbb"]
    '"""",""""""'    | ['"', '""']
  }

}

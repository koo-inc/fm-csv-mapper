package jp.co.freemind.csv.internal

import org.codehaus.jparsec.error.ParserException
import spock.lang.Shared
import spock.lang.Specification

import static jp.co.freemind.csv.internal.PathParser.*;

/**
 * Created by kakusuke on 15/11/05.
 */
class PathParserTest extends Specification {
  @Shared
  def parser = new PathParser()

  def "test parse"() {
    expect:
    assert parser.parse(path) == expected

    where:
    path             || expected
    'foo'            || [obj('foo')]
    'foo.bar'        || [obj('foo'), obj('bar')]
    'foo[0]'         || [obj('foo'), array('0')]
    'foo . bar '     || [obj('foo '), obj(' bar ')]
    'foo.bar[1].buz' || [obj('foo'), obj('bar'), array('1'), obj('buz')]
  }

  def "test parse error"() {
    when:
    parser.parse(path)

    then:
    thrown(ParserException)

    where:
    path << ['.foo', '["bar"]']
  }

  def obj(value) {
    ObjectPathSegment.of(value)
  }
  def array(value) {
    ArrayPathSegment.of(value)
  }
}

package jp.co.freemind.csv.internal

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by kakusuke on 15/11/06.
 */
class FlattenSerializerTest extends Specification {
  @Shared
  def encoder = new ObjectMapper().registerModule(new FlattenModule())
  def decoder = new ObjectMapper()

  def "test serialize"() {
    expect:
    assert convert(inMap) == outMap

    where:
    inMap                                              || outMap
    [foo: '1', bar: '2']                               || [foo: '1', bar: '2']
    ['foo.bar': '1']                                   || [foo: [bar: '1']]
    ['foo.bar.buz': '1', 'bar': '2']                   || [foo: [bar: [buz: '1']], bar: '2']
    ['foo[0]': '1', 'foo[1]': '2']                     || [foo: ['1', '2']]
    ['foo[1]': '1']                                    || [foo: [null, '1']]
    ['foo[0].bar': '1', 'foo[1].bar': '2']             || [foo: [[bar: '1'], [bar: '2']]]
    ['foo[1].bar': null]                               || [foo: [[:], [bar: null]]]
    ['foo[0][0]': '1', 'foo[0][1].bar': '2']           || [foo: [['1', [bar: '2']]]]
    ['foo[1].bar': '1', 'foo[0].bar': '2', 'bar': '3'] || [foo: [[bar: '2'], [bar: '1']], bar: '3']
    ['foo[1][0]': '1']                                 || [foo: [[], ['1']]]
  }

  def convert(value) {
    decoder.readValue(encoder.writeValueAsString(value), Map)
  }
}

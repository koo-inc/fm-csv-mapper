package jp.co.freemind.csv.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import groovy.transform.EqualsAndHashCode;

/**
 * Created by kakusuke on 15/07/27.
 */
@EqualsAndHashCode
public class Sample {
  String a
  Boolean b
  Integer c

  @JsonPropertyOrder(["foo", "bar", "buz"])
  static class CsvFormat {
    @JsonProperty("foo")
    String a
    @JsonProperty("buz")
    Boolean b
    @JsonProperty("bar")
    Integer c
  }
}

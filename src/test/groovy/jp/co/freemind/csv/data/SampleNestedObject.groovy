package jp.co.freemind.csv.data
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
/**
 * Created by kakusuke on 15/07/27.
 */
@EqualsAndHashCode
@ToString
public class SampleNestedObject {
  Nested a
  List<Nested> b

  @EqualsAndHashCode
  @ToString
  static class Nested {
    String c
    String d
  }

  static class CsvFormat {
    @JsonProperty("foo")
    Nested a
    @JsonProperty("bar")
    List<Nested> b

    static class Nested {
      @JsonProperty("buz")
      String c
      @JsonProperty("qux")
      String d
    }
  }
}

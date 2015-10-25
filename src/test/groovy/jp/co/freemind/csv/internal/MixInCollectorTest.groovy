package jp.co.freemind.csv.internal

import spock.lang.Shared
import spock.lang.Specification

import static MixInCollector.*

/**
 * Created by kakusuke on 15/10/24.
 */
class MixInCollectorTest extends Specification {
  @Shared
  def collector = new MixInCollector();

  def "test plain class"() {
    when:
    def mixins = collector.collect(A, B)

    then:
    assert mixins == [new Pair(A, B)]
  }

  def "test nested class"() {
    when:
    def mixins = collector.collect(A, C)

    then:
    assert mixins == [new Pair(A, C), new Pair(A.Child, C.ChildMixin)]
  }

  def "test generic type"() {
    when:
    def mixins = collector.collect(A, D)

    then:
    assert mixins == [new Pair(A, D), new Pair(A.Item, D.ItemMixin)]
  }

  def "test nested generic type"() {
    when:
    def mixins = collector.collect(A, E)

    then:
    assert mixins == [new Pair(A, E), new Pair(A.Item, E.ItemMixin)]
  }

  static class A {
    Integer a
    String b
    Child c
    List<Item> d
    Map<String, List<Item>> e;
    static class Child {
      String c
    }
    static class Item {
      String d
    }
  }
  static class B {
    Integer a
    String b
    A.Child c
    List<A.Item> d
  }
  static class C {
    Integer a
    String b
    ChildMixin c
    List<A.Item> d
    static class ChildMixin {
      String c
    }
  }
  static class D {
    Integer a
    String b
    A.Child c
    List<ItemMixin> d
    static class ItemMixin {
      String d
    }
  }
  static class E {
    Integer a
    String b
    A.Child c
    List<A.Item> d
    Map<String, List<ItemMixin>> e
    static class ItemMixin {
      String d
    }
  }
}

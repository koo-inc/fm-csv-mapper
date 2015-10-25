package jp.co.freemind.csv.internal;

import jp.co.freemind.csv.exception.IllegalMixInMappingException;
import lombok.Value;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by kakusuke on 15/10/24.
 */
public class MixInCollector {

  public List<Pair> collect(Class<?> source, Class<?> mixin) {
    List<Pair> pairs = new ArrayList<>();
    pairs.add(new Pair(source, mixin));
    for (Field field : mixin.getDeclaredFields()) {
      Field targetField = findField(source, field.getName());

      Class<?>[] targetTypeArgs = aggregateFieldClasses(targetField.getGenericType());
      Class<?>[] mixinTypeArgs = aggregateFieldClasses(field.getGenericType());
      for (int i = 0; i < mixinTypeArgs.length; i++) {
        if (!targetTypeArgs[i].equals(mixinTypeArgs[i])) {
          pairs.addAll(collect(targetTypeArgs[i], mixinTypeArgs[i]));
        }
      }
    }
    return pairs;
  }

  private Field findField(Class<?> source, String name) {
    Class<?> current = source;
    while (source != Object.class) {
      for (Field field : current.getDeclaredFields()) {
        if (Objects.equals(field.getName(), name)) {
          return field;
        }
      }
      current = current.getSuperclass();
    }
    throw new IllegalMixInMappingException(name + " field is not exist in " + source.getSimpleName());
  }

  private Class<?>[] aggregateFieldClasses(Type type) {
    if (type instanceof Class) return new Class<?>[] { (Class<?>) type };
    return Arrays.stream(((ParameterizedType) type).getActualTypeArguments())
      .map(this::aggregateFieldClasses)
      .flatMap(Arrays::stream)
      .toArray(Class<?>[]::new);
  }


  @Value public static class Pair {
    private final Class<?> source;
    private final Class<?> mixin;
  }
}

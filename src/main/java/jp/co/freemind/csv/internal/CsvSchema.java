package jp.co.freemind.csv.internal;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

/**
 * Created by kakusuke on 15/07/28.
 */
public class CsvSchema {

  @Getter private final Class<?> formatClass;
  @Getter private final String[] headers;

  public CsvSchema(Class<?> formatClass) {
    this.formatClass = formatClass;
    this.headers = createHeaders(formatClass);
  }

  public String toJson(CsvLine line, Set<String> ignoreHeader) {
    String[] headers = getHeaders();
    String[] values = line.getFields();
    StringJoiner joiner = new StringJoiner(",", "{", "}");
    for (int i = 0, len = Math.min(headers.length, values.length); i < len; i++) {
      if (ignoreHeader.contains(headers[i])) continue;
      joiner.add(quote(headers[i]) + ":" + quote(values[i]));
    }
    return joiner.toString();
  }

  public int getColumnNumber(String fieldName) {
    String[] headers = getHeaders();
    for (int i = 0, len = headers.length; i < len; i++) {
      if (headers[i].equals(fieldName)) {
        return i + 1;
      }
    }
    return -1;
  }

  private String[] createHeaders(Class<?> formatClass) {
    JsonPropertyOrder order = formatClass.getAnnotation(JsonPropertyOrder.class);
    if (order != null) {
      return order.value();
    }

    Set<String> fieldHeaders = new HashSet<>();
    fieldHeaders.addAll(getFieldNames(formatClass));
    fieldHeaders.addAll(getPropertyNames(formatClass));
    return fieldHeaders.stream().sorted().toArray(String[]::new);
  }

  private Set<String> getFieldNames(Class<?> formatClass) {
    Set<String> set = new HashSet<>();
    List<Class<?>> classes = new ArrayList<>();
    while(formatClass != null) {
      classes.add(formatClass);
      formatClass = formatClass.getSuperclass();
    }

    Collections.reverse(classes);

    for (Class<?> clazz: classes) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isTransient(field.getModifiers())) continue;
        if (field.isAnnotationPresent(Transient.class)) continue;

        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
        if (jsonProperty != null) {
          set.add(jsonProperty.value());
        }
        else {
          set.add(field.getName());
        }
      }
    }
    return set;
  }

  private Set<String> getPropertyNames(Class<?> formatClass) {
    Set<String> set = new HashSet<>();
    try {
      BeanInfo info = Introspector.getBeanInfo(formatClass);
      for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
        Method read = descriptor.getReadMethod();
        Method write = descriptor.getWriteMethod();
        if (read.isAnnotationPresent(Transient.class)) continue;
        if (write.isAnnotationPresent(Transient.class)) continue;
        if (Modifier.isTransient(read.getModifiers())) continue;
        if (Modifier.isTransient(write.getModifiers())) continue;
        set.add(descriptor.getName());
      }
      return set;
    } catch (IntrospectionException e) {
      return Collections.emptySet();
    }
  }

  private String quote(String str) {
    return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  private BeanInfo createBeanInfo(Class<?> targetClass) {
    try {
      return Introspector.getBeanInfo(targetClass);
    } catch (IntrospectionException e) {
      throw new IllegalArgumentException(e);
    }
  }
}

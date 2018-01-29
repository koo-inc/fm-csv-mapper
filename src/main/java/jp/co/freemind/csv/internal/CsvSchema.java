package jp.co.freemind.csv.internal;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.freemind.csv.CsvFormatter;
import lombok.Getter;

/**
 * Created by kakusuke on 15/07/28.
 */
public class CsvSchema {
  static final private ObjectMapper MAPPER = new ObjectMapper().registerModule(new FlattenModule());

  @Getter private final Class<?> formatClass;
  @Getter private final String[] propertyNames;
  private final String nullValue;
  private final int skipLineCount;

  public <T> CsvSchema(CsvFormatter<T> formatter) {
    this.formatClass = formatter.getFormatClass();
    this.propertyNames = formatter.getOrderPaths() != null ? formatter.getOrderPaths() : createHeaders(formatClass);
    this.nullValue = formatter.getNullValue();
    this.skipLineCount = formatter.getSkipLineCount();
  }

  public String toJson(CsvLine line, Set<String> ignoreHeader) {
    Map<String, String> map = new HashMap<>();

    String[] headers = getPropertyNames();
    String[] values = line.getFields();

    for (int i = 0, len = Math.min(headers.length, values.length); i < len; i++) {
      if (ignoreHeader.contains(headers[i])) continue;
      map.put(headers[i], Objects.equals(values[i], nullValue) ? null : values[i]);
    }

    try {
      return MAPPER.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  public int getColumnNumber(String fieldName) {
    String[] headers = getPropertyNames();
    for (int i = 0, len = headers.length; i < len; i++) {
      if (headers[i].equals(fieldName)) {
        return i + 1;
      }
    }
    return -1;
  }

  public int getColumnIndex(String fieldName) {
    int columnNumber = getColumnNumber(fieldName);
    return columnNumber > 0 ? columnNumber - 1 : -1;
  }

  public int getLineIndex(int lineNumber) {
    return lineNumber - (this.skipLineCount + 1);
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
        if (read != null) {
          if (read.isAnnotationPresent(Transient.class)) continue;
          if (Modifier.isTransient(read.getModifiers())) continue;
        }
        if (write != null) {
          if (write.isAnnotationPresent(Transient.class)) continue;
          if (Modifier.isTransient(write.getModifiers())) continue;
        }
        set.add(descriptor.getName());
      }
      return set;
    } catch (IntrospectionException e) {
      return Collections.emptySet();
    }
  }

}

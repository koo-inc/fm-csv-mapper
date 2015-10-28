package jp.co.freemind.csv.internal;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.Getter;

/**
 * Created by kakusuke on 15/07/28.
 */
public class CsvSchema {
  static final private JsonFactory FACTORY = new JsonFactory();

  @Getter private final Class<?> formatClass;
  @Getter private final String[] propertyNames;
  private final String nullValue;

  public CsvSchema(Class<?> formatClass, String nullValue) {
    this.formatClass = formatClass;
    this.propertyNames = createHeaders(formatClass);
    this.nullValue = nullValue;
  }

  public String toJson(CsvLine line, Set<String> ignoreHeader) {
    try (StringWriter writer = new StringWriter();
         JsonGenerator generator = FACTORY.createGenerator(writer)) {

      generator.writeStartObject();

      String[] headers = getPropertyNames();
      String[] values = line.getFields();

      for (int i = 0, len = Math.min(headers.length, values.length); i < len; i++) {
        if (ignoreHeader.contains(headers[i])) continue;
        generator.writeObjectField(headers[i], Objects.equals(values[i], nullValue) ? null : values[i]);
      }
      generator.writeEndObject();
      generator.flush();

      return writer.toString();
    } catch (IOException e) {
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

  private String quote(String str) {
    if (Objects.equals(str, nullValue)) {
      return null;
    }
    return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}

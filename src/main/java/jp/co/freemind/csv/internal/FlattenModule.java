package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;


/**
 * Created by kakusuke on 15/10/28.
 */
public class FlattenModule extends Module {
  @Override
  public String getModuleName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }

  @Override
  public void setupModule(SetupContext context) {
    context.addDeserializers(new FlattenDeserializers());
  }

  public static class FlattenDeserializers extends SimpleDeserializers {
    private static final FlattenDeserializer deser = new FlattenDeserializer();

    @Override
    public JsonDeserializer<?> findArrayDeserializer(ArrayType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      return deser;
    }

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      return deser;
    }

    @Override
    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      return deser;
    }

    @Override
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
      return deser;
    }

    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      return deser;
    }

    @Override
    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      return deser;
    }
  }

  public static class FlattenDeserializer extends JsonDeserializer<Map<String, String>> {
    public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      Map<String, String> map = new LinkedHashMap<>();
      flatten("", p.getCodec().readTree(p), map);
      return map;
    }

    private void flatten(String currentPath, JsonNode node, Map<String, String> map) {
      if (node.isObject()) {
        ObjectNode objectNode = (ObjectNode) node;
        Iterator<Map.Entry<String, JsonNode>> itr = objectNode.fields();
        String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";
        while (itr.hasNext()) {
          Map.Entry<String, JsonNode> entry = itr.next();
          flatten(pathPrefix + entry.getKey(), entry.getValue(), map);
        }
      }
      else if (node.isArray()) {
        ArrayNode arrayNode = (ArrayNode) node;
        for (int i = 0, len = arrayNode.size(); i < len; i++) {
          flatten(currentPath + "[" + i + "]", arrayNode.get(i), map);
        }
      }
      else if (node.isNull()) {
        map.put(currentPath, null);
      }
      else if (node.isValueNode()) {
        map.put(currentPath, node.asText());
      }
    }
  }
}

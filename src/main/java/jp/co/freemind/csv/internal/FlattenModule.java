package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import jp.co.freemind.csv.internal.PathParser.PathSegment;


/**
 * Created by kakusuke on 15/10/28.
 */
class FlattenModule extends Module {
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
    context.addSerializers(new FlattenSerializers());
  }

  static class FlattenDeserializers extends SimpleDeserializers {
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

  static class FlattenDeserializer extends JsonDeserializer<Map<String, String>> {
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

  static class FlattenSerializers extends SimpleSerializers {
    private static final FlattenSerializer serializer = new FlattenSerializer();

    @Override
    public JsonSerializer<?> findMapSerializer(SerializationConfig config, MapType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      return serializer;
    }

    @Override
    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config, MapLikeType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
      return serializer;
    }
  }

  static class FlattenSerializer extends JsonSerializer<Map<String, String>> {
    private static final PathParser parser = new PathParser();

    @Override
    public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      ArrayList<String> keys = new ArrayList<>(value.keySet());

      ObjectNode root = (ObjectNode) gen.getCodec().createObjectNode();

      for (String key : keys) {
        List<PathSegment> path = parser.parse(key);

        TreeNode cur = root;
        for (int i = 0, len = path.size() - 1; i < len; i++) {
          PathSegment segment = path.get(i);
          if (!hasNextNode(cur, segment)) {
            if (path.get(i + 1).isArray()) {
              writeArrayNode(cur, segment);
            }
            else {
              writeObjectNode(cur, segment);
            }
          }
          cur = getNextNode(cur, segment);
        }

        writeValue(cur, path.get(path.size() - 1), value.get(key));
      }
      gen.writeTree(root);
    }

    private boolean hasNextNode(TreeNode node, PathSegment segment) {
      if (node.isArray()) {
        return node.get(Integer.valueOf(segment.getName())) != null;
      }
      else {
        return node.get(segment.getName()) != null;
      }
    }

    private void writeObjectNode(TreeNode node, PathSegment segment) {
      if (node.isObject()) {
        ((ObjectNode) node).putObject(segment.getName());
      }
      else {
        int index = Integer.valueOf(segment.getName());
        expand(((ArrayNode) node), index, JsonNodeType.OBJECT);
        ((ArrayNode) node).set(index, JsonNodeFactory.instance.objectNode());
      }
    }

    private void writeValue(TreeNode node, PathSegment segment, String val) {
      if (node.isObject()) {
        ((ObjectNode) node).put(segment.getName(), val);
      }
      else {
        int index = Integer.valueOf(segment.getName());
        expand(((ArrayNode) node), index, JsonNodeType.NULL);
        ((ArrayNode) node).set(index, JsonNodeFactory.instance.textNode(val));
      }
    }

    private void writeArrayNode(TreeNode node, PathSegment segment) {
      if (node.isObject()) {
        ((ObjectNode) node).putArray(segment.getName());
      }
      else {
        int index = Integer.valueOf(segment.getName());
        expand(((ArrayNode) node), index, JsonNodeType.ARRAY);
        ((ArrayNode) node).set(index, JsonNodeFactory.instance.arrayNode());
      }
    }

    private TreeNode getNextNode(TreeNode node, PathSegment segment) {
      if (node.isObject()) {
        node = node.get(segment.getName());
      }
      else {
        node = node.get(Integer.valueOf(segment.getName()));
      }
      return node;
    }

    private void expand(ArrayNode node, int index, JsonNodeType nodeType) {
      for (int i = node.size(); i <= index; i++) {
        switch(nodeType) {
          case ARRAY: node.addArray(); break;
          case OBJECT: node.addObject(); break;
          default: node.addNull(); break;
        }
      }
    }
  }
}

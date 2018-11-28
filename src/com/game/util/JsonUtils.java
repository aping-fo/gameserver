package com.game.util;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.server.util.ServerLogger;

/**
 * JSON 转换相关的工具类 注意,Map的Key只能为简单类型 ,不可采用复杂类型.
 */
@SuppressWarnings("unchecked")
public final class JsonUtils {

    private static TypeFactory typeFactory = TypeFactory.defaultInstance();

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将 map 转换为 JSON 的字符串格式
     *
     * @param map
     * @return
     */
    public static String map2String(Map<?, ?> map) {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, map);
        } catch (Exception e) {
            ServerLogger.err(e, "将MAP[{}]转换为JSON字符串时发生异常");
        }
        return writer.toString();
    }

    /**
     * 将对象转换为 JSON 的字符串格式
     *
     * @param object
     * @return
     */
    public static String object2String(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            ServerLogger.err(e, String.format("将对象%s转换为JSON字符串时发生异常", object.getClass()));
            return null;
        }
    }

    /**
     * 将 JSON 格式的字符串转换为数组
     *
     * @param <T>
     * @param content 字符串
     * @param clz     数组类型
     * @return
     */
    public static <T> T[] string2Array(String content, Class<T> clz) {
        JavaType type = ArrayType.construct(typeFactory.constructType(clz),
                null, null);
        try {
            return (T[]) mapper.readValue(content, type);
        } catch (Exception e) {
            ServerLogger.err(e, ("将字符串[{}]转换为数组时出现异常"));
            return null;
        }
    }

    /**
     * 将 JSON 格式的字符串转换为集合
     *
     * @param <T>
     * @param content        字符串
     * @param collectionType 集合类型
     * @param elementType    元素类型
     * @return
     */
    public static <C extends Collection<E>, E> C string2Collection(
            String content, Class<C> collectionType, Class<E> elementType) {
        try {
            JavaType type = typeFactory.constructCollectionType(collectionType,
                    elementType);
            return mapper.readValue(content, type);
        } catch (Exception e) {
            ServerLogger.err(e, ("将字符串[{}]转换为集合[{}]时出现异常"));
            return null;
        }
    }

    /**
     * 将 JSON 格式的字符串转换为 map
     *
     * @param content
     * @return
     */
    public static Map<String, Object> string2Map(String content) {
        JavaType type = typeFactory.constructMapType(HashMap.class,
                String.class, Object.class);
        try {
            return mapper.readValue(content, type);
        } catch (Exception e) {
            ServerLogger.err(e, ("将字符串[{}]转换为Map时出现异常"));
            return null;
        }
    }

    /**
     * 将字符串转换为{@link HashMap}对象实例
     *
     * @param content   被转换的字符串
     * @param keyType   键类型
     * @param valueType 值类型
     * @return
     */
    public static <K, V> Map<K, V> string2Map(String content, Class<K> keyType,
                                              Class<V> valueType) {
        JavaType type = typeFactory.constructMapType(HashMap.class, keyType,
                valueType);
        try {
            return (Map<K, V>) mapper.readValue(content, type);
        } catch (Exception e) {
            ServerLogger.err(e, ("将字符串[{}]转换为Map时出现异常"));
            return null;
        }
    }

    /**
     * 将字符串转换为特定的{@link Map}对象实例
     *
     * @param content   被转换的字符串
     * @param keyType   键类型
     * @param valueType 值类型
     * @param mapType   指定的{@link Map}类型
     * @return
     */
    public static <M extends Map<K, V>, K, V> M string2Map(String content,
                                                           Class<K> keyType, Class<V> valueType, Class<M> mapType) {
        JavaType type = typeFactory.constructMapType(mapType, keyType,
                valueType);
        try {
            return mapper.readValue(content, type);
        } catch (Exception e) {
            ServerLogger.err(e, ("将字符串[{}]转换为Map时出现异常"));
            return null;
        }
    }

    /**
     * 将 JSON 格式的字符串转换为对象
     *
     * @param <T>
     * @param content 字符串
     * @param clz     对象类型
     * @return
     */
    public static <T> T string2Object(String content, Class<T> clz) {
        JavaType type = typeFactory.constructType(clz);
        try {
            return (T) mapper.readValue(content, type);
        } catch (Exception e) {
            ServerLogger.err(e, String.format("将字符串%s转换为对象%s时出现异常", content, clz.getName()));
            return null;
        }
    }

    private JsonUtils() {
        throw new IllegalAccessError("该类不允许实例化");
    }


    /**
     * 将 list 转换为 JSON 的字符串格式
     *
     * @param list
     * @return
     */
    public static String list2String(List<?> list) {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, list);
        } catch (Exception e) {
            ServerLogger.err(e, "将MAP[{}]转换为JSON字符串时发生异常");
        }
        return writer.toString();
    }
}

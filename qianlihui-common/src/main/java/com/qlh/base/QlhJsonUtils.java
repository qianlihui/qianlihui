package com.qlh.base;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;

public class QlhJsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    public static String toFormattedJson(final Object obj) {
        return QlhException.runtime(() -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
    }

    public static String toJson(Object obj) {
        return QlhException.runtime(() -> mapper.writeValueAsString(obj));
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return QlhException.runtime(() -> mapper.readValue(json, clazz));
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        return QlhException.runtime(() -> mapper.readValue(json,
                mapper.getTypeFactory().constructParametricType(List.class, clazz)));
    }

    public static <T> T convert(Object obj, Class<T> clazz) {
        return toObject(toJson(obj), clazz);
    }
}

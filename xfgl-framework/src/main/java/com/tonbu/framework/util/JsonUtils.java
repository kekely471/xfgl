package com.tonbu.framework.util;


import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.type.TypeReference;

public final class JsonUtils {

    protected static Logger logger=LogManager.getLogger(JsonUtils.class.getName());

    private static ObjectMapper mapper = null;

    private JsonUtils() {
    }

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(org.codehaus.jackson.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapper.configure(org.codehaus.jackson.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            mapper.configure(org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd"));
        }

        return mapper;
    }

    public static String toJson(Object obj) {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = null;

        try {
            gen = (new JsonFactory()).createJsonGenerator(sw);
            getMapper().writeValue(gen, obj);
        } catch (IOException var12) {
            var12.printStackTrace();
        } finally {
            if (gen != null) {
                try {
                    gen.close();
                } catch (IOException var11) {
                    gen = null;
                }
            }

        }

        return sw.toString();
    }

    public static String formatToJson(String jsonString) {
        Map<String, Object> map = fromJson(jsonString);
        return map != null ? toJson(map) : jsonString;
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        Object t = null;

        try {
            if (jsonString != null) {
                t = getMapper().readValue(jsonString, clazz);
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return (T) t;
    }

    public static Map<String, Object> fromJson(String jsonString) {
        Map map = null;

        try {
            if (jsonString != null) {
                map = (Map)getMapper().readValue(jsonString, new TypeReference<LinkedHashMap<String, Object>>() {
                });
            }
        } catch (JsonParseException var3) {
            var3.printStackTrace();
        } catch (JsonMappingException var4) {
            var4.printStackTrace();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return map;
    }

    public static void main(String[] args) {

        System.out.println(formatToJson("{key:'asd'}"));
    }
}

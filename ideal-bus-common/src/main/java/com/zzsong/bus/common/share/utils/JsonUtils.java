package com.zzsong.bus.common.share.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author 宋志宗 on 2020/8/20
 */
public class JsonUtils {
  private JsonUtils() {
  }

  private static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern(DateTimes.YYYY_MM_DD_HH_MM_SS, Locale.SIMPLIFIED_CHINESE);
  private static final DateTimeFormatter DATE_FORMATTER
      = DateTimeFormatter.ofPattern(DateTimes.YYYY_MM_DD, Locale.SIMPLIFIED_CHINESE);
  private static final DateTimeFormatter TIME_FORMATTER
      = DateTimeFormatter.ofPattern(DateTimes.HH_MM_SS, Locale.SIMPLIFIED_CHINESE);

  private static final SimpleModule JAVA_TIME_MODULE = new JavaTimeModule()
      .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER))
      .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER))
      .addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER))
      .addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER))
      .addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER))
      .addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));

  public static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(JAVA_TIME_MODULE)
      .findAndRegisterModules();

  public static final ObjectMapper IGNORE_NULL_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .registerModule(JAVA_TIME_MODULE)
      .findAndRegisterModules();

  public static SimpleModule getJavaTimeModule() {
    return JAVA_TIME_MODULE;
  }

  @Nonnull
  public static <T> String toJsonString(@Nonnull T t) {
    return toJsonString(t, false, false);
  }

  @Nonnull
  public static <T> String toJsonStringIgnoreNull(@Nonnull T t) {
    return toJsonString(t, true, false);
  }

  @Nonnull
  public static <T> String toJsonString(@Nonnull T t, boolean ignoreNull, boolean pretty) {
    ObjectMapper writer = JsonUtils.MAPPER;
    if (ignoreNull) {
      writer = JsonUtils.IGNORE_NULL_MAPPER;
    }
    try {
      if (pretty) {
        return writer.writerWithDefaultPrettyPrinter().writeValueAsString(t);
      } else {
        return writer.writeValueAsString(t);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static <T> T parseJson(@Nonnull String jsonString, @Nonnull Class<T> clazz) {
    try {
      return IGNORE_NULL_MAPPER.readValue(jsonString, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static <T> T parseJson(@Nonnull String jsonString, JavaType javaType) {
    try {
      return IGNORE_NULL_MAPPER.readValue(jsonString, javaType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static <T> T parseJson(@Nonnull String jsonString, @Nonnull TypeReference<T> type) {
    try {
      return IGNORE_NULL_MAPPER.readValue(jsonString, type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static JavaType getJavaType(@Nonnull Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      Class<?> rowClass = (Class<?>) parameterizedType.getRawType();
      JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
      for (int i = 0; i < actualTypeArguments.length; i++) {
        javaTypes[i] = getJavaType(actualTypeArguments[i]);
      }
      return TypeFactory.defaultInstance().constructParametricType(rowClass, javaTypes);
    } else {
      Class<?> cla = (Class<?>) type;
      return TypeFactory.defaultInstance().constructParametricType(cla, new JavaType[0]);
    }
  }
}

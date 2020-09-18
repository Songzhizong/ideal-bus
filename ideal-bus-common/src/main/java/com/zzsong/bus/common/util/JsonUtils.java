package com.zzsong.bus.common.util;

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
  private static final DateTimeFormatter dateTimeFormatter
      = DateTimeFormatter.ofPattern(DateTimes.yyyy_MM_dd_HH_mm_ss, Locale.SIMPLIFIED_CHINESE);
  private static final DateTimeFormatter dateFormatter
      = DateTimeFormatter.ofPattern(DateTimes.yyyy_MM_dd, Locale.SIMPLIFIED_CHINESE);
  private static final DateTimeFormatter timeFormatter
      = DateTimeFormatter.ofPattern(DateTimes.HH_mm_ss, Locale.SIMPLIFIED_CHINESE);

  private static final SimpleModule javaTimeModule = new JavaTimeModule()
      .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
      .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
      .addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
      .addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter))
      .addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter))
      .addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

  public static final ObjectMapper mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(javaTimeModule)
      .findAndRegisterModules();

  public static final ObjectMapper ignoreNullMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .registerModule(javaTimeModule)
      .findAndRegisterModules();

  public static SimpleModule getJavaTimeModule() {
    return javaTimeModule;
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
    ObjectMapper writer = JsonUtils.mapper;
    if (ignoreNull) {
      writer = JsonUtils.ignoreNullMapper;
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
      return ignoreNullMapper.readValue(jsonString, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static <T> T parseJson(@Nonnull String jsonString, JavaType javaType) {
    try {
      return ignoreNullMapper.readValue(jsonString, javaType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  public static <T> T parseJson(@Nonnull String jsonString, @Nonnull TypeReference<T> type) {
    try {
      return ignoreNullMapper.readValue(jsonString, type);
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

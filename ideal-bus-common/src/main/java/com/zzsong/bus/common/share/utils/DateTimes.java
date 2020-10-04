package com.zzsong.bus.common.share.utils;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author 宋志宗 on 2020/8/30
 */
@SuppressWarnings("unused")
public final class DateTimes {
  private DateTimes() {
  }

  /**
   * 2020-12-12
   */
  public static final String YYYY_MM_DD = "yyyy-MM-dd";
  /**
   * 2020-12-12 19
   */
  public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";
  /**
   * 2020-12-12 19:21
   */
  public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
  /**
   * 2020-12-12 19:21:56
   */
  public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
  /**
   * 2020-12-12 19:21:56.555
   */
  public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
  /**
   * 12-12 19:21:56
   */
  public static final String MM_DD_HH_MM_SS = "MM-dd HH:mm:ss";
  /**
   * 12-12 19
   */
  public static final String MM_dd_HH = "MM-dd HH";
  /**
   * 19:21:56
   */
  public static final String HH_MM_SS = "HH:mm:ss";
  /**
   * 19:21
   */
  public static final String HH_MM = "HH:mm";

  private static final ZoneOffset CHINA_ZONE_OFFSET = ZoneOffset.of("+8");
  private static final Locale CHINA_LOCAL = Locale.SIMPLIFIED_CHINESE;

  @Nonnull
  public static String format(@Nonnull LocalDateTime localDateTime, @Nonnull String pattern) {
    return format(localDateTime, pattern, CHINA_LOCAL);
  }

  @Nonnull
  public static String format(@Nonnull LocalDateTime localDateTime,
                              @Nonnull String pattern, @Nonnull Locale locale) {
    return localDateTime.format(DateTimeFormatter.ofPattern(pattern, locale));
  }

  @Nonnull
  public static LocalDateTime parse(@Nonnull String dateTimeString, @Nonnull String pattern) {
    return parse(dateTimeString, pattern, CHINA_LOCAL);
  }

  @Nonnull
  public static LocalDateTime parse(@Nonnull String dateTimeString,
                                    @Nonnull String pattern, @Nonnull Locale locale) {
    return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern, locale));
  }

  @Nonnull
  public static LocalDateTime parse(long timestamp) {
    return parse(timestamp, CHINA_ZONE_OFFSET);
  }

  @Nonnull
  public static LocalDateTime parse(long timestamp, ZoneOffset zoneOffset) {
    Instant instant = Instant.ofEpochMilli(timestamp);
    return LocalDateTime.ofInstant(instant, zoneOffset);
  }

  public static long getTimestamp(LocalDateTime localDateTime) {
    return getTimestamp(localDateTime, CHINA_ZONE_OFFSET);
  }

  public static long getTimestamp(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
    return localDateTime.toInstant(zoneOffset).toEpochMilli();
  }

  @Nonnull
  public static LocalDateTime now() {
    return now(CHINA_ZONE_OFFSET);
  }

  @Nonnull
  public static LocalDateTime now(@Nonnull ZoneOffset zoneOffset) {
    return LocalDateTime.now(zoneOffset);
  }

  @Nonnull
  public static String calculateTimeDifference(long start, long end) {
    if (start == end) {
      return "0";
    }
    long time = Math.abs(end - start);
    long h = time / (1000 * 60 * 60);
    long m = time % (1000 * 60 * 60) / (1000 * 60);
    long s = time % (1000 * 60 * 60) % (1000 * 60) / 1000;
    long ms = time % (1000 * 60 * 60) % (1000 * 60) % 1000;
    StringBuilder sb = new StringBuilder();
    if (h != 0) {
      sb.append(h).append("小时");
    }
    if (m != 0) {
      sb.append(m).append("分钟");
    }
    sb.append(s).append(".").append(ms).append("秒");
    return sb.toString();
  }
}

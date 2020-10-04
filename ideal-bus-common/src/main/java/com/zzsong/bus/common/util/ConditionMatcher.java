package com.zzsong.bus.common.util;

import com.zzsong.bus.common.message.EventHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author 宋志宗 on 2020/5/27
 */
@Slf4j
public class ConditionMatcher {
  private ConditionMatcher() {
  }

  /**
   * 将条件表达式解析为条件组
   *
   * @param conditionExpression 条件表达式
   * @return 条件组
   * @author 宋志宗 on 2020/7/11 3:46 下午
   */
  @Nonnull
  public static List<Set<String>> parseConditionString(@Nonnull String conditionExpression) {
    if (StringUtils.isBlank(conditionExpression)) {
      return Collections.emptyList();
    }
    String[] group = StringUtils.split(conditionExpression, "|");
    List<Set<String>> conditionsGroup = new ArrayList<>();
    for (String s : group) {
      String[] split = StringUtils.split(s, "&");
      Set<String> set = new HashSet<>(Arrays.asList(split));
      conditionsGroup.add(set);
    }
    return conditionsGroup;
  }

  /**
   * 条件匹配
   * <p>同组为且,异组为或</p>
   *
   * @param conditionsGroup 条件组
   * @param headers         事件头
   * @return 事件头是否符合条件
   * @author 宋志宗 on 2020/7/13 10:59 上午
   */
  public static boolean match(@Nonnull List<Set<String>> conditionsGroup, @Nonnull EventHeaders headers) {
    if (conditionsGroup.size() == 0) {
      return true;
    }
    if (headers.isEmpty()) {
      return false;
    }
    for (Set<String> condition : conditionsGroup) {
      // 组内判断, 只要有一组满足条件则整体满足
      if (matchConditions(condition, headers)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 通过条件表达式判断是否符合
   *
   * @param conditionExpression 条件表达式
   * @param headers             事件头
   * @return 是否符合条件
   * @author 宋志宗 on 2020/7/13 10:59 上午
   */
  public static boolean match(@Nonnull String conditionExpression, @Nonnull EventHeaders headers) {
    List<Set<String>> conditionsGroup = parseConditionString(conditionExpression);
    return match(conditionsGroup, headers);
  }

  /**
   * 组内判断
   *
   * @param conditions 组内条件列表
   * @param headers    事件头
   * @return 整组是否符合条件
   * @author 宋志宗 on 2020/7/13 10:58 上午
   */
  private static boolean matchConditions(@Nonnull Set<String> conditions, @Nonnull EventHeaders headers) {
    if (conditions.size() == 0) {
      return true;
    }
    for (String condition : conditions) {
      char operator = '0';
      int index = -1;
      char[] chars = condition.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        char c = chars[i];
        if (c == '>' || c == '<' || c == '=' || c == '^') {
          operator = c;
          index = i;
          break;
        }
      }
      if (index < 1) {
        log.info("条件判断不通过,condition缺少运算符: {}", condition);
        return false;
      }
      String key = StringUtils.substring(condition, 0, index);
      Set<String> headerValues = headers.get(key);
      if (headerValues == null || headerValues.isEmpty()) {
        return false;
      }
      String value = StringUtils.substring(condition, index + 1);
      switch (operator) {
        case '>': {
          boolean flag = false;
          for (String headerValue : headerValues) {
            long target;
            long cond;
            try {
              target = Long.parseLong(headerValue);
              cond = Long.parseLong(value);
            } catch (NumberFormatException e) {
              log.info("条件判断不通过,parseLong exception,condition={},header={}", condition, headerValue);
              return false;
            }
            if (cond < target) {
              flag = true;
            }
          }
          if (!flag) {
            return false;
          }
          break;
        }
        case '<': {
          boolean flag = false;
          for (String headerValue : headerValues) {
            long target;
            long cond;
            try {
              target = Long.parseLong(headerValue);
              cond = Long.parseLong(value);
            } catch (NumberFormatException e) {
              log.warn("条件判断不通过,parseLong exception,condition={},header={}", condition, headerValue);
              return false;
            }
            if (cond > target) {
              flag = true;
            }
          }
          if (!flag) {
            return false;
          }
          break;
        }
        case '=': {
          if (!headerValues.contains(value)) {
            return false;
          }
          break;
        }
        case '^': {
          String[] split = value.split(",");
          boolean flag = false;
          for (String s : split) {
            if (headerValues.contains(s)) {
              flag = true;
              break;
            }
          }
          if (!flag) {
            return false;
          }
        }
        default: {
          log.warn("不合法的操作符: {}", operator);
          break;
        }
      }
    }
    // 全部满足了才返回true
    return true;
  }
}

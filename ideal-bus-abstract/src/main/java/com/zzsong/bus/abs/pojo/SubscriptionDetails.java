package com.zzsong.bus.abs.pojo;

import com.zzsong.bus.abs.constants.DBDefaults;
import com.zzsong.bus.abs.constants.ApplicationTypeEnum;
import com.zzsong.bus.abs.domain.Subscription;
import com.zzsong.bus.common.util.ConditionMatcher;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.beans.Transient;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Getter
@Setter
public class SubscriptionDetails extends Subscription {

  /**
   * 订阅者类型
   */
  @Nonnull
  private ApplicationTypeEnum applicationType;

  /**
   * 应用编码, 外部应用拥有此属性
   */
  @Nonnull
  private String externalApplication = DBDefaults.STRING_VALUE;

  /**
   * 接收推送的地址, 外部应用拥有此属性
   */
  @Nonnull
  private String receiveUrl = DBDefaults.STRING_VALUE;

  /**
   * 订阅条件分组
   */
  private transient List<Set<String>> conditionGroup = Collections.emptyList();

  @Override
  public void setCondition(@Nonnull String condition) {
    super.setCondition(condition);
    this.conditionGroup = ConditionMatcher.parseConditionString(condition);
  }

  @Transient
  public List<Set<String>> getConditionGroup() {
    return conditionGroup;
  }
}

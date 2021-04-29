package com.zzsong.bus.common.transfer;

import lombok.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/18 10:54 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResubscribeArgs {

  /**
   * 订阅者id
   */
  @Nonnull
  private Long applicationId;

  /**
   * 订阅关系信息
   */
  @Nonnull
  private List<SubscriptionArgs> subscriptionArgsList = Collections.emptyList();

  @Nonnull
  public ResubscribeArgs checkAndGet() {
    //noinspection ConstantConditions
    if (applicationId == null) {
      throw new IllegalArgumentException("订阅者id不能为空");
    }
    for (SubscriptionArgs args : subscriptionArgsList) {
      args.checkAndGet();
    }
    return this;
  }
}

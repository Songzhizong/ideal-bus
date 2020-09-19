package com.zzsong.bus.common.transfer;

import lombok.*;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * @author 宋志宗 on 2020/9/18 10:54 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoSubscribeArgs {

  /**
   * 订阅者id
   */
  @Nonnull
  @NotNull(message = "订阅者id不能为空")
  private Long applicationId;

  /**
   * 订阅关系信息
   */
  @Valid
  @Nonnull
  private List<SubscriptionArgs> subscriptionArgsList = Collections.emptyList();
}

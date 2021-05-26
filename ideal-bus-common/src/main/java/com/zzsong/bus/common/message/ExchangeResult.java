package com.zzsong.bus.common.message;

import lombok.*;

/**
 * @author 宋志宗 on 2021/5/14
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResult {
  private boolean success;
  private String message;
}

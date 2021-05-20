package com.zzsong.bus.broker.core.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 宋志宗 on 2020/11/26
 */
@Deprecated
public final class RouteTransferFactory {
  private RouteTransferFactory() {
  }

  private static final Map<TransferType, RouteTransfer> TRANSFER_MAPPING = new HashMap<>();

  protected static void register(@Nonnull TransferType type, @Nonnull RouteTransfer transfer) {
    TRANSFER_MAPPING.put(type, transfer);
  }

  @Nullable
  public static RouteTransfer get(@Nonnull TransferType type) {
    return TRANSFER_MAPPING.get(type);
  }
}

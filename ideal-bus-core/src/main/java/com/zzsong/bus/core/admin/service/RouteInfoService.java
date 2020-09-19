package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.storage.RouteInfoStorage;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Service
public class RouteInfoService {
  @Nonnull
  private final RouteInfoStorage storage;

  public RouteInfoService(@Nonnull RouteInfoStorage storage) {
    this.storage = storage;
  }
}

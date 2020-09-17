package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.storage.RouteInstanceStorage;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Service
public class RouteInstanceService {
  @Nonnull
  private final RouteInstanceStorage storage;

  public RouteInstanceService(@Nonnull RouteInstanceStorage storage) {
    this.storage = storage;
  }
}

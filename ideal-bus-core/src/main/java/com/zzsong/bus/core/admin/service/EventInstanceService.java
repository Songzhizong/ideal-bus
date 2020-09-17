package com.zzsong.bus.core.admin.service;

import com.zzsong.bus.abs.storage.EventInstanceStorage;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2020/9/17
 */
@Service
public class EventInstanceService {
  @Nonnull
  private final EventInstanceStorage storage;

  public EventInstanceService(@Nonnull EventInstanceStorage storage) {
    this.storage = storage;
  }
}

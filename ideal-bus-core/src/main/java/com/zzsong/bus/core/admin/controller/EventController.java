package com.zzsong.bus.core.admin.controller;

import com.zzsong.bus.base.domain.Event;
import com.zzsong.bus.base.transfer.SaveEventArgs;
import com.zzsong.bus.common.transfer.Res;
import com.zzsong.bus.core.admin.service.EventService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 事件管理
 *
 * @author 宋志宗 on 2020/9/16
 */
@RestController
@RequestMapping("/event")
public class EventController {
  @Nonnull
  private final EventService eventService;

  public EventController(@Nonnull EventService eventService) {
    this.eventService = eventService;
  }

  @Nonnull
  @PostMapping("/save")
  public Mono<Res<Event>> save(@Validated @RequestBody @Nonnull SaveEventArgs args) {
    return eventService.save(args).map(Res::data);
  }
}

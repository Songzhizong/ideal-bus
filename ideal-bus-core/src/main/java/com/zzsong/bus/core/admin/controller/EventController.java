package com.zzsong.bus.core.admin.controller;

import com.zzsong.bus.abs.domain.Event;
import com.zzsong.bus.abs.transfer.QueryEventArgs;
import com.zzsong.bus.abs.transfer.SaveEventArgs;
import com.zzsong.bus.common.transfer.Paging;
import com.zzsong.bus.common.transfer.Res;
import com.zzsong.bus.core.admin.service.EventService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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

  /**
   * 新建事件
   */
  @Nonnull
  @PostMapping("/create")
  public Mono<Res<Event>> create(@Validated @RequestBody
                                 @Nonnull SaveEventArgs args) {
    return eventService.create(args).map(Res::data);
  }

  /**
   * 更新
   */
  @Nonnull
  @PostMapping("/update")
  public Mono<Res<Event>> update(@Validated @RequestBody
                                 @Nonnull SaveEventArgs args) {
    return eventService.update(args).map(Res::data);
  }

  /**
   * 删除
   * <p>如果存在订阅关系则无法删除</p>
   */
  @Nonnull
  @DeleteMapping("/delete/{topic}")
  public Mono<Res<Long>> delete(@PathVariable("topic") @Nonnull String topic) {
    return eventService.delete(topic).map(Res::data);
  }

  @Nonnull
  @GetMapping("/query")
  public Mono<Res<List<Event>>> query(@Nullable QueryEventArgs args, @Nullable Paging paging) {
    if (paging == null) {
      paging = Paging.of(1, 10);
    }
    return eventService.query(args, paging);
  }
}

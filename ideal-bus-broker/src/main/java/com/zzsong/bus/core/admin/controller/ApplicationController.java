package com.zzsong.bus.core.admin.controller;

import com.zzsong.bus.abs.domain.Application;
import com.zzsong.bus.abs.transfer.CreateApplicationArgs;
import com.zzsong.bus.abs.transfer.QueryApplicationArgs;
import com.zzsong.bus.abs.transfer.UpdateApplicationArgs;
import com.zzsong.bus.abs.share.Paging;
import com.zzsong.bus.abs.share.Res;
import com.zzsong.bus.core.admin.service.ApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 订阅者管理
 *
 * @author 宋志宗 on 2020/9/16
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

  @Nonnull
  private final ApplicationService applicationService;

  public ApplicationController(@Nonnull ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @Nonnull
  @PostMapping("/create")
  public Mono<Res<Application>> create(@Validated @RequestBody
                                      @Nonnull CreateApplicationArgs args) {
    return applicationService.create(args).map(Res::data);
  }

  @Nonnull
  @PostMapping("/update")
  public Mono<Res<Application>> update(@Validated @RequestBody
                                      @Nonnull UpdateApplicationArgs args) {
    return applicationService.update(args).map(Res::data);
  }

  @Nonnull
  @DeleteMapping("/delete/{applicationId}")
  public Mono<Res<Long>> delete(@PathVariable("applicationId") long applicationId) {
    return applicationService.delete(applicationId).map(Res::data);
  }

  @Nonnull
  @GetMapping("/query")
  public Mono<Res<List<Application>>> query(@Nullable QueryApplicationArgs args,
                                            @Nullable Paging paging) {
    if (paging == null) {
      paging = Paging.of(1, 10);
    }
    paging.descBy("applicationId");
    return applicationService.query(args, paging);
  }
}

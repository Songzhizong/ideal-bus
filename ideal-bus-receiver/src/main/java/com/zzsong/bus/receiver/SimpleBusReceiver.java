package com.zzsong.bus.receiver;

import com.zzsong.bus.common.message.DeliveredEvent;
import com.zzsong.bus.common.message.DeliveredResult;
import com.zzsong.bus.receiver.deliver.EventDeliverer;
import com.zzsong.bus.receiver.deliver.EventDelivererImpl;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;

/**
 * @author 宋志宗 on 2020/9/18
 */
public class SimpleBusReceiver implements BusReceiver {
  private final EventDeliverer eventDeliverer;

  public SimpleBusReceiver(ExecutorService executorService) {
    Scheduler scheduler = Schedulers.fromExecutor(executorService);
    this.eventDeliverer = new EventDelivererImpl(scheduler);
  }

  @Nonnull
  @Override
  public Mono<DeliveredResult> receive(@Nonnull DeliveredEvent event) {
    return eventDeliverer.deliver(event);
  }
  
  public void startReceiver() {
    initEventListeners();
  }

  protected void initEventListeners() {

  }
}

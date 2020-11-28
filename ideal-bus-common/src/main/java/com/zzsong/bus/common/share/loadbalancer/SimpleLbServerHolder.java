package com.zzsong.bus.common.share.loadbalancer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 宋志宗 on 2020/8/20
 */
@Slf4j
public class SimpleLbServerHolder<Server extends LbServer> implements LbServerHolder<Server> {

  @Getter
  private final String serverName;
  @Nonnull
  private final LbFactory<Server> lbFactory;
  /**
   * 所有服务列表
   */
  private List<Server> allServers = new ArrayList<>();
  private final List<Server> reachableServers = new LinkedList<>();
  private final Lock allServersLock = new ReentrantLock();


  SimpleLbServerHolder(@Nonnull String serverName,
                       @Nonnull LbFactory<Server> lbFactory) {
    this.serverName = serverName;
    this.lbFactory = lbFactory;
  }

  @Override
  public void addServers(@Nonnull List<Server> newServers) {
    try {
      allServersLock.lock();
      int size = allServers.size();
      Map<String, Integer> indexMap = new HashMap<>(size);
      for (int i = 0; i < size; i++) {
        final Server server = allServers.get(i);
        final String instanceId = server.getInstanceId();
        indexMap.put(instanceId, i);
      }
      for (Server newServer : newServers) {
        String instanceId = newServer.getInstanceId();
        final Integer integer = indexMap.get(instanceId);
        if (integer == null) {
          // 如果服务之前未注册则直接添加到服务列表
          allServers.add(newServer);
        } else {
          // 服务已注册, 将原有的替换成新的并销毁原对象
          Server server = allServers.set(integer, newServer);
          server.dispose();
        }
        markServerReachable(newServer);
      }
    } finally {
      allServersLock.unlock();
    }
    this.serverChange();
  }

  @Override
  public void markServerReachable(@Nonnull Server server) {
    String instanceId = server.getInstanceId();
    synchronized (reachableServers) {
      boolean flag = true;
      for (int i = 0; i < reachableServers.size(); i++) {
        if (instanceId.equals(reachableServers.get(i).getInstanceId())) {
          reachableServers.set(i, server);
          flag = false;
          break;
        }
      }
      if (flag) {
        reachableServers.add(server);
      }
    }
    this.serverChange();
  }

  @Override
  public void markServerDown(@Nonnull Server server) {
    String instanceId = server.getInstanceId();
    boolean removed = false;
    synchronized (reachableServers) {
      final Iterator<Server> iterator = reachableServers.iterator();
      while (iterator.hasNext()) {
        Server next = iterator.next();
        if (instanceId.equals(next.getInstanceId())) {
          iterator.remove();
          removed = true;
          break;
        }
      }
    }
    if (removed) {
      this.serverChange();
    }
  }

  @Override
  public void removeServer(@Nonnull Server server) {
    allServersLock.lock();
    try {
      List<Server> newAllServers = new ArrayList<>(allServers.size());
      for (Server lbServer : allServers) {
        if (!server.getInstanceId().equals(lbServer.getInstanceId())) {
          newAllServers.add(lbServer);
        } else {
          markServerDown(server);
        }
      }
      this.allServers = newAllServers;
    } finally {
      allServersLock.unlock();
    }
    this.serverChange();
  }

  @Nonnull
  @Override
  public List<Server> getReachableServers() {
    return Collections.unmodifiableList(reachableServers);
  }

  @Nonnull
  @Override
  public List<Server> getAllServers() {
    return Collections.unmodifiableList(allServers);
  }

  private void serverChange() {
    final LbFactoryEvent event = new LbFactoryEvent();
    event.setServerName(serverName);
    event.setAllServers(getAllServers());
    event.setReachableServers(getReachableServers());
    lbFactory.serverChange(event);
  }

  @Override
  public void destroy() {
  }
}

package com.zzsong.bus.common.message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * @author 宋志宗 on 2020/5/26
 */
public class EventHeaders implements Map<String, Set<String>>, Serializable {
  private static final long serialVersionUID = 117500675943825408L;
  private static final String PUBLISHER = "X-Bus-Publisher";
  private final Map<String, Set<String>> targetMap;

  @Nonnull
  public static EventHeaders create() {
    return new EventHeaders();
  }

  public EventHeaders() {
    this.targetMap = new LinkedHashMap<>();
  }

  @Nullable
  public String getPublisher() {
    return this.getOne(PUBLISHER);
  }

  @Nonnull
  public EventHeaders setPublisher(@Nonnull String publisher) {
    return this.set(PUBLISHER, publisher);
  }

  @Nullable
  public String getOne(@Nonnull String key) {
    Set<String> values = this.targetMap.get(key);
    return values == null || values.isEmpty() ? null : values.iterator().next();
  }

  @Nonnull
  public EventHeaders add(@Nonnull String key, @Nonnull String value) {
    Set<String> values = this.targetMap.computeIfAbsent(key, k -> new LinkedHashSet<>());
    values.add(value);
    return this;
  }

  @Nonnull
  public EventHeaders addAll(@Nonnull String key, @Nonnull Collection<String> values) {
    Set<String> currentValues = this.targetMap.computeIfAbsent(key, k -> new LinkedHashSet<>());
    currentValues.addAll(values);
    return this;
  }

  @Nonnull
  public EventHeaders addAll(@Nonnull EventHeaders values) {
    values.forEach(this::addAll);
    return this;
  }

  @Nonnull
  public EventHeaders set(@Nonnull String key, @Nonnull String value) {
    Set<String> values = new LinkedHashSet<>();
    values.add(value);
    this.targetMap.put(key, values);
    return this;
  }

  @Nonnull
  public EventHeaders setAll(@Nonnull Map<String, String> values) {
    values.forEach(this::set);
    return this;
  }

  // Map implementation

  @Override
  public int size() {
    return this.targetMap.size();
  }

  @Override
  public boolean isEmpty() {
    return this.targetMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return this.targetMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return this.targetMap.containsValue(value);
  }

  @Override
  public Set<String> get(Object key) {
    return this.targetMap.get(key);
  }

  @Nullable
  @Override
  public Set<String> put(String key, Set<String> value) {
    return this.targetMap.put(key, value);
  }

  @Override
  public Set<String> remove(Object key) {
    return this.targetMap.remove(key);
  }

  @Override
  public void putAll(@Nonnull Map<? extends String, ? extends Set<String>> m) {
    this.targetMap.putAll(m);
  }

  @Override
  public void clear() {
    this.targetMap.clear();
  }

  @Nonnull
  @Override
  public Set<String> keySet() {
    return this.targetMap.keySet();
  }

  @Nonnull
  @Override
  public Collection<Set<String>> values() {
    return this.targetMap.values();
  }

  @Nonnull
  @Override
  public Set<Entry<String, Set<String>>> entrySet() {
    return this.targetMap.entrySet();
  }
}

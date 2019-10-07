package com.hogly.cluster.multitenancy.instance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationInstanceContext {

  private final Map<Class, Object> context;

  private ApplicationInstanceContext(Map<Class, Object> context) {
    this.context = context;
  }

  public static ApplicationInstanceContext newInstance() {
    return new ApplicationInstanceContext(new ConcurrentHashMap<>());
  }

  public <T> ApplicationInstanceContext add(Class<T> type, T instance) {
    context.put(type, instance);
    return this;
  }

  public <T> T get(Class<T> type) {
    if (context.containsKey(type)) {
      return (T) context.get(type);
    }
    throw new IllegalArgumentException("There is no instance for the type: " + type.getName());
  }

}

package com.headissue.badges;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jens Wilke
 */
public class GcChurnMonitor {

  private final Map<String, AtomicLong> churn = new ConcurrentHashMap<>();
  private List<Long> usedAfterGc = Collections.synchronizedList(new ArrayList<>());
  private List<Long> committedAfterGc = Collections.synchronizedList(new ArrayList<>());

  public GcChurnMonitor() {
    NotificationListener listener;
    try {
      final Class<?> infoKlass = Class.forName("com.sun.management.GarbageCollectionNotificationInfo");
      final Field notifNameField = infoKlass.getField("GARBAGE_COLLECTION_NOTIFICATION");
      final Method infoMethod = infoKlass.getMethod("from", CompositeData.class);
      final Method getGcInfo = infoKlass.getMethod("getGcInfo");
      final Method getMemoryUsageBeforeGc = getGcInfo.getReturnType().getMethod("getMemoryUsageBeforeGc");
      final Method getMemoryUsageAfterGc = getGcInfo.getReturnType().getMethod("getMemoryUsageAfterGc");

      listener = new NotificationListener() {
        @Override
        public void handleNotification(Notification n, Object o) {
          try {
            if (n.getType().equals(notifNameField.get(null))) {
              StringBuilder debugLine = new StringBuilder();
              Object info = infoMethod.invoke(null, n.getUserData());
              Object gcInfo = getGcInfo.invoke(info);
              Map<String, MemoryUsage> mapBefore = (Map<String, MemoryUsage>) getMemoryUsageBeforeGc.invoke(gcInfo);
              Map<String, MemoryUsage> mapAfter = (Map<String, MemoryUsage>) getMemoryUsageAfterGc.invoke(gcInfo);
              // sum up the different pools
              long committed = 0;
              long used = 0;
              long cleanup = 0;
              for (Map.Entry<String, MemoryUsage> entry : mapAfter.entrySet()) {
                String name = entry.getKey();
                MemoryUsage after = entry.getValue();
                committed += after.getCommitted();
                used += after.getUsed();
                debugLine.append(name).append("=").append(after.getUsed()).append(", ");
                MemoryUsage before = mapBefore.get(name);
                long c = before.getUsed() - after.getUsed();
                cleanup += c;
                if (c > 0) {
                  churn.computeIfAbsent(name, s -> new AtomicLong()).addAndGet(c);
                }
              }
              usedAfterGc.add(used);
              committedAfterGc.add(committed);
              System.out.println("[GC Notification Listener] " + debugLine + "Total used=" + used + ", Total committed=" + committed + ", churn=" + cleanup);
            }
          } catch (IllegalAccessException e) {
            // Do nothing, counters would not get populated
          } catch (InvocationTargetException e) {
            // Do nothing, counters would not get populated
          }
        }
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
      ((NotificationEmitter) bean).addNotificationListener(listener, null, null);
    }
  }

  public long printChurn() {
    long total = 0;
    for (String space : churn.keySet()) {
      final long bytes = churn.get(space).get();
      System.out.println("[printChrun] " + space + " " + bytes);
      total += bytes;
    }
    return total;
  }

}

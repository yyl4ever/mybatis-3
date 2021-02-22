/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.ibatis.cache.Cache;

/**
 * Weak Reference cache decorator.
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 *
 * @author Clinton Begin
 * 弱引用比软引用的引用强度还要弱。弱引用可以引用一个对象，但无法阻止这个对象被 GC 回收，
 * 也就是说，在 JVM 进行垃圾回收的时候，若发现某个对象只有一个弱引用指向它，那么这个对象会被 GC 立刻回收。
 *
 * 从这个特性我们可以得到一个结论：只被弱引用指向的对象只在两次 GC 之间存活。而只被软引用指向的对象是在 JVM 内存紧张的时候才被回收，
 * 它是可以经历多次 GC 的，这就是两者最大的区别。
 * 在 WeakReference 指向的对象被回收时，也会将 WeakReference 对象添加到关联的队列中。
 *
 * JDK 提供了一个基于弱引用实现的 HashMap 集合—— WeakHashMap，其中的 Entry 继承了 WeakReference，Entry 中使用弱引用指向 Key，使用强引用指向 Value。
 * 当没有强引用指向 Key 的时候，Key 可以被 GC 回收。当再次操作 WeakHashMap 的时候，就会遍历关联的引用队列，从 WeakHashMap 中清理掉相应的 Entry。
 *
 * WeakCache，它的实现与 SoftCache 十分类似，两者的唯一区别在于：WeakCache 中存储的是 WeakEntry 对象，
 * 它继承了 WeakReference，通过 WeakReference 指向 Value 对象。具体的实现与 SoftCache 基本相同
 */
public class WeakCache implements Cache {
  private final Deque<Object> hardLinksToAvoidGarbageCollection;
  private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
  private final Cache delegate;
  private int numberOfHardLinks;

  public WeakCache(Cache delegate) {
    this.delegate = delegate;
    this.numberOfHardLinks = 256;
    this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
    this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    removeGarbageCollectedItems();
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.numberOfHardLinks = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    removeGarbageCollectedItems();
    delegate.putObject(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries));
  }

  @Override
  public Object getObject(Object key) {
    Object result = null;
    @SuppressWarnings("unchecked") // assumed delegate cache is totally managed by this cache
    WeakReference<Object> weakReference = (WeakReference<Object>) delegate.getObject(key);
    if (weakReference != null) {
      result = weakReference.get();
      if (result == null) {
        delegate.removeObject(key);
      } else {
        hardLinksToAvoidGarbageCollection.addFirst(result);
        if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
          hardLinksToAvoidGarbageCollection.removeLast();
        }
      }
    }
    return result;
  }

  @Override
  public Object removeObject(Object key) {
    removeGarbageCollectedItems();
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    hardLinksToAvoidGarbageCollection.clear();
    removeGarbageCollectedItems();
    delegate.clear();
  }

  private void removeGarbageCollectedItems() {
    WeakEntry sv;
    while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {
      delegate.removeObject(sv.key);
    }
  }

  /**
   * WeakEntry 对象，它继承了 WeakReference，通过 WeakReference 指向 Value 对象
   */
  private static class WeakEntry extends WeakReference<Object> {
    private final Object key;

    private WeakEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
      super(value, garbageCollectionQueue);
      this.key = key;
    }
  }

}

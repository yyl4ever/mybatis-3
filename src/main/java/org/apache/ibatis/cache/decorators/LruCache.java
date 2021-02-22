/**
 *    Copyright 2009-2020 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.cache.Cache;

/**
 * Lru (least recently used) cache decorator.
 *
 * @author Clinton Begin
 * LruCache 就是使用 LRU 策略清理缓存的装饰器实现，如果 LruCache 发现缓存需要清理，它会清除最近最少使用的缓存条目。
 */
public class LruCache implements Cache {

  private final Cache delegate;

  /**
   *  LinkedHashMap 集合（keyMap 字段），用来记录各个缓存条目最近的使用情况;
   *  keyMap 覆盖了 LinkedHashMap 默认的 removeEldestEntry() 方法实现，当 LruCache 中缓存条目达到上限的时候，
   *  返回 true，即删除 Entry 链表中 head 指向的 Entry。
   *  LruCache 就是依赖 LinkedHashMap 上述的这些特点来确定最久未使用的缓存条目并完成删除的。
   */
  private Map<Object, Object> keyMap;

  /**
   * 指向最近最少使用的 Key
   */
  private Object eldestKey;

  public LruCache(Cache delegate) {
    this.delegate = delegate;
    setSize(1024);
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  /**
   * LruCache 中的 keyMap 覆盖了 LinkedHashMap 默认的 removeEldestEntry() 方法实现，当 LruCache 中缓存条目达到上限的时候，
   * 返回 true，即删除 Entry 链表中 head 指向的 Entry。
   * LruCache 就是依赖 LinkedHashMap 上述的这些特点来确定最久未使用的缓存条目并完成删除的。
   * @param size
   */
  public void setSize(final int size) {
    keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
      private static final long serialVersionUID = 4267176411845948333L;
      // 调用LinkedHashMap.put()方法时，会调用removeEldestEntry()方法
      // 决定是否删除head指向的Entry数据
      @Override
      protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
        boolean tooBig = size() > size;
        if (tooBig) { // 已到达缓存上限，更新eldestKey字段，并返回true，LinkedHashMap会删除该Key,即删除 Entry 链表中 head 指向的 Entry。
          eldestKey = eldest.getKey();
        }
        return tooBig;
      }
    };
  }

  @Override
  public void putObject(Object key, Object value) {
    // 将 KV 数据写入底层被装饰的 Cache 对象中
    delegate.putObject(key, value);
    // 将 KV 数据写入 keyMap 集合中，此时可能会触发 eldestKey 数据的清理
    cycleKeyList(key);
  }

  @Override
  public Object getObject(Object key) {
    // 更新 Key 在这个 LinkedHashMap 集合中的顺序,默默修改 Entry 链表，将条目移动到链表的尾部
    keyMap.get(key); // touch
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyMap.clear();
  }

  private void cycleKeyList(Object key) {
    // 将KV数据写入keyMap集合
    keyMap.put(key, key);
    if (eldestKey != null) {
      // 如果eldestKey不为空，则将从底层Cache中删除
      delegate.removeObject(eldestKey);
      eldestKey = null;
    }
  }

}

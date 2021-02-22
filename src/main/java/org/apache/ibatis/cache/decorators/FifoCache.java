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

import java.util.Deque;
import java.util.LinkedList;

import org.apache.ibatis.cache.Cache;

/**
 * FIFO (first in, first out) cache decorator.
 *
 * @author Clinton Begin
 *  FIFO（先入先出）策略的装饰器,在系统运行过程中，我们会不断向 Cache 中增加缓存条目，
 *  当 Cache 中的缓存条目达到上限的时候，则会将 Cache 中最早写入的缓存条目清理掉，这也就是先入先出的基本原理。
 */
public class FifoCache implements Cache {

  /**
   * 装饰器模式的体现
   */
  private final Cache delegate;

  /**
   *  a double-ended queue
   *  keyList 队列（LinkedList），
   *  主要利用 LinkedList 集合有序性，记录缓存条目写入 Cache 的先后顺序,
   *  记录缓存项中的Key
   */
  private final Deque<Object> keyList;

  /**
   * 当前 Cache 的大小上限（size 字段），当 Cache 大小超过该值时，
   * 就会从 keyList 集合中查找最早的缓存条目并进行清理
   */
  private int size;

  public FifoCache(Cache delegate) {
    this.delegate = delegate;
    this.keyList = new LinkedList<>();
    this.size = 1024;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    // 执行 FIFO 策略清理缓
    cycleKeyList(key);
    delegate.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    // 直接委托给底层 delegate 这个被装饰的 Cache 对象的同名方法
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    // 直接委托给底层 delegate 这个被装饰的 Cache 对象的同名方法
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
    keyList.clear();
  }

  private void cycleKeyList(Object key) {
    keyList.addLast(key);// 记录缓存项中的Key
    // 当keyList长度超过上限，表示Cache缓存达到上限，开始进行清理
    if (keyList.size() > size) {
      // 找到keyList中最早写入的key，并从底层Cache中删除该缓存条目
      Object oldestKey = keyList.removeFirst();
      delegate.removeObject(oldestKey);
    }
  }

}

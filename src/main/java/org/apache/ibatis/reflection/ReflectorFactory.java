/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.reflection;

/**
 * Reflector 初始化过程会有一系列的反射操作，为了提升 Reflector 的初始化速度，
 * MyBatis 提供了 ReflectorFactory 这个工厂接口对 Reflector 对象进行缓存，
 * 其中最核心的方法是用来获取 Reflector 对象的 findForClass() 方法。
 */
public interface ReflectorFactory {

  boolean isClassCacheEnabled();

  void setClassCacheEnabled(boolean classCacheEnabled);

  /**
   * 用来获取 Reflector 对象的 findForClass() 方法
   *
   * @param type
   * @return
   */
  Reflector findForClass(Class<?> type);
}

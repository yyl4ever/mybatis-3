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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * MapperProxy 是生成 Mapper 接口代理对象的关键，它实现了 InvocationHandler 接口。
 * MapperProxy 则封装了核心的代理逻辑，将拦截到的目标方法委托给对应的 MapperMethod 处理。
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -4724728412955527868L;
  private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
      | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;
  /**
   * 针对 JDK 8 中的特殊处理，该字段指向了 MethodHandles.Lookup 的构造方法。
   */
  private static final Constructor<Lookup> lookupConstructor;
  /**
   * 除了 JDK 8 之外的其他 JDK 版本会使用该字段，该字段指向 MethodHandles.privateLookupIn() 方法。
   */
  private static final Method privateLookupInMethod;
  /**
   * 记录了当前 MapperProxy 关联的 SqlSession 对象。
   * 在与当前 MapperProxy 关联的代理对象中，会用该 SqlSession 访问数据库。
   */
  private final SqlSession sqlSession;
  /**
   * Mapper 接口类型，也是当前 MapperProxy 关联的代理对象实现的接口类型。
   */
  private final Class<T> mapperInterface;
  /**
   * 用于缓存 MapperMethodInvoker 对象的集合。
   * methodCache 中的 key 是 Mapper 接口中的方法，value 是该方法对应的 MapperMethodInvoker 对象。
   */
  private final Map<Method, MapperMethodInvoker> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  static {
    Method privateLookupIn;
    try {
      privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
    } catch (NoSuchMethodException e) {
      privateLookupIn = null;
    }
    privateLookupInMethod = privateLookupIn;

    Constructor<Lookup> lookup = null;
    if (privateLookupInMethod == null) {
      // JDK 1.8
      try {
        lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        lookup.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
            e);
      } catch (Exception e) {
        lookup = null;
      }
    }
    lookupConstructor = lookup;
  }

  // 被拦截的方法在这里处理
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      // 如果是 Object 的方法，就走这个分支
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else {
        // 我们的方法就会走到这个分支
        // 执行代理逻辑以及目标方法
        return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  /**
   * 首先会查询 methodCache 缓存，如果查询的方法为 default 方法，则会根据当前使用的 JDK 版本，
   * 获取对应的 MethodHandle 并封装成 DefaultMethodInvoker 对象写入缓存；
   * 如果查询的方法是非 default 方法，则创建 PlainMethodInvoker 对象写入缓存。
   * @param method
   * @return
   * @throws Throwable
   */
  private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {

    // 尝试从methodCache缓存中查询方法对应的MapperMethodInvoker

    try {
      // 如果方法在缓存中没有对应的MapperMethodInvoker，则进行创建
      return methodCache.computeIfAbsent(method, m -> {
        // 如果是接口里面的 default 方法，就走这个分支
        if (m.isDefault()) {// 针对default方法的处理
          // 这里根据JDK版本的不同，获取方法对应的MethodHandle的方式也有所不同
          // 在JDK 8中使用的是lookupConstructor字段，而在JDK 9中使用的是
          // privateLookupInMethod字段。获取到MethodHandle之后，会使用
          // DefaultMethodInvoker进行封装
          try {
            if (privateLookupInMethod == null) {
              return new DefaultMethodInvoker(getMethodHandleJava8(method));
            } else {
              return new DefaultMethodInvoker(getMethodHandleJava9(method));
            }
          } catch (IllegalAccessException | InstantiationException | InvocationTargetException
              | NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        } else {
          // 对于其他方法，会创建MapperMethod并使用PlainMethodInvoker封装
          // 我们的方法会走到这个分支
          // new MapperMethod：将接口，方法和 sqlSession 设置进 MapperMethod 的属性中
          // 返回了一个静态内部类的实例
          return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
        }
      });
    } catch (RuntimeException re) {
      Throwable cause = re.getCause();
      throw cause == null ? re : cause;
    }
  }

  private MethodHandle getMethodHandleJava9(Method method)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Class<?> declaringClass = method.getDeclaringClass();
    return ((Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup())).findSpecial(
        declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
        declaringClass);
  }

  private MethodHandle getMethodHandleJava8(Method method)
      throws IllegalAccessException, InstantiationException, InvocationTargetException {
    final Class<?> declaringClass = method.getDeclaringClass();
    return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
  }

  interface MapperMethodInvoker {
    Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
  }

  private static class PlainMethodInvoker implements MapperMethodInvoker {
    private final MapperMethod mapperMethod;

    public PlainMethodInvoker(MapperMethod mapperMethod) {
      super();
      this.mapperMethod = mapperMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      // 将参数传入 mapperMethod 对象中执行
      // 直接执行MapperMethod.execute()方法完成方法调用
      return mapperMethod.execute(sqlSession, args);
    }
  }

  private static class DefaultMethodInvoker implements MapperMethodInvoker {
    private final MethodHandle methodHandle;

    public DefaultMethodInvoker(MethodHandle methodHandle) {
      super();
      this.methodHandle = methodHandle;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      // 首先将MethodHandle绑定到一个实例对象上，然后调用invokeWithArguments()方法执行目标方法
      return methodHandle.bindTo(proxy).invokeWithArguments(args);
    }
  }
}

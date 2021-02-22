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
package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wraps a database connection.
 * Handles the connection lifecycle that comprises: its creation, preparation, commit/rollback and close.
 *
 * @author Clinton Begin
 *  MyBatis 中对数据库事务的抽象，其中定义了提交事务、回滚事务，以及获取事务底层数据库连接的方法。
 */
public interface Transaction {

  /**
   * Retrieve inner database connection.
   * @return DataBase connection
   * @throws SQLException
   *           the SQL exception
   */
  Connection getConnection() throws SQLException;

  /**
   * 在日常使用数据库事务的时候，我们最常用的操作就是提交和回滚事务，
   * Transaction 接口将这两个操作抽象为 commit() 方法和 rollback() 方法。
   * 在 commit() 方法和 rollback() 方法中，JdbcTransaction
   * 都是通过 java.sql.Connection 的同名方法实现事务的提交和回滚的。
   */
  /**
   * Commit inner database connection.
   * @throws SQLException
   *           the SQL exception
   */
  void commit() throws SQLException;

  /**
   * Rollback inner database connection.
   * @throws SQLException
   *           the SQL exception
   */
  void rollback() throws SQLException;

  /**
   * Close inner database connection.
   * @throws SQLException
   *           the SQL exception
   */
  void close() throws SQLException;

  /**
   * Get transaction timeout if set.
   *
   * @return the timeout
   * @throws SQLException
   *           the SQL exception
   */
  Integer getTimeout() throws SQLException;

}

package cn.dinodev.sql.builder.clause;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.dialect.Dialect;

public interface ClauseSupport<T extends SqlBuilder> {

  /**
   * 返回当前构建器实例（用于链式调用）。
   * 
   * @return 当前构建器实例
   */
  T self();

  /**
   * 获取当前使用的数据库方言。
   * 
   * @return 数据库方言实例
   */
  Dialect dialect();
}

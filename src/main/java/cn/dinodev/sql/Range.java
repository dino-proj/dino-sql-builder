// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

import java.io.Serializable;

/**
 * 范围接口，定义了获取开始和结束值的通用方法
 * @param <T> 范围值的类型，必须实现Serializable接口
 * @author Cody Lu
 * @since 2022-03-07
 */

public interface Range<T extends Serializable> extends Serializable {
  /**
   * 开始
   * @return 返回开始值
   */
  T getBegin();

  /**
   * 结束
   * @return 返回结束值
   */
  T getEnd();

  /**
   * 创建一个新的范围对象。
   *
   * @param begin 范围的起始值
   * @param end 范围的结束值
   * @param <T> 范围值的类型，必须实现 Serializable 接口
   * @return 包含指定起始值和结束值的 Range 实例
   */
  static <T extends Serializable> Range<T> of(T begin, T end) {
    return new Range<>() {
      private static final long serialVersionUID = 1L;

      @Override
      public T getBegin() {
        return begin;
      }

      @Override
      public T getEnd() {
        return end;
      }
    };
  }
}

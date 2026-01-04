// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.utils;

import java.util.Locale;

/**
 * 命名工具类，提供字符串命名转换的便捷方法
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public final class StringUtils {

  private static final char UNDER_SCORE_CHAR = '_';

  private StringUtils() {
  }

  /**
   * 将下划线风格（snake_case）命名字符串转换为驼峰命名（camelCase）字符串。
   * 例如：user_name -> userName
   *
   * @param name 下划线风格命名字符串，如 "user_name"。
   * @return 转换后的驼峰命名字符串，如 "userName"。
   */
  public static String toCamel(String name) {
    if (isBlank(name)) {
      // garbage in, garbage out
      return name;
    }
    StringBuilder result = new StringBuilder();
    boolean nextIsUpper = false;
    if (name.length() > 1 && name.charAt(1) == UNDER_SCORE_CHAR) {
      result.append(Character.toUpperCase(name.charAt(0)));
    } else {
      result.append(Character.toLowerCase(name.charAt(0)));
    }
    for (int i = 1; i < name.length(); i++) {
      char currentChar = name.charAt(i);
      if (currentChar == UNDER_SCORE_CHAR) {
        nextIsUpper = true;
      } else {
        if (nextIsUpper) {
          result.append(Character.toUpperCase(currentChar));
          nextIsUpper = false;
        } else {
          result.append(Character.toLowerCase(currentChar));
        }
      }
    }
    return result.toString();
  }

  /**
   * 将驼峰命名（camelCase 或 PascalCase）字符串转换为下划线风格（snake_case）命名。
   * 常见转换示例：
   * <ul>
   *   <li>"userName" -> "user_name"</li>
   *   <li>"UserName" -> "user_name"</li>
   *   <li>"USER_NAME" -> "user_name"</li>
   *   <li>"user_name" -> "user_name"</li>
   *   <li>"user" -> "user"</li>
   *   <li>"User" -> "user"</li>
   *   <li>"USER" -> "user"</li>
   *   <li>"_user" -> "user"</li>
   *   <li>"_User" -> "user"</li>
   *   <li>"__user" -> "_user"</li>
   *   <li>"user__name" -> "user__name"</li>
   * </ul>
   *
   * @param name 待转换的字符串
   * @return 转换后的下划线风格字符串
   */
  public static String toSnake(String name) {
    if (name == null) {
      return name;
    }
    int length = name.length();
    StringBuilder result = new StringBuilder(length * 2);
    int resultLength = 0;
    boolean wasPrevTranslated = false;
    for (int i = 0; i < length; i++) {
      char currentChar = name.charAt(i);
      // skip first starting underscore
      if (i > 0 || currentChar != UNDER_SCORE_CHAR) {
        if (Character.isUpperCase(currentChar)) {
          if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != UNDER_SCORE_CHAR) {
            result.append(UNDER_SCORE_CHAR);
            resultLength++;
          }
          currentChar = Character.toLowerCase(currentChar);
          wasPrevTranslated = true;
        } else {
          wasPrevTranslated = false;
        }
        result.append(currentChar);
        resultLength++;
      }
    }
    return resultLength > 0 ? result.toString() : name;
  }

  /**
   * 根据方法名（如 getXxx、setXxx、isXxx）提取属性名。
   * 例如：getName -> name，isActive -> active。
   *
   * @param name 方法名，需以 "get"、"set" 或 "is" 开头
   * @return 属性名（首字母小写）
   * @throws IllegalArgumentException 如果方法名不符合规范
   */
  public static String methodToProperty(String name) {
    if (name.startsWith("is")) {
      name = name.substring(2);
    } else if (name.startsWith("get") || name.startsWith("set")) {
      name = name.substring(3);
    } else {
      throw new IllegalArgumentException(
          "Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }
    boolean hasSecondCharLowerCase = name.length() > 1
        && !Character.isUpperCase(name.charAt(1));
    if (name.length() == 1 || hasSecondCharLowerCase) {
      name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }
    return name;
  }

  /**
   * 获取字符序列的长度。
   *
   * @param cs 字符序列
   * @return 长度，若为 null 返回 0
   */
  public static int length(final CharSequence cs) {
    return cs == null ? 0 : cs.length();
  }

  /**
   * 判断字符序列是否为 null、空串或仅包含空白字符。
   *
   * @param cs 字符序列
   * @return 若为空或仅空白字符返回 true，否则返回 false
   */
  public static boolean isBlank(final CharSequence cs) {
    final int strLen = length(cs);
    if (strLen == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * 判断字符序列是否不为 null、非空且包含非空白字符。
   *
   * @param cs 字符序列
   * @return 若包含非空白字符返回 true，否则返回 false
   */
  public static boolean isNotBlank(final CharSequence cs) {
    return !isBlank(cs);
  }

  /**
   * 统计字符在字符串中出现的次数。
   *
   * @param str 字符序列
   * @param ch 需要统计的字符
   * @return 出现次数
   */
  public static int countMatches(final CharSequence str, final char ch) {
    if (isEmpty(str)) {
      return 0;
    }
    int count = 0;
    for (int i = 0; i < str.length(); i++) {
      if (ch == str.charAt(i)) {
        count++;
      }
    }
    return count;
  }

  /**
   * 判断字符序列是否为 null 或空串。
   *
   * @param cs 字符序列
   * @return 若为 null 或空串返回 true，否则返回 false
   */
  public static boolean isEmpty(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  /**
   * 如果字符串未被指定字符包裹，则自动包裹。
   * 例如 wrapIfMissing("abc", '"') -> "\"abc\""
   *
   * @param str 原始字符串
   * @param wrapWith 包裹字符，如 '"' 或 '\''
   * @return 包裹后的字符串
   */
  public static String wrapIfMissing(final String str, final char wrapWith) {
    if (isEmpty(str) || wrapWith == '\0') {
      return str;
    }
    final boolean wrapStart = str.charAt(0) != wrapWith;
    final boolean wrapEnd = str.charAt(str.length() - 1) != wrapWith;
    if (!wrapStart && !wrapEnd) {
      return str;
    }

    final StringBuilder builder = new StringBuilder(str.length() + 2);
    if (wrapStart) {
      builder.append(wrapWith);
    }
    builder.append(str);
    if (wrapEnd) {
      builder.append(wrapWith);
    }
    return builder.toString();
  }

  /**
   * 判断字符序列是否包含指定子串。
   *
   * @param seq 原始字符序列
   * @param searchSeq 需要查找的子串
   * @return 包含返回 true，否则返回 false
   */
  public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
    if (seq == null || searchSeq == null) {
      return false;
    }
    return indexOf(seq, searchSeq, 0) >= 0;
  }

  /**
   * 判断字符序列是否包含指定字符。
   *
   * @param seq 原始字符序列
   * @param searchChar 需要查找的字符
   * @return 包含返回 true，否则返回 false
   */
  public static boolean contains(final CharSequence seq, final int searchChar) {
    if (isEmpty(seq)) {
      return false;
    }
    return indexOf(seq, searchChar, 0) >= 0;
  }

  /**
   * 查找子串在字符序列中的索引位置。
   *
   * @param cs 原始字符序列
   * @param searchChar 需要查找的子串
   * @param start 起始查找位置
   * @return 首次出现的索引，未找到返回 -1
   */
  static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
    if (cs instanceof String) {
      return ((String) cs).indexOf(searchChar.toString(), start);
    }
    if (cs instanceof StringBuilder) {
      return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
    }
    if (cs instanceof StringBuffer) {
      return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
    }
    return cs.toString().indexOf(searchChar.toString(), start);
  }

  /**
   * 查找字符在字符序列中的索引位置。
   * 支持 Unicode 补充字符。
   *
   * @param cs 原始字符序列
   * @param searchChar 需要查找的字符（int 类型，支持补充字符）
   * @param start 起始查找位置
   * @return 首次出现的索引，未找到返回 -1
   */
  static int indexOf(final CharSequence cs, final int searchChar, int start) {
    if (cs instanceof String) {
      return ((String) cs).indexOf(searchChar, start);
    }
    final int sz = cs.length();
    if (start < 0) {
      start = 0;
    }
    if (searchChar < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
      for (int i = start; i < sz; i++) {
        if (cs.charAt(i) == searchChar) {
          return i;
        }
      }
      return -1;
    }
    // 补充字符处理
    if (searchChar <= Character.MAX_CODE_POINT) {
      final char[] chars = Character.toChars(searchChar);
      for (int i = start; i < sz - 1; i++) {
        final char high = cs.charAt(i);
        final char low = cs.charAt(i + 1);
        if (high == chars[0] && low == chars[1]) {
          return i;
        }
      }
    }
    return -1;
  }
}

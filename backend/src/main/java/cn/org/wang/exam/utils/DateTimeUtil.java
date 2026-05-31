package cn.org.wang.exam.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供日期时间格式化、获取当前时间等功能
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/28 13:39
 */
public class DateTimeUtil {

    /**
     * 私有构造器，防止实例化工具类
     */
    private DateTimeUtil() {
    }

    /**
     * 日期格式模式：yyyy-MM-dd
     */
    private static String dataFormat = "yyyy-MM-dd";

    /**
     * 日期时间格式模式：yyyy-MM-dd HH:mm:ss
     */
    private static String format = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前日期时间
     * 使用yyyy-MM-dd HH:mm:ss格式
     *
     * @return 当前日期时间
     */
    public static LocalDateTime getDateTime() {
        return LocalDateTime.parse(datetimeToStr(LocalDateTime.now()), DateTimeFormatter.ofPattern(format));
    }

    /**
     * 获取当前日期
     * 使用yyyy-MM-dd格式
     *
     * @return 当前日期
     */
    public static LocalDate getDate() {
        return LocalDate.parse(dateToStr(LocalDate.now()), DateTimeFormatter.ofPattern(dataFormat));
    }

    /**
     * 将LocalDateTime格式化为字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 需要转换的日期时间对象
     * @return 格式化后的字符串
     */
    public static String datetimeToStr(LocalDateTime dateTime) {
        return DateTimeFormatter.ofPattern(format).format(dateTime);

    }

    /**
     * 将LocalDate格式化为字符串
     * 格式：yyyy-MM-dd
     *
     * @param date 需要转换的日期对象
     * @return 格式化后的字符串
     */
    public static String dateToStr(LocalDate date) {
        return DateTimeFormatter.ofPattern(dataFormat).format(date);

    }

}

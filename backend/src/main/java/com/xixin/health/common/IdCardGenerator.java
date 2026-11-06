package com.xixin.health.common;

/**
 * 身份证号工具 —— 生成模拟身份证号（仅用于开发演示）
 * 格式：6位地区码 + 8位出生日期 + 3位顺序码 + 1位校验码
 * 第17位奇数为男、偶数为女
 */
public class IdCardGenerator {

    // 常见地区代码（前6位）
    private static final String[][] REGIONS = {
        {"110101", "北京市东城区"},
        {"110102", "北京市西城区"},
        {"110105", "北京市朝阳区"},
        {"110106", "北京市丰台区"},
        {"110108", "北京市海淀区"},
        {"310101", "上海市黄浦区"},
        {"310104", "上海市徐汇区"},
        {"310115", "上海市浦东新区"},
        {"440101", "广州市市辖区"},
        {"440103", "广州市荔湾区"},
        {"440104", "广州市越秀区"},
        {"440305", "深圳市南山区"},
        {"440306", "深圳市宝安区"},
        {"330101", "杭州市市辖区"},
        {"330106", "杭州市西湖区"},
        {"320101", "南京市市辖区"},
        {"320102", "南京市玄武区"},
        {"500101", "重庆市万州区"},
        {"510101", "成都市市辖区"},
        {"510107", "成都市武侯区"},
    };

    /**
     * 生成18位身份证号
     * @param birthDate 出生日期 yyyyMMdd 格式
     * @param isMale true=男 false=女
     * @return 18位身份证号
     */
    public static String generate(String birthDate, boolean isMale) {
        if (birthDate == null || birthDate.length() != 8) {
            birthDate = "19900101";
        }
        // 随机选择地区
        String region = REGIONS[(int) (Math.random() * REGIONS.length)][0];
        // 顺序码（3位），第17位奇数为男、偶数为女
        int seq = 100 + (int) (Math.random() * 900);
        if (isMale) {
            seq = (seq / 10) * 10 + 1 + (int) (Math.random() * 4) * 2; // 奇数
            if (seq % 2 == 0) seq++;  // 确保奇数
        } else {
            seq = (seq / 10) * 10 + (int) (Math.random() * 5) * 2; // 偶数
            if (seq % 2 == 1) seq++;  // 确保偶数
        }
        // 17位本体
        String body = region + birthDate + String.format("%03d", seq);
        // 计算校验码
        char checkChar = calcCheckChar(body);
        return body + checkChar;
    }

    /**
     * 根据身份证号获取性别
     */
    public static String getGender(String idCard) {
        if (idCard == null || idCard.length() < 18) return "未知";
        int digit17 = Integer.parseInt(idCard.substring(16, 17));
        return (digit17 % 2 == 1) ? "男" : "女";
    }

    /**
     * 根据身份证号计算年龄
     */
    public static int getAge(String idCard) {
        if (idCard == null || idCard.length() < 18) return 0;
        try {
            int birthYear = Integer.parseInt(idCard.substring(6, 10));
            return java.time.LocalDate.now().getYear() - birthYear;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据身份证号获取出生日期 yyyy-MM-dd
     */
    public static String getBirthDate(String idCard) {
        if (idCard == null || idCard.length() < 14) return "";
        return idCard.substring(6, 10) + "-" + idCard.substring(10, 12) + "-" + idCard.substring(12, 14);
    }

    /**
     * 身份证校验码计算（ISO 7064:1983, MOD 11-2）
     */
    private static char calcCheckChar(String body) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkChars = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (body.charAt(i) - '0') * weights[i];
        }
        return checkChars[sum % 11];
    }
}

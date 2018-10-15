package com.lcx.util;

import java.security.MessageDigest;
import java.util.Stack;

/**
 * @ClassName:PwdEnCoder
 * @Auther: liuchangxin
 * @Date: 2018/8/28 15:23
 * @Description: TODO(加盐加密算法)
 */
public class PwdEnCoder {
    public static void main(String[] args) {
        System.out.println();
    }
      /***
       * 密文加密和解析字典，必须private，可以根据需要打乱这些字符的顺序，打乱后，可以得到不同的密码，最好按需打乱
       */
      private static final char[] array = { 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g',
              'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '8', '5', '2', '7', '3', '6', '4', '0', '9', '1',
              'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X',
              'C', 'V', 'B', 'N', 'M', '+', '-' };
    /**
     * @param number
     *            long类型的10进制数,该数必须大于0
     * @return string类型的密文
     */
    public static String encode(long number) {
        Long rest = number;
        // 创建栈
        Stack<Character> stack = new Stack<Character>();
        StringBuilder result = new StringBuilder(0);
        while (rest >= 1) {
            // 进栈,
            // 也可以使用(rest - (rest / 64) * 64)作为求余算法
            stack.add(array[new Long(rest % 64).intValue()]);
            rest = rest / 64;
        }
        for (; !stack.isEmpty();) {
            // 出栈
            result.append(stack.pop());
        }
        return result.toString();

    }

    /**
     * 支持范围是A-Z,a-z,0-9,+,-
     *
     * @param str
     * @return
     */
    public static long decode(String str) {
        // 倍数
        int multiple = 1;
        long result = 0;
        Character c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(str.length() - i - 1);
            result += decodeChar(c) * multiple;
            multiple = multiple * 64;
        }
        return result;
    }

    private static int decodeChar(Character c) {
        for (int i = 0; i < array.length; i++) {
            if (c == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 哈希盐
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
}

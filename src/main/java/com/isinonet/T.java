package com.isinonet;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by Administrator on 2018-07-19.
 * 2018-07-19
 */
public class T {
    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger("D");
        String s;
        BufferedReader bufferedReader = new BufferedReader(new FileReader("data/comment.txt"));
        while ((s = bufferedReader.readLine()) != null) {
            String[] strings = s.split("\t");
            if(strings.length > 1) {
                logger.info(strings[0]+"\t"+decodeUnicode(strings[1]));
            }
        }

    }

    /*
     * 中文转unicode编码
     */
    public static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }

    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        String[] strings = dataStr.split("\\\\u");
        for (String s : strings) {
            if (s.length() >= 4) {
                char letter = (char) Integer.parseInt(s.substring(0, 4), 16); // 16进制parse整形字符串。
                buffer.append(new Character(letter).toString());
            }
        }
//       while (start > -1) {
//         end = dataStr.indexOf("\\u", start + 2);
//         String charStr = "";
//         if (end == -1) {
//            charStr = dataStr.substring(start + 2, dataStr.length());
//         } else {
//            charStr = dataStr.substring(start + 2, end);
//         }
//         start = end;
//      }
        return buffer.toString();
    }
}

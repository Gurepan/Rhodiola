package info.ilambda.rhodiola.core.util;

import java.util.Properties;

public class StringUtils {
    private StringUtils() {

    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (isEmpty(cs)) {
            return true;
        }
        strLen = cs.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String resolve(Properties properties, String origin) {
        if (isBlank(origin)) {
            throw new NullPointerException("origin is not found");
        }
        int a = origin.indexOf("${") + 2;
        if (a < 0) {
            return origin;
        }
        String mid = origin.substring(a);
        int b = mid.indexOf("}");
        if (b < 0) {
            return origin;
        }
        String last = mid.substring(0, b);
        String res = properties.getProperty(last);
        if (res == null) {
            throw new NullPointerException("No properties about " + last);
        }
        return origin.replace("${" + last + "}", res);
    }

    public static String toString(Object[] objects) {
        if (objects == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object.toString() + System.lineSeparator());
        }
        return builder.toString();
    }
}

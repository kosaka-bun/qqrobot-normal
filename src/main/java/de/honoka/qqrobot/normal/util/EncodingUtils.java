package de.honoka.qqrobot.normal.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 用于编码、解码URL中的专有字符
 */
public class EncodingUtils {

    /**
     * Decodes the passed UTF-8 String using an algorithm that's
     * compatible with
     * JavaScript's <code>decodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The UTF-8 encoded String to be decoded
     * @return the decoded String
     */
    public static String decodeURIComponent(String s) {
        if(s == null) {
            return null;
        }
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    /**
     * Encodes the passed String as UTF-8 using an algorithm that's
     * compatible with
     * JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The String to be encoded
     * @return the encoded String
     */
    public static String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20")
                .replaceAll("%21", "!")
                .replaceAll("%27", "'")
                .replaceAll("%28", "(")
                .replaceAll("%29", ")")
                .replaceAll("%7E", "~");
    }

    /**
     * Private constructor to prevent this class from being instantiated.
     */
    private EncodingUtils() {
        super();
    }
}

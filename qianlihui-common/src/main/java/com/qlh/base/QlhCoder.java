package com.qlh.base;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class QlhCoder {

    public static final String DefaultEncoding = "UTF-8";

    public static String encodeURL(String v) {
        return QlhException.runtime(() -> URLEncoder.encode(v, DefaultEncoding));
    }

    public static String decodeURL(String url) {
        return QlhException.runtime(() -> URLDecoder.decode(url, DefaultEncoding));
    }

    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }
}

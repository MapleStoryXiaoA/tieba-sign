package com.MapleStoryXiaoA.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPUtils {
    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";

    public static byte[] compress(String str, String encoding) {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(str.getBytes().length);
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);

            gzip.write(str.getBytes(encoding));
//            byte[] bytes = out.toByteArray();

            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] bytes = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
//        return new String(Base64Coder.encode(bytes));//Base64Coder 对 压缩数据 进行 编码
    }

    public static byte[] compress(String str) {
        return compress(str, GZIP_ENCODE_UTF_8);
    }

    public static byte[] unCompress(byte[] bytes) {
        int bufferSize = 4096;
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream unGzip = new GZIPInputStream(in, bufferSize);
            byte[] buffer = new byte[bufferSize];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            unGzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes1 = out.toByteArray();

        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes1;
    }

    public static String unCompressToString(byte[] bytes, String encoding) throws IOException {
        int bufferSize = 4096;
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream ungzip;

        ungzip = new GZIPInputStream(in, bufferSize);
        byte[] buffer = new byte[bufferSize];
        int n;
        while ((n = ungzip.read(buffer)) > 0) {
            out.write(buffer, 0, n);
            out.flush();
        }

        ungzip.close();
        in.close();
        String s = out.toString(encoding);
        out.close();
        return s;
    }

    public static String unCompressToString(byte[] bytes) throws IOException {
        return unCompressToString(bytes, GZIP_ENCODE_UTF_8);
    }
}

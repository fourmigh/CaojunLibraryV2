package org.caojun.library.file;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class File2MD5 {

    public static String md5(File file) {

        try {
            byte[] hash;
            byte[] buffer = new byte[8192];
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            int len;
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            hash = md.digest();

            //对生成的16字节数组进行补零操作
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }
}

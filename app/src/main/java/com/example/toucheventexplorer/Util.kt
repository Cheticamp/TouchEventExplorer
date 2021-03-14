package com.example.toucheventexplorer;

import java.security.MessageDigest;

import javax.annotation.Nonnull;

class Util {

    static String hash(@Nonnull String toHash) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            byte[] array = digest.digest(toHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

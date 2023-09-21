package com.takephoto.vendor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {

    public String toSH256(String clearText){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] textBytes = clearText.getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = digest.digest(textBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

}

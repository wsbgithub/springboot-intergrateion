package com.wangsb.springbootintergration.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordGenerator {

    public static void main(String[] args) {
        String password = generate128BitPassword();
        System.out.println("Generated 128-bit password: " + password);
    }

    public static String generate128BitPassword() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest("some_random_string".getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating password", e);
        }
    }
}

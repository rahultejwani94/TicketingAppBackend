package com.example.demo.utilities;

import java.util.UUID;

public class UUIDUtil {
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}


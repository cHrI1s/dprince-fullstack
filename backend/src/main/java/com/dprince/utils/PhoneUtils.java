package com.dprince.utils;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PhoneUtils {
    public static String getPhone(@Nullable Long code, @NonNull Long phone){
        List<String> phoneParts = new ArrayList<>();
        phoneParts.add(code == null ? "+91" : "+"+code);
        phoneParts.add(phone.toString());
        return String.join(" ", phoneParts);
    }
}

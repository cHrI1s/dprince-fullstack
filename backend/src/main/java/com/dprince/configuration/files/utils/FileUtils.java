package com.dprince.configuration.files.utils;

import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final List<String> IMAGE_EXTENSIONS = new ArrayList<>();
    private static final List<String> ALLOWED_EXTENSIONS = new ArrayList<>();
    public static boolean isImage(String mimeType){
        return IMAGE_EXTENSIONS.contains(mimeType);
    }

    private static final List<String> EXCEL_EXTENSIONS = new ArrayList<>();
    public static boolean isExcel(String mimeType){
        return EXCEL_EXTENSIONS.contains(mimeType);
    }

    static {
        IMAGE_EXTENSIONS.add("image/png");
        IMAGE_EXTENSIONS.add("image/jpeg");
        IMAGE_EXTENSIONS.add("image/jpg");

        EXCEL_EXTENSIONS.add("text/csv");

        ALLOWED_EXTENSIONS.addAll(IMAGE_EXTENSIONS);
        ALLOWED_EXTENSIONS.addAll(EXCEL_EXTENSIONS);
    }
}

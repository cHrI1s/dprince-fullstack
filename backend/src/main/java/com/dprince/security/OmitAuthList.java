package com.dprince.security;

import com.dprince.apis.institution.InstitutionController;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;


public final class OmitAuthList {
    public static List<String> getOmitList(){
        List<String> list = new ArrayList<>();
        list.add("/user/login");
        list.add("/user/email-login");
        list.add("/user/phone-login");
        list.add("/user/login-verify-otp");
        list.add("/user/resend-otp");
        // list.add("/user/backup");
        // Uses to create the first super admin
        // list.add("/user/create-super-admin");
        list.add("/files/logo/{fileName:.+}");
        list.add("/files/public-image/{fileName:.+}");
        list.add("/files/get-file/{token:[\\w\\-]+}/{fileName:.+}");


        list.add("/institution/confirm-deletion/{deletionToken}");

        list.add("/plan/get-all");

        list.addAll(InstitutionController.omittedLinks);
        return list;
    }

    /**
     * These endpoints are only used in development deployment
     * @return List<String>
     */
    public static List<String> getDevOmitList(){
        List<String> list = new ArrayList<>();
        list.add("/api-docs");
        list.add("/swagger-ui.html");
        list.add("/swagger-ui/index.html");
        return list;
    }

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    public static boolean isUrlMatched(String url, List<String> antMatchedUrls) {
        for (String pattern : antMatchedUrls) {
            if (antPathMatcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }
}
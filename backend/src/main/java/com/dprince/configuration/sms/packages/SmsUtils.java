package com.dprince.configuration.sms.packages;

public class SmsUtils {
    public static int countSmses(int length){
        int smses = 1;
        int normalSmesLength = length - 160;
        if(normalSmesLength>0){
            double otherSmes = (double) normalSmesLength/145;
            smses += (int) Math.ceil(otherSmes);
        }
        return smses;
    }
}

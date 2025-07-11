package com.dprince.configuration.sms.vos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SmsBalance {
    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ErrorMessage")
    private String errorMessage = "Failed";

    @JsonProperty("Balance")
    private String balance;
    public void populateStats() {
        if(!StringUtils.isEmpty(this.balance)){
            String[] balances = this.balance.split("\\|");
            for (String balanceParts : balances) {
                String[] parts = balanceParts.split(":");
                if(parts.length==2){
                    if(parts[0].equals("Promo")) {
                        Long value = new Long(parts[1]);
                        this.setPromo(value);
                    }
                    if(parts[0].equals("Trans")) {
                        Long value = new Long(parts[1]);
                        this.setTrans(value);
                    }
                }
            }
        }
    }

    private Long promo = 0L;
    private Long trans = 0L;

    public static SmsBalance getFlatInstance(){
        SmsBalance balance = new SmsBalance();
        balance.setTrans(0L);
        balance.setPromo(0L);
        return balance;
    }
}

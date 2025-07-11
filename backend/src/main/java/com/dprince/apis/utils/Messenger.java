package com.dprince.apis.utils;

import com.dprince.entities.InstitutionMember;
import com.dprince.entities.InstitutionReceipt;
import org.springframework.lang.NonNull;

public class Messenger {
    public static String getMessage(String specificMessage,
                                    @NonNull InstitutionMember singleMember){
        specificMessage = specificMessage.replace("{{firstname}}", singleMember.getFirstName().toUpperCase());
        specificMessage = specificMessage.replace("{{lastname}}", singleMember.getLastName().toUpperCase());
        specificMessage = specificMessage.replace("{{fullname}}", singleMember.getFullName().toUpperCase());
        if(singleMember.getInstitution()!=null){
            specificMessage = specificMessage.replace("{{institution}}", singleMember.getInstitution().getName().toUpperCase());
            specificMessage = specificMessage.replace("[[institution]]", singleMember.getInstitution().getName().toUpperCase());

            specificMessage = specificMessage.replace("{{address}}", singleMember.getInstitution().getAddress().toUpperCase());
            specificMessage = specificMessage.replace("[[address]]", singleMember.getInstitution().getAddress().toUpperCase());

            String bibleVerse = singleMember.getInstitution().getBibleVerse(),
                    website = singleMember.getInstitution().getWebsite();
            if(bibleVerse==null) bibleVerse = "";
            if(website==null) website = "";
            specificMessage = specificMessage.replace("{{bibleverse}}", bibleVerse.toUpperCase());
            specificMessage = specificMessage.replace("[[bibleverse]]", bibleVerse.toUpperCase());

            specificMessage = specificMessage.replace("{{thewebsite}}", website);
            specificMessage = specificMessage.replace("[[thewebsite]]", website);

            specificMessage = specificMessage.replace("{{themail}}", singleMember.getInstitution().getEmail().toLowerCase());
            specificMessage = specificMessage.replace("[[themail]]", singleMember.getInstitution().getEmail().toLowerCase());

            specificMessage = specificMessage.replace("{{theaddress}}", singleMember.getInstitution().getAddress().toLowerCase());
            specificMessage = specificMessage.replace("[[theaddress]]", singleMember.getInstitution().getAddress().toLowerCase());

            String phone = "+"+singleMember.getInstitution().getPhoneCode()+singleMember.getInstitution().getPhone();
            specificMessage = specificMessage.replace("{{thephone}}", phone);
            specificMessage = specificMessage.replace("[[thephone]]", phone);
        }

        if(singleMember.getReceipt()!=null){
            InstitutionReceipt receipt = singleMember.getReceipt();
            specificMessage = specificMessage.replace("{{receiptno}}", receipt.getReceiptNo());
            specificMessage = specificMessage.replace("[[receiptno]]", receipt.getReceiptNo());

            long amount = receipt.getTotal();
            specificMessage = specificMessage.replace("{{amount}}", "Rs."+amount);
            specificMessage = specificMessage.replace("[[amount]]", "Rs."+amount);
            specificMessage = specificMessage.replace("{{receiptamount}}", "Rs."+amount);
            specificMessage = specificMessage.replace("[[receiptamount]]", "Rs."+amount);

            String receiptDate = DateUtils.getDateString(receipt.getEntryDate());
            specificMessage = specificMessage.replace("{{receiptdate}}", receiptDate);
            specificMessage = specificMessage.replace("[[receiptdate]]", receiptDate);
            specificMessage = specificMessage.replace("{{date}}", receiptDate);
            specificMessage = specificMessage.replace("[[date]]", receiptDate);

            specificMessage = specificMessage.replace("{{receiptpayment}}", receipt.getPaymentMode().getDisplayName());
            specificMessage = specificMessage.replace("[[receiptpayment]]", receipt.getPaymentMode().getDisplayName());

            if(receipt.getPaidSubscriptions()!=null
                    && !receipt.getPaidSubscriptions().isEmpty()) {
                String paidSubscriptionsString = String.join(", ", receipt.getPaidSubscriptions());
                specificMessage = specificMessage.replace("{{date}}", paidSubscriptionsString.toUpperCase());
                specificMessage = specificMessage.replace("[[date]]", paidSubscriptionsString.toUpperCase());
            }
        }


        // For sms
        specificMessage = specificMessage.replace("[[firstname]]", singleMember.getFirstName().toUpperCase());
        specificMessage = specificMessage.replace("[[lastname]]", singleMember.getLastName().toUpperCase());
        specificMessage = specificMessage.replace("[[fullname]]", singleMember.getFullName().toUpperCase());
        return specificMessage;
    }

    public static String formatToEmail(String specificMessage){
        if(specificMessage==null) return null;
        specificMessage = specificMessage.replace("<p>", "<div>");
        specificMessage = specificMessage.replace("</p>", "</div>");
        return specificMessage;
    }

    public static String formatToSMS(String specificMessage){
        if(specificMessage==null) return null;
        specificMessage = specificMessage.replace("<p>", "\n");
        specificMessage = specificMessage.replace("</p>", "");
        return specificMessage;
    }
}

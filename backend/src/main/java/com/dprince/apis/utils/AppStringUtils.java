package com.dprince.apis.utils;

import java.util.*;

/**
 * * @author Chris Ndayishimiye
 * * @created 12/18/23
 */
public class AppStringUtils {
    public static String ucWords(String input) {
        // Check if the input is null or empty
        if (input == null || input.isEmpty()) return input;
        // Split the input string into words
        String[] words = input.split("\\s+");

        // Use StringBuilder to build the result
        StringBuilder capitalizedString = new StringBuilder();

        // Iterate through each word
        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize the first letter and append the rest of the word
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                capitalizedString.append(capitalizedWord).append(" ");
            }
        }
        // Trim the trailing space and return the result
        return capitalizedString.toString().trim();
    }


    public static List<String> explode(String text, String delimiter){
        List<String> list = new ArrayList<>();
        String[] parts = text.split(delimiter);
        for(String singlePart: parts){
            list.add(singlePart.trim());
        }
        Collections.sort(list);
        return list;
    }

    public static String generateString(){
        return UUID.randomUUID().toString();
    }

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(randomIndex));
        }

        return stringBuilder.toString();
    }

    public static boolean isEmpty(String string){
        if(string==null) return true;
        return string.trim().isEmpty();
    }
}

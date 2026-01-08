package com.khorshed.mybank.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    public static String formatCurrency(double amount) {
        // Format as TAKA (৳)
        return "৳" + String.format("%.2f", amount);
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() != 10) {
            return accountNumber;
        }
        return accountNumber.substring(0, 4) + " " + 
               accountNumber.substring(4, 7) + " " + 
               accountNumber.substring(7);
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        return phone.matches("\\d{10,15}");
    }

    public static boolean isValidAmount(String amount) {
        if (amount == null || amount.isEmpty()) return false;
        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

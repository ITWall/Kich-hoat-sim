package com.viettel.tungns.kichhoatsim;

import java.util.ArrayList;

public class Utils {
    public static String convertSmsPhoneNumberToString(ArrayList smsPhoneNumberList) {
        String smsPhoneNumber = "";
        for (int i = 0; i < smsPhoneNumberList.size(); i++) {
            smsPhoneNumber += smsPhoneNumberList.get(i);
            if (i < smsPhoneNumberList.size() - 1) {
                smsPhoneNumber += ";";
            }
        }
        return smsPhoneNumber;
    }
}

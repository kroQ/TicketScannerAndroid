package com.krok.ticketscanner;

class ConstantsHolder {

    final static String PASWD_SALT = "$2a$06$fNzxjujt9RpeEEgbaDc/n.";
    final static String ERROR = "ERROR";
    final static String FALSE = "FALSE";
    final static String QR_UPDATED_EXIT = "QR_UPDATED_EXIT";
    final static String REGISTER_COMPLETED = "Register completed";
    final static String LOGIN_IN_USE = "LOGIN_IN_USE";
    final static String EVENT_NOT_EXIST = "EVENT_NOT_EXIST";

    //SharedPreferences
    final static String SHARED_PREF_KEY = "SHARED_PREF";
    final static String USER_LOGIN = "USER_LOGIN";
    final static String EVENT_CODE = "EVENT_CODE";
    final static String USER_ID = "USER_ID";
    final static String EVENT_ID = "EVENT_ID";

    //server IP address
    final static String IP_ADDRESS = "http://192.168.188.26:8080";

    //urls
    final static String URL_LOGIN = "/user/login";
    final static String URL_REGISTER = "/user/register";
    final static String URL_EVENT = "/event/";
}

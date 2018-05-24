package com.krok.ticketscanner;

class ConstantsHolder {

    final static String PASWD_SALT = "$2a$06$fNzxjujt9RpeEEgbaDc/n.";
    final static String REGISTER_COMPLETED = "Register completed";
    final static String CREATED_EVENT = "Created event";

    //SharedPreferences
    final static String SHARED_PREF_KEY = "SHARED_PREF";
    final static String USER_LOGIN = "USER_LOGIN";
    final static String EVENT_NAME = "EVENT_NAME";
    final static String USER_ID = "USER_ID";
    final static String EVENT_ID = "EVENT_ID";

    //server IP address
    final static String IP_ADDRESS = "http://192.168.188.26:8080";

    //urls
    final static String URL_LOGIN = "/user/login";
    final static String URL_REGISTER = "/user/register";
    final static String URL_EVENT = "/event/";
    final static String URL_EVENT_ALL = "/event/all/";
    final static String URL_TICKET = "/ticket/";
}

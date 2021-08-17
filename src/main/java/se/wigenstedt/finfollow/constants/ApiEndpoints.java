package se.wigenstedt.finfollow.constants;

/**
 * Created by Julia Wigenstedt
 * Date: 2021-07-26
 * Time: 13:18
 * Project: FinFollow
 * Copyright: MIT
 */
public class ApiEndpoints {

    public static final String LOGIN = "authentication/sessions/bankid";
    public static final String STATUS = "authentication/sessions/bankid/collect";
    public static final String ACCOUNT_OVERVIEW = "account-overview/overview/categorizedAccounts";

    public static final String NORDNET_SESSION = "https://www.nordnet.se/api/2/login/anonymous";
    public static final String NORDNET_AUTOSTART_TOKEN = "https://www.nordnet.se/api/2/authentication/eid/se/bankid/start";
    public static final String NORDNET_BANKID_POLL = "https://www.nordnet.se/api/2/authentication/eid/se/bankid/poll?order_ref=";
}

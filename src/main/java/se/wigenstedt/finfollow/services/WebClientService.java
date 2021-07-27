package se.wigenstedt.finfollow.services;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;


import javax.servlet.http.Cookie;

import java.util.*;

import static se.wigenstedt.finfollow.constants.ApiEndpoints.*;

/**
 * Created by Julia Wigenstedt
 * Date: 2021-07-26
 * Time: 13:12
 * Project: FinFollow
 * Copyright: MIT
 */
@Service
@RequiredArgsConstructor
public class WebClientService {


    private final WebClient webClient = WebClient.create("https://www.avanza.se/_api/");

    public String login() {

        return webClient.post().uri(LOGIN)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String accountOverview(String persistance, String csid, String transId) {
        return webClient.get().uri(ACCOUNT_OVERVIEW)
                .cookie("AZAPERSISTANCE", persistance)
                .cookie("csid",csid)
                .cookie("AZABANKIDTRANSID", transId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<Object> getStatus(String id) throws JSONException {

        String login = "";
        String[] split = null;
        String[] split2 = null;

                //Dessa headers kommer bara initieras om state är "COMPLETE".
        HttpHeaders headers = null;
        HttpHeaders headers2 = null;
        String response = webClient.get().uri(STATUS)
                .cookie("AZABANKIDTRANSID", id)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //Jag skapar ett jsonObject för att lättare kunna komma åt värden till specifika nycklar.
        JSONObject jsonObject = new JSONObject(response);
        if(jsonObject.get("state").equals("COMPLETE")) {
            JSONArray jsonArray =
                    jsonObject.getJSONArray("logins");

            JSONObject object = jsonArray.getJSONObject(0);
            String path = object.get("loginPath").toString();
            String customerId = object.get("customerId").toString();

            WebClient.ResponseSpec result = webClient.get().uri(path.substring(6))
                    .retrieve();

            login = result
                    .bodyToMono(String.class)
                    .block();

            headers = result.toBodilessEntity()
                    .map(HttpEntity::getHeaders)
                    .block();


            WebClient.ResponseSpec collect = webClient.get().uri("authentication/sessions/bankid/collect/" + customerId)
                    .cookie("AZABANKIDTRANSID",id)
                    .retrieve();

            WebClient.ResponseSpec session = webClient.get().uri("https://www.avanza.se/_cqbe/authentication/session/")
                    .cookie("AZABANKIDTRANSID",id)
                    .retrieve();


            JSONObject cookie = new JSONObject(headers);
            String value = cookie.get("Set-Cookie").toString();




            System.out.println(cookie);
            System.out.println(value);

            split = value.split("[=;]");
            System.out.println(split[1]);




            headers2 = session.toBodilessEntity()
                    .map(HttpEntity::getHeaders)
                    .block();
            JSONObject cookie2 = new JSONObject(headers2);
            String value2 = cookie2.get("Set-Cookie").toString();
            split2 = value2.split("[=;]");
            System.out.println(split2[1]);

        }
        assert login != null;
        if(login.isEmpty()) {
            assert response != null;
            return List.of(response);
        } else {
            if(split[0] != null){

                return List.of(login, split[1], split2[1]);
            } else return List.of(login);
        }
    }
}

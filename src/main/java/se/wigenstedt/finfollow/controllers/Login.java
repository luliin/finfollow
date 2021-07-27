package se.wigenstedt.finfollow.controllers;

import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpMessage;
import lombok.RequiredArgsConstructor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import se.wigenstedt.finfollow.services.WebClientService;
import se.wigenstedt.finfollow.util.QrGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Julia Wigenstedt
 * Date: 2021-07-26
 * Time: 12:50
 * Project: FinFollow
 * Copyright: MIT
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("login/bankid")
@Slf4j
public class Login {


    private final WebClientService webClientService;

    @SneakyThrows
    @GetMapping(value = "", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<BufferedImage> login(HttpServletResponse response) {

        //Svaret kommer som en JSON.
        // Vi är dock endast intresserade av autostartToken som är det allra sista värdet i strängen.
        String json = webClientService.login();



        String[] split = json.split("[,:]");
        List<String> list = Arrays.stream(split)
                .map(element -> element.replaceAll("[\"}]", ""))
                .collect(Collectors.toList());

        String output = list.get(list.size() - 1);
        String link = list.get(1);

        Cookie cookie3 = new Cookie("csid", null);
        cookie3.setMaxAge(0);



        ResponseCookie cookie = ResponseCookie.from("AZABANKIDTRANSID", link)
                .httpOnly(true)
                .path("/")
                .build();

        ResponseCookie cookie2 = ResponseCookie.from("csid", null)
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).header(HttpHeaders.SET_COOKIE, cookie2.toString()).body(QrGenerator.generateQR("bankid:///?autostarttoken=" + output + "&redirect=null" ));
    }

    @GetMapping("status")
    public ResponseEntity<String> getStatus(@CookieValue(value = "AZABANKIDTRANSID", defaultValue = "") String sessionId, HttpServletResponse response)  {
        List<String> result = null;
        try {
            result = webClientService.getStatus(sessionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"statusCode\": 500,\"message\": \"Access Denied\",\n" +
                    "  \"time\": \""+ LocalDateTime.now() +"\",\n" +
                    "  \"errors\": [\n" +
                    "    \n" +
                    "  ],\n" +
                    "  \"additional\": {\n" +
                    "    \n" +
                    "  }}");
        }
        if(result.size()==1) {
               return ResponseEntity.ok(result.get(0));
           } else {

               String csid = result.get(1);
               String azapersistance = result.get(2);

               ResponseCookie cookie = ResponseCookie.from("csid", csid)
                       .path("/")
                       .httpOnly(true)
                       .build();
               ResponseCookie cookie2 = ResponseCookie.from("AZAPERSISTANCE", azapersistance)
                       .path("/")
                       .httpOnly(true)
                       .build();
               return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).header(HttpHeaders.SET_COOKIE, cookie2.toString()).body(result.get(0));
           }




    }



    @Bean
    public HttpMessageConverter<BufferedImage> createImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

}

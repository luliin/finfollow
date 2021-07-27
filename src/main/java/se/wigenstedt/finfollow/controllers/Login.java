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

        ResponseCookie cookie = ResponseCookie.from("AZABANKIDTRANSID", link)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(QrGenerator.generateQR("bankid:///?autostarttoken=" + output + "&redirect=null" ));
    }

    @GetMapping("status")
    public ResponseEntity<String> getStatus(@CookieValue(value = "AZABANKIDTRANSID", defaultValue = "") String sessionId, HttpServletResponse response) throws JSONException {
           var result = webClientService.getStatus(sessionId);
           if(result.size()==1) {
               return ResponseEntity.ok((String)result.get(0));
           } else {

               HttpHeaders headers = (HttpHeaders) result.get(1);
               HttpHeaders headers1 = (HttpHeaders) result.get(2);
               log.info(">><< Header: " + headers);
               log.info(">><< Header2: " + headers1);
               String csid = (String) result.get(3);

               ResponseCookie cookie = ResponseCookie.from("csid", csid)
                       .httpOnly(true)
                       .path("/")
                       .build();
               return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).headers(headers1).body((String)result.get(0));
           }




    }



    @Bean
    public HttpMessageConverter<BufferedImage> createImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

}

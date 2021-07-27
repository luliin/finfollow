package se.wigenstedt.finfollow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.wigenstedt.finfollow.services.WebClientService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by Julia Wigenstedt
 * Date: 2021-07-26
 * Time: 15:05
 * Project: FinFollow
 * Copyright: MIT
 */
@RestController
@RequestMapping("account/overview")
@CrossOrigin
@RequiredArgsConstructor
public class AccountOverview {

    private final WebClientService webClientService;

    @GetMapping
    public ResponseEntity<String> accountOverview(@CookieValue(value = "AZAPERSISTANCE", defaultValue = "") String persistence,
                                                  @CookieValue(value = "csid", defaultValue = "") String csid,
                                                  @CookieValue(value = "AZABANKIDTRANSID", defaultValue = "") String transId) {
        try {
            return ResponseEntity.ok(webClientService.accountOverview(persistence, csid, transId));
        } catch (Exception e) {
            UUID uuid = UUID.randomUUID();
            String traceId = uuid.toString().replace("-", "");
            traceId= traceId.substring(0, 16);

            return ResponseEntity.status(401).body("{\"traceId\": \""+traceId+"\",\"statusCode\": 401, \"message\": \"Access Denied\", \"time\": \""+ LocalDateTime.now() +"\", \"errors\": [], \"additional\": {}}");
        }
    }
}

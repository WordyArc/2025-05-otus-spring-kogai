package ru.otus.hw.integration;


import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "httpbin",
        url = "${integrations.httpbin.url:https://httpbin.org}"
)
public interface HttpBinClient {

    @GetMapping(path = "/delay/{seconds}")
    Map<String, Object> delay(@PathVariable("seconds") int seconds);

    @GetMapping(path = "/status/{code}")
    Map<String, Object> status(@PathVariable("code") int code);
}

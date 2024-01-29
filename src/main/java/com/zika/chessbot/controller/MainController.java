package com.zika.chessbot.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zika.chessbot.classes.Search;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500")

public class MainController {

    @GetMapping(value = "/bot")
    @ResponseBody
    public Map<String, String> getMethodName(@RequestParam String fen) {
        Map<String, String> map = new HashMap();
        map.put("response", new Search(fen, 3).decide());
        return map;
    }
}

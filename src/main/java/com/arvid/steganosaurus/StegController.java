package com.arvid.steganosaurus;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class StegController {

    @PostMapping("/file")
    public String fileUpload(@RequestPart(value = "file") MultipartFile file) throws IOException {
        final int CHUNK = 8;
        ArrayList<Character> chars = decode(file.getBytes(), CHUNK, 1);
        log.info("LSB 1: {}", chars.stream().map(String::valueOf).collect(Collectors.joining()));
        chars = decode(file.getBytes(), CHUNK, 2);
        log.info("LSB 2: {}", chars.stream().map(String::valueOf).collect(Collectors.joining()));
        return Arrays.toString(file.getBytes());
    }

    public int getLSB(byte b, int k) {
        return b << -k >>> -k;
    }

    public ArrayList<Character> decode(byte[] bytes, final int CHUNK_SIZE, final int K_LSB) {
        ArrayList<Character> chars = new ArrayList<>();
        List<Integer> lsbs = new ArrayList<>();
        log.info("loopar igenom {}st bytes", bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            int lsb = getLSB(bytes[i], K_LSB);
            lsbs.add(lsb);
            if (i > 0 && i % (CHUNK_SIZE-1) == 0) {
                chars.add(toAscii(lsbs));
                lsbs.clear();
            }
        }
        log.info("resultat blev {}st chars", chars.size());
        return chars;
    }

    public char toAscii(List<Integer> binary) {
        String temp = binary.stream().map(Integer::toBinaryString).map(String::valueOf).collect(Collectors.joining());
        return (char) Integer.parseInt(temp, 2);
    }

    @Bean
    public CommonsMultipartResolver filterMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

}


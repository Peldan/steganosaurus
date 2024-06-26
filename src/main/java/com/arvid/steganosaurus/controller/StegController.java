package com.arvid.steganosaurus.controller;


import com.arvid.steganosaurus.utility.ByteUtility;
import com.arvid.steganosaurus.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.UnsupportedFormatException;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class StegController {

    @PostMapping("/file")
    public String fileUpload(@RequestPart(value = "file") MultipartFile file) throws IOException, UnsupportedFormatException {
        InputStream inputStream = new ByteArrayInputStream(file.getBytes());
        BufferedImage img = ImageIO.read(inputStream);
        WritableRaster raster = img.getRaster();
        DataBufferByte bufferByte = (DataBufferByte) raster.getDataBuffer();
        int offset = FileUtility.getOffset(inputStream);
        List<Character> chars = ByteUtility.decode(bufferByte.getData(), offset, 1);
        log.info("Decoded: {}", chars.stream().map(String::valueOf).collect(Collectors.joining()));
        return Arrays.toString(file.getBytes());
    }

    @Bean
    public CommonsMultipartResolver filterMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

}


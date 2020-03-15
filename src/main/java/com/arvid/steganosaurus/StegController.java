package com.arvid.steganosaurus;


import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

@Slf4j
@RestController
public class StegController {
    @PostMapping("/file")
    public String fileUpload(@RequestPart(value = "file") MultipartFile file) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(file.getBytes());
        BufferedImage img = ImageIO.read(inputStream);
        WritableRaster raster = img.getRaster();
        DataBufferByte bufferByte = (DataBufferByte) raster.getDataBuffer();
        List<Character> chars = decode(bufferByte.getData(), 1);
        log.info("LSB 1: {}", chars.stream().map(String::valueOf).collect(Collectors.joining()));
        chars = decode(bufferByte.getData(), 2);
        log.info("LSB 2: {}", chars.stream().map(String::valueOf).collect(Collectors.joining()));
        return Arrays.toString(file.getBytes());
    }

    public int getLSB(byte b, int k) {
        return b << -k >>> -k;
    }

    public List<Character> decode(byte[] data, final int K_LSB) {
        List<Integer> lsbs = new ArrayList<>();
        for (byte b : data) {
            int lsb = getLSB(b, K_LSB);
            lsbs.add(lsb);
        }
        return new ArrayList<>(toAscii(lsbs));
    }

    public byte[] encode(String text, byte[] data){
        byte[] result = new byte[data.length];
        System.arraycopy(data, 0, result, 0, data.length);
        char[] toHide = text.toCharArray();
        int byteNo = 0;
        for (char c : toHide) {
            byte curr = (byte) c;
            char[] bits = getBits(curr);
            for (char bit : bits) {
                byte toInsertInto = data[byteNo];
                int inserted = toInsertInto;
                int lsb = getLSB(toInsertInto, 1);
                if (lsb != bit) {
                    inserted = (lsb & ~1) | bit;
                }
                result[byteNo] = (byte) inserted;
                byteNo++;
            }
        }
        return result;
    }

    public List<Character> toAscii(List<Integer> binary) {
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < binary.size(); i += 8) {
            List<Integer> subList = binary.subList(i, Math.min((i + 8), binary.size()));
            if (!subList.isEmpty()) {
                strings.add(subList.stream().map(Integer::toBinaryString).map(String::valueOf).collect(Collectors.joining()));
            }
        }
        return strings.stream().map(e -> (char) Integer.parseInt(e, 2)).collect(toCollection(ArrayList::new));
    }

    public char[] getBits(byte b){
        return String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0').toCharArray();
    }

    @Bean
    public CommonsMultipartResolver filterMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

}


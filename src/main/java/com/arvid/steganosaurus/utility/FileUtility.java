package com.arvid.steganosaurus.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.exception.UnsupportedFormatException;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtility {

    @Bean
    private static Tika tika() {
        return new Tika();
    }

    public static int getOffset(InputStream inputStream) throws IOException, UnsupportedFormatException {
        String mime = tika().detect(inputStream);
        int offset;
        if (mime.equals("image/png")) {
            offset = 8;
        } else {
            throw new UnsupportedFormatException("Ej implementerat");
        }
        return offset;
    }

}

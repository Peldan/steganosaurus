package com.arvid.steganosaurus;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

@Slf4j
public class StegControllerTest {

    StegController controller = new StegController();

    @Test
    public void testGetLSB(){
        byte[] chars = new byte[]{'A', 'R', 'V', 'I', 'D'};
        for(byte b: chars){
            Assertions.assertEquals((char)b & 0b1, controller.getLSB(b, 1));
            Assertions.assertEquals((char)b & 0b11, controller.getLSB(b, 2));
        }
    }

    @Test
    public void testToAscii(){
        List<Integer> integers = Arrays.asList(0,1,0,0,0,0,0,1);
        Assertions.assertEquals('A', controller.toAscii(integers));
    }

    @Test
    public void testExtractLetters(){
        String[] byteStrings = {"64", "66", "67", "68", "69", "70", "71", "72"};
        byte[] bytes = new byte[byteStrings.length];
        int i = 0;
        for(String byteString: byteStrings){
            bytes[i++] = (byte) Integer.parseInt(byteString);
        }
        Assertions.assertEquals('*', controller.decode(bytes, 8, 1).get(0));
    }

    @Test
    public void testExtractFromRealImage() throws IOException {
        File testFile = getTestFile();
        FileInputStream fis = new FileInputStream(testFile);
        ArrayList<Character> chars = controller.decode(fis.readAllBytes(), 8, 1);
        String result = chars.stream().collect(Collector.of(StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
        log.info("Result {}", result);
        Assertions.assertTrue(result.contains("Vafan"));
    }

    private File getTestFile(){
        String resource = "test.png";
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource(resource)).getFile());
    }

}

package com.arvid.steganosaurus;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
        Assertions.assertEquals('A', controller.toAscii(integers).get(0));
    }

    @Test
    public void testExtractFromRealImage() throws IOException {
        File testFile = getTestFile();
        FileInputStream fis = new FileInputStream(testFile);
        WritableRaster raster = ImageIO.read(fis).getRaster();
        DataBufferByte bufferByte = (DataBufferByte) raster.getDataBuffer();
        List<Character> chars = controller.decode(bufferByte.getData(), 1);
        String result = chars.stream().collect(Collector.of(StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
        log.info("Result {}", result);
        log.info("b64: {}", Base64.decodeBase64(result));
        Assertions.assertTrue(result.contains("Vafan"));
    }

    @Test
    public void testEncode(){
        Random r = new Random();
        String alphabet = "abcdefghixyz";
        byte[] before = new byte[24];
        for(int i = 0; i < before.length; i++){
            before[i] = (byte) alphabet.charAt(r.nextInt(alphabet.length()));
        }
        byte[] encoded = controller.encode("LOL", before);
        Assertions.assertEquals("LOL", controller.decode(encoded, 1).stream().map(String::valueOf).collect(Collectors.joining()));
    }

    @Test
    public void testGetBits(){
        Assertions.assertEquals("01000001", new String(controller.getBits((byte) 'A')));
    }

    private File getTestFile(){
        String resource = "vafan.png";
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource(resource)).getFile());
    }

}

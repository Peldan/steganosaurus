package com.arvid.steganosaurus.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.UnsupportedFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static com.arvid.steganosaurus.utility.FileUtility.getOffset;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class ByteUtilityTest {

    @Test
    public void testGetLSB(){
        byte[] chars = new byte[]{'A', 'R', 'V', 'I', 'D'};
        for(byte b: chars){
            Assertions.assertEquals((char)b & 0b1, ByteUtility.getLSB(b, 1));
            Assertions.assertEquals((char)b & 0b11, ByteUtility.getLSB(b, 2));
        }
    }

    @Test
    public void testToAscii(){
        List<Integer> integers = Arrays.asList(0,1,0,0,0,0,0,1);
        Assertions.assertEquals('A', ByteUtility.toAscii(integers).get(0));
    }

    @Test
    public void testExtractFromRealImage() {
        File testFile = getTestFile("images/test2.png");
        try(FileInputStream fis = new FileInputStream(testFile)){
            WritableRaster raster = ImageIO.read(fis).getRaster();
            DataBufferByte bufferByte = (DataBufferByte) raster.getDataBuffer();
            List<Character> chars = ByteUtility.decode(bufferByte.getData(), getOffset(fis),1);
            String result = chars.stream().collect(Collector.of(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append,
                    StringBuilder::toString));
            log.info("Resultat: {}", result);
            Assertions.assertTrue(result.contains("Vafan"));
        } catch (IOException | UnsupportedFormatException ex) {
            fail(ex.getMessage());
        }
    }

    //FIXME
    //@Test
    public void testHideImageInImage() throws Exception {
        File destFile = getTestFile("images/test1.png");
        File srcFile = getTestFile("images/vafan.png");
        FileInputStream fis = new FileInputStream(destFile);
        int offset_dest = getOffset(fis);
        WritableRaster raster = ImageIO.read(fis).getRaster();
        DataBufferByte destBuf = (DataBufferByte) raster.getDataBuffer();
        fis = new FileInputStream(srcFile);
        int offset_in = getOffset(fis);
        raster = ImageIO.read(fis).getRaster();
        DataBufferByte srcBuf = (DataBufferByte) raster.getDataBuffer();
        byte[] result = ByteUtility.encode(srcBuf.getData(), destBuf.getData(), offset_in, offset_dest, 1);
        FileOutputStream fos = new FileOutputStream("result.png");
        fos.write(result);
        byte[] decoded = ByteUtility.decode(result, getOffset(new FileInputStream(fos.getFD())), 1).stream().map(e -> Character.toString(e)).collect(Collectors.joining()).getBytes();
        fos = new FileOutputStream("decoded.png");
        fos.write(decoded);
        fos.close();
        log.info("decoded: {}", Arrays.toString(decoded));
        Assertions.assertEquals(decoded, srcBuf.getData());
    }

    @Test
    public void testEncodeWithOffset() throws Exception {
        Random r = new Random();
        String alphabet = "abcdefghixyz";
        byte[] before = new byte[25];
        for(int i = 0; i < before.length; i++){
            before[i] = (byte) alphabet.charAt(r.nextInt(alphabet.length()));
        }
        byte[] encoded = ByteUtility.encode("LOL".getBytes(), before, 1, 0, 1);
        Assertions.assertEquals("LOL", ByteUtility.decode(encoded, 1,1).stream().map(String::valueOf).collect(Collectors.joining()));
    }

    @Test
    public void testGetBits(){
        Assertions.assertEquals("01000001", new String(ByteUtility.getBits((byte) 'A')));
    }

    private File getTestFile(String file){
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource(file)).getFile());
    }

}

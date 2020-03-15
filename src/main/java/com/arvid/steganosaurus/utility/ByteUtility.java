package com.arvid.steganosaurus.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ByteUtility {
    public static char[] getBits(byte b) {
        return String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0').toCharArray();
    }

    public static List<Character> toAscii(List<Integer> binary) {
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < binary.size(); i += 8) {
            List<Integer> subList = binary.subList(i, Math.min((i + 8), binary.size()));
            if (!subList.isEmpty()) {
                strings.add(subList.stream().map(Integer::toBinaryString).map(String::valueOf).collect(Collectors.joining()));
            }
        }
        return strings.stream().map(e -> (char) Integer.parseInt(e, 2)).collect(toCollection(ArrayList::new));
    }

    public static byte[] encode(byte[] in, byte[] dest, final int K_LSB) {
        byte[] result = new byte[dest.length];
        System.arraycopy(dest, 0, result, 0, dest.length);
        char[] toHide = new String(in, StandardCharsets.UTF_8).toCharArray();
        int byteNo = 0;
        for (char c : toHide) {
            byte curr = (byte) c;
            char[] bits = ByteUtility.getBits(curr);
            for (char bit : bits) {
                byte toInsertInto = dest[byteNo];
                int inserted = toInsertInto;
                int lsb = ByteUtility.getLSB(toInsertInto, K_LSB);
                if (lsb != bit) {
                    inserted = (lsb & ~1) | bit;
                }
                result[byteNo] = (byte) inserted;
                byteNo++;
            }
        }
        return result;
    }

    public static int getLSB(byte b, int k) {
        return b << -k >>> -k;
    }

    public static List<Character> decode(byte[] data, final int K_LSB) {
        List<Integer> lsbs = new ArrayList<>();
        for (byte b : data) {
            int lsb = getLSB(b, K_LSB);
            lsbs.add(lsb);
        }
        return new ArrayList<>(toAscii(lsbs));
    }
}

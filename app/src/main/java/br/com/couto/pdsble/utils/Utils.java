package br.com.couto.pdsble.utils;

public class Utils {

    public static String hexArrayToString(byte[] message) {
        String aux = "";
        for (int i = 0; i < message.length; i++) {
            aux = aux + Utils.hexValues(message[i]) + " ";
        }

        return aux;
    }

    public static String hexValues(byte b) {
        return "0x" + Integer.toHexString(unsignedByteToInt(b));
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}

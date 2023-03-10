package com.threerings.projectx.hazard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.MessageDigest;

/**
 * @author Leego Yih
 */
public class ProjectXDigester {
    private static final String HAZARD_PATH = "dist/code/projectx-hazard.jar";
    private static final String GETDOWN_PATH = "dist/getdown.txt";
    private static final String DIGEST_PATH = "dist/digest.txt";
    private static final String DIGEST_FORMAT = "getdown.txt = %s\n" +
            "code/config.jar = 97dc52917be4b477baab19650c1e6e44\n" +
            "code/projectx-config.jar = 5af64d60145a37b15cba457f20a2e477\n" +
            "code/projectx-pcode.jar = dc9696cf73a2e0014ff496ca120caf1f\n" +
            "code/lwjgl.jar = dc1155da5323de2e0296ee57a4114722\n" +
            "code/lwjgl_util.jar = 804cce88f36362a1443f6d605c0c6416\n" +
            "code/jinput.jar = f46bd97bfc6bade5860d547cba0f3418\n" +
            "code/jutils.jar = b249c7cc6a5561975566edd27e545aeb\n" +
            "code/jshortcut.jar = a47e2845c0eb3a0697685ee6a954dc97\n" +
            "code/commons-beanutils.jar = 1f3a38860fe4c9b6c73536813a99bb66\n" +
            "code/commons-digester.jar = a4dd528366512d2e9440d571ba301a06\n" +
            "code/commons-logging.jar = 073a5c734e213bcbbb43af48ed479da3\n" +
            "code/getdown-pro-new.jar = f95d57efba9682f4a3587ec2d5b5343b\n" +
            "native/liblwjgl64.so = 37c8b24a9b8e3be2fab5c15ce5aee1ba\n" +
            "native/liblwjgl.so = 73f4a63a1913340b6fbeb797e7fa2f74\n" +
            "native/libjinput-linux.so = b7fa3ebd479701d6082ef567caa60ab4\n" +
            "native/libjinput-linux64.so = 0199e951eb87a477509af2efd3ef5666\n" +
            "native/libopenal64.so = c37b4211431a33a4ab014ef47b05b363\n" +
            "native/libopenal.so = 88132ac290c53e72094a0709185adb21\n" +
            "native/libkeybd.so = e58259216295a562768d27b3b8844ede\n" +
            "native/libfroth.so = d9c8efef3a03e73712adcaf8e77318ea\n" +
            "native/lwjgl64.dll = 73cb2a967d771c65bc46c543f61a9898\n" +
            "native/lwjgl.dll = 0e11fae8d0307d26e5e2e3d2e91611bd\n" +
            "native/jinput-dx8.dll = c390eac9fe17e51f346d814196654667\n" +
            "native/jinput-dx8_64.dll = 417e444a48b2946b5c09a5984d75a034\n" +
            "native/jinput-raw.dll = 27bd561b5e1c06677cf7f759f3c6b640\n" +
            "native/jinput-raw_64.dll = f061d876f007c7ca36dde6d91e3db7b0\n" +
            "native/OpenAL64.dll = 5a4fb0e69f0064c2caa41c2650d15f5e\n" +
            "native/OpenAL32.dll = 9c24ed831ddfa8319382b2bfd9691aa9\n" +
            "native/froth.dll = 38102f2e515ebc5626da7edc05fb3a8d\n" +
            "native/froth64.dll = 222d00a516b3717a6428c9c3da5bd89e\n" +
            "native/liblwjgl.dylib = 9462601db63f1cb88a21d43393529431\n" +
            "native/libjinput-osx.jnilib = f07b536d41a8e3609a9353a080b3fe17\n" +
            "native/openal.dylib = bc569baa70d2bb599af6a939de356cb2\n" +
            "native/libfroth.dylib = 3e0bbbf6bc91d35480d0fc1521db3fd8\n" +
            "background.png = 50eed6569dbebe3e4e290f092507bf25\n" +
            "progress.png = a33e8cafb98f940707fc98df71d9e6ed\n" +
            "icon_128.png = f3692c1ff8c59df61946c9dd0f4a0ef4\n" +
            "icon_32.png = 14d391606d9349a6a53ad25e22090234\n" +
            "icon_16.png = ed41cb20ac02b9d26f23625e9944c7ca\n" +
            "playnow.png = 7cc7e9fa3f53f8a66c7dcd3fd160296c\n" +
            "playnow_de.png = 8923b784d68aa3f74d6259aeafd8ad4a\n" +
            "playnow_es.png = 8f6a1412013409656a1ed15a9ea1a8b0\n" +
            "playnow_fr.png = 105c8e47c136732722561bac42e7130b\n" +
            "rsrc/intro-bundle.jar = b593cc2773ec0c4c4085721eaa423e03\n" +
            "rsrc/full-music-bundle.jar = 6bc4743b82b3cd59d9775423c01042b3\n" +
            "rsrc/full-rest-bundle.jar = ef3a136793d5903e57cac166b9f4497e\n" +
            "code/projectx-hazard.jar = %s\n";

    public static void main(String[] args) throws Exception {
        String hazardMD5 = md5(HAZARD_PATH);
        String getdownMD5 = md5(GETDOWN_PATH);
        String digestContent = String.format(DIGEST_FORMAT, getdownMD5, hazardMD5);
        String digestMD5 = md5(digestContent.getBytes("UTF-8"));
        System.out.printf("projectx-hazard:%s\ngetdown.txt:%s\ndigest.txt:%s\n%n", hazardMD5, getdownMD5, digestMD5);

        FileWriter writer = new FileWriter(DIGEST_PATH);
        writer.write(digestContent + "digest.txt = " + digestMD5 + "\n");
        writer.flush();
        writer.close();
    }

    public static String md5(String path) throws Exception {
        File file = new File(path);
        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        return md5(data);
    }

    public static String md5(byte[] data) throws Exception {
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return encodeHex(hash);
    }

    /** Table for byte to hex string translation. */
    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /** Table for HEX to DEC byte translation. */
    private static final int[] DEC = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};

    public static int getDec(int index) {
        // Fast for correct values, slower for incorrect ones
        try {
            return DEC[index - 48];
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    public static byte getHex(int index) {
        return (byte) HEX[index];
    }

    public static String encodeHex(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        int i = 0;
        char[] chars = new char[bytes.length << 1];
        for (byte b : bytes) {
            chars[i++] = HEX[(b & 0xf0) >> 4];
            chars[i++] = HEX[b & 0x0f];
        }
        return new String(chars);
    }

    public static byte[] decodeHex(String input) {
        if (input == null) {
            return null;
        }
        if ((input.length() & 1) == 1) {
            // Odd number of characters
            throw new IllegalArgumentException("Odd digits");
        }
        char[] inputChars = input.toCharArray();
        byte[] result = new byte[input.length() >> 1];
        for (int i = 0; i < result.length; i++) {
            int upperNibble = getDec(inputChars[2 * i]);
            int lowerNibble = getDec(inputChars[2 * i + 1]);
            if (upperNibble < 0 || lowerNibble < 0) {
                // Non hex character
                throw new IllegalArgumentException("Non hex");
            }
            result[i] = (byte) ((upperNibble << 4) + lowerNibble);
        }
        return result;
    }
}

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Steganography {
    public static void main(String[] args) {
       new Steganography();
    }

    byte[] contents;
    StringBuilder[] binContents;
    public Steganography() {
//      <=========MAIN METHOD==========>
        mainMethod();

//      <=========DEBUG================>
//        debug();

//      <=========RAND================>
//        bmpEditingPractice("Test.bmp");
    }

    private void bmpEditingPractice(String imgName) {
        try {
            File f = new File("src/main/Images/Default/tiger.bmp");
            contents = Files.readAllBytes(f.toPath());

            //convert each byte from 0-255 into a binary string
            binContents = byteToBinary(contents);

            int c = 0, b = 0, g = 0, r = 0;;
            for (int i = 54; i < binContents.length; i++) {
                //pixel data starts at byte 54 everything before that is important file information
                StringBuilder binByte = binContents[i];
                //KEY: make rgb pixels with byte val, converting each to binary, then adding to pixel data
                //Image width: 300 BGR pixels
                Random rand = new Random();
                String[] nextBGRpixel = new String[3];
                nextBGRpixel[0] = Integer.toBinaryString(b); //Blue
                nextBGRpixel[1] = Integer.toBinaryString(g); //Green
                nextBGRpixel[2] = Integer.toBinaryString(r); //Red

                binByte.replace(0, binByte.length(), nextBGRpixel[c]);
                c++;
                if (c == 3) {
                    c = 0;
                    b = rand.nextInt(255);
                    g = rand.nextInt(255);
                    r = rand.nextInt(255);
                }

            }

            imgFromBytes(stringToBytes(binContents), imgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mainMethod() {
        try {
            File f = new File("src/main/Images/Default/tiger.bmp");
            contents = Files.readAllBytes(f.toPath());
            //convert each byte from 0-255 into a binary string
            binContents = byteToBinary(contents);

            String msg = "Hello how are you?";
            System.out.println("BinMsg: " + msgToBinary(msg));
            encodeMessage(binContents, msg, 54);

            byte[] encodedContents = stringToBytes(binContents);
            String fileName = "Encoded_Image.bmp";
            imgFromBytes(encodedContents, fileName);

            File encodedImg = new File("src/main/Images/Encoded/" + fileName);
            byte[] encImgBytes = Files.readAllBytes(encodedImg.toPath());
            String binMsg = getBinMessage(byteToBinary(encImgBytes), 54, msg.length());
            System.out.println("Decoded Msg: " + java.util.Arrays.toString(binMsg.split("(?<=\\G........)")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBinMessage(StringBuilder[] encodedBin, int start, int msgLen) throws IOException {
        StringBuilder[] LSB = leastSigBits(encodedBin);
        StringBuilder masterString = new StringBuilder();

        for (int i = start; i < start + msgLen*8/2; i++) { //20 = msgLen * 8bits / 2LSB
            StringBuilder s = LSB[i];
            masterString.append(s.toString());
        }

        return masterString.toString();
    }

    //Take in full BinImgData --> modify LSB (starting at byte 54) to be the message
    private void encodeMessage(StringBuilder[] binContents, String msg, int start) {
        String binMsg = msgToBinary(msg);
        char[] arrBinMsg = binMsg.toCharArray();
        int mI = 0;
        for (int i = start; i < binContents.length; i++) {
            //pixel data starts at byte 54 everything before that is important file information
            StringBuilder binByte = binContents[i];
            if (mI < arrBinMsg.length - 1) {
                binByte.replace(binByte.length()-2, binByte.length()-1, String.valueOf(arrBinMsg[mI])); //replaces second from last char
                binByte.replace(binByte.length()-1, binByte.length(), String.valueOf(arrBinMsg[++mI])); //replaces last char
            } else {
                break;
            }
            mI++;
        }

    }

    private String msgToBinary(String message) {
        StringBuilder result = new StringBuilder();
        for (char c: message.toCharArray()) {
            result.append(String.format("%8s", Integer.toBinaryString(c)).replaceAll(" ", "0"));
        }

        return result.toString();
    }

    private byte[] stringToBytes(StringBuilder[] binString) {
        byte[] res = new byte[binString.length];
        int j = 0;
        for (StringBuilder s: binString) {
            res[j++] = (byte) Long.parseLong(String.valueOf(s), 2);
        }
        return res;
    }

    private void imgFromBytes(byte[] bytes, String imgName) throws IOException {
        BufferedImage bImg = ImageIO.read(new ByteArrayInputStream(bytes));
        ImageIO.write(bImg, "bmp", new File("src/main/Images/Encoded/" + imgName));
    }

    private StringBuilder[] byteToBinary(byte[] contents) {
        //create string array of the binary representation of each byte ["1000010", "00000000", ...]
        StringBuilder[] binContents = new StringBuilder[contents.length];
        int j = 0;
        for (byte b: contents) {
            StringBuilder binString = new StringBuilder(Integer.toBinaryString(b));
            while (binString.length() < 8) { //make every binary string 8 characters long
                binString.insert(0, "0");
            }

            binContents[j] = new StringBuilder(binString.toString());
            j++;
        }

        return binContents;
    }

    private StringBuilder[] leastSigBits(StringBuilder[] binContents) {
        StringBuilder[] LSBits = new StringBuilder[binContents.length];

        int j = 0;
        for (StringBuilder s: binContents) {
            LSBits[j] = new StringBuilder(s.substring(s.length() - 2));
            j++;
        }

        return LSBits;
    }
}


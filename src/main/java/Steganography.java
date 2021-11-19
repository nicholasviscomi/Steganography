import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import Utilities.FileHelper;

public class Steganography {
    public static void main(String[] args) {
       new Steganography();
    }

    byte[] contents;
    StringBuilder[] binContents;
    String BWImgPath = "src/main/Images/Default/1bit.bmp";
    String TigerImgPath = "src/main/Images/Default/tiger.bmp";
    String LenaImgPath = "src/main/Images/Default/lena512.bmp";
    String DoggieImgPath = "src/main/Images/Default/doggie.bmp";

    FileHelper fh = new FileHelper();
    public Steganography() {
//      <=========MAIN METHOD==========>
        try {
            String msg = fh.readTxtFromFile("src/main/RandTextFile.txt");
            mainMethod(msg, "Doggie", DoggieImgPath);
        } catch (Exception e) { e.printStackTrace(); }

//      <=========DEBUG================>
//        debug();
    }

    private void mainMethod(String msg, String newDirName, String srcImagePath) {
        try {
            File f = new File(srcImagePath);
            contents = Files.readAllBytes(f.toPath());
            int start = getPixelOffset(contents); //header field showing where image data starts

            //convert each byte from 0-255 into a binary string
            binContents = byteToBinary(contents, 24);

            //Puts the message into the 8th bit of every piece of pixel data
            encodeMessage(binContents, msg, start, 1);

            //turns the string sof binary intro bytes that can then be turned into an image
            byte[] encodedContents = binaryToBytes(binContents);
            cleanOutputDir(newDirName);
            imgFromBytes(encodedContents, "EncodedImage.bmp", newDirName);

            File encodedImg = new File("src/output/" + newDirName + "/EncodedImage.bmp");
            byte[] encImgBytes = Files.readAllBytes(encodedImg.toPath());

            String binMsg = getBinMessage(byteToBinary(encImgBytes, 24), getPixelOffset(encImgBytes), msg.length(), 1);
            String plainTxtMsg = binaryToMsg(binMsg);

            File decodedMsgFile = new File("src/output/" + newDirName + "/EncodedMessage.txt");
            FileWriter fw = new FileWriter(decodedMsgFile.getPath(), false);
            fw.write(plainTxtMsg);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void debug() {
        StringBuilder[] test;
//        = {
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100"),
//                new StringBuilder("01001101"), new StringBuilder("01011111"), new StringBuilder("01001100"), new StringBuilder("01001100")
//        };

        try {
            File f = new File(DoggieImgPath);
            test = byteToBinary(Files.readAllBytes(f.toPath()), 24);

            String msg = "Fart";
            System.out.println("BinMsg: " + java.util.Arrays.toString(msgToBinary(msg).split("(?<=\\G........)")));//msgToBinary returns the correct output
//            System.out.println("Reg Bin Contents: " + Arrays.toString(test));
            encodeMessage(test, msg, 0, 1);
//            System.out.println("Enc Bin Contents: " + Arrays.toString(test));

            String decMsg = getBinMessage(test, 0, msg.length(), 1);
            System.out.println(binaryToMsg(decMsg));
//            System.out.println("Decoded Msg: " + java.util.Arrays.toString(decMsg.split("(?<=\\G........)")));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getPixelOffset(byte[] contents) {
        byte[] pixelOffsetBytes = { contents[10], contents[11], contents[12], contents[13] };
        return java.nio.ByteBuffer.wrap(pixelOffsetBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
    private byte[] headerForImage(String path) {
        File file;
        byte[] header = new byte[54];
        try {
            file = new File(path);
            BufferedImage bImg = ImageIO.read(file);
            int height = bImg.getHeight(), width = bImg.getWidth();
        //-------Block 1------ (14 bytes)
            //FileType: 2 bytes
                //'BM'
            header[0] = 66;
            header[1] = 77;

            //FileSize: 4 bytes

            //Reserved: 2 bytes
            header[6] = 0;
            header[7] = 0;
            //Reserved: 2 bytes
            header[8] = 0;
            header[9] = 0;

            //PixelDataOffset:4 bytes (index 10-13)
                //number of bytes between the start of the file and the first byte of pixel data


        //------Block 2----- (40 bytes)
            //HeaderSize: 4 bytes
                //should be 40
            //ImageWidth: 4 bytes
                //width of final image in pixels

            //ImageHeight: 4 bytes
                //see above
            //Planes: 2 bytes
                //should be 1 in decimal
            //BitsPerPixel: 2 bytes
                //Should be 1 for this use
            //Compression: 4 bytes
                //should be 0
            //ImageSize: 4 bytes
                //should be 0
            //XpixelsPerMeter: 4 bytes
                //set to 0
            //YpixelsPerMeter: 4 bytes
                //set to 0
            //TotalColors: 4 bytes
                //number of colors in the palette (probably be 2 for black and white)
            //ImportantColors: 4 bytes
                //ignored by setting to 0
        //-----Block 3: Color Palette------
            /* The integer value of the pixel points to the color index in this table and
            that color is printed on the screen.
            Each entry in this table is 4 bytes: 3 to show intensity of RGB, last is set to 0

             If the first entry (index 0) is black, any pixel with the integer value 0 will be black
             If the second entry (index 1) is white, any pixel with the integer value 1 will be black
             */
            //2 entries: first black, second white

        } catch (Exception e) {
            e.printStackTrace();
        }
        return header;
    }

    private String readHeader(String path) {
        StringBuilder res = new StringBuilder();
        try {
            File file = new File(path);
            byte[] bytes = Files.readAllBytes(file.toPath());
            for (int i = 0; i < 54; i++) {
                res.append(bytes[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res.toString();
    }

    //retrieves the message hidden in the least significant bit of the image contents
    private String getBinMessage(StringBuilder[] encodedBin, int start, int msgLen, int numLSB) {
        StringBuilder[] LSB = leastSigBits(encodedBin, numLSB);
        StringBuilder masterString = new StringBuilder();

        //loops for the length of the message
        for (int i = start; i < start + msgLen*8/numLSB; i++) {
            StringBuilder s = LSB[i];
            masterString.append(s.toString());
        }

        return masterString.toString();
    }

    //Take in full BinImgData --> modify LSB (starting at byte 54) to be the message
    private void encodeMessage(StringBuilder[] binContents, String msg, int start, int numLSB) {
        String binMsg = msgToBinary(msg);
        char[] arrBinMsg = binMsg.toCharArray();
        int mI = 0;
        for (int i = start; i < binContents.length; i++) {
            //pixel data starts at byte 54 everything before that is important file information
            StringBuilder binByte = binContents[i];
            if (mI < arrBinMsg.length) {
                binByte.replace(binByte.length()-1, binByte.length(), String.valueOf(arrBinMsg[mI])); //replaces last char
            } else {
                break;
            }
            mI++;
        }

    }

    private String binaryToMsg(String binary) {
        StringBuilder res = new StringBuilder();
        String[] binStrings = binary.split("(?<=\\G........)"); //splits it up every 8 character (8bit ints)
        for (String s: binStrings) {
            int num = Integer.parseInt(s, 2);
            res.append((char) num);
        }
        return res.toString();
    }

    private String msgToBinary(String message) {
        StringBuilder result = new StringBuilder();
        for (char c: message.toCharArray()) {
            result.append(String.format("%8s", Integer.toBinaryString(c)).replaceAll(" ", "0"));
        }

        return result.toString();
    }

    private byte[] binaryToBytes(StringBuilder[] binString) {
        byte[] res = new byte[binString.length];
        int j = 0;
        for (StringBuilder s: binString) {
            res[j++] = (byte) Long.parseLong(String.valueOf(s), 2); //turns a string of binary into a long then a byte
        }
        return res;
    }

    private void cleanOutputDir(String newDirName) throws IOException {
        File dir = new File("src/output/" + newDirName);
        for (File f: Objects.requireNonNull(dir.listFiles())) {
            Files.deleteIfExists(Path.of("src/output/" + newDirName + "/" + f.getName()));
        }
    }

    private void imgFromBytes(byte[] bytes, String imgName, String newDirName) throws IOException {
        BufferedImage bImg = ImageIO.read(new ByteArrayInputStream(bytes));

        //creates a new directory to place the encoded image and decoded text
        File dir = new File("src/output/" + newDirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File f = new File("src/output/" + newDirName + "/" + imgName);
        if (f.createNewFile()) {
            ImageIO.write(bImg, "bmp", f);
        }
    }

    private StringBuilder[] byteToBinary(byte[] contents, int bpp) {
        //create string array of the binary representation of each byte ["1000010", "00000000", ...]
        StringBuilder[] binContents = new StringBuilder[contents.length];
        if (bpp == 24) {
            int j = 0;
            for (byte b: contents) {
                StringBuilder binString = new StringBuilder(Integer.toBinaryString(b));
                //make every binary string 8 characters long (turns 0 --> 00000000)
                while (binString.length() < 8) {
                    binString.insert(0, "0");
                }

                binContents[j] = new StringBuilder(binString.toString());
                j++;
            }
        } else if (bpp == 1) {
            //add functionality for black and white (1bpp) images

        }
        return binContents;
    }

    private StringBuilder[] leastSigBits(StringBuilder[] binContents, int numLSB) {
        //Skims the LSB from binary contents (takes the 1 from 01001101)
        StringBuilder[] LSBits = new StringBuilder[binContents.length];
        int j = 0;
        for (StringBuilder s: binContents) {
            LSBits[j] = new StringBuilder(s.substring(s.length() - numLSB));
            j++;
        }

        return LSBits;
    }
}


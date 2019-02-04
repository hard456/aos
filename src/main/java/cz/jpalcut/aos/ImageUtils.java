package cz.jpalcut.aos;

import org.apache.commons.math3.complex.Complex;

import java.awt.image.BufferedImage;

public class ImageUtils {

    private static int convertRGBToGrayLevel(int value){
        int r = (value >> 16) & 0xFF;
        int g = (value >> 8) & 0xFF;
        int b = (value & 0xFF);
        return  (r + g + b) / 3;
    }

    public static Complex[][] create2DArray(BufferedImage image){
        Complex[][] array = new Complex[image.getHeight()][image.getWidth()];
        for (int i = 0; i < image.getHeight(); i++){
            for (int j = 0; j < image.getWidth(); j++){
                array[i][j] = Complex.valueOf(convertRGBToGrayLevel(image.getRGB(i, j)),0);
            }
        }
        return array;
    }

    public static int convertGrayLevelToRGB(int value){
        return ((value << 16) + (value << 8) + value);
    }

}

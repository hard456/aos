package cz.jpalcut.aos;

import org.apache.commons.math3.complex.Complex;

import java.awt.image.BufferedImage;

public class ImageUtils {



    /**
     * Převede RGB do stupně šedi
     * @param img BufferedImage
     * @return BufferedImage
     */
    public static BufferedImage convertImgToGreyScale(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); ++x) {
            for (int y = 0; y < img.getHeight(); ++y) {
                int rgb = img.getRGB(x, y);
                int grayLevel = convertPixelToGrayLevel(rgb);
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                img.setRGB(x, y, gray);
            }
        }
        return img;
    }

    public static int convertPixelToGrayLevel(int pixel){
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = (pixel & 0xFF);
        return  (r + g + b) / 3;
    }

    public static Complex[][] create2DArray(BufferedImage image){
        Complex[][] array = new Complex[image.getHeight()][image.getWidth()];
        for (int i = 0; i < image.getHeight(); i++){
            for (int j = 0; j < image.getWidth(); j++){
                array[i][j] = Complex.valueOf(convertPixelToGrayLevel(image.getRGB(j, i)),0);
            }
        }
        return array;
    }


}

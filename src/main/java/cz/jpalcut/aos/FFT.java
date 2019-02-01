package cz.jpalcut.aos;

import java.awt.image.BufferedImage;

public class FFT {



    public BufferedImage compute(BufferedImage image){
        image = ImageUtils.convertImgToGreyScale(image);
        return image;
    }

}

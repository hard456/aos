package cz.jpalcut.aos;

import org.apache.commons.math3.complex.Complex;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FFT {

    private boolean inverse;

    public FFT(boolean inverse) {
        this.inverse = inverse;
    }

    public Complex[][] compute(Complex[][] matrix){
//        System.out.println("W="+image.getWidth()+":H="+image.getHeight());

        //řádky
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = recursiveFFT(matrix[i]);
        }

        //sloupce
        for (int i = 0; i < matrix.length; i++) {
            matrix = setColumn(matrix, i, recursiveFFT(getColumn(matrix, i)));
        }

        return matrix;
    }

    public Complex[] recursiveFFT(Complex[] row){
        double angle;
        Complex[] result = new Complex[row.length];
        Complex[] evenIndexes, oddIndexes;
        Complex coefficient;

        if(row.length == 1){
            result[0] = row[0];
            return result;
        }

        evenIndexes = recursiveFFT(getEvenIndexes(row));
        oddIndexes = recursiveFFT(getOddIndexes(row));

        for (int i = 0; i < row.length / 2; i++){
            if(!inverse){
                angle = -2.0 * Math.PI * i / row.length;
                coefficient = new Complex(1.0 * Math.cos(angle), 1.0 * Math.sin(angle));
            }
            else{
                angle = 2.0 * Math.PI * i / row.length;
                coefficient = new Complex(1.0 * Math.cos(angle), 1.0 * Math.sin(angle));
            }

            result[i] = evenIndexes[i].add(oddIndexes[i].multiply(coefficient));
            result[i + row.length / 2] = evenIndexes[i].subtract(oddIndexes[i].multiply(coefficient));
        }
        return result;
    }

    public double getMaxIntensity(Complex[][] matrix){
        double intensity, maxValue = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                Complex number = matrix[i][j];

                if (number == null) {
                    continue;
                }
                intensity = Math.sqrt(number.getReal() * number.getReal() + number.getImaginary() * number.getImaginary());
                maxValue = Math.max(maxValue, intensity);
            }
        }
        return maxValue;
    }

    public BufferedImage createFFTImage(BufferedImage image, Complex[][] matrix){
        double intensity;
        double maxValue = getMaxIntensity(matrix);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                Complex c = matrix[i][j];
                if (c == null) {
                    c = new Complex(0.0, 0.0);
                }
                double value = Math.sqrt(c.getReal() * c.getReal() + c.getImaginary() * c.getImaginary());

                intensity = Math.log(value) / Math.log(maxValue);
                int rgb = Color.HSBtoRGB(0f, 0f, (float) intensity);
                image.setRGB(i, j, rgb);
            }
        }
        return image;
    }

    public BufferedImage createIFFTImage(BufferedImage image, Complex[][] matrix){
        int value;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                Complex c = matrix[i][j];
                value = (int)(c.getReal()/(matrix.length * matrix[0].length));
                image.setRGB(i, j, ImageUtils.convertGrayLevelToRGB(value));
            }
        }
        return image;
    }

    public BufferedImage centerFFTImage(BufferedImage image){
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        newImage.getGraphics().drawImage(image, 0, 0, w / 2, h / 2, w / 2, h / 2, w, h, null);
        newImage.getGraphics().drawImage(image, w / 2, h / 2, w, h, 0, 0, w / 2, h / 2, null);
        newImage.getGraphics().drawImage(image, w / 2, 0, w, h / 2, 0, h / 2, w / 2, h, null);
        newImage.getGraphics().drawImage(image, 0, h / 2, w / 2, h, w / 2, 0, w, h / 2, null);

        return newImage;
    }

    public Complex[] getOddIndexes(Complex[] row){
        Complex[] array = new Complex[row.length / 2];
        for (int i = 0; i < array.length; i++){
            array[i] = row[2 * i + 1];
        }
        return array;
    }

    public Complex[] getEvenIndexes(Complex[] row){
        Complex[] array = new Complex[row.length / 2];
        for (int i = 0; i < array.length; i++){
            array[i] = row[2 * i];
        }
        return array;
    }

    public Complex[] getColumn(Complex[][] matrix, int index){
        Complex[] column = new Complex[matrix.length];
        for (int i = 0; i < matrix.length; i++){
            column[i] = matrix[i][index];
        }
        return column;
    }

    public Complex[][] setColumn(Complex[][] matrix, int index, Complex[] column){
        for (int i = 0; i < matrix.length; i++){
            matrix[i][index] = column[i];
        }
        return matrix;
    }

}

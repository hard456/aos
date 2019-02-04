package cz.jpalcut.aos;

import org.apache.commons.math3.complex.Complex;

import java.awt.image.BufferedImage;

public class FFT {

    public Complex[][] compute(BufferedImage image){
        System.out.println("W="+image.getWidth()+":H="+image.getHeight());
        image = ImageUtils.convertImgToGreyScale(image);
//        int[][] img = ImageUtils.convertBufferedImageTo2DArray(image);
        Complex[][] matrix = ImageUtils.create2DArray(image);
        Complex[] tmp2 = new Complex[matrix.length];
        Complex[][] tmp = new Complex[image.getWidth()][image.getWidth()];
//TODO:
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++){
                tmp2[j] = matrix[i][j];
            }

            for (int k = 0; k < matrix.length; k++){
                tmp[i][k] = recursiveFFT(tmp2)[k];
            }
        }
//
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++){
                tmp2[j] = matrix[j][i];
            }

            for (int k = 0; k < matrix.length; k++){
                matrix[k][i] = recursiveFFT(tmp2)[k];
            }
        }
//        for (int i = 0; i < matrix.length; i++){
//            matrix[i] = recursiveFFT(tmp[i]);
//        }
//
//        for (int i = 0; i < matrix.length; i++){
//            matrix[i] = recursiveFFT(matrix[i]);
//        }

        return matrix;
    }

    public Complex[] recursiveFFT(Complex[] row){
        double magnitude, angle;
        Complex[] result = new Complex[row.length];
        Complex[] evenIndexes, oddIndexes;

        if(row.length == 1){
            result[0] = row[0];
            return result;
        }

        evenIndexes = recursiveFFT(getEvenIndexes(row));
        oddIndexes = recursiveFFT(getOddIndexes(row));

        for (int i = 0; i < row.length / 2; i++){
            magnitude = 1.0;
            angle = -2.0 * Math.PI * i / row.length;
            Complex coefficient = new Complex(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
            result[i] = evenIndexes[i].add(oddIndexes[i].multiply(coefficient));
            result[i + row.length / 2] = evenIndexes[i].subtract(oddIndexes[i].multiply(coefficient));
        }
        return result;
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

}

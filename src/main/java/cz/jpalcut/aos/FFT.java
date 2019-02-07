package cz.jpalcut.aos;

import org.apache.commons.math3.complex.Complex;

import java.awt.image.BufferedImage;

/**
 * Třída pro výpočet FFT/IFFT
 */
public class FFT {

    private boolean inverse;

    /**
     * Konstruktor třídy
     * @param inverse true - pro nastavení IFFT, false - nastavení výpočtu FFT
     */
    public FFT(boolean inverse) {
        this.inverse = inverse;
    }

    /**
     * Vypočítání FFT matice Complex[][]
     * @param matrix Complex[][]
     * @return Complex[][]
     */
    public Complex[][] compute(Complex[][] matrix){
        //řádky
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = recursiveFFT(matrix[i]);
        }

        //sloupce
        for (int i = 0; i < matrix[0].length; i++) {
            matrix = setColumn(matrix, i, recursiveFFT(getColumn(matrix, i)));
        }

        return matrix;
    }

    /**
     * Provádí rekurzivní výpočet FFT pro Complex[]
     * @param row Complex[]
     * @return Complex[]
     */
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

    /**
     * Provede násobení po prvku 2 matic (Complex[][])
     * @param first Complex[][]
     * @param second Complex[][]
     * @return Complex[][]
     */
    public Complex[][] convolution(Complex[][] first, Complex[][] second){
        Complex[][] newMatrix = new Complex[first.length][first[0].length];
        for (int i = 0; i < first.length; i++){
            for (int j = 0; j < first[i].length; j++){
                newMatrix[i][j] = first[i][j].multiply(second[i][j]);
            }
        }
        return newMatrix;
    }

    /**
     * Provede dělení po prvku 2 matic (Complex[][]) podle nastaveného prahu
     * @param first Complex[][]
     * @param second Complex[][]
     * @param threshold double
     * @return Complex[][]
     */
    public Complex[][] deconvolution(Complex[][] first, Complex[][] second, double threshold){
        Complex[][] newMatrix = new Complex[first.length][first[0].length];
        for (int i = 0; i < first.length; i++){
            for (int j = 0; j < first[i].length; j++){
                if(second[i][j].abs() < threshold){
                    second[i][j] = new Complex(0.001,0.0);
                }
                newMatrix[i][j] = first[i][j].divide(second[i][j]);
            }
        }
        return newMatrix;
    }

    /**
     * Vrátí největší intensitu z Complex[][]
     * @param matrix Complex[][]
     * @return double
     */
    public double getMaxIntensity(Complex[][] matrix){
        double intensity, maxValue = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
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

    /**
     * Vytvoří BufferedImage frekvenční oblasti z Complex[][]
     * @param image BufferedImage
     * @param matrix Complex[][]
     * @return BufferedImage
     */
    public BufferedImage createFFTImage(BufferedImage image, Complex[][] matrix){
        double intensity;
        double maxValue = getMaxIntensity(matrix);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                Complex c = matrix[i][j];
                double value = Math.sqrt(c.getReal() * c.getReal() + c.getImaginary() * c.getImaginary());
                intensity = Math.log(value) / Math.log(maxValue);
                intensity = Math.min(255,Math.max(0,intensity*255));
                image.setRGB(j, i, Utils.convertGrayLevelToRGB((int)intensity));
            }
        }
        return image;
    }

    /**
     * Vytvoří BufferedImage časové oblasti z Complex[][]
     * @param image BufferedImage
     * @param matrix Complex[][]
     * @return BufferedImage
     */
    public BufferedImage createIFFTImage(BufferedImage image, Complex[][] matrix){
        int value;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                Complex c = matrix[i][j];
                value = (int)(c.getReal()/(matrix.length * matrix[i].length));
                value = Math.min(255,Math.max(0,value));
                image.setRGB(j, i, Utils.convertGrayLevelToRGB(value));
            }
        }
        return image;
    }

    /**
     * Vycentruje BufferedImage pro zobrazení fourierovy transformace
     * @param image BufferedImage
     * @return BufferedImage
     */
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

    /**
     * Vrátí liché indexy pole
     * @param row Complex[]
     * @return Complex[]
     */
    public Complex[] getOddIndexes(Complex[] row){
        Complex[] array = new Complex[row.length / 2];
        for (int i = 0; i < array.length; i++){
            array[i] = row[2 * i + 1];
        }
        return array;
    }

    /**
     * Vrátí sudé indexy pole
     * @param row Complex[]
     * @return Complex[]
     */
    public Complex[] getEvenIndexes(Complex[] row){
        int length = row.length/2;
        Complex[] array = new Complex[length];
        for (int i = 0; i < length; i++){
            array[i] = row[2 * i];
        }
        return array;
    }

    /**
     * Vrátí sloupec matice Complex[][] podle index
     * @param matrix Complex[][]
     * @param index int
     * @return Complex[]
     */
    public Complex[] getColumn(Complex[][] matrix, int index){
        Complex[] column = new Complex[matrix.length];
        for (int i = 0; i < matrix.length; i++){
            column[i] = matrix[i][index];
        }
        return column;
    }

    /**
     * Upraví sloupec matice Complex[][] podle index
     * @param matrix Complex[][]
     * @param index int
     * @param column Complex[]
     * @return Complex[][]
     */
    public Complex[][] setColumn(Complex[][] matrix, int index, Complex[] column){
        for (int i = 0; i < matrix.length; i++){
            matrix[i][index] = column[i];
        }
        return matrix;
    }

}

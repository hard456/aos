package cz.jpalcut.aos;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.math3.complex.Complex;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Controller {

    @FXML
    public BorderPane imageBorder;

    @FXML
    ImageView imageView;

    private BufferedImage bufferedImage;

    public void open() {
        File file;
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image", "*.jpg",
                "*.jpeg", "*.bmp", "*.png");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(extFilter);
        file = fileChooser.showOpenDialog(null);

        if(file != null){
            try {
                bufferedImage = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        showImage();
    }

    public void quit() {
        System.exit(0);
    }

    public void useFFT() {
        Complex[][] matrix;
        double intensity, maxValue = 0.0;
        if (bufferedImage != null) {
            FFT fft = new FFT();
            matrix = fft.compute(bufferedImage);

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

            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    Complex c = matrix[i][j];
                    if (c == null) {
                        c = new Complex(0.0, 0.0);
                    }
                    double value = Math.sqrt(c.getReal() * c.getReal() + c.getImaginary() * c.getImaginary());

                    intensity = value / maxValue;
                    int rgb = Color.HSBtoRGB(0f, 0f, (float) intensity);
                    bufferedImage.setRGB(i, j, rgb);
                }


            }
            int w = matrix.length;
            int h = w;

            BufferedImage newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());

            newImage.getGraphics().drawImage(bufferedImage, 0, 0, w / 2, h / 2, w / 2, h / 2, w, h, null);
            newImage.getGraphics().drawImage(bufferedImage, w / 2, h / 2, w, h, 0, 0, w / 2, h / 2, null);
            newImage.getGraphics().drawImage(bufferedImage, w / 2, 0, w, h / 2, 0, h / 2, w / 2, h, null);
            newImage.getGraphics().drawImage(bufferedImage, 0, h / 2, w / 2, h, w / 2, 0, w, h / 2, null);

            bufferedImage = newImage;

            showImage();
        }

    }

    private void showImage(){
        if(bufferedImage != null){
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//            imageView.setPreserveRatio(true);
//            imageView.fitWidthProperty().bind(Main.getStage().widthProperty());
//            imageView.fitHeightProperty().bind(Main.getStage().heightProperty());
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);
            imageView.setImage(image);
        }
    }


}

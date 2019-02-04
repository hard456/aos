package cz.jpalcut.aos;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.commons.math3.complex.Complex;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Controller {

    @FXML
    public BorderPane imageBorder;

    @FXML
    ImageView imageView;

    private BufferedImage bufferedImage;

    private Complex[][] matrixFFT;

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
        showImage(bufferedImage);
    }

    public void quit() {
        System.exit(0);
    }

    public void useInverseFFT(){
        Complex[][] matrix;
        if(matrixFFT != null){
            FFT fft = new FFT(true);
            matrix = fft.compute(matrixFFT);
            bufferedImage = fft.createIFFTImage(bufferedImage, matrix);
            showImage(bufferedImage);
        }
    }

    public void useFFT() {
        if (bufferedImage != null) {
            FFT fft = new FFT(false);
            Complex[][] matrix = ImageUtils.create2DArray(bufferedImage);
            matrix = fft.compute(matrix);
            matrixFFT = matrix;
            bufferedImage = fft.createFFTImage(bufferedImage, matrix);
            bufferedImage = fft.centerFFTImage(bufferedImage);
            showImage(bufferedImage);
        }
    }

    private void showImage(BufferedImage img){
        if(img != null){
            Image image = SwingFXUtils.toFXImage(img, null);
//            imageView.setPreserveRatio(true);
//            imageView.fitWidthProperty().bind(Main.getStage().widthProperty());
//            imageView.fitHeightProperty().bind(Main.getStage().heightProperty());
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);
            imageView.setImage(image);
        }
    }


}

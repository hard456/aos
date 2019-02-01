package cz.jpalcut.aos;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;


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

    public void useFFT(){
        if(bufferedImage != null){
            FFT fft = new FFT();
            bufferedImage = fft.compute(bufferedImage);
            showImage();
        }
    }

    private void showImage(){
        if(bufferedImage != null){
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(Main.getStage().widthProperty());
            imageView.fitHeightProperty().bind(Main.getStage().heightProperty());
            imageView.setImage(image);
        }
    }


}

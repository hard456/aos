package cz.jpalcut.aos;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.commons.math3.complex.Complex;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Controller pro správu gui.fxml
 */
public class Controller {

    @FXML
    Button FFTButton, IFFTButton, convolutionButton, deconvolutionButton;

    @FXML
    TextField convolutionMask, deconvolutionMask, thresholdField;

    @FXML
    MenuItem openMI, saveAsMI;

    @FXML
    ImageView imageView;

    @FXML
    Label statusText;

    private BufferedImage bufferedImage;

    private Complex[][] matrixImage;

    /**
     * Načtení obrázku
     */
    public void open() {
        File file;
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image", "*.jpg",
                "*.jpeg", "*.bmp", "*.png");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(extFilter);
        file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                bufferedImage = ImageIO.read(file);
            } catch (IOException e) {
                setStatus("Nastala chyba při načtení obrázku.", "RED");
            }

        }

        if (!Utils.isNumberPowerOfTwo(bufferedImage.getWidth()) || !Utils.isNumberPowerOfTwo(bufferedImage.getHeight())) {
            bufferedImage = null;
            imageView.setImage(null);
            disableAllButtons();
            openMI.setDisable(false);
            setStatus("Výška nebo šířka obrázku nemá velikost 2^n.", "RED");
        } else {
            showImage();
            disableAllButtons();
            FFTButton.setDisable(false);
            saveAsMI.setDisable(false);
            setStatus("Obrázek byl načten.", "GREEN");
        }

    }

    /**
     * Uložení obrázku
     */
    public void saveAs(){
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg",
                    "*.jpeg", "*.bmp");
            fileChooser.getExtensionFilters().addAll(extFilter);

            fileChooser.setTitle("Uložit obrázek");
            fileChooser.setInitialFileName(String.valueOf(new Random().nextInt()*1000));
            File saveFile = fileChooser.showSaveDialog(Main.getStage());
            if (saveFile != null) {
                try {
                    BufferedImage saveImage = bufferedImage;
                    ImageIO.write(saveImage, "png", saveFile);
                    setStatus("Obrázek byl uložen.", "GREEN");
                } catch (IOException ex) {
                    setStatus("Obrázek se nepodařilo uložit.", "RED");
                }
            }
    }

    /**
     * Aplikace inverzní FFT na matici
     */
    public void useInverseFFT() {
        Task task = new Task<Void>() {
            @Override
            public Void call() {
        Complex[][] matrix;
        FFT fft = new FFT(true);
        matrix = fft.compute(matrixImage);
        bufferedImage = fft.createIFFTImage(bufferedImage, matrix);
        showImage();
        IFFTButton.setDisable(true);
        FFTButton.setDisable(false);
        convolutionButton.setDisable(true);
        deconvolutionButton.setDisable(true);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            enableButtonsAfterIFFT();
            setStatus("Byla provedena IFFT.", "GREEN");
        });
        disableAllButtons();
        setStatus("Provádí se IFFT.", "BLUE");
        new Thread(task).start();


    }

    /**
     * Aplikace FFT na obrázek
     */
    public void useFFT() {
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                FFT fft = new FFT(false);
                Complex[][] matrix = Utils.create2DArray(bufferedImage);
                matrix = fft.compute(matrix);
                matrixImage = matrix.clone();
                bufferedImage = fft.createFFTImage(bufferedImage, matrix);
                bufferedImage = fft.centerFFTImage(bufferedImage);
                showImage();
                FFTButton.setDisable(true);
                IFFTButton.setDisable(false);
                convolutionButton.setDisable(false);
                deconvolutionButton.setDisable(false);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            enableButtonsAfterFFT();
            setStatus("Byla provedena FFT.", "GREEN");
        });
        disableAllButtons();
        setStatus("Provádí se FFT.", "BLUE");
        new Thread(task).start();
    }

    /**
     * Zobrazí přenásobený FFT obrázek maskou, na kterou se aplikuje FFT
     */
    public void useFilterConvolution() {

        double[][] filter = Utils.parseArray(convolutionMask.getText());

        if (filter == null) {
            setStatus("Filtr nebyl definován správně.", "RED");
            return;
        }

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                Complex[][] complexes;
                complexes = Utils.arrayToComplexArray(filter, bufferedImage.getWidth(), bufferedImage.getHeight());
                FFT fft = new FFT(false);
                complexes = fft.compute(complexes);
                complexes = fft.convolution(matrixImage, complexes);
                matrixImage = complexes.clone();
                FFT ifft = new FFT(true);
                complexes = ifft.compute(complexes);
                bufferedImage = fft.createIFFTImage(bufferedImage, complexes);
                showImage();
                IFFTButton.setDisable(true);
                FFTButton.setDisable(false);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            enableButtonsAfterFilter();
            setStatus("Byla provedena konvoluce.", "GREEN");
        });
        disableAllButtons();
        setStatus("Provádí se konvoluce.", "BLUE");
        new Thread(task).start();
    }

    /**
     * Zobrazí vydělený FFT obrázek maskou, na kterou se aplikuje FFT
     */
    public void useFilterDeconvolution() {
        double[][] filter = Utils.parseArray(deconvolutionMask.getText());

        if (filter == null) {
            setStatus("Filtr nebyl definován správně.", "RED");
            return;
        }
        Double threshold = Utils.parseDoubleNumber(thresholdField.getText());

        if (threshold == null) {
            setStatus("Práh nebyl definován správně.", "RED");
            return;
        }

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                Complex[][] complexes;
                complexes = Utils.arrayToComplexArray(filter, bufferedImage.getWidth(), bufferedImage.getHeight());
                FFT fft = new FFT(false);
                complexes = fft.compute(complexes);
                complexes = fft.deconvolution(matrixImage, complexes, threshold);
                matrixImage = complexes.clone();
                FFT ifft = new FFT(true);
                complexes = ifft.compute(complexes);
                bufferedImage = fft.createIFFTImage(bufferedImage, complexes);
                showImage();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            enableButtonsAfterFilter();
            setStatus("Byla provedena dekonvoluce.", "GREEN");
        });
        disableAllButtons();
        setStatus("Provádí se dekonvoluce.", "BLUE");
        new Thread(task).start();
    }

    /**
     * Zobrazení obrázku
     */
    private void showImage() {
        if (bufferedImage != null) {
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);
            imageView.setImage(image);
        }
    }

    /**
     * Nastavení statusu
     * @param text String
     * @param color String
     */
    private void setStatus(String text, String color) {
        statusText.setText(text);
        statusText.setTextFill(Color.web(color));
    }

    /**
     * Zablokování všech tlačítek
     */
    private void disableAllButtons() {
        openMI.setDisable(true);
        saveAsMI.setDisable(true);
        FFTButton.setDisable(true);
        IFFTButton.setDisable(true);
        convolutionButton.setDisable(true);
        deconvolutionButton.setDisable(true);
    }

    /**
     * Odblokování tlačítek po provedení filtru
     */
    private void enableButtonsAfterFilter() {
        FFTButton.setDisable(false);
        openMI.setDisable(false);
        saveAsMI.setDisable(false);
        convolutionButton.setDisable(false);
        deconvolutionButton.setDisable(false);
    }

    /**
     * Odblokování tlačítek po použití FFT
     */
    private void enableButtonsAfterFFT(){
        openMI.setDisable(false);
        saveAsMI.setDisable(false);
        IFFTButton.setDisable(false);
        convolutionButton.setDisable(false);
        deconvolutionButton.setDisable(false);
    }

    /**
     * Odblokování tlačítek po použití IFFT
     */
    private void enableButtonsAfterIFFT(){
        openMI.setDisable(false);
        saveAsMI.setDisable(false);
        FFTButton.setDisable(false);
    }

}

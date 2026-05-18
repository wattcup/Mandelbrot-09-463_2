package ru.gr0946x.ui.saveAndOpen;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.Painter;

public class FractalSaver {

    public enum SaveFormat {
        FRAC("Фрактал (.frac)", "frac"),
        JPG("JPEG изображение (.jpg)", "jpg"),
        PNG("PNG изображение (.png)", "png");

        private final String description;
        private final String extension;

        SaveFormat(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }

        public String getDescription() {
            return description;
        }

        public String getExtension() {
            return extension;
        }
    }

    public static boolean saveFractal(JFrame parent, Painter painter, Converter conv) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить фрактал");

        FileNameExtensionFilter filterFrac = fracFilter();

        fileChooser.addChoosableFileFilter(filterFrac);
        fileChooser.addChoosableFileFilter(jpgFilter());
        fileChooser.addChoosableFileFilter(pngFilter());

        fileChooser.setFileFilter(filterFrac);

        if (fileChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File selectedFile = fileChooser.getSelectedFile();
        SaveFormat chosenFormat = getSelectedFormat(fileChooser);

        selectedFile = ensureCorrectExtension(selectedFile, chosenFormat);

        try {
            switch (chosenFormat) {
                case FRAC:
                    return saveAsFrac(selectedFile, conv);
                case JPG:
                    return saveAsImage(selectedFile, painter, conv, "jpg");
                case PNG:
                    return saveAsImage(selectedFile, painter, conv, "png");
                default:
                    JOptionPane.showMessageDialog(parent,
                            "Неподдерживаемый формат файла", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Ошибка при сохранении: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private static SaveFormat getSelectedFormat(JFileChooser fileChooser) {
        var filter = fileChooser.getFileFilter();
        if (filter instanceof FileNameExtensionFilter) {
            String[] exts = ((FileNameExtensionFilter) filter).getExtensions();
            if (exts != null && exts.length > 0) {
                String ext = exts[0].toLowerCase();
                switch (ext) {
                    case "frac": return SaveFormat.FRAC;
                    case "jpg": case "jpeg": return SaveFormat.JPG;
                    case "png": return SaveFormat.PNG;
                }
            }
        }
        String fileName = fileChooser.getSelectedFile().getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return SaveFormat.JPG;
        if (fileName.endsWith(".png")) return SaveFormat.PNG;
        return SaveFormat.FRAC;
    }

    public static FileNameExtensionFilter fracFilter() {
        return new FileNameExtensionFilter(SaveFormat.FRAC.getDescription(), SaveFormat.FRAC.getExtension());
    }
    public static FileNameExtensionFilter jpgFilter() {
        return new FileNameExtensionFilter(SaveFormat.JPG.getDescription(), SaveFormat.JPG.getExtension());
    }
    public static FileNameExtensionFilter pngFilter() {
        return new FileNameExtensionFilter(SaveFormat.PNG.getDescription(), SaveFormat.PNG.getExtension());
    }

    private static File ensureCorrectExtension(File file, SaveFormat format) {
        String fileName = file.getName();
        String ext = format.getExtension();

        if (!fileName.toLowerCase().endsWith("." + ext.toLowerCase())) {
            for (SaveFormat f : SaveFormat.values()) {
                if (fileName.toLowerCase().endsWith("." + f.getExtension().toLowerCase())) {
                    fileName = fileName.substring(0, fileName.length() - f.getExtension().length() - 1);
                    break;
                }
            }
            return new File(file.getParentFile(), fileName + "." + ext);
        }
        return file;
    }

    private static boolean saveAsFrac(File file, Converter conv) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("# Mandelbrot Fractal Save File v1.0");
            writer.newLine();
            writer.write("# Этот файл содержит параметры отображения фрактала");
            writer.newLine();
            writer.newLine();

            writer.write("xMin=" + conv.getXMin());
            writer.newLine();
            writer.write("xMax=" + conv.getXMax());
            writer.newLine();
            writer.write("yMin=" + conv.getYMin());
            writer.newLine();
            writer.write("yMax=" + conv.getYMax());
            writer.newLine();

            writer.newLine();
            writer.write("# Метаданные");
            writer.newLine();
            writer.write("fractalType=Mandelbrot");
            writer.newLine();
            writer.write("savedAt=" + System.currentTimeMillis());
            writer.newLine();

            return true;
        }
    }

    private static boolean saveAsImage(File file, Painter painter, Converter conv, String format) throws IOException {
        BufferedImage image = renderFractalToImage(painter);
        addCoordinateOverlay(image, conv);
        return ImageIO.write(image, format, file);
    }

    private static BufferedImage renderFractalToImage(Painter painter) {
        int width = painter.getWidth();
        int height = painter.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        painter.paint(g2d);
        g2d.dispose();

        return image;
    }

    private static void addCoordinateOverlay(BufferedImage image, Converter conv) {
        Graphics2D g2d = image.createGraphics();

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.setColor(Color.WHITE);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.setColor(new Color(0, 0, 0, 180));

        String coordText = String.format("Re: [%.4f; %.4f] | Im: [%.4f; %.4f]",
                conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax());

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(coordText);
        int textHeight = fm.getHeight();
        int padding = 5;

        g2d.fillRect(5, image.getHeight() - textHeight - padding - 5,
                textWidth + padding * 2, textHeight + padding);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.WHITE);
        g2d.drawString(coordText, 5 + padding, image.getHeight() - padding - 5);

        g2d.dispose();
    }

    public static Converter loadFractalParams(File file) throws IOException {
        double xMin = -2.0, xMax = 1.0, yMin = -1.0, yMax = 1.0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "xMin": xMin = Double.parseDouble(value); break;
                    case "xMax": xMax = Double.parseDouble(value); break;
                    case "yMin": yMin = Double.parseDouble(value); break;
                    case "yMax": yMax = Double.parseDouble(value); break;
                }
            }
        }

        return new Converter(xMin, xMax, yMin, yMax);
    }
}
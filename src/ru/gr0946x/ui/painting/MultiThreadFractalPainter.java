package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;

public class MultiThreadFractalPainter implements Painter {

    private final Fractal fractal;
    private final Converter conv;
    private final ColorFunction colorFunction;
    private final int threadCount;

    private BufferedImage cachedImage;
    private boolean needsRedraw = true;

    public MultiThreadFractalPainter(Fractal f, Converter conv, ColorFunction cf) {
        this(f, conv, cf, Runtime.getRuntime().availableProcessors());
    }

    public MultiThreadFractalPainter(Fractal f, Converter conv, ColorFunction cf, int threadCount) {
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
        this.threadCount = threadCount;
    }

    @Override
    public int getWidth() {
        return conv.getWidth();
    }

    @Override
    public int getHeight() {
        return conv.getHeight();
    }

    @Override
    public void setWidth(int width) {
        conv.setWidth(width);
        needsRedraw = true;
    }

    @Override
    public void setHeight(int height) {
        conv.setHeight(height);
        needsRedraw = true;
    }

    @Override
    public void refresh() {
        needsRedraw = true;
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) return;

        if (needsRedraw || cachedImage == null ||
                cachedImage.getWidth() != w ||
                cachedImage.getHeight() != h) {

            cachedImage = renderMultithreaded();
            needsRedraw = false;
        }

        g.drawImage(cachedImage, 0, 0, null);
    }

    private BufferedImage renderMultithreaded() {
        int w = getWidth();
        int h = getHeight();

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        int rowsPerThread = Math.max(1, h / threadCount);

        @SuppressWarnings("unchecked")
        Future<int[]>[] futures = new Future[threadCount];

        for (int t = 0; t < threadCount; t++) {
            int startRow = t * rowsPerThread;
            int endRow = (t == threadCount - 1) ? h : startRow + rowsPerThread;
            final int startY = startRow;
            final int endY = endRow;

            futures[t] = executor.submit(() -> renderRows(startY, endY));
        }

        try {
            for (int t = 0; t < threadCount; t++) {
                int[] pixels = futures[t].get();
                int startRow = t * rowsPerThread;
                int endRow = (t == threadCount - 1) ? h : startRow + rowsPerThread;
                int rowsCount = endRow - startRow;

                for (int y = 0; y < rowsCount; y++) {
                    for (int x = 0; x < w; x++) {
                        image.setRGB(x, startRow + y, pixels[y * w + x]);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return image;
    }

    private int[] renderRows(int startY, int endY) {
        int w = getWidth();
        int rows = endY - startY;
        int[] pixels = new int[rows * w];
        for (int y = startY; y < endY; y++) {
            int rowOffset = (y - startY) * w;
            for (int x = 0; x < w; x++) {
                double crtX = conv.xScr2Crt(x);
                double crtY = conv.yScr2Crt(y);
                double value = fractal.inSetProbability(crtX, crtY);
                pixels[rowOffset + x] = colorFunction.getColor((float) value).getRGB();
            }
        }
        return pixels;
    }
}
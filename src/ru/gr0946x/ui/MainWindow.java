package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private final java.util.ArrayDeque<FractalState> history = new java.util.ArrayDeque<>();

    public MainWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));

        setJMenuBar(new MenuBar());

        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(mandelbrot, conv, (value) -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float) abs(sin(5 * value));
            var g = (float) abs(cos(8 * value) * sin(3 * value));
            var b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);

        new RightClickDrag(mainPanel, conv);

        mainPanel.addSelectListener((r) -> {
            saveCurrentState();
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mainPanel.repaint();
        });

        mainPanel.addMouseWheelListener(e -> {
            saveCurrentState();
            int rotation = e.getWheelRotation();

            double factor;
            if (rotation < 0) {
                factor = 0.8;
            } else {
                factor = 1.2;
            }

            double xMin = conv.xScr2Crt(0);
            double xMax = conv.xScr2Crt(mainPanel.getWidth());
            double yMin = conv.yScr2Crt(mainPanel.getHeight());
            double yMax = conv.yScr2Crt(0);

            double mouseX = conv.xScr2Crt(e.getX());
            double mouseY = conv.yScr2Crt(e.getY());

            double newWidth = (xMax - xMin) * factor;
            double newHeight = (yMax - yMin) * factor;

            double tX = (mouseX - xMin) / (xMax - xMin);
            double tY = (mouseY - yMin) / (yMax - yMin);

            double newXMin = mouseX - newWidth * tX;
            double newXMax = mouseX + newWidth * (1 - tX);
            double newYMin = mouseY - newHeight * tY;
            double newYMax = mouseY + newHeight * (1 - tY);

            conv.setXShape(newXMin, newXMax);
            conv.setYShape(newYMin, newYMax);

            mainPanel.repaint();
        });

        setContent();
        mainPanel.setFocusable(true);
        mainPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == java.awt.event.KeyEvent.VK_Z) {
                    undo();
                }
            }
        });
    }

    private void setContent() {
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }
    private void saveCurrentState() {
        if (history.size() >= 100) {
            history.removeFirst();
        }
        history.addLast(new FractalState(
                conv.xScr2Crt(0),
                conv.xScr2Crt(mainPanel.getWidth()),
                conv.yScr2Crt(mainPanel.getHeight()),
                conv.yScr2Crt(0)
        ));
    }

    private void undo() {
        if (!history.isEmpty()) {
            FractalState lastState = history.removeLast();
            conv.setXShape(lastState.xMin, lastState.xMax);
            conv.setYShape(lastState.yMin, lastState.yMax);
            mainPanel.repaint();
        }
    }
}

package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Julia;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;

public class JuliaWindow extends JFrame {

    public JuliaWindow(double cRe, double cIm, ColorFunction colorFunction) {
        setTitle("Множество Жюлиа для c = " + String.format("%.5f", cRe) + " + " + String.format("%.5f", cIm) + "i");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setMinimumSize(new Dimension(400, 400));

        Julia julia = new Julia(cRe, cIm);
        Converter conv = new Converter(-2.0, 2.0, -2.0, 2.0);
        Painter painter = new FractalPainter(julia, conv, colorFunction);

        PaintPanel panel = new PaintPanel(painter);
        panel.setBackground(Color.BLACK);

        panel.addMouseWheelListener(e -> {
            zoomJulia(conv, panel, e.getX(), e.getY(), e.getWheelRotation());
        });

        new RightClickDrag(panel, conv);

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void zoomJulia(Converter conv, JPanel panel, int mouseX, int mouseY, int rotation) {
        double factor = (rotation < 0) ? 0.8 : 1.2;

        double xMin = conv.xScr2Crt(0);
        double xMax = conv.xScr2Crt(panel.getWidth());
        double yMin = conv.yScr2Crt(panel.getHeight());
        double yMax = conv.yScr2Crt(0);

        double mouseXcrt = conv.xScr2Crt(mouseX);
        double mouseYcrt = conv.yScr2Crt(mouseY);

        double newWidth = (xMax - xMin) * factor;
        double newHeight = (yMax - yMin) * factor;

        double tX = (mouseXcrt - xMin) / (xMax - xMin);
        double tY = (mouseYcrt - yMin) / (yMax - yMin);

        double newXMin = mouseXcrt - newWidth * tX;
        double newXMax = mouseXcrt + newWidth * (1 - tX);
        double newYMin = mouseYcrt - newHeight * tY;
        double newYMax = mouseYcrt + newHeight * (1 - tY);

        conv.setXShape(newXMin, newXMax);
        conv.setYShape(newYMin, newYMax);

        panel.repaint();
    }
}
package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Julia;
import ru.gr0946x.ui.painting.MultiThreadFractalPainter;
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
        Painter painter = new MultiThreadFractalPainter((x, y) -> julia.inSetProbability(x, y), conv, colorFunction);

        PaintPanel panel = new PaintPanel(painter);
        panel.setBackground(Color.BLACK);

        panel.addMouseWheelListener(e -> {
            zoomJulia(conv, panel, painter, e.getX(), e.getY(), e.getWheelRotation());
        });

        new RightClickDrag(panel, conv, painter);

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void zoomJulia(Converter conv, JPanel panel, Painter painter, int mouseX, int mouseY, int rotation) {
        double factor = (rotation < 0) ? 0.8 : 1.2;

        double xMin = conv.getXMin();
        double xMax = conv.getXMax();
        double yMin = conv.getYMin();
        double yMax = conv.getYMax();

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

        painter.refresh();
        panel.repaint();
    }
}
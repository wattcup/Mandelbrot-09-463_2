package ru.gr0946x.ui;

import ru.gr0946x.Converter;

public class AspectRatioManager {

    public static void fitToPanel(Converter conv, int panelWidth, int panelHeight) {
        if (panelWidth <= 0 || panelHeight <= 0) return;

        double xMin = conv.getXMin();
        double xMax = conv.getXMax();
        double yMin = conv.getYMin();
        double yMax = conv.getYMax();

        double currentWidth = xMax - xMin;
        double currentHeight = yMax - yMin;
        double centerX = (xMin + xMax) / 2.0;
        double centerY = (yMin + yMax) / 2.0;

        double panelAspect = (double) panelWidth / panelHeight;
        double currentAspect = currentWidth / currentHeight;

        double newWidth, newHeight;
        if (panelAspect > currentAspect) {
            newHeight = currentHeight;
            newWidth = newHeight * panelAspect;
        } else {
            newWidth = currentWidth;
            newHeight = newWidth / panelAspect;
        }

        conv.setXShape(centerX - newWidth / 2.0, centerX + newWidth / 2.0);
        conv.setYShape(centerY - newHeight / 2.0, centerY + newHeight / 2.0);
    }


    public static void zoomWithAspect(Converter conv, int panelWidth, int panelHeight,
                                      double factor, int mouseX, int mouseY) {
        if (panelWidth <= 0 || panelHeight <= 0) return;

        double xMin = conv.getXMin();
        double xMax = conv.getXMax();
        double yMin = conv.getYMin();
        double yMax = conv.getYMax();

        double mouseCrtX = conv.xScr2Crt(mouseX);
        double mouseCrtY = conv.yScr2Crt(mouseY);

        double currentWidth = xMax - xMin;
        double currentHeight = yMax - yMin;

        double tX = (mouseCrtX - xMin) / currentWidth;
        double tY = (mouseCrtY - yMin) / currentHeight;

        double newWidth = currentWidth * factor;
        double newHeight = currentHeight * factor;

        double panelAspect = (double) panelWidth / panelHeight;
        double areaAspect = newWidth / newHeight;

        double finalWidth, finalHeight;
        if (areaAspect > panelAspect) {
            finalWidth = newHeight * panelAspect;
            finalHeight = newHeight;
        } else {
            finalWidth = newWidth;
            finalHeight = newWidth / panelAspect;
        }

        double newXMin = mouseCrtX - tX * finalWidth;
        double newXMax = mouseCrtX + (1 - tX) * finalWidth;
        double newYMin = mouseCrtY - tY * finalHeight;
        double newYMax = mouseCrtY + (1 - tY) * finalHeight;

        conv.setXShape(newXMin, newXMax);
        conv.setYShape(newYMin, newYMax);
    }
}
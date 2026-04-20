package ru.gr0946x.ui;
import ru.gr0946x.Converter;

import java.util.ArrayDeque;
public class FractalState {
    public final double xMin, xMax, yMin, yMax;

    public FractalState(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
    public static void saveCurrentState(Converter conv, ArrayDeque<FractalState> history) {
        if (history.size() >= 100) {
            history.removeFirst();
        }
        history.addLast(new FractalState(
                conv.getXMin(),
                conv.getXMax(),
                conv.getYMin(),
                conv.getYMax()
        ));
    }
    public static void undo(Converter conv, ArrayDeque<FractalState> history, javax.swing.JPanel mainPanel) {
        if (!history.isEmpty()) {
            FractalState lastState = history.removeLast();
            conv.setXShape(lastState.xMin, lastState.xMax);
            conv.setYShape(lastState.yMin, lastState.yMax);
            mainPanel.repaint();
        }
    }
}
package ru.gr0946x.ui.fractals;
import java.awt.Color;
import static java.lang.Math.*;

public class ElectricColorScheme implements ColorFunction {
    @Override
    public Color getColor(float value) {
        if (value == 1.0f) return Color.BLACK;
        float r = (float) abs(sin(10 * value) * 0.5);
        float g = (float) abs(cos(5 * value));
        float b = (float) abs(sin(3 * value) + 0.5);
        return new Color(Math.min(1f, r), Math.min(1f, g), Math.min(1f, b));
    }
}

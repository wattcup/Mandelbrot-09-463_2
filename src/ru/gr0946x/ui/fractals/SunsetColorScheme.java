package ru.gr0946x.ui.fractals;
import java.awt.Color;

public class SunsetColorScheme implements ColorFunction {
    @Override
    public Color getColor(float value) {
        if (value == 1.0f) return Color.BLACK;
        float hue = 0.7f + value * 0.3f;
        return Color.getHSBColor(hue, 0.8f, 1.0f);
    }
}

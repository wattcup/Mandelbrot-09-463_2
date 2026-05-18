package ru.gr0946x.ui.fractals;

import java.awt.Color;
import static java.lang.Math.*;

public class DefaultColorScheme implements ColorFunction {
    @Override
    public Color getColor(float value) {
        if (value == 1.0f) return Color.BLACK;
        var r = (float) abs(sin(5 * value));
        var g = (float) abs(cos(8 * value) * sin(3 * value));
        var b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
        return new Color(r, g, b);
    }
}

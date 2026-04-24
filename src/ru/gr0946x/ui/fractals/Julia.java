package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

public class Julia implements Fractal {
    private final double cRe;
    private final double cIm;
    private final int maxIterations = 100;
    private final double R2 = 4;

    public Julia(double cRe, double cIm) {
        this.cRe = cRe;
        this.cIm = cIm;
    }

    @Override
    public float inSetProbability(double x, double y) {
        // Для Жюлиа: z0 = точка на экране (x,y), c = фиксированная константа
        var z = new Complex(x, y);
        var c = new Complex(cRe, cIm);
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations) {
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float) i / maxIterations;
    }
}
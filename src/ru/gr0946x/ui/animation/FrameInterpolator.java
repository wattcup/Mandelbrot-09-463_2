package ru.gr0946x.ui.animation;

import java.util.ArrayList;
import java.util.List;

public class FrameInterpolator {

    public record FrameState(double xMin, double xMax, double yMin, double yMax) {}

    public static List<FrameState> generateStates(List<KeyFrame> keyFrames, int duration) {
        List<FrameState> states = new ArrayList<>();

        int totalFramesCount = duration * 30;
        int intervals = keyFrames.size() - 1;
        int framesPerInterval = totalFramesCount / intervals;

        for (int i = 0; i < intervals; i++) {
            KeyFrame start = keyFrames.get(i);
            KeyFrame end = keyFrames.get(i + 1);

            for (int j = 0; j < framesPerInterval; j++) {
                double t = (double) j / framesPerInterval;
                states.add(new FrameState(
                        lerp(start.getXMin(), end.getXMin(), t),
                        lerp(start.getXMax(), end.getXMax(), t),
                        lerp(start.getYMin(), end.getYMin(), t),
                        lerp(start.getYMax(), end.getYMax(), t)
                ));
            }
        }

        KeyFrame last = keyFrames.get(keyFrames.size() - 1);
        states.add(new FrameState(last.getXMin(), last.getXMax(), last.getYMin(), last.getYMax()));

        return states;
    }

    private static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }
}

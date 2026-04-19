package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightClickDrag {
    private Point lastDragPoint;
    private boolean isDragging = false;
    private final JPanel targetPanel;
    private final Converter converter;

    public RightClickDrag(JPanel panel, Converter converter) {
        this.targetPanel = panel;
        this.converter = converter;
        attachDragListeners();
    }

    private void attachDragListeners() {
        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    isDragging = true;
                    lastDragPoint = e.getPoint();
                    targetPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && lastDragPoint != null) {
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;

                    int width = targetPanel.getWidth();
                    int height = targetPanel.getHeight();

                    // Получаем текущие границы из Converter
                    double xMin = converter.xScr2Crt(0);
                    double xMax = converter.xScr2Crt(width);
                    double yMin = converter.yScr2Crt(height);
                    double yMax = converter.yScr2Crt(0);

                    double worldWidth = xMax - xMin;
                    double worldHeight = yMax - yMin;

                    double dxWorld = dx * (worldWidth / width);
                    double dyWorld = -dy * (worldHeight / height);

                    // Сдвигаем границы
                    converter.setXShape(xMin - dxWorld, xMax - dxWorld);
                    converter.setYShape(yMin - dyWorld, yMax - dyWorld);

                    lastDragPoint = e.getPoint();
                    targetPanel.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;
                    targetPanel.setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        targetPanel.addMouseListener(dragAdapter);
        targetPanel.addMouseMotionListener(dragAdapter);
    }
}
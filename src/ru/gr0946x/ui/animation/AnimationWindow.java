package ru.gr0946x.ui.animation;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.FractalState;
import ru.gr0946x.ui.RightClickDrag;
import ru.gr0946x.ui.SelectablePanel;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.MultiThreadFractalPainter;
import ru.gr0946x.ui.painting.Painter;
import ru.gr0946x.ui.AspectRatioManager;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;

import static java.lang.Math.*;

public class AnimationWindow extends JFrame {

    private static final int MAX_KEY_FRAMES = 50;

    private final SelectablePanel mainPanel;
    public final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private final java.util.ArrayDeque<FractalState> history = new java.util.ArrayDeque<>();

    private final DefaultListModel<KeyFrame> listModel;
    private final JList<KeyFrame> framesList;
    private final JButton btnAddFrame;
    private final JButton btnRemoveFrame;
    private final JButton btnCreateFrame;

    private final JSlider durationSlider;
    private final JLabel durationLabel;

    public AnimationWindow() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 650));

        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new MultiThreadFractalPainter(mandelbrot, conv, (value) -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float) abs(sin(5 * value));
            var g = (float) abs(cos(8 * value) * sin(3 * value));
            var b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });

        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);

        mainPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                AspectRatioManager.fitToPanel(conv, mainPanel.getWidth(), mainPanel.getHeight());
                painter.refresh();
                mainPanel.repaint();
            }
        });

        new RightClickDrag(mainPanel, conv, painter);


        listModel = new DefaultListModel<>();
        framesList = new JList<>(listModel);
        framesList.setFocusable(false);
        framesList.setCellRenderer(new KeyFrameRenderer());
        framesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        btnAddFrame = new JButton("+");
        btnAddFrame.setFocusable(false);
        btnAddFrame.addActionListener(e -> {
            if (listModel.getSize() == MAX_KEY_FRAMES) {
                JOptionPane.showMessageDialog(this, "Достигнут лимит количества кадров (" + MAX_KEY_FRAMES + ").");
                return;
            }

            ImageIcon image = createImage();
            KeyFrame frame = new KeyFrame(
                    conv.getXMin(), conv.getXMax(),
                    conv.getYMin(), conv.getYMax(),
                    image
            );
            if (!listModel.contains(frame)) {
                listModel.addElement(frame);
            }
        });

        btnRemoveFrame = new JButton("-");
        btnRemoveFrame.setFocusable(false);
        btnRemoveFrame.addActionListener(e -> {
            int selectedIndex = framesList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
            }
        });

        durationSlider = new JSlider(5, 15, 10);
        durationSlider.setFocusable(false);
        durationSlider.setMajorTickSpacing(5);
        durationSlider.setPaintTicks(true);
        durationSlider.setPaintLabels(true);
        durationLabel = new JLabel("Длительность: 10 сек");

        durationSlider.addChangeListener(_ -> {
            durationLabel.setText("Длительность: " + durationSlider.getValue() + " сек");
        });

        btnCreateFrame = new JButton("Создать видео");
        btnCreateFrame.setFocusable(false);

        btnCreateFrame.setEnabled(false);
        listModel.addListDataListener(new ListDataListener() {
            private void updateButton() {
                btnCreateFrame.setEnabled(listModel.getSize() > 1);
            }

            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateButton();
            }

            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateButton();
            }

            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateButton();
            }
        });

        btnCreateFrame.addActionListener(_ -> {
            VideoExportManager.export(
                    this,
                    listModel,
                    durationSlider.getValue(),
                    mainPanel.getWidth(),
                    mainPanel.getHeight()
            );
        });

        mainPanel.addSelectListener((r) -> {
            FractalState.saveCurrentState(conv, history);
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            painter.refresh();
            painter.updateIterations(getCurrentZoom());
            mainPanel.repaint();
        });

        mainPanel.addMouseWheelListener(e -> {
            FractalState.saveCurrentState(conv, history);
            int rotation = e.getWheelRotation();
            double factor = (rotation < 0) ? 0.8 : 1.2; // 0.8 – увеличение, 1.2 – уменьшение
            AspectRatioManager.zoomWithAspect(conv, mainPanel.getWidth(), mainPanel.getHeight(),
                    factor, e.getX(), e.getY());
            painter.refresh();
            painter.updateIterations(getCurrentZoom());
            mainPanel.repaint();
        });

        painter.updateIterations(getCurrentZoom());
        setContent();
        mainPanel.setFocusable(true);
        mainPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == java.awt.event.KeyEvent.VK_Z) {
                    FractalState.undo(conv, history, mainPanel);
                    painter.refresh();
                    painter.updateIterations(getCurrentZoom());
                }
            }
        });
    }

    private ImageIcon createImage() {
        return new ImageIcon(ImageConverter.render(160, 120, painter));
    }

    private void setContent() {
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);

        JScrollPane scrollPane = new JScrollPane(framesList);

        JPanel plusMinusPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        plusMinusPanel.add(btnAddFrame);
        plusMinusPanel.add(btnRemoveFrame);

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(gl.createSequentialGroup()
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(8)
                                .addComponent(plusMinusPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(8)
                                .addComponent(btnCreateFrame)
                                .addGap(16)
                                .addComponent(durationLabel)
                                .addComponent(durationSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                )
                .addGap(8)
        );

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane, 200, 200, 200)
                        .addComponent(plusMinusPanel, 200, 200, 200)
                        .addComponent(btnCreateFrame, 200, 200, 200)
                        .addComponent(durationLabel, 200, 200, 200)
                        .addComponent(durationSlider, 200, 200, 200)
                )
                .addGap(8)
        );
    }

    private double getCurrentZoom() {
        double xRange = conv.getXMax() - conv.getXMin();
        double yRange = conv.getYMax() - conv.getYMin();
        double zoom = 1.0 / Math.max(xRange, yRange);


        setTitle("Множество Мандельброта - Zoom: " + String.format("%.2f", zoom) + "x");

        return zoom;
    }
}
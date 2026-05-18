package ru.gr0946x.ui;

import ru.gr0946x.ui.animation.AnimationWindow;
import javax.swing.*;

import static ru.gr0946x.ui.saveAndOpen.SaveAndOpenActions.openAction;
import static ru.gr0946x.ui.saveAndOpen.SaveAndOpenActions.saveAction;


public class MenuBar extends JMenuBar {

    private MainWindow mainWindow;

    public MenuBar(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        var saveMenu = new JMenu("Сохранить");
        var saveItem = new JMenuItem("Сохранить как...");
        saveItem.addActionListener(e -> saveAction(mainWindow));
        saveMenu.add(saveItem);
        add(saveMenu);

        var openMenu = new JMenu("Открыть");
        var openItem = new JMenuItem("Открыть .frac...");
        openItem.addActionListener(e -> openAction(mainWindow));
        openMenu.add(openItem);
        add(openMenu);

        var videoMenu = new JMenu("Экскурсия по фракталу");
        var videoItem = new JMenuItem("Создать анимацию");
        videoItem.addActionListener(e -> {
            AnimationWindow animationWindow = new AnimationWindow();
            animationWindow.setVisible(true);
        });
        videoMenu.add(videoItem);
        add(videoMenu);

        var colorMenu = new JMenu("Цветовые схемы");

        // Схема 1: Стандартная
        var item1 = new JMenuItem("Классика");
        item1.addActionListener(e -> {
            mainWindow.getPainter().setColorFunction(new ru.gr0946x.ui.fractals.DefaultColorScheme());
            mainWindow.repaint();
        });

        var item2 = new JMenuItem("Электрик (Синий)");
        item2.addActionListener(e -> {
            mainWindow.getPainter().setColorFunction(new ru.gr0946x.ui.fractals.ElectricColorScheme());
            mainWindow.repaint();
        });

        var item3 = new JMenuItem("Закат (Розовый)");
        item3.addActionListener(e -> {
            mainWindow.getPainter().setColorFunction(new ru.gr0946x.ui.fractals.SunsetColorScheme());
            mainWindow.repaint();
        });


        colorMenu.add(item1);
        colorMenu.add(item2);
        colorMenu.add(item3);
        add(colorMenu);
    }


}

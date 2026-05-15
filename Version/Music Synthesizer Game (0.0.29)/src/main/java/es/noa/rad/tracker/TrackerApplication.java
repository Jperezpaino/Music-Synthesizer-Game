package es.noa.rad.tracker;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ventana principal del Music Tracker.
 * Contiene toolbar, barra de escala, panel tracker y barra de estado.
 */
public final class TrackerApplication {

  // -------------------------------------------------------------------------
  // Colores de aplicacion (variables — se actualizan con el tema)
  // -------------------------------------------------------------------------

  private static Color C_BG, C_TOOLBAR_BG, C_TITLE, C_TEXT;
  private static Color C_BTN_CLR, C_BTN_CLR_H;
  private static Color C_SCALE_BG, C_SCALE_BDR;
  private static Color C_STATUS_BG;

  // Fichero actualmente abierto (null = sin guardar)
  private static File   currentFile  = null;
  private static final String DEFAULT_TITLE = "Music Synthesizer Tracker";

  // -------------------------------------------------------------------------
  // Paleta clara / oscura
  // -------------------------------------------------------------------------

  private static void assignAppColors(final boolean dark) {
    if (dark) {
      C_BG         = new Color(15,  15,  22);
      C_TOOLBAR_BG = new Color(22,  22,  34);
      C_TITLE      = new Color(100, 160, 255);
      C_TEXT       = new Color(200, 200, 220);
      C_BTN_CLR    = new Color(180,  60,  50);
      C_BTN_CLR_H  = new Color(210,  80,  65);
      C_SCALE_BG   = new Color(20,  20,  32);
      C_SCALE_BDR  = new Color(60,  60,  90);
      C_STATUS_BG  = new Color(18,  18,  28);
    } else {
      C_BG         = new Color(235, 237, 250);
      C_TOOLBAR_BG = new Color(210, 215, 238);
      C_TITLE      = new Color(30,   80, 195);
      C_TEXT       = new Color(30,   35,  70);
      C_BTN_CLR    = new Color(180,  50,  40);
      C_BTN_CLR_H  = new Color(210,  70,  55);
      C_SCALE_BG   = new Color(218, 222, 242);
      C_SCALE_BDR  = new Color(120, 128, 185);
      C_STATUS_BG  = new Color(198, 203, 232);
    }
  }

  static {
    assignAppColors(true);
  }

  // -------------------------------------------------------------------------
  // Punto de entrada
  // -------------------------------------------------------------------------

  public static void main(final String[] args) {
    SwingUtilities.invokeLater(TrackerApplication::launchWindow);
  }

  // -------------------------------------------------------------------------
  // Construccion de la ventana
  // -------------------------------------------------------------------------

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void launchWindow() {
    // Instalar el Look & Feel nativo del sistema operativo
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception ignored) {}

    applyLookAndFeel(true);
    NotePlayer.warmUp();

    final JFrame frame = new JFrame(DEFAULT_TITLE);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setBackground(C_BG);
    frame.setLayout(new BorderLayout(0, 0));

    // -- 4 canales independientes (un TrackerPanel por canal)
    final TrackerPanel[] trackers = new TrackerPanel[NotePlayer.NUM_CHANNELS];
    for (int i = 0; i < trackers.length; i++) {
      trackers[i] = new TrackerPanel();
      trackers[i].setChannelIndex(i);
    }
    final SequencerEngine sequencer = new SequencerEngine();

    // -- Menu bar
    frame.setJMenuBar(buildMenuBar(trackers, frame, sequencer));

    // -- Toolbar (BPM, Play, Clear)
    final JPanel toolbar = buildToolbar(trackers, frame, sequencer);

    // -- Tabs con los 4 canales
    final JTabbedPane tabs = buildChannelTabs(trackers, frame, sequencer);
    tabs.setName("channelTabs");

    // -- Status bar
    final JPanel status = buildStatusBar(trackers, frame);

    // Ensamblado
    frame.add(toolbar, BorderLayout.NORTH);
    frame.add(tabs,    BorderLayout.CENTER);
    frame.add(status,  BorderLayout.SOUTH);

    frame.pack();
    frame.setMinimumSize(frame.getSize());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // -------------------------------------------------------------------------
  // Toolbar
  // -------------------------------------------------------------------------

  private static JPanel buildToolbar(final TrackerPanel[] trackers,
                                      final JFrame frame,
                                      final SequencerEngine sequencer) {
    final JPanel bar = new JPanel(new BorderLayout(0, 0));
    bar.setBackground(C_TOOLBAR_BG);
    bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

    final JLabel title = new JLabel("\uD83C\uDFB5 Music Synthesizer Tracker");
    title.setForeground(C_TITLE);
    title.setFont(new Font("SansSerif", Font.BOLD, 16));
    bar.add(title, BorderLayout.WEST);

    // -- Panel derecho: [BPM-] [bpmLabel] [BPM+]  [▶ Play / ■ Stop]  [Limpiar]
    final JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    right.setBackground(C_TOOLBAR_BG);

    // BPM controls
    final JLabel bpmLbl = new JLabel("BPM: " + sequencer.getBpm());
    bpmLbl.setForeground(C_TEXT);
    bpmLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
    bpmLbl.setPreferredSize(new Dimension(72, 28));
    bpmLbl.setHorizontalAlignment(SwingConstants.CENTER);
    bpmLbl.setName("bpmLabel");

    final JButton bpmDown = makeButton("-", new Color(50, 55, 80), new Color(70, 75, 110));
    bpmDown.setPreferredSize(new Dimension(34, 28));
    bpmDown.setFont(new Font("SansSerif", Font.BOLD, 14));

    final JButton bpmUp = makeButton("+", new Color(50, 55, 80), new Color(70, 75, 110));
    bpmUp.setPreferredSize(new Dimension(34, 28));
    bpmUp.setFont(new Font("SansSerif", Font.BOLD, 14));

    bpmDown.addActionListener(e -> {
      sequencer.setBpm(sequencer.getBpm() - 5);
      bpmLbl.setText("BPM: " + sequencer.getBpm());
    });
    bpmUp.addActionListener(e -> {
      sequencer.setBpm(sequencer.getBpm() + 5);
      bpmLbl.setText("BPM: " + sequencer.getBpm());
    });

    // Play / Stop button (reproduce TODOS los canales simultaneamente)
    final JButton playBtn = makeButton("\u25B6 Play",
        new Color(30, 130, 70), new Color(45, 165, 90));
    playBtn.setPreferredSize(new Dimension(90, 28));
    playBtn.setName("playBtn");

    playBtn.addActionListener(e -> {
      if (sequencer.isRunning()) {
        sequencer.stop();
        playBtn.setText("\u25B6 Play");
      } else {
        sequencer.start(trackers);
        playBtn.setText("\u25A0 Stop");
      }
    });

    // Limpiar — limpia solo el canal actualmente visible
    final JButton clearBtn = makeButton("\u2B1B Limpiar", C_BTN_CLR, C_BTN_CLR_H);
    clearBtn.addActionListener(e -> {
      sequencer.stop();
      playBtn.setText("\u25B6 Play");
      // Limpiar el canal activo en el JTabbedPane
      findByName(frame, "channelTabs", JTabbedPane.class).stream().findFirst().ifPresent(c -> {
        final int tabIdx = ((JTabbedPane) c).getSelectedIndex();
        if (tabIdx >= 0 && tabIdx < trackers.length) {
          trackers[tabIdx].clearAll();
        }
      });
    });

    right.add(bpmDown);
    right.add(bpmLbl);
    right.add(bpmUp);
    right.add(playBtn);
    right.add(clearBtn);
    bar.add(right, BorderLayout.EAST);

    return bar;
  }

  // -------------------------------------------------------------------------
  // Menu bar — Archivo
  // -------------------------------------------------------------------------

  private static JMenuBar buildMenuBar(final TrackerPanel[] trackers,
                                       final JFrame frame,
                                       final SequencerEngine sequencer) {
    final JMenuBar menuBar = new JMenuBar();
    menuBar.setBackground(C_TOOLBAR_BG);
    menuBar.setBorder(BorderFactory.createEmptyBorder());

    final JMenu fileMenu = new JMenu("Archivo");
    fileMenu.setForeground(C_TEXT);
    fileMenu.setFont(new Font("SansSerif", Font.BOLD, 12));
    fileMenu.setName("menuArchivo");
    menuBar.add(fileMenu);

    // -- Abrir (Ctrl+O)
    final JMenuItem openItem = new JMenuItem("Abrir...", menuIcon("open"));
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    openItem.addActionListener(e -> {
      stopSequencer(frame, sequencer);
      // Por ahora carga solo en canal 0 hasta que MelodyFile soporte multi-canal
      final File loaded = MelodyFile.load(frame, trackers[0], sequencer,
          newBpm -> updateBpmLabel(frame, newBpm),
          newScale -> SwingUtilities.invokeLater(() ->
              findByName(frame, "scaleCombo", JComboBox.class)
                  .forEach(c -> ((JComboBox<?>) c).setSelectedItem(newScale))
          )
      );
      if (loaded != null) {
        currentFile = loaded;
        frame.setTitle(DEFAULT_TITLE + " \u2014 " + loaded.getName());
      }
    });
    fileMenu.add(openItem);

    // -- Guardar (Ctrl+S)
    final JMenuItem saveItem = new JMenuItem("Guardar", menuIcon("save"));
    saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    saveItem.addActionListener(e -> {
      if (currentFile != null) {
        MelodyFile.saveToFile(currentFile, trackers[0], sequencer, frame);
        frame.setTitle(DEFAULT_TITLE + " \u2014 " + currentFile.getName());
      } else {
        final File saved = MelodyFile.saveAs(frame, trackers[0], sequencer);
        if (saved != null) {
          currentFile = saved;
          frame.setTitle(DEFAULT_TITLE + " \u2014 " + saved.getName());
        }
      }
    });
    fileMenu.add(saveItem);

    // -- Guardar como (Ctrl+Shift+S)
    final JMenuItem saveAsItem = new JMenuItem("Guardar como...", menuIcon("saveas"));
    saveAsItem.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    saveAsItem.addActionListener(e -> {
      final File saved = MelodyFile.saveAs(frame, trackers[0], sequencer);
      if (saved != null) {
        currentFile = saved;
        frame.setTitle(DEFAULT_TITLE + " \u2014 " + saved.getName());
      }
    });
    fileMenu.add(saveAsItem);

    fileMenu.addSeparator();

    // -- Exportar WAV (Ctrl+E)
    final JMenuItem exportItem = new JMenuItem("Exportar WAV...", menuIcon("wav"));
    exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
    exportItem.addActionListener(e ->
        WavExporter.export(frame, trackers[0], sequencer));
    fileMenu.add(exportItem);

    fileMenu.addSeparator();

    // -- Cerrar (Ctrl+W) — limpia todos los canales
    final JMenuItem closeItem = new JMenuItem("Cerrar", menuIcon("close"));
    closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
    closeItem.addActionListener(e -> {
      stopSequencer(frame, sequencer);
      for (final TrackerPanel tp : trackers) {
        tp.clearAll();
        tp.setScale(TrackerScale.EASY_PLUS);
      }
      sequencer.setBpm(120);
      updateBpmLabel(frame, 120);
      SwingUtilities.invokeLater(() ->
          findByName(frame, "scaleCombo", JComboBox.class)
              .forEach(c -> ((JComboBox<?>) c).setSelectedItem(TrackerScale.EASY_PLUS))
      );
      currentFile = null;
      frame.setTitle(DEFAULT_TITLE);
    });
    fileMenu.add(closeItem);

    fileMenu.addSeparator();

    // -- Salir (Alt+F4 / Ctrl+Q)
    final JMenuItem exitItem = new JMenuItem("Salir", menuIcon("exit"));
    exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
    exitItem.addActionListener(e -> {
      stopSequencer(frame, sequencer);
      frame.dispose();
      System.exit(0);
    });
    fileMenu.add(exitItem);

    return menuBar;
  }

  /** Para el sequencer y resetea el texto del boton Play. */
  private static void stopSequencer(final JFrame frame, final SequencerEngine sequencer) {
    if (sequencer.isRunning()) {
      sequencer.stop();
      findByName(frame, "playBtn", JButton.class)
          .forEach(b -> ((JButton) b).setText("\u25B6 Play"));
    }
  }

  /** Actualiza el texto del label de BPM. */
  private static void updateBpmLabel(final JFrame frame, final int bpm) {
    findByName(frame, "bpmLabel", JLabel.class)
        .forEach(l -> ((JLabel) l).setText("BPM: " + bpm));
  }

  // -------------------------------------------------------------------------
  // Channel tabs — 4 pestanas, una por canal MIDI
  // -------------------------------------------------------------------------

  private static JTabbedPane buildChannelTabs(final TrackerPanel[] trackers,
                                               final JFrame frame,
                                               final SequencerEngine sequencer) {
    final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
    tabs.setBackground(C_TOOLBAR_BG);
    tabs.setForeground(C_TEXT);
    tabs.setName("channelTabs");

    final String[] labels = {
        "Canal 1 \uD83C\uDFB9", "Canal 2 \uD83C\uDFB8",
        "Canal 3 \uD83C\uDFBA", "Canal 4 \uD83E\uDD41"
    };

    for (int i = 0; i < trackers.length; i++) {
      final int idx = i;
      final JPanel page = new JPanel(new BorderLayout(0, 0));
      page.setBackground(C_TOOLBAR_BG);

      // Franja superior: instrumento + escala
      page.add(buildChannelStrip(trackers[idx], idx), BorderLayout.NORTH);

      // Grid del tracker en scroll
      final JScrollPane scroll = new JScrollPane(trackers[idx],
          JScrollPane.VERTICAL_SCROLLBAR_NEVER,
          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setBorder(BorderFactory.createEmptyBorder());
      page.add(scroll, BorderLayout.CENTER);

      tabs.addTab(labels[i], page);
    }
    return tabs;
  }

  /** Franja de controles por canal: selector de instrumento y de escala. */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static JPanel buildChannelStrip(final TrackerPanel tracker, final int idx) {
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
    bar.setBackground(C_SCALE_BG);
    bar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 1, 0, C_SCALE_BDR),
        BorderFactory.createEmptyBorder(0, 6, 0, 6)
    ));

    // -- Instrumento
    final JLabel instLbl = new JLabel("Instrumento:");
    instLbl.setForeground(C_TEXT);
    instLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    bar.add(instLbl);

    final JComboBox<String> instCombo = new JComboBox<>(InstrumentList.NAMES);
    instCombo.setSelectedIndex(NotePlayer.getProgram(idx));
    instCombo.setBackground(C_SCALE_BG);
    instCombo.setForeground(C_TEXT);
    instCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
    instCombo.setPreferredSize(new Dimension(220, 28));
    instCombo.setName("instCombo_" + idx);
    instCombo.addActionListener(e ->
        NotePlayer.setProgram(idx, instCombo.getSelectedIndex()));
    bar.add(instCombo);

    // -- Escala
    final JLabel scaleLbl = new JLabel("Escala:");
    scaleLbl.setForeground(C_TEXT);
    scaleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    bar.add(scaleLbl);

    final JComboBox<TrackerScale> scaleCombo = new JComboBox<>(TrackerScale.values());
    scaleCombo.setSelectedItem(TrackerScale.EASY_PLUS);
    scaleCombo.setBackground(C_SCALE_BG);
    scaleCombo.setForeground(C_TEXT);
    scaleCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
    scaleCombo.setPreferredSize(new Dimension(240, 28));
    scaleCombo.setName("scaleCombo");

    scaleCombo.setRenderer(new ListCellRenderer<TrackerScale>() {
      @Override
      public Component getListCellRendererComponent(
          final JList<? extends TrackerScale> list,
          final TrackerScale value, final int index,
          final boolean isSelected, final boolean cellHasFocus) {
        final JLabel l = new JLabel();
        l.setOpaque(true);
        if (value != null) {
          l.setText("<html><b>" + value.getFullName() + "</b>"
              + "  <font color='gray'>(" + value.getNoteCount() + " notas)</font></html>");
        }
        l.setBackground(isSelected ? new Color(70, 90, 140) : C_SCALE_BG);
        l.setForeground(C_TEXT);
        l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return l;
      }
    });

    scaleCombo.addActionListener(e -> {
      final TrackerScale sel = (TrackerScale) scaleCombo.getSelectedItem();
      if (sel != null) tracker.setScale(sel);
    });
    bar.add(scaleCombo);

    return bar;
  }

  // -------------------------------------------------------------------------
  // Status bar — con boton de tema
  // -------------------------------------------------------------------------

  private static JPanel buildStatusBar(final TrackerPanel[] trackers, final JFrame frame) {
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
    bar.setBackground(C_STATUS_BG);
    bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_SCALE_BDR));
    bar.setName("statusBar");

    final JLabel info = new JLabel(
        "C2\u2013C5 \u00b7 16 pasos \u00b7 12 escalas \u00b7 4 canales MIDI");
    info.setForeground(C_TEXT);
    info.setFont(new Font("SansSerif", Font.PLAIN, 11));
    bar.add(info);

    // Separador
    final JLabel sep = new JLabel(" | ");
    sep.setForeground(C_TEXT);
    bar.add(sep);

    // Boton de toggle de tema
    final String initLabel = TrackerTheme.isDark() ? "\u2600 Tema Claro" : "\uD83C\uDF19 Tema Oscuro";
    final JButton themeBtn = makeButton(initLabel,
        new Color(55, 100, 160), new Color(75, 125, 195));
    themeBtn.setPreferredSize(new Dimension(120, 22));
    themeBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
    themeBtn.setName("themeBtn");

    themeBtn.addActionListener(e -> {
      TrackerTheme.toggle();
      final boolean dark = TrackerTheme.isDark();
      TrackerPanel.applyTheme(dark);
      for (final TrackerPanel tp : trackers) tp.refreshUI();
      assignAppColors(dark);
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (final Exception ignored) { }
      applyLookAndFeel(dark);
      themeBtn.setText(dark ? "\u2600 Tema Claro" : "\uD83C\uDF19 Tema Oscuro");
      SwingUtilities.updateComponentTreeUI(frame);
      frame.repaint();
    });

    bar.add(themeBtn);

    return bar;
  }

  // -------------------------------------------------------------------------
  // Look & Feel
  // -------------------------------------------------------------------------

  private static void applyLookAndFeel(final boolean dark) {
    if (dark) {
      final Color comboFg   = new Color(200, 200, 220);
      final Color comboBg   = new Color(30,   30,  46);
      final Color comboBdr  = new Color(60,   60,  90);
      UIManager.put("ComboBox.foreground",             comboFg);
      UIManager.put("ComboBox.background",             comboBg);
      UIManager.put("ComboBox.selectionBackground",    new Color(60, 80, 140));
      UIManager.put("ComboBox.selectionForeground",    comboFg);
      UIManager.put("ComboBox.border",
          BorderFactory.createLineBorder(comboBdr));
      UIManager.put("List.background",                 comboBg);
      UIManager.put("List.foreground",                 comboFg);
      UIManager.put("List.selectionBackground",        new Color(60, 80, 140));
      UIManager.put("ScrollBar.background",            new Color(22, 22, 34));
      UIManager.put("ScrollBar.thumb",                 new Color(60, 60, 90));
      UIManager.put("Button.background",               comboBg);
      UIManager.put("Button.foreground",               comboFg);
    } else {
      final Color comboFg  = new Color(20,  25,  60);
      final Color comboBg  = new Color(218, 222, 245);
      final Color comboBdr = new Color(120, 130, 195);
      UIManager.put("ComboBox.foreground",             comboFg);
      UIManager.put("ComboBox.background",             comboBg);
      UIManager.put("ComboBox.selectionBackground",    new Color(100, 130, 220));
      UIManager.put("ComboBox.selectionForeground",    Color.WHITE);
      UIManager.put("ComboBox.border",
          BorderFactory.createLineBorder(comboBdr));
      UIManager.put("List.background",                 comboBg);
      UIManager.put("List.foreground",                 comboFg);
      UIManager.put("List.selectionBackground",        new Color(100, 130, 220));
      UIManager.put("ScrollBar.background",            new Color(210, 215, 240));
      UIManager.put("ScrollBar.thumb",                 new Color(130, 140, 200));
      UIManager.put("Button.background",               comboBg);
      UIManager.put("Button.foreground",               comboFg);
    }
  }

  // -------------------------------------------------------------------------
  // Helper: buscar componente por nombre
  // -------------------------------------------------------------------------

  private static java.util.List<Component> findByName(
      final java.awt.Container root, final String name,
      final Class<? extends Component> type) {
    final java.util.List<Component> result = new java.util.ArrayList<>();
    for (final Component c : root.getComponents()) {
      if (type.isInstance(c) && name.equals(c.getName())) result.add(c);
      if (c instanceof java.awt.Container) {
        result.addAll(findByName((java.awt.Container) c, name, type));
      }
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Helper: boton personalizado
  // -------------------------------------------------------------------------

  private static JButton makeButton(
      final String text, final Color normalBg, final Color hoverBg) {
    final JButton btn = new JButton(text) {
      private static final long serialVersionUID = 1L;
      private boolean hovered = false;
      {
        addMouseListener(new MouseAdapter() {
          @Override public void mouseEntered(final MouseEvent e) {
            hovered = true; repaint();
          }
          @Override public void mouseExited(final MouseEvent e) {
            hovered = false; repaint();
          }
        });
      }
      @Override
      protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        final Color top = hovered ? hoverBg.brighter() : normalBg;
        final Color bot = hovered ? hoverBg              : normalBg.darker();
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(120, 28));
    return btn;
  }

  // -------------------------------------------------------------------------
  // Helper: iconos para items de menu (dibujados con Java2D, sin ficheros)
  // -------------------------------------------------------------------------

  /**
   * Devuelve un {@link Icon} de 16x16 px dibujado con Java2D.
   * Claves soportadas: "open", "save", "saveas", "wav", "close", "exit".
   */
  private static Icon menuIcon(final String type) {
    return new Icon() {
      private static final int SZ = 16;

      @Override public int getIconWidth()  { return SZ; }
      @Override public int getIconHeight() { return SZ; }

      @Override
      public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        switch (type) {

          case "open": {
            // base carpeta
            g2.setColor(new Color(220, 170, 40));
            g2.fillRoundRect(1, 5, 14, 9, 2, 2);
            // solapa
            g2.setColor(new Color(240, 200, 80));
            g2.fillRoundRect(1, 3, 6, 4, 2, 2);
            // interior
            g2.setColor(new Color(255, 220, 100));
            g2.fillRect(2, 7, 12, 6);
            // borde
            g2.setColor(new Color(160, 120, 20));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(1, 5, 13, 8, 2, 2);
            break;
          }

          case "save": {
            // disquete
            g2.setColor(new Color(90, 100, 120));
            g2.fillRoundRect(1, 1, 14, 14, 2, 2);
            g2.setColor(new Color(235, 240, 255));
            g2.fillRect(3, 2, 8, 5);
            g2.setColor(new Color(60, 65, 80));
            g2.fillRect(5, 9, 6, 5);
            g2.setColor(new Color(150, 160, 180));
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawLine(4, 4, 9, 4);
            g2.drawLine(4, 6, 9, 6);
            g2.setColor(new Color(50, 55, 70));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(1, 1, 13, 13, 2, 2);
            break;
          }

          case "saveas": {
            g2.setColor(new Color(90, 100, 120));
            g2.fillRoundRect(1, 1, 12, 12, 2, 2);
            g2.setColor(new Color(235, 240, 255));
            g2.fillRect(2, 2, 7, 4);
            g2.setColor(new Color(60, 65, 80));
            g2.fillRect(4, 8, 5, 4);
            g2.setColor(new Color(50, 55, 70));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(1, 1, 11, 11, 2, 2);
            // flecha de "como"
            g2.setColor(new Color(40, 180, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(12, 10, 12, 14);
            g2.drawLine(12, 14, 10, 12);
            g2.drawLine(12, 14, 14, 12);
            break;
          }

          case "wav": {
            g2.setColor(new Color(30, 100, 200));
            g2.fillRoundRect(0, 0, 16, 16, 3, 3);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            final int[] wx = {1, 3, 5, 7, 9, 11, 13, 15};
            final int[] wy = {8, 4, 8, 12, 8, 4, 8, 8};
            for (int i = 0; i < wx.length - 1; i++) {
              g2.drawLine(wx[i], wy[i], wx[i + 1], wy[i + 1]);
            }
            break;
          }

          case "close": {
            g2.setColor(new Color(240, 242, 255));
            g2.fillRoundRect(2, 1, 10, 13, 2, 2);
            g2.setColor(new Color(160, 170, 200));
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawRoundRect(2, 1, 10, 13, 2, 2);
            g2.setColor(new Color(210, 50, 40));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(5, 5, 9, 9);
            g2.drawLine(9, 5, 5, 9);
            break;
          }

          case "exit": {
            g2.setColor(new Color(140, 145, 160));
            g2.fillRoundRect(1, 1, 9, 14, 2, 2);
            g2.setColor(new Color(210, 215, 230));
            g2.fillRect(2, 2, 7, 12);
            g2.setColor(new Color(220, 170, 40));
            g2.fillOval(7, 7, 2, 2);
            g2.setColor(new Color(210, 50, 40));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(10, 8, 15, 8);
            g2.drawLine(13, 5, 15, 8);
            g2.drawLine(13, 11, 15, 8);
            break;
          }

          default: break;
        }

        g2.dispose();
      }
    };
  }
}
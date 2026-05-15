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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
    applyLookAndFeel(true);

    final JFrame frame = new JFrame("Music Synthesizer Tracker");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setBackground(C_BG);
    frame.setLayout(new BorderLayout(0, 4));

    // -- Tracker
    final TrackerPanel     tracker   = new TrackerPanel();
    final SequencerEngine  sequencer = new SequencerEngine();

    // -- Toolbar
    final JPanel toolbar = buildToolbar(tracker, frame, sequencer);
    frame.add(toolbar, BorderLayout.NORTH);

    // -- Scale bar
    final JPanel scaleBar = buildScaleBar(tracker, frame);
    frame.add(scaleBar, BorderLayout.NORTH);

    final JPanel northStack = new JPanel(new BorderLayout(0, 0));
    northStack.setBackground(C_BG);
    northStack.add(toolbar,  BorderLayout.NORTH);
    northStack.add(scaleBar, BorderLayout.SOUTH);
    frame.add(northStack, BorderLayout.NORTH);

    // -- Scroll (no barras)
    final JScrollPane scroll = new JScrollPane(tracker,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(C_BG);
    frame.add(scroll, BorderLayout.CENTER);

    // -- Status bar
    final JPanel status = buildStatusBar(tracker, frame);
    frame.add(status, BorderLayout.SOUTH);

    // -- Scale change listener
    tracker.addPropertyChangeListener("scale", evt -> {
      final boolean maximized =
          (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
      if (!maximized) {
        frame.pack();
        frame.setMinimumSize(frame.getPreferredSize());
      } else {
        frame.setMinimumSize(frame.getPreferredSize());
      }
      updateScaleLabel(frame, tracker);
    });

    frame.pack();
    frame.setMinimumSize(frame.getSize());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // -------------------------------------------------------------------------
  // Toolbar
  // -------------------------------------------------------------------------

  private static JPanel buildToolbar(final TrackerPanel tracker,
                                      final JFrame frame,
                                      final SequencerEngine sequencer) {
    final JPanel bar = new JPanel(new BorderLayout(0, 0));
    bar.setBackground(C_TOOLBAR_BG);
    bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

    final JLabel title = new JLabel("🎵 Music Synthesizer Tracker");
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

    // Play / Stop button
    final JButton playBtn = makeButton("\u25B6 Play",
        new Color(30, 130, 70), new Color(45, 165, 90));
    playBtn.setPreferredSize(new Dimension(90, 28));

    playBtn.addActionListener(e -> {
      if (sequencer.isRunning()) {
        sequencer.stop();
        playBtn.setText("\u25B6 Play");
      } else {
        sequencer.start(tracker);
        playBtn.setText("\u25A0 Stop");
      }
    });

    // Limpiar
    final JButton clearBtn = makeButton("\u2B1B Limpiar", C_BTN_CLR, C_BTN_CLR_H);
    clearBtn.addActionListener(e -> {
      sequencer.stop();
      playBtn.setText("\u25B6 Play");
      tracker.clearAll();
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
  // Scale bar
  // -------------------------------------------------------------------------

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static JPanel buildScaleBar(final TrackerPanel tracker, final JFrame frame) {
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
    bar.setBackground(C_SCALE_BG);
    bar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 1, 0, C_SCALE_BDR),
        BorderFactory.createEmptyBorder(0, 6, 0, 6)
    ));
    bar.setName("scaleBar");

    final JLabel scaleLbl = new JLabel("Escala:");
    scaleLbl.setForeground(C_TEXT);
    scaleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    bar.add(scaleLbl);

    final JComboBox<TrackerScale> combo = new JComboBox<>(TrackerScale.values());
    combo.setSelectedItem(TrackerScale.EASY_PLUS);
    combo.setBackground(C_SCALE_BG);
    combo.setForeground(C_TEXT);
    combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    combo.setPreferredSize(new Dimension(280, 28));
    combo.setName("scaleCombo");

    combo.setRenderer(new ListCellRenderer<TrackerScale>() {
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

    bar.add(combo);

    final String initial = buildNotesText(tracker);
    final JLabel notesLbl = new JLabel(initial);
    notesLbl.setForeground(C_TEXT);
    notesLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
    notesLbl.setName("notesLabel");
    bar.add(notesLbl);

    combo.addActionListener(e -> {
      final TrackerScale sel = (TrackerScale) combo.getSelectedItem();
      if (sel != null) tracker.setScale(sel);
    });

    tracker.addPropertyChangeListener("scale", evt -> {
      final String txt = buildNotesText(tracker);
      notesLbl.setText(txt);
    });

    return bar;
  }

  private static String buildNotesText(final TrackerPanel tracker) {
    final TrackerScale sc = tracker.getCurrentScale();
    if (sc == null) return "";
    return sc.getNoteCount() + " notas activas (C2\u2013C5)";
  }

  private static void updateScaleLabel(final JFrame frame, final TrackerPanel tracker) {
    SwingUtilities.invokeLater(() -> {
      final String txt = buildNotesText(tracker);
      findByName(frame, "notesLabel", JLabel.class)
          .forEach(l -> ((JLabel) l).setText(txt));
    });
  }

  // -------------------------------------------------------------------------
  // Status bar — con boton de tema
  // -------------------------------------------------------------------------

  private static JPanel buildStatusBar(final TrackerPanel tracker, final JFrame frame) {
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
    bar.setBackground(C_STATUS_BG);
    bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_SCALE_BDR));
    bar.setName("statusBar");

    final JLabel info = new JLabel("C2\u2013C5 \u00b7 16 pasos \u00b7 12 escalas");
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
      tracker.refreshUI();
      assignAppColors(dark);
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
}
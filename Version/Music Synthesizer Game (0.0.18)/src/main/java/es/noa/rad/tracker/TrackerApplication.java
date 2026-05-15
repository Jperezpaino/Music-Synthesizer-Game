package es.noa.rad.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Aplicacion principal del Tracker Visual.
 *
 * <p>Lanza una ventana independiente con el tracker dinamico. El numero
 * de filas se adapta automaticamente a la escala seleccionada en el
 * combo box (de 16 filas en pentatonica hasta 37 en cromatica).</p>
 */
public final class TrackerApplication {

  // -------------------------------------------------------------------------
  // Colores
  // -------------------------------------------------------------------------

  private static final Color C_BG         = new Color(15,  15,  22);
  private static final Color C_TOOLBAR_BG = new Color(22,  22,  34);
  private static final Color C_TITLE      = new Color(100, 160, 255);
  private static final Color C_TEXT       = new Color(200, 200, 220);
  private static final Color C_BTN_CLR    = new Color(180,  60,  50);
  private static final Color C_BTN_CLR_H  = new Color(210,  80,  65);
  private static final Color C_SCALE_BG   = new Color(20,  20,  32);
  private static final Color C_SCALE_BDR  = new Color(60,  60,  90);

  // -------------------------------------------------------------------------
  // Constructor privado
  // -------------------------------------------------------------------------

  private TrackerApplication() {
    // No instanciable
  }

  // -------------------------------------------------------------------------
  // Punto de entrada
  // -------------------------------------------------------------------------

  /**
   * Lanza la ventana del tracker.
   *
   * @param args Argumentos de linea de comandos (no usados)
   */
  public static void main(final String[] args) {
    SwingUtilities.invokeLater(TrackerApplication::launchWindow);
  }

  // -------------------------------------------------------------------------
  // Construccion de la ventana
  // -------------------------------------------------------------------------

  /**
   * Construye y muestra la ventana principal. Debe llamarse desde el EDT.
   */
  public static void launchWindow() {
    applyDarkLookAndFeel();

    final JFrame frame = new JFrame("Tracker Visual — Music Synthesizer");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().setBackground(C_BG);
    frame.setLayout(new BorderLayout(0, 0));

    final TrackerPanel trackerPanel = new TrackerPanel();

    // Al cambiar la escala: solo reempaquetar si la ventana NO esta maximizada.
    // Si esta maximizada, el contenido ya ocupa toda la pantalla y no hace falta.
    // En ambos casos se actualiza el minimumSize para que el nuevo contenido
    // no quede cortado si el usuario restaura o reduce la ventana.
    trackerPanel.addPropertyChangeListener("scale", evt ->
        SwingUtilities.invokeLater(() -> {
          final boolean maximized =
              (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
          if (!maximized) {
            frame.pack();
          }
          frame.setMinimumSize(frame.getPreferredSize());
        }));

    final JPanel toolbar  = buildToolbar(trackerPanel);
    final JPanel scaleBar = buildScaleBar(trackerPanel);

    final JPanel north = new JPanel(new BorderLayout(0, 0));
    north.setBackground(C_BG);
    north.add(toolbar,  BorderLayout.NORTH);
    north.add(scaleBar, BorderLayout.SOUTH);

    final JScrollPane scroll = new JScrollPane(trackerPanel,
        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBackground(C_BG);
    scroll.getViewport().setBackground(C_BG);
    scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60), 1));

    frame.add(north,          BorderLayout.NORTH);
    frame.add(scroll,         BorderLayout.CENTER);
    frame.add(buildStatusBar(), BorderLayout.SOUTH);

    frame.pack();
    // El minimo es exactamente el tamano empaquetado: el SO impide reducir la ventana
    frame.setMinimumSize(frame.getSize());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // -------------------------------------------------------------------------
  // Toolbar
  // -------------------------------------------------------------------------

  private static JPanel buildToolbar(final TrackerPanel tracker) {
    final JPanel bar = new JPanel(new BorderLayout(0, 0));
    bar.setBackground(C_TOOLBAR_BG);
    bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 75)));

    final JLabel title = new JLabel("  Tracker Visual", SwingConstants.LEFT);
    title.setFont(new Font("SansSerif", Font.BOLD, 14));
    title.setForeground(C_TITLE);
    title.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    bar.add(title, BorderLayout.WEST);

    final JLabel sub = new JLabel(
        "Selecciona una escala y haz clic en las celdas para activarlas",
        SwingConstants.CENTER);
    sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
    sub.setForeground(new Color(100, 100, 140));
    bar.add(sub, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
    buttons.setBackground(C_TOOLBAR_BG);
    final JButton btnClear = makeButton("Borrar todo", C_BTN_CLR, C_BTN_CLR_H);
    btnClear.addActionListener(e -> tracker.clearAll());
    buttons.add(btnClear);
    bar.add(buttons, BorderLayout.EAST);

    return bar;
  }

  // -------------------------------------------------------------------------
  // Selector de escala (JComboBox)
  // -------------------------------------------------------------------------

  /**
   * Construye la barra con el combo box de seleccion de escala.
   * Al cambiar la seleccion se aplica la escala al tracker y se actualiza
   * la etiqueta de informacion.
   *
   * @param tracker Panel del tracker
   * @return Panel de la barra de escala
   */
  private static JPanel buildScaleBar(final TrackerPanel tracker) {
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
    bar.setBackground(C_SCALE_BG);
    bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_SCALE_BDR));

    final JLabel lbl = new JLabel("Escala:");
    lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
    lbl.setForeground(new Color(140, 140, 180));
    bar.add(lbl);

    final JComboBox<TrackerScale> combo = new JComboBox<>(TrackerScale.values());
    combo.setSelectedItem(TrackerScale.EASY_PLUS);
    combo.setBackground(new Color(35, 35, 55));
    combo.setForeground(C_TEXT);
    combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    combo.setFocusable(false);
    combo.setPreferredSize(new Dimension(360, 26));

    // Renderer personalizado: muestra "Basica+ — Pentatonica mayor  (5 notas)"
    combo.setRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public Component getListCellRendererComponent(
          final JList<?> list, final Object value, final int index,
          final boolean isSelected, final boolean hasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        if (value instanceof TrackerScale) {
          final TrackerScale s = (TrackerScale) value;
          setText(String.format(
              "<html><b style='color:#a0c0ff'>%s</b>&nbsp;&nbsp;%s"
              + "&nbsp;&nbsp;<i style='color:#7878a0'>(%s)</i></html>",
              s.getShortName(), s.getFullName(), s.getType()));
        }
        setBackground(isSelected ? new Color(45, 75, 135) : new Color(28, 28, 48));
        setForeground(C_TEXT);
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return this;
      }
    });

    // Etiqueta de info: cuantas notas activas tiene la escala actual (de 37 en total)
    final JLabel infoLbl = new JLabel();
    infoLbl.setFont(new Font("SansSerif", Font.ITALIC, 10));
    infoLbl.setForeground(new Color(90, 90, 130));
    updateInfo(infoLbl, TrackerScale.EASY_PLUS.getNoteCount());

    combo.addActionListener(e -> {
      final TrackerScale sel = (TrackerScale) combo.getSelectedItem();
      if (sel != null) {
        tracker.setScale(sel);
        updateInfo(infoLbl, sel.getNoteCount());
      }
    });

    bar.add(combo);
    bar.add(infoLbl);
    return bar;
  }

  /** Actualiza la etiqueta mostrando cuantas notas tiene la escala seleccionada. */
  private static void updateInfo(final JLabel lbl, final int activeNotes) {
    lbl.setText(String.format("  %d notas activas  (C2 \u2013 C5)", activeNotes));
  }

  // -------------------------------------------------------------------------
  // Barra de estado
  // -------------------------------------------------------------------------

  private static JPanel buildStatusBar() {
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
    bar.setBackground(C_TOOLBAR_BG);
    bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 60)));
    final JLabel info = new JLabel(
        "C = Do  |  D = Re  |  E = Mi  |  F = Fa  |  G = Sol  |  A = La  |  B = Si  |  # = Sostenido");
    info.setFont(new Font("SansSerif", Font.PLAIN, 10));
    info.setForeground(new Color(90, 90, 120));
    bar.add(info);
    return bar;
  }

  // -------------------------------------------------------------------------
  // Boton personalizado
  // -------------------------------------------------------------------------

  private static JButton makeButton(final String text,
                                     final Color normalBg,
                                     final Color hoverBg) {
    final JButton btn = new JButton(text) {
      private static final long serialVersionUID = 1L;
      private boolean hovered = false;

      {
        this.addMouseListener(new java.awt.event.MouseAdapter() {
          @Override public void mouseEntered(final java.awt.event.MouseEvent e) {
            hovered = true; repaint();
          }
          @Override public void mouseExited(final java.awt.event.MouseEvent e) {
            hovered = false; repaint();
          }
        });
      }

      @Override
      protected void paintComponent(final java.awt.Graphics g) {
        final java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setColor(hovered ? hoverBg : normalBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        super.paintComponent(g);
      }
    };

    btn.setForeground(C_TEXT);
    btn.setFont(new Font("SansSerif", Font.BOLD, 11));
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(false);
    btn.setPreferredSize(new Dimension(120, 26));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return btn;
  }

  // -------------------------------------------------------------------------
  // Look and Feel oscuro
  // -------------------------------------------------------------------------

  private static void applyDarkLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
      // usar L&F por defecto
    }
    UIManager.put("Panel.background",             C_BG);
    UIManager.put("ScrollPane.background",        C_BG);
    UIManager.put("Viewport.background",          C_BG);
    UIManager.put("ScrollBar.background",         C_TOOLBAR_BG);
    UIManager.put("ScrollBar.thumb",              new Color(60, 60, 90));
    UIManager.put("Button.background",            new Color(40, 40, 60));
    UIManager.put("Button.foreground",            C_TEXT);
    UIManager.put("ComboBox.background",          new Color(35, 35, 55));
    UIManager.put("ComboBox.foreground",          C_TEXT);
    UIManager.put("ComboBox.selectionBackground", new Color(45, 75, 135));
    UIManager.put("ComboBox.selectionForeground", C_TEXT);
    UIManager.put("List.background",              new Color(28, 28, 48));
    UIManager.put("List.foreground",              C_TEXT);
    UIManager.put("List.selectionBackground",     new Color(45, 75, 135));
    UIManager.put("List.selectionForeground",     C_TEXT);
  }
}
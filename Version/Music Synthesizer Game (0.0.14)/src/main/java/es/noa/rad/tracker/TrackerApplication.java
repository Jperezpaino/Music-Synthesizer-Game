package es.noa.rad.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Aplicacion principal del Tracker Visual.
 *
 * <p>Lanza una ventana independiente con el tracker de notas.
 * Esta es la clase de entrada para explorar y ampliar el tracker
 * paso a paso, separada del sintetizador principal.</p>
 *
 * <p>Para lanzar: ejecutar {@code es.noa.rad.tracker.TrackerApplication}
 * como clase principal de Java.</p>
 */
public final class TrackerApplication {

  // -------------------------------------------------------------------------
  // Colores de la aplicacion
  // -------------------------------------------------------------------------

  private static final Color C_BG         = new Color(15,  15,  22);
  private static final Color C_TOOLBAR_BG = new Color(22,  22,  34);
  private static final Color C_TITLE      = new Color(100, 160, 255);
  private static final Color C_TEXT       = new Color(200, 200, 220);
  private static final Color C_BTN_CLR    = new Color(180,  60,  50);
  private static final Color C_BTN_CLR_H  = new Color(210,  80,  65);
  private static final Color C_BTN_BG     = new Color(40,   40,  60);
  private static final Color C_BTN_BG_H   = new Color(55,   55,  80);

  // -------------------------------------------------------------------------
  // Constructor privado — clase de utilidad
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
   * Construye y muestra la ventana principal del tracker.
   * Debe llamarse desde el hilo de eventos de Swing (EDT).
   */
  public static void launchWindow() {
    applyDarkLookAndFeel();

    final JFrame frame = new JFrame("Tracker Visual — Music Synthesizer");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().setBackground(C_BG);
    frame.setLayout(new BorderLayout(0, 0));

    // Tracker panel
    final TrackerPanel trackerPanel = new TrackerPanel();

    // Toolbar superior
    final JPanel toolbar = buildToolbar(trackerPanel, frame);

    // Scroll por si la ventana es pequena
    final JScrollPane scroll = new JScrollPane(trackerPanel,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.setBackground(C_BG);
    scroll.getViewport().setBackground(C_BG);
    scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60), 1));

    // Barra de estado inferior
    final JPanel statusBar = buildStatusBar();

    frame.add(toolbar,   BorderLayout.NORTH);
    frame.add(scroll,    BorderLayout.CENTER);
    frame.add(statusBar, BorderLayout.SOUTH);

    frame.pack();
    frame.setMinimumSize(new Dimension(frame.getWidth(), frame.getHeight()));
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // -------------------------------------------------------------------------
  // Toolbar
  // -------------------------------------------------------------------------

  /**
   * Construye la barra de herramientas con titulo y botones de accion.
   *
   * @param tracker Panel del tracker para vincular acciones
   * @param frame   Ventana padre (para el titulo)
   * @return Panel de toolbar listo para usar
   */
  private static JPanel buildToolbar(final TrackerPanel tracker, final JFrame frame) {
    final JPanel bar = new JPanel(new BorderLayout(0, 0));
    bar.setBackground(C_TOOLBAR_BG);
    bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 75)));

    // Titulo
    final JLabel title = new JLabel("  🎼  TRACKER VISUAL", SwingConstants.LEFT);
    title.setFont(new Font("SansSerif", Font.BOLD, 14));
    title.setForeground(C_TITLE);
    title.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    bar.add(title, BorderLayout.WEST);

    // Subtitulo / descripcion
    final JLabel sub = new JLabel(
        "16 notas  ×  16 pasos   —   haz clic en las celdas para activarlas",
        SwingConstants.CENTER);
    sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
    sub.setForeground(new Color(100, 100, 140));
    bar.add(sub, BorderLayout.CENTER);

    // Botones
    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
    buttons.setBackground(C_TOOLBAR_BG);

    final JButton btnClear = makeButton("🗑  Borrar todo", C_BTN_CLR, C_BTN_CLR_H);
    btnClear.addActionListener(e -> {
      tracker.clearAll();
      updateFrameTitle(frame, 0);
    });
    buttons.add(btnClear);

    bar.add(buttons, BorderLayout.EAST);
    return bar;
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
  // Helpers de UI
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
    btn.setPreferredSize(new Dimension(130, 28));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    return btn;
  }

  private static void updateFrameTitle(final JFrame frame, final int activeCount) {
    frame.setTitle("Tracker Visual — Music Synthesizer"
        + (activeCount > 0 ? "  [" + activeCount + " activos]" : ""));
  }

  private static void applyDarkLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
      // Usar L&F por defecto si falla
    }
    UIManager.put("Panel.background",       C_BG);
    UIManager.put("ScrollPane.background",  C_BG);
    UIManager.put("Viewport.background",    C_BG);
    UIManager.put("ScrollBar.background",   C_TOOLBAR_BG);
    UIManager.put("ScrollBar.thumb",        new Color(60, 60, 90));
    UIManager.put("Button.background",      C_BTN_BG);
    UIManager.put("Button.foreground",      C_TEXT);
    UIManager.put("Button.border",          BorderFactory.createEmptyBorder(4, 8, 4, 8));
  }
}

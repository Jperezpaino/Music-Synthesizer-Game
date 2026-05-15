package es.noa.rad.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
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

  // Colores del selector de escalas
  private static final Color C_SCALE_BG        = new Color(20,  20,  32);
  private static final Color C_SCALE_SEL       = new Color(50, 100, 180);
  private static final Color C_SCALE_SEL_H     = new Color(60, 120, 210);
  private static final Color C_SCALE_IDLE      = new Color(32,  32,  50);
  private static final Color C_SCALE_IDLE_H    = new Color(42,  42,  65);
  private static final Color C_SCALE_BORDER    = new Color(60,  60,  90);
  private static final Color C_SCALE_TEXT_SEL  = new Color(230, 240, 255);
  private static final Color C_SCALE_TEXT_IDLE = new Color(130, 130, 170);

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

    // Panel selector de escalas (debajo del toolbar)
    final JPanel scaleBar = buildScaleSelector(trackerPanel);

    // Contenedor norte: toolbar + selector de escalas
    final JPanel north = new JPanel(new BorderLayout(0, 0));
    north.setBackground(C_BG);
    north.add(toolbar,   BorderLayout.NORTH);
    north.add(scaleBar,  BorderLayout.SOUTH);

    // Scroll por si la ventana es pequena
    final JScrollPane scroll = new JScrollPane(trackerPanel,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.setBackground(C_BG);
    scroll.getViewport().setBackground(C_BG);
    scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60), 1));

    // Barra de estado inferior
    final JPanel statusBar = buildStatusBar();

    frame.add(north,     BorderLayout.NORTH);
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
  // Selector de escalas
  // -------------------------------------------------------------------------

  /**
   * Construye el panel con los 7 botones de seleccion de escala.
   * Usa un {@link ButtonGroup} para que solo uno este seleccionado.
   * Al arrancar, la escala cromatica (Experta) esta activa por defecto.
   *
   * @param tracker Panel del tracker para aplicar la escala elegida
   * @return Panel listo para usar
   */
  private static JPanel buildScaleSelector(final TrackerPanel tracker) {
    final JPanel outer = new JPanel(new BorderLayout(0, 0));
    outer.setBackground(C_SCALE_BG);
    outer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_SCALE_BORDER));

    // Etiqueta izquierda
    final JLabel lbl = new JLabel("  ESCALA  ", SwingConstants.CENTER);
    lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
    lbl.setForeground(new Color(90, 90, 130));
    lbl.setPreferredSize(new Dimension(70, 44));
    outer.add(lbl, BorderLayout.WEST);

    // Grid de botones
    final TrackerScale[] scales = TrackerScale.values();
    final JPanel grid = new JPanel(new GridLayout(1, scales.length, 4, 0));
    grid.setBackground(C_SCALE_BG);
    grid.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 8));

    final ButtonGroup group = new ButtonGroup();

    // Aplicar escala cromatica por defecto
    tracker.setScale(TrackerScale.CROMATICA);

    for (final TrackerScale scale : scales) {
      final boolean isDefault = (scale == TrackerScale.CROMATICA);
      final JToggleButton btn = makeScaleButton(scale, isDefault);
      btn.addActionListener(e -> tracker.setScale(scale));
      group.add(btn);
      grid.add(btn);
      if (isDefault) {
        btn.setSelected(true);
      }
    }

    outer.add(grid, BorderLayout.CENTER);
    return outer;
  }

  /**
   * Crea un boton de escala con apariencia personalizada de tres lineas:
   * nombre corto (grande), nombre completo y tipo.
   *
   * @param scale     Escala que representa este boton
   * @param selected  Si debe aparecer seleccionado al crearse
   * @return Boton de escala listo para usar
   */
  private static JToggleButton makeScaleButton(final TrackerScale scale,
                                                final boolean selected) {
    final JToggleButton btn = new JToggleButton() {
      private static final long serialVersionUID = 1L;
      private boolean hovered = false;

      {
        this.addMouseListener(new MouseAdapter() {
          @Override public void mouseEntered(final MouseEvent e) { hovered = true;  repaint(); }
          @Override public void mouseExited (final MouseEvent e) { hovered = false; repaint(); }
        });
        this.setSelected(selected);
      }

      @Override
      protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final boolean sel = this.isSelected();
        final Color   bg  = sel ? (hovered ? C_SCALE_SEL_H  : C_SCALE_SEL)
                                : (hovered ? C_SCALE_IDLE_H : C_SCALE_IDLE);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        if (sel) {
          g2.setColor(new Color(100, 160, 255, 120));
          g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        } else {
          g2.setColor(C_SCALE_BORDER);
          g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        }

        final Color textColor = sel ? C_SCALE_TEXT_SEL : C_SCALE_TEXT_IDLE;
        final int cx = getWidth() / 2;

        // Linea 1 — nombre corto
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(textColor);
        final java.awt.FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(scale.getShortName(),
            cx - fm1.stringWidth(scale.getShortName()) / 2, 15);

        // Linea 2 — nombre completo
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(sel ? new Color(180, 210, 255) : new Color(100, 100, 140));
        final java.awt.FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(scale.getFullName(),
            cx - fm2.stringWidth(scale.getFullName()) / 2, 26);

        // Linea 3 — tipo
        g2.setFont(new Font("SansSerif", Font.ITALIC, 8));
        g2.setColor(sel ? new Color(140, 180, 230) : new Color(75, 75, 110));
        final java.awt.FontMetrics fm3 = g2.getFontMetrics();
        g2.drawString(scale.getType(),
            cx - fm3.stringWidth(scale.getType()) / 2, 36);

        g2.dispose();
      }
    };

    btn.setPreferredSize(new Dimension(120, 44));
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return btn;
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

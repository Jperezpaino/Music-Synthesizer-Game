package es.noa.rad.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel principal del Tracker Visual.
 *
 * <p>Muestra una cuadricula de 16 filas x 16 columnas estilo tracker/piano-roll
 * simplificado. Las filas representan notas (escala cromatica de C4 a D#5) y
 * las columnas representan pasos de tiempo.</p>
 *
 * <p>Cada celda se puede activar/desactivar con un clic. Las teclas negras
 * (sostenidos) tienen un color distinto a las blancas para facilitar la lectura.</p>
 */
public final class TrackerPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // -------------------------------------------------------------------------
  // Dimensiones de la cuadricula
  // -------------------------------------------------------------------------

  /** Numero de filas (notas) */
  public static final int ROWS = 16;

  /** Numero de columnas (pasos de tiempo) */
  public static final int COLS = 16;

  /** Ancho de cada celda de paso en pixels */
  private static final int CELL_W = 40;

  /** Alto de cada celda de paso en pixels */
  private static final int CELL_H = 30;

  /** Ancho de la columna de etiquetas de nota */
  private static final int LABEL_W = 70;

  // -------------------------------------------------------------------------
  // Colores — tema oscuro
  // -------------------------------------------------------------------------

  private static final Color C_BG            = new Color(18,  18,  26);
  private static final Color C_HEADER_BG     = new Color(25,  25,  36);
  private static final Color C_LABEL_WHITE   = new Color(220, 220, 235);
  private static final Color C_LABEL_BLACK   = new Color(160, 160, 190);
  private static final Color C_LABEL_BG_W    = new Color(36,  36,  52);
  private static final Color C_LABEL_BG_B    = new Color(24,  24,  38);
  private static final Color C_CELL_OFF_W    = new Color(38,  40,  58);
  private static final Color C_CELL_OFF_B    = new Color(26,  26,  40);
  private static final Color C_CELL_ON_W     = new Color(64,  140, 220);
  private static final Color C_CELL_ON_B     = new Color(200,  90,  50);
  private static final Color C_CELL_HOVER_W  = new Color(55,   60,  82);
  private static final Color C_CELL_HOVER_B  = new Color(40,   38,  58);
  private static final Color C_GRID_LINE     = new Color(50,   50,  72);
  private static final Color C_BEAT_LINE     = new Color(80,   80, 110);
  private static final Color C_OCT_SEPARATOR = new Color(100, 120, 170);
  private static final Color C_HEADER_TEXT   = new Color(130, 130, 160);
  private static final Color C_STEP_NUM      = new Color(80,   80, 110);
  private static final Color C_STEP_BEAT     = new Color(140, 160, 210);

  // -------------------------------------------------------------------------
  // Definicion de las 16 notas (filas)
  // -------------------------------------------------------------------------

  /** Nombres de nota para la etiqueta de cada fila */
  private static final String[] NOTE_NAMES = {
      "C",  "C#", "D",  "D#",
      "E",  "F",  "F#", "G",
      "G#", "A",  "A#", "B",
      "C",  "C#", "D",  "D#"
  };

  /** Octava de cada fila */
  private static final int[] NOTE_OCTAVES = {
      4, 4, 4, 4,
      4, 4, 4, 4,
      4, 4, 4, 4,
      5, 5, 5, 5
  };

  /**
   * Indica si cada fila corresponde a una tecla negra (sostenido / bemol).
   * true = tecla negra, false = tecla blanca.
   */
  private static final boolean[] IS_BLACK = {
      false, true,  false, true,
      false, false, true,  false,
      true,  false, true,  false,
      false, true,  false, true
  };

  // -------------------------------------------------------------------------
  // Colores para filas deshabilitadas por escala
  // -------------------------------------------------------------------------

  private static final Color C_CELL_DISABLED_W = new Color(26, 26, 36);
  private static final Color C_CELL_DISABLED_B = new Color(18, 18, 28);
  private static final Color C_LABEL_DISABLED   = new Color(55, 55, 75);

  // -------------------------------------------------------------------------
  // Estado de la cuadricula
  // -------------------------------------------------------------------------

  /** active[fila][columna] = true si ese paso esta activado */
  private final boolean[][] active = new boolean[ROWS][COLS];

  /**
   * rowEnabled[fila] = false cuando la escala actual no incluye esa nota.
   * Las filas deshabilitadas no se pueden editar y se muestran en gris oscuro.
   */
  private final boolean[] rowEnabled = new boolean[ROWS];

  /** Escala actualmente seleccionada (null = sin filtro = todas activas). */
  private TrackerScale currentScale = null;

  // -------------------------------------------------------------------------
  // Subcomponentes
  // -------------------------------------------------------------------------

  /** Matriz de celdas de paso para acceso directo */
  private final StepCell[][] cells = new StepCell[ROWS][COLS];

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Crea el panel del tracker con la cuadricula completa.
   */
  public TrackerPanel() {
    // Por defecto todas las filas habilitadas (escala cromatica)
    for (int r = 0; r < ROWS; r++) {
      this.rowEnabled[r] = true;
    }

    this.setBackground(C_BG);
    this.setLayout(new BorderLayout(0, 0));
    this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    this.add(this.buildHeaderRow(),  BorderLayout.NORTH);
    this.add(this.buildMainArea(),   BorderLayout.CENTER);
  }

  // -------------------------------------------------------------------------
  // Construccion de la cabecera (numeros de paso)
  // -------------------------------------------------------------------------

  /**
   * Construye la fila superior con los numeros de paso (1-16).
   * Los pasos al inicio de cada grupo de 4 van en color mas brillante.
   */
  private JPanel buildHeaderRow() {
    final JPanel header = new JPanel(new BorderLayout(0, 0));
    header.setBackground(C_HEADER_BG);
    header.setPreferredSize(new Dimension(LABEL_W + COLS * CELL_W, 24));

    // Espacio vacio sobre las etiquetas de nota
    final JLabel corner = new JLabel("NOTA", SwingConstants.CENTER);
    corner.setPreferredSize(new Dimension(LABEL_W, 24));
    corner.setForeground(C_HEADER_TEXT);
    corner.setFont(new Font("SansSerif", Font.BOLD, 10));
    corner.setOpaque(true);
    corner.setBackground(C_HEADER_BG);
    header.add(corner, BorderLayout.WEST);

    // Numeros de paso
    final JPanel nums = new JPanel(new GridLayout(1, COLS, 0, 0));
    nums.setBackground(C_HEADER_BG);
    for (int col = 0; col < COLS; col++) {
      final boolean isBeat = (col % 4 == 0);
      final JLabel lbl = new JLabel(String.valueOf(col + 1), SwingConstants.CENTER);
      lbl.setPreferredSize(new Dimension(CELL_W, 24));
      lbl.setFont(new Font("Monospaced", Font.BOLD, 10));
      lbl.setForeground(isBeat ? C_STEP_BEAT : C_STEP_NUM);
      lbl.setOpaque(true);
      lbl.setBackground(isBeat ? new Color(30, 32, 48) : C_HEADER_BG);
      if (col % 4 == 0 && col > 0) {
        lbl.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, C_BEAT_LINE));
      }
      nums.add(lbl);
    }
    header.add(nums, BorderLayout.CENTER);

    return header;
  }

  // -------------------------------------------------------------------------
  // Construccion del area principal (etiquetas + cuadricula)
  // -------------------------------------------------------------------------

  /**
   * Construye el area principal: columna de etiquetas de nota a la izquierda
   * y cuadricula de celdas a la derecha.
   */
  private JPanel buildMainArea() {
    final JPanel area = new JPanel(new BorderLayout(0, 0));
    area.setBackground(C_BG);

    area.add(this.buildNoteLabels(), BorderLayout.WEST);
    area.add(this.buildGrid(),       BorderLayout.CENTER);

    return area;
  }

  /**
   * Construye la columna de etiquetas de nota (izquierda).
   * Cada etiqueta muestra el nombre de la nota y su octava.
   */
  private JPanel buildNoteLabels() {
    final JPanel labels = new JPanel();
    labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
    labels.setBackground(C_BG);
    labels.setPreferredSize(new Dimension(LABEL_W, ROWS * CELL_H));

    for (int row = 0; row < ROWS; row++) {
      final boolean black = IS_BLACK[row];
      final String  name  = NOTE_NAMES[row] + NOTE_OCTAVES[row];

      final JLabel lbl = new JLabel(name, SwingConstants.CENTER) {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(final Graphics g) {
          // Fondo degradado sutil
          final Graphics2D g2 = (Graphics2D) g.create();
          g2.setColor(black ? C_LABEL_BG_B : C_LABEL_BG_W);
          g2.fillRect(0, 0, getWidth(), getHeight());

          // Indicador lateral de tecla negra
          if (black) {
            g2.setColor(new Color(200, 90, 50, 80));
            g2.fillRect(0, 0, 4, getHeight());
          } else {
            g2.setColor(new Color(64, 140, 220, 60));
            g2.fillRect(0, 0, 4, getHeight());
          }
          g2.dispose();
          super.paintComponent(g);
        }
      };

      lbl.setOpaque(false);
      lbl.setForeground(black ? C_LABEL_BLACK : C_LABEL_WHITE);
      lbl.setFont(new Font("Monospaced", black ? Font.PLAIN : Font.BOLD, 11));
      lbl.setMaximumSize(new Dimension(LABEL_W, CELL_H));
      lbl.setMinimumSize(new Dimension(LABEL_W, CELL_H));
      lbl.setPreferredSize(new Dimension(LABEL_W, CELL_H));

      // Separador entre las dos octavas (despues de la fila 11 = B4)
      if (row == 11) {
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, C_OCT_SEPARATOR));
      } else {
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_GRID_LINE));
      }

      labels.add(lbl);
      // Relleno de altura exacta
      if (row < ROWS - 1) {
        labels.add(Box.createRigidArea(new Dimension(0, 0)));
      }
    }

    return labels;
  }

  /**
   * Construye la cuadricula de 16x16 celdas de paso.
   */
  private JPanel buildGrid() {
    final JPanel grid = new JPanel(new GridLayout(ROWS, COLS, 0, 0));
    grid.setBackground(C_BG);

    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        final StepCell cell = new StepCell(row, col);
        this.cells[row][col] = cell;
        grid.add(cell);
      }
    }

    return grid;
  }

  // -------------------------------------------------------------------------
  // API publica
  // -------------------------------------------------------------------------

  /**
   * Activa o desactiva un paso concreto de la cuadricula.
   *
   * @param row  Fila (0-15)
   * @param col  Columna (0-15)
   * @param on   {@code true} para activar
   */
  public void setStep(final int row, final int col, final boolean on) {
    if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
      this.active[row][col] = on;
      this.cells[row][col].repaint();
    }
  }

  /**
   * Devuelve si un paso esta activado.
   *
   * @param row Fila (0-15)
   * @param col Columna (0-15)
   * @return {@code true} si esta activo
   */
  public boolean isStepActive(final int row, final int col) {
    return this.active[row][col];
  }

  /**
   * Borra toda la cuadricula (desactiva todos los pasos).
   */
  public void clearAll() {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        this.active[r][c] = false;
        this.cells[r][c].repaint();
      }
    }
  }

  /**
   * Aplica una escala musical al tracker.
   *
   * <p>Las filas cuya nota no pertenece a la escala quedan deshabilitadas:
   * se muestran en gris oscuro y no se pueden editar. Los pasos que ya
   * estaban activados en filas deshabilitadas se borran automaticamente.</p>
   *
   * @param scale Escala a aplicar, o {@code null} para habilitar todas las filas
   */
  public void setScale(final TrackerScale scale) {
    this.currentScale = scale;
    for (int r = 0; r < ROWS; r++) {
      final boolean enabled = (scale == null) || scale.isRowActive(r);
      this.rowEnabled[r] = enabled;
      if (!enabled) {
        // Limpiar pasos en filas deshabilitadas
        for (int c = 0; c < COLS; c++) {
          this.active[r][c] = false;
        }
      }
    }
    // Repintar toda la cuadricula
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        this.cells[r][c].repaint();
      }
    }
  }

  /**
   * Devuelve la escala actualmente activa.
   *
   * @return Escala actual, o {@code null} si no hay filtro
   */
  public TrackerScale getCurrentScale() {
    return this.currentScale;
  }

  /**
   * Devuelve una copia del estado completo de la cuadricula.
   *
   * @return Matriz booleana [filas][columnas]
   */
  public boolean[][] getSteps() {
    final boolean[][] copy = new boolean[ROWS][COLS];
    for (int r = 0; r < ROWS; r++) {
      System.arraycopy(this.active[r], 0, copy[r], 0, COLS);
    }
    return copy;
  }

  /**
   * Devuelve el nombre de la nota de una fila.
   *
   * @param row Fila (0-15)
   * @return Nombre con octava, ej. "C4", "C#5"
   */
  public static String getNoteName(final int row) {
    if (row < 0 || row >= ROWS) {
      return "?";
    }
    return NOTE_NAMES[row] + NOTE_OCTAVES[row];
  }

  // -------------------------------------------------------------------------
  // Celda de paso (componente interno)
  // -------------------------------------------------------------------------

  /**
   * Celda individual de la cuadricula. Se pinta a si misma segun su estado
   * (activa/inactiva/hover) y notifica al padre al hacer clic.
   */
  private final class StepCell extends JPanel {

    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;
    private boolean   hovered = false;

    StepCell(final int row, final int col) {
      this.row = row;
      this.col = col;

      this.setPreferredSize(new Dimension(CELL_W, CELL_H));
      this.setOpaque(false);

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
          // Ignorar clic si la fila esta deshabilitada por la escala
          if (!TrackerPanel.this.rowEnabled[StepCell.this.row]) {
            return;
          }
          TrackerPanel.this.active[StepCell.this.row][StepCell.this.col]
              = !TrackerPanel.this.active[StepCell.this.row][StepCell.this.col];
          StepCell.this.repaint();
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
          StepCell.this.hovered = true;
          StepCell.this.repaint();
        }

        @Override
        public void mouseExited(final MouseEvent e) {
          StepCell.this.hovered = false;
          StepCell.this.repaint();
        }
      });
    }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final boolean black  = IS_BLACK[this.row];
      final boolean on     = TrackerPanel.this.active[this.row][this.col];
      final boolean isBeat = (this.col % 4 == 0);
      final boolean enabled = TrackerPanel.this.rowEnabled[this.row];

      final int w = this.getWidth();
      final int h = this.getHeight();

      // --- Fondo de celda ---
      final Color bg;
      if (!enabled) {
        bg = black ? C_CELL_DISABLED_B : C_CELL_DISABLED_W;
      } else if (on) {
        bg = black ? C_CELL_ON_B : C_CELL_ON_W;
      } else if (this.hovered) {
        bg = black ? C_CELL_HOVER_B : C_CELL_HOVER_W;
      } else {
        bg = black ? C_CELL_OFF_B : C_CELL_OFF_W;
      }
      g2.setColor(bg);
      g2.fillRoundRect(2, 2, w - 4, h - 4, 6, 6);

      // --- Brillo interior cuando esta activa ---
      if (on) {
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(3, 3, w - 6, (h - 6) / 2, 5, 5);
      }

      // --- Borde de la celda ---
      if (on) {
        g2.setColor(black
            ? new Color(220, 110, 70)
            : new Color(90, 170, 255));
        g2.drawRoundRect(2, 2, w - 4, h - 4, 6, 6);
      } else {
        g2.setColor(C_GRID_LINE);
        g2.drawRoundRect(2, 2, w - 4, h - 4, 6, 6);
      }

      // --- Linea de separacion de beat (cada 4 columnas) ---
      if (isBeat && this.col > 0) {
        g2.setColor(C_BEAT_LINE);
        g2.fillRect(0, 0, 2, h);
      }

      // --- Linea separadora de octava (despues de la fila 11 = B4) ---
      if (this.row == 11) {
        g2.setColor(C_OCT_SEPARATOR);
        g2.fillRect(0, h - 2, w, 2);
      }

      g2.dispose();
    }
  }
}

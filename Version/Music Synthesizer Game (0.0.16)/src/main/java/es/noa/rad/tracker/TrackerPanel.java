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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel principal del Tracker Visual con filas dinamicas.
 *
 * <p>El numero de filas depende de la escala seleccionada. El rango base
 * cubre de C4 a C7 (37 semitonos); cada escala filtra los semitonos que
 * le corresponden:</p>
 * <ul>
 *   <li>Pentatonica (5 notas/octava) -&gt; 16 filas</li>
 *   <li>Hexatonica  (6 notas/octava) -&gt; 19 filas</li>
 *   <li>Diatonica   (7 notas/octava) -&gt; 22 filas</li>
 *   <li>Cromatica   (12 notas/oct.)  -&gt; 37 filas</li>
 * </ul>
 */
public final class TrackerPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // -------------------------------------------------------------------------
  // Constantes de layout
  // -------------------------------------------------------------------------

  /** Numero de columnas (pasos de tiempo) — fijo */
  public static final int COLS = 16;

  private static final int CELL_W  = 40;
  private static final int CELL_H  = 30;
  private static final int LABEL_W = 70;

  // -------------------------------------------------------------------------
  // Rango de notas: C4 a C7 inclusive (37 semitonos, indices 0..36)
  // -------------------------------------------------------------------------

  private static final int RANGE_BASE_OCTAVE = 4;
  private static final int RANGE_SEMITONES   = 37;  // 0 = C4, 36 = C7

  private static final String[]  NAMES_12 = {
      "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
  };
  private static final boolean[] BLACK_12 = {
      false, true, false, true, false,
      false, true, false, true, false, true, false
  };

  // -------------------------------------------------------------------------
  // Colores
  // -------------------------------------------------------------------------

  private static final Color C_BG           = new Color(18,  18,  26);
  private static final Color C_HEADER_BG    = new Color(25,  25,  36);
  private static final Color C_LABEL_WHITE  = new Color(220, 220, 235);
  private static final Color C_LABEL_BLACK  = new Color(160, 160, 190);
  private static final Color C_LABEL_BG_W   = new Color(36,  36,  52);
  private static final Color C_LABEL_BG_B   = new Color(24,  24,  38);
  private static final Color C_CELL_OFF_W   = new Color(38,  40,  58);
  private static final Color C_CELL_OFF_B   = new Color(26,  26,  40);
  private static final Color C_CELL_ON_W    = new Color(64,  140, 220);
  private static final Color C_CELL_ON_B    = new Color(200,  90,  50);
  private static final Color C_CELL_HOVER_W = new Color(55,   60,  82);
  private static final Color C_CELL_HOVER_B = new Color(40,   38,  58);
  private static final Color C_GRID_LINE    = new Color(50,   50,  72);
  private static final Color C_BEAT_LINE    = new Color(80,   80, 110);
  private static final Color C_OCT_SEP      = new Color(100, 120, 170);
  private static final Color C_HEADER_TEXT  = new Color(130, 130, 160);
  private static final Color C_STEP_NUM     = new Color(80,   80, 110);
  private static final Color C_STEP_BEAT    = new Color(140, 160, 210);

  // -------------------------------------------------------------------------
  // Estado dinamico
  // -------------------------------------------------------------------------

  /** Lista de notas (filas) para la escala actual — orden descendente (C7 arriba) */
  private List<NoteInfo> noteList = new ArrayList<>();

  /** active[fila][columna] */
  private boolean[][] active;

  /** Celdas de la cuadricula */
  private StepCell[][] cells;

  /** Escala actualmente seleccionada */
  private TrackerScale currentScale;

  /** Contenedor que se reconstruye en cada cambio de escala */
  private final JPanel dynamicArea;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Crea el TrackerPanel con la escala cromatica por defecto.
   */
  public TrackerPanel() {
    this.setBackground(C_BG);
    this.setLayout(new BorderLayout(0, 0));
    this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    this.add(buildHeaderRow(), BorderLayout.NORTH);

    this.dynamicArea = new JPanel(new BorderLayout(0, 0));
    this.dynamicArea.setBackground(C_BG);
    this.add(this.dynamicArea, BorderLayout.CENTER);

    applyScaleInternal(TrackerScale.CROMATICA);
  }

  // -------------------------------------------------------------------------
  // Cambio de escala
  // -------------------------------------------------------------------------

  /**
   * Aplica una nueva escala. Reconstruye las filas y dispara el evento
   * {@code "scale"} para que la ventana pueda redimensionarse.
   *
   * @param scale Nueva escala a aplicar
   */
  public void setScale(final TrackerScale scale) {
    final TrackerScale old = this.currentScale;
    applyScaleInternal(scale);
    this.firePropertyChange("scale", old, scale);
  }

  /**
   * Devuelve la escala actualmente activa.
   *
   * @return Escala actual
   */
  public TrackerScale getCurrentScale() {
    return this.currentScale;
  }

  /**
   * Numero de filas (notas visibles) de la escala actual.
   *
   * @return Numero de filas
   */
  public int getRowCount() {
    return this.noteList.size();
  }

  /** Reconstruccion interna sin disparar eventos. */
  private void applyScaleInternal(final TrackerScale scale) {
    this.currentScale = scale;
    this.noteList = buildNoteList(scale);
    final int rows = this.noteList.size();
    this.active = new boolean[rows][COLS];
    this.cells  = new StepCell[rows][COLS];

    this.dynamicArea.removeAll();
    this.dynamicArea.add(buildNoteLabels(), BorderLayout.WEST);
    this.dynamicArea.add(buildGrid(),       BorderLayout.CENTER);
    this.dynamicArea.revalidate();
    this.dynamicArea.repaint();
  }

  /**
   * Genera la lista de notas para una escala en el rango C4-C7.
   * Las notas estan en orden descendente (C7 primero).
   *
   * @param scale Escala a usar como filtro
   * @return Lista de notas de arriba a abajo
   */
  private static List<NoteInfo> buildNoteList(final TrackerScale scale) {
    final List<NoteInfo> list = new ArrayList<>();
    for (int st = RANGE_SEMITONES - 1; st >= 0; st--) {
      final int semInOct = st % 12;
      if (scale.isRowActive(semInOct)) {
        final int octave = RANGE_BASE_OCTAVE + st / 12;
        list.add(new NoteInfo(semInOct, NAMES_12[semInOct], octave, BLACK_12[semInOct]));
      }
    }
    return list;
  }

  // -------------------------------------------------------------------------
  // Cabecera (estatica — siempre 16 columnas)
  // -------------------------------------------------------------------------

  private JPanel buildHeaderRow() {
    final JPanel header = new JPanel(new BorderLayout(0, 0));
    header.setBackground(C_HEADER_BG);
    header.setPreferredSize(new Dimension(LABEL_W + COLS * CELL_W, 24));

    final JLabel corner = new JLabel("NOTA", SwingConstants.CENTER);
    corner.setPreferredSize(new Dimension(LABEL_W, 24));
    corner.setForeground(C_HEADER_TEXT);
    corner.setFont(new Font("SansSerif", Font.BOLD, 10));
    corner.setOpaque(true);
    corner.setBackground(C_HEADER_BG);
    header.add(corner, BorderLayout.WEST);

    final JPanel nums = new JPanel(new GridLayout(1, COLS, 0, 0));
    nums.setBackground(C_HEADER_BG);
    for (int col = 0; col < COLS; col++) {
      final boolean isBeat = (col % 4 == 0);
      final JLabel  lbl    = new JLabel(String.valueOf(col + 1), SwingConstants.CENTER);
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
  // Etiquetas de nota (dinamicas)
  // -------------------------------------------------------------------------

  private JPanel buildNoteLabels() {
    final int    rows   = this.noteList.size();
    final JPanel labels = new JPanel();
    labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
    labels.setBackground(C_BG);
    labels.setPreferredSize(new Dimension(LABEL_W, rows * CELL_H));

    for (int row = 0; row < rows; row++) {
      final NoteInfo ni     = this.noteList.get(row);
      final boolean  black  = ni.isBlack;
      final String   text   = ni.label();
      final boolean  octSep = isOctaveSeparator(row);

      final boolean isBlackFinal = black;

      final JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
        private static final long serialVersionUID = 1L;
        @Override
        protected void paintComponent(final Graphics g) {
          final Graphics2D g2 = (Graphics2D) g.create();
          g2.setColor(isBlackFinal ? C_LABEL_BG_B : C_LABEL_BG_W);
          g2.fillRect(0, 0, getWidth(), getHeight());
          g2.setColor(isBlackFinal
              ? new Color(200, 90, 50, 80)
              : new Color(64, 140, 220, 60));
          g2.fillRect(0, 0, 4, getHeight());
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
      lbl.setBorder(octSep
          ? BorderFactory.createMatteBorder(0, 0, 2, 0, C_OCT_SEP)
          : BorderFactory.createMatteBorder(0, 0, 1, 0, C_GRID_LINE));

      labels.add(lbl);
      if (row < rows - 1) {
        labels.add(Box.createRigidArea(new Dimension(0, 0)));
      }
    }
    return labels;
  }

  // -------------------------------------------------------------------------
  // Cuadricula (dinamica)
  // -------------------------------------------------------------------------

  private JPanel buildGrid() {
    final int    rows = this.noteList.size();
    final JPanel grid = new JPanel(new GridLayout(rows, COLS, 0, 0));
    grid.setBackground(C_BG);

    for (int row = 0; row < rows; row++) {
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
   * Activa o desactiva un paso.
   *
   * @param row Fila (0-based, segun la escala actual)
   * @param col Columna (0-15)
   * @param on  {@code true} para activar
   */
  public void setStep(final int row, final int col, final boolean on) {
    if (row >= 0 && row < this.noteList.size() && col >= 0 && col < COLS) {
      this.active[row][col] = on;
      this.cells[row][col].repaint();
    }
  }

  /**
   * Devuelve si un paso esta activo.
   *
   * @param row Fila (0-based)
   * @param col Columna (0-15)
   * @return {@code true} si esta activo
   */
  public boolean isStepActive(final int row, final int col) {
    return this.active[row][col];
  }

  /**
   * Borra todos los pasos activos.
   */
  public void clearAll() {
    for (int r = 0; r < this.noteList.size(); r++) {
      for (int c = 0; c < COLS; c++) {
        this.active[r][c] = false;
        this.cells[r][c].repaint();
      }
    }
  }

  /**
   * Devuelve una copia del estado completo.
   *
   * @return Matriz [filas][columnas]
   */
  public boolean[][] getSteps() {
    final int       rows = this.noteList.size();
    final boolean[][] copy = new boolean[rows][COLS];
    for (int r = 0; r < rows; r++) {
      System.arraycopy(this.active[r], 0, copy[r], 0, COLS);
    }
    return copy;
  }

  // -------------------------------------------------------------------------
  // Helper — separador de octava
  // -------------------------------------------------------------------------

  /**
   * Indica si la fila {@code row} debe mostrar un separador de octava
   * en su borde inferior (la fila siguiente pertenece a una octava diferente).
   */
  private boolean isOctaveSeparator(final int row) {
    if (row >= this.noteList.size() - 1) {
      return false;
    }
    return this.noteList.get(row).octave != this.noteList.get(row + 1).octave;
  }

  // -------------------------------------------------------------------------
  // NoteInfo — descriptor de cada fila
  // -------------------------------------------------------------------------

  /**
   * Datos de una nota (fila) del tracker.
   */
  public static final class NoteInfo {
    /** Semitono dentro de la octava (0-11) */
    public final int     semitone;
    /** Nombre de la nota, ej. "C#" */
    public final String  name;
    /** Octava, ej. 4, 5, 6, 7 */
    public final int     octave;
    /** {@code true} si es tecla negra */
    public final boolean isBlack;

    NoteInfo(final int semitone, final String name,
             final int octave,  final boolean isBlack) {
      this.semitone = semitone;
      this.name     = name;
      this.octave   = octave;
      this.isBlack  = isBlack;
    }

    /**
     * Etiqueta de visualizacion, ej. "C#5".
     *
     * @return Nombre + octava
     */
    public String label() {
      return this.name + this.octave;
    }
  }

  // -------------------------------------------------------------------------
  // StepCell — celda individual
  // -------------------------------------------------------------------------

  private final class StepCell extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int     row;
    private final int     col;
    private       boolean hovered = false;

    StepCell(final int row, final int col) {
      this.row = row;
      this.col = col;
      this.setPreferredSize(new Dimension(CELL_W, CELL_H));
      this.setOpaque(false);

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
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

      final NoteInfo ni    = TrackerPanel.this.noteList.get(this.row);
      final boolean  black = ni.isBlack;
      final boolean  on    = TrackerPanel.this.active[this.row][this.col];
      final boolean  beat  = (this.col % 4 == 0);
      final boolean  octSep = TrackerPanel.this.isOctaveSeparator(this.row);

      final int w = this.getWidth();
      final int h = this.getHeight();

      // Fondo
      final Color bg;
      if (on) {
        bg = black ? C_CELL_ON_B : C_CELL_ON_W;
      } else if (this.hovered) {
        bg = black ? C_CELL_HOVER_B : C_CELL_HOVER_W;
      } else {
        bg = black ? C_CELL_OFF_B : C_CELL_OFF_W;
      }
      g2.setColor(bg);
      g2.fillRoundRect(2, 2, w - 4, h - 4, 6, 6);

      // Brillo activa
      if (on) {
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(3, 3, w - 6, (h - 6) / 2, 5, 5);
      }

      // Borde
      if (on) {
        g2.setColor(black ? new Color(220, 110, 70) : new Color(90, 170, 255));
      } else {
        g2.setColor(C_GRID_LINE);
      }
      g2.drawRoundRect(2, 2, w - 4, h - 4, 6, 6);

      // Linea de beat
      if (beat && this.col > 0) {
        g2.setColor(C_BEAT_LINE);
        g2.fillRect(0, 0, 2, h);
      }

      // Linea separadora de octava
      if (octSep) {
        g2.setColor(C_OCT_SEP);
        g2.fillRect(0, h - 2, w, 2);
      }

      g2.dispose();
    }
  }
}
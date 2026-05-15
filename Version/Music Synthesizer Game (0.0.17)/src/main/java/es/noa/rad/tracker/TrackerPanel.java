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
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel principal del Tracker Visual.
 *
 * <p>Siempre muestra las 37 notas del rango C2-C5 (MIDI 36-72).
 * Las notas que no pertenecen a la escala seleccionada se muestran
 * como filas "fantasma" (atenuadas, no interactivas).
 * Las notas activas pueden activarse/desactivarse haciendo clic.</p>
 */
public final class TrackerPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // -------------------------------------------------------------------------
  // Constantes de layout
  // -------------------------------------------------------------------------

  /** Numero de columnas (pasos de tiempo) - fijo. */
  public static final int COLS   = 16;
  /** Numero de filas - siempre 37 (C2 a C5 inclusive). */
  public static final int ROWS   = 37;

  private static final int CELL_W  = 40;
  private static final int CELL_H  = 20;
  private static final int LABEL_W = 70;

  // Rango C2 (MIDI 36) -> C5 (MIDI 72): 37 semitonos
  private static final int RANGE_BASE_OCTAVE = 2;
  private static final int RANGE_SEMITONES   = 37;

  private static final String[]  NAMES_12 = {
      "C","C#","D","D#","E","F","F#","G","G#","A","A#","B"
  };
  private static final boolean[] BLACK_12 = {
      false,true,false,true,false,
      false,true,false,true,false,true,false
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

  // Colores para filas fantasma (nota no pertenece a la escala actual)
  private static final Color C_GHOST_LABEL  = new Color(22,  22,  32);
  private static final Color C_GHOST_TEXT   = new Color(48,  48,  65);
  private static final Color C_GHOST_CELL   = new Color(18,  18,  26);
  private static final Color C_GHOST_BORDER = new Color(35,  35,  50);

  // -------------------------------------------------------------------------
  // Estado
  // -------------------------------------------------------------------------

  /** Lista permanente de las 37 notas (C5 arriba, C2 abajo). */
  private final List<NoteInfo> noteList;

  /** rowActive[i] = true si la fila i pertenece a la escala actual. */
  private final boolean[] rowActive = new boolean[ROWS];

  /** active[fila][col] = paso activado por el usuario. */
  private final boolean[][] active = new boolean[ROWS][COLS];

  /** Celdas de la cuadricula (construidas una vez). */
  private final StepCell[][] cells = new StepCell[ROWS][COLS];

  /** Etiquetas de las notas (construidas una vez). */
  private final NoteLabel[] noteLabels = new NoteLabel[ROWS];

  /** Escala actualmente seleccionada. */
  private TrackerScale currentScale;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /** Crea el TrackerPanel con la escala Expert (cromatica) por defecto. */
  public TrackerPanel() {
    this.noteList = buildAllNotes();
    this.setBackground(C_BG);
    this.setLayout(new BorderLayout(0, 0));
    this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    this.add(buildHeaderRow(), BorderLayout.NORTH);

    final JPanel area = new JPanel(new BorderLayout(0, 0));
    area.setBackground(C_BG);
    buildStaticGrid(area);
    this.add(area, BorderLayout.CENTER);

    applyScaleInternal(TrackerScale.EXPERT);
  }

  // -------------------------------------------------------------------------
  // API de escala
  // -------------------------------------------------------------------------

  /** Cambia la escala activa y actualiza la vista. */
  public void setScale(final TrackerScale scale) {
    final TrackerScale old = this.currentScale;
    applyScaleInternal(scale);
    this.firePropertyChange("scale", old, scale);
  }

  /** Devuelve la escala actualmente activa. */
  public TrackerScale getCurrentScale() { return this.currentScale; }

  /** Siempre devuelve 37 (rango fijo C2-C5). */
  public int getRowCount() { return ROWS; }

  // -------------------------------------------------------------------------
  // Logica interna de escala
  // -------------------------------------------------------------------------

  private void applyScaleInternal(final TrackerScale scale) {
    this.currentScale = scale;
    for (int row = 0; row < ROWS; row++) {
      this.rowActive[row] = scale.isRowActive(this.noteList.get(row).semitone);
    }
    // Repintar etiquetas y celdas para mostrar / ocultar estado fantasma
    for (int row = 0; row < ROWS; row++) {
      this.noteLabels[row].repaint();
      for (int col = 0; col < COLS; col++) {
        this.cells[row][col].repaint();
      }
    }
  }

  /** Genera la lista de las 37 notas en orden descendente (C5 primero). */
  private static List<NoteInfo> buildAllNotes() {
    final List<NoteInfo> list = new ArrayList<>();
    for (int st = RANGE_SEMITONES - 1; st >= 0; st--) {
      final int semInOct = st % 12;
      final int octave   = RANGE_BASE_OCTAVE + st / 12;
      list.add(new NoteInfo(semInOct, NAMES_12[semInOct], octave, BLACK_12[semInOct]));
    }
    return list;
  }

  // -------------------------------------------------------------------------
  // Construccion estatica de la UI (una sola vez)
  // -------------------------------------------------------------------------

  private void buildStaticGrid(final JPanel area) {
    // Panel de etiquetas de nota
    final JPanel labelsPanel = new JPanel();
    labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
    labelsPanel.setBackground(C_BG);
    labelsPanel.setPreferredSize(new Dimension(LABEL_W, ROWS * CELL_H));

    for (int row = 0; row < ROWS; row++) {
      final NoteLabel lbl = new NoteLabel(row);
      this.noteLabels[row] = lbl;
      labelsPanel.add(lbl);
    }

    // Panel de celdas
    final JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS, 0, 0));
    gridPanel.setBackground(C_BG);
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        final StepCell cell = new StepCell(row, col);
        this.cells[row][col] = cell;
        gridPanel.add(cell);
      }
    }

    area.add(labelsPanel, BorderLayout.WEST);
    area.add(gridPanel,   BorderLayout.CENTER);
  }

  // -------------------------------------------------------------------------
  // Cabecera estatica (numeros de paso)
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
  // Helper — separador de octava
  // -------------------------------------------------------------------------

  private boolean isOctaveSeparator(final int row) {
    if (row >= ROWS - 1) return false;
    return this.noteList.get(row).octave != this.noteList.get(row + 1).octave;
  }

  // -------------------------------------------------------------------------
  // API publica de pasos
  // -------------------------------------------------------------------------

  /** Activa o desactiva un paso. Solo tiene efecto en filas activas de la escala. */
  public void setStep(final int row, final int col, final boolean on) {
    if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
      this.active[row][col] = on;
      this.cells[row][col].repaint();
    }
  }

  /** Devuelve si un paso esta activo. */
  public boolean isStepActive(final int row, final int col) {
    return this.active[row][col];
  }

  /** Borra todos los pasos activos. */
  public void clearAll() {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        this.active[r][c] = false;
        this.cells[r][c].repaint();
      }
    }
  }

  /** Devuelve una copia del estado completo [filas][columnas]. */
  public boolean[][] getSteps() {
    final boolean[][] copy = new boolean[ROWS][COLS];
    for (int r = 0; r < ROWS; r++) {
      System.arraycopy(this.active[r], 0, copy[r], 0, COLS);
    }
    return copy;
  }

  // -------------------------------------------------------------------------
  // NoteInfo — descriptor inmutable de cada fila
  // -------------------------------------------------------------------------

  /** Datos de una nota (fila) del tracker. */
  public static final class NoteInfo {
    /** Semitono dentro de la octava (0-11). */
    public final int     semitone;
    /** Nombre de la nota, ej. "C#". */
    public final String  name;
    /** Octava, ej. 2, 3, 4, 5. */
    public final int     octave;
    /** true si es tecla negra del piano. */
    public final boolean isBlack;

    NoteInfo(final int semitone, final String name,
             final int octave,  final boolean isBlack) {
      this.semitone = semitone;
      this.name     = name;
      this.octave   = octave;
      this.isBlack  = isBlack;
    }

    /** Etiqueta de visualizacion, ej. "C#4". */
    public String label() { return this.name + this.octave; }
  }

  // -------------------------------------------------------------------------
  // NoteLabel — etiqueta de la nota con estilo activo / fantasma
  // -------------------------------------------------------------------------

  private final class NoteLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    private final int     row;
    private final boolean noteIsBlack;

    NoteLabel(final int row) {
      super(TrackerPanel.this.noteList.get(row).label(), SwingConstants.CENTER);
      this.row        = row;
      this.noteIsBlack = TrackerPanel.this.noteList.get(row).isBlack;
      setMaximumSize(new Dimension(LABEL_W, CELL_H));
      setMinimumSize(new Dimension(LABEL_W, CELL_H));
      setPreferredSize(new Dimension(LABEL_W, CELL_H));
      setOpaque(false);
      setFont(new Font("Monospaced", this.noteIsBlack ? Font.PLAIN : Font.BOLD, 10));
      final boolean octSep = TrackerPanel.this.isOctaveSeparator(row);
      setBorder(octSep
          ? BorderFactory.createMatteBorder(0, 0, 2, 0, C_OCT_SEP)
          : BorderFactory.createMatteBorder(0, 0, 1, 0, C_GRID_LINE));
    }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D g2     = (Graphics2D) g.create();
      final boolean    active = TrackerPanel.this.rowActive[this.row];
      if (active) {
        g2.setColor(this.noteIsBlack ? C_LABEL_BG_B : C_LABEL_BG_W);
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Franja de color lateral
        g2.setColor(this.noteIsBlack
            ? new Color(200, 90, 50, 80)
            : new Color(64, 140, 220, 60));
        g2.fillRect(0, 0, 4, getHeight());
        setForeground(this.noteIsBlack ? C_LABEL_BLACK : C_LABEL_WHITE);
      } else {
        // Fila fantasma: fondo muy oscuro, texto casi invisible
        g2.setColor(C_GHOST_LABEL);
        g2.fillRect(0, 0, getWidth(), getHeight());
        setForeground(C_GHOST_TEXT);
      }
      g2.dispose();
      super.paintComponent(g);
    }
  }

  // -------------------------------------------------------------------------
  // StepCell — celda individual con estilo activo / fantasma
  // -------------------------------------------------------------------------

  private final class StepCell extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int     row;
    private final int     col;
    private       boolean hovered = false;

    StepCell(final int row, final int col) {
      this.row = row;
      this.col = col;
      setPreferredSize(new Dimension(CELL_W, CELL_H));
      setOpaque(false);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
          // Ignorar clic en filas fantasma
          if (!TrackerPanel.this.rowActive[StepCell.this.row]) return;
          TrackerPanel.this.active[StepCell.this.row][StepCell.this.col]
              = !TrackerPanel.this.active[StepCell.this.row][StepCell.this.col];
          StepCell.this.repaint();
        }
        @Override
        public void mouseEntered(final MouseEvent e) {
          if (!TrackerPanel.this.rowActive[StepCell.this.row]) return;
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
      final Graphics2D g2     = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final NoteInfo ni     = TrackerPanel.this.noteList.get(this.row);
      final boolean  active = TrackerPanel.this.rowActive[this.row];
      final boolean  on     = TrackerPanel.this.active[this.row][this.col];
      final boolean  beat   = (this.col % 4 == 0);
      final boolean  octSep = TrackerPanel.this.isOctaveSeparator(this.row);
      final int w = getWidth(), h = getHeight();

      if (!active) {
        // ---- Celda fantasma ----
        g2.setColor(C_GHOST_CELL);
        g2.fillRect(0, 0, w, h);
        if (beat && this.col > 0) {
          g2.setColor(new Color(35, 35, 50));
          g2.fillRect(0, 0, 2, h);
        }
        if (octSep) {
          g2.setColor(C_GHOST_BORDER);
          g2.fillRect(0, h - 2, w, 2);
        }
        g2.dispose();
        return;
      }

      // ---- Celda activa ----
      final Color bg;
      if (on)          bg = ni.isBlack ? C_CELL_ON_B    : C_CELL_ON_W;
      else if (hovered) bg = ni.isBlack ? C_CELL_HOVER_B : C_CELL_HOVER_W;
      else              bg = ni.isBlack ? C_CELL_OFF_B   : C_CELL_OFF_W;

      g2.setColor(bg);
      g2.fillRoundRect(2, 2, w - 4, h - 4, 5, 5);

      if (on) {
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(3, 3, w - 6, (h - 6) / 2, 4, 4);
      }

      g2.setColor(on
          ? (ni.isBlack ? new Color(220, 110, 70) : new Color(90, 170, 255))
          : C_GRID_LINE);
      g2.drawRoundRect(2, 2, w - 4, h - 4, 5, 5);

      if (beat && this.col > 0) {
        g2.setColor(C_BEAT_LINE);
        g2.fillRect(0, 0, 2, h);
      }
      if (octSep) {
        g2.setColor(C_OCT_SEP);
        g2.fillRect(0, h - 2, w, 2);
      }

      g2.dispose();
    }
  }
}
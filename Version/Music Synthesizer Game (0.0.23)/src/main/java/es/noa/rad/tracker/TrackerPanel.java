package es.noa.rad.tracker;

import java.awt.BasicStroke;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel principal del Tracker Visual con soporte de tema claro/oscuro.
 *
 * <p>Siempre muestra las 37 notas del rango C2-C5. Las notas fuera de la
 * escala seleccionada aparecen como filas fantasma (atenuadas, no interactivas).
 * Los colores se actualizan llamando a {@link #applyTheme(boolean)} seguido
 * de {@link #refreshUI()}.</p>
 */
public final class TrackerPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // -------------------------------------------------------------------------
  // Constantes de layout (inmutables)
  // -------------------------------------------------------------------------

  public static final int COLS = 16;
  public static final int ROWS = 37;

  private static final int CELL_W  = 40;
  private static final int CELL_H  = 20;
  private static final int LABEL_W = 70;

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
  // Colores del tema (mutables — actualizados por applyTheme)
  // -------------------------------------------------------------------------

  private static Color C_BG, C_HEADER_BG;
  private static Color C_LABEL_WHITE, C_LABEL_BLACK, C_LABEL_BG_W, C_LABEL_BG_B;
  private static Color C_CELL_OFF_W, C_CELL_OFF_B;
  private static Color C_CELL_ON_W, C_CELL_ON_B;
  private static Color C_CELL_HOVER_W, C_CELL_HOVER_B;
  private static Color C_GRID_LINE, C_BEAT_LINE, C_OCT_SEP;
  private static Color C_HEADER_TEXT, C_STEP_NUM, C_STEP_BEAT, C_STEP_BEAT_BG;
  private static Color C_GHOST_LABEL, C_GHOST_TEXT, C_GHOST_CELL, C_GHOST_BORDER, C_GHOST_BEAT;
  private static Color C_CELL_SHINE;

  static { applyTheme(true); }

  /**
   * Actualiza los colores estaticos segun el tema indicado.
   * Debe llamarse antes de {@link #refreshUI()} para que los cambios
   * sean visibles.
   *
   * @param dark true para tema oscuro, false para tema claro
   */
  public static void applyTheme(final boolean dark) {
    if (dark) {
      C_BG           = new Color(18,  18,  26);
      C_HEADER_BG    = new Color(25,  25,  36);
      C_LABEL_WHITE  = new Color(220, 220, 235);
      C_LABEL_BLACK  = new Color(160, 160, 190);
      C_LABEL_BG_W   = new Color(36,  36,  52);
      C_LABEL_BG_B   = new Color(24,  24,  38);
      C_CELL_OFF_W   = new Color(38,  40,  58);
      C_CELL_OFF_B   = new Color(26,  26,  40);
      C_CELL_ON_W    = new Color(64,  140, 220);
      C_CELL_ON_B    = new Color(200,  90,  50);
      C_CELL_HOVER_W = new Color(55,   60,  82);
      C_CELL_HOVER_B = new Color(40,   38,  58);
      C_GRID_LINE    = new Color(50,   50,  72);
      C_BEAT_LINE    = new Color(80,   80, 110);
      C_OCT_SEP      = new Color(100, 120, 170);
      C_HEADER_TEXT  = new Color(130, 130, 160);
      C_STEP_NUM     = new Color(80,   80, 110);
      C_STEP_BEAT    = new Color(140, 160, 210);
      C_STEP_BEAT_BG = new Color(30,   32,  48);
      C_GHOST_LABEL  = new Color(22,   22,  32);
      C_GHOST_TEXT   = new Color(48,   48,  65);
      C_GHOST_CELL   = new Color(18,   18,  26);
      C_GHOST_BORDER = new Color(35,   35,  50);
      C_GHOST_BEAT   = new Color(35,   35,  50);
      C_CELL_SHINE   = new Color(255, 255, 255,  40);
    } else {
      C_BG           = new Color(245, 245, 250);
      C_HEADER_BG    = new Color(222, 224, 240);
      C_LABEL_WHITE  = new Color(20,   25,  55);
      C_LABEL_BLACK  = new Color(70,   72, 108);
      C_LABEL_BG_W   = new Color(208, 212, 238);
      C_LABEL_BG_B   = new Color(192, 196, 228);
      C_CELL_OFF_W   = new Color(195, 200, 228);
      C_CELL_OFF_B   = new Color(178, 182, 215);
      C_CELL_ON_W    = new Color(64,  140, 220);
      C_CELL_ON_B    = new Color(200,  90,  50);
      C_CELL_HOVER_W = new Color(168, 175, 212);
      C_CELL_HOVER_B = new Color(155, 160, 202);
      C_GRID_LINE    = new Color(165, 170, 202);
      C_BEAT_LINE    = new Color(112, 122, 172);
      C_OCT_SEP      = new Color(62,   88, 158);
      C_HEADER_TEXT  = new Color(72,   78, 125);
      C_STEP_NUM     = new Color(118, 124, 168);
      C_STEP_BEAT    = new Color(42,   62, 152);
      C_STEP_BEAT_BG = new Color(208, 212, 238);
      C_GHOST_LABEL  = new Color(232, 234, 246);
      C_GHOST_TEXT   = new Color(162, 166, 192);
      C_GHOST_CELL   = new Color(225, 228, 242);
      C_GHOST_BORDER = new Color(178, 183, 212);
      C_GHOST_BEAT   = new Color(178, 183, 212);
      C_CELL_SHINE   = new Color(0,     0,  50,  15);
    }
  }

  // -------------------------------------------------------------------------
  // Estado
  // -------------------------------------------------------------------------

  private final List<NoteInfo> noteList;
  private final boolean[] rowActive  = new boolean[ROWS];
  private final boolean[][] active   = new boolean[ROWS][COLS];
  private final StepCell[][]  cells  = new StepCell[ROWS][COLS];
  private final NoteLabel[]   noteLabels = new NoteLabel[ROWS];
  private TrackerScale currentScale;

  /** Columna que marca el cursor de reproduccion (-1 = sin reproduccion). */
  private volatile int playColumn = -1;

  /**
   * Callback opcional que se invoca antes de activar/desactivar un paso.
   * Usar para detener el sequencer desde fuera cuando el usuario edita la grid.
   */
  private Runnable onStepClick;

  /** Registra el callback que se ejecutara al hacer clic en cualquier celda. */
  public void setOnStepClick(final Runnable callback) {
    this.onStepClick = callback;
  }

  // Referencias a componentes para actualizar el tema sin reconstruir
  private JPanel  headerPanel;
  private JLabel  headerCorner;
  private final JLabel[] headerStepLabels = new JLabel[COLS];
  private JPanel  labelsPanel;
  private JPanel  gridPanel;
  private JPanel  gridArea;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  public TrackerPanel() {
    this.noteList = buildAllNotes();
    setBackground(C_BG);
    setLayout(new BorderLayout(0, 0));
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    setMinimumSize(new Dimension(LABEL_W + COLS * (CELL_W / 2), ROWS * 12 + 24));
    add(buildHeaderRow(), BorderLayout.NORTH);

    this.gridArea = new JPanel(new BorderLayout(0, 0));
    this.gridArea.setBackground(C_BG);
    buildStaticGrid(this.gridArea);
    add(this.gridArea, BorderLayout.CENTER);

    applyScaleInternal(TrackerScale.EASY_PLUS);
  }

  // -------------------------------------------------------------------------
  // Actualizacion del tema
  // -------------------------------------------------------------------------

  /**
   * Actualiza los colores de todos los componentes estaticos (cabecera,
   * fondos de panel) y repinta el panel completo.
   * Llamar despues de {@link #applyTheme(boolean)}.
   */
  public void refreshUI() {
    setBackground(C_BG);
    if (this.headerPanel != null) {
      this.headerPanel.setBackground(C_HEADER_BG);
      this.headerCorner.setBackground(C_HEADER_BG);
      this.headerCorner.setForeground(C_HEADER_TEXT);
      for (int col = 0; col < COLS; col++) {
        final boolean isBeat = (col % 4 == 0);
        this.headerStepLabels[col].setForeground(isBeat ? C_STEP_BEAT : C_STEP_NUM);
        this.headerStepLabels[col].setBackground(isBeat ? C_STEP_BEAT_BG : C_HEADER_BG);
      }
    }
    if (this.labelsPanel != null) this.labelsPanel.setBackground(C_BG);
    if (this.gridPanel   != null) this.gridPanel.setBackground(C_BG);
    if (this.gridArea    != null) this.gridArea.setBackground(C_BG);
    repaint();
  }

  // -------------------------------------------------------------------------
  // API de escala
  // -------------------------------------------------------------------------

  public void setScale(final TrackerScale scale) {
    final TrackerScale old = this.currentScale;
    applyScaleInternal(scale);
    this.firePropertyChange("scale", old, scale);
  }

  public TrackerScale getCurrentScale() { return this.currentScale; }

  public int getRowCount() { return ROWS; }

  /** @return true si la fila esta activa en la escala actual */
  public boolean isRowActive(final int row) {
    return row >= 0 && row < ROWS && this.rowActive[row];
  }

  /** @return informacion de nota (semitono, octava, nombre) de la fila indicada */
  public NoteInfo getNoteInfo(final int row) {
    return this.noteList.get(row);
  }

  /**
   * Establece la columna del cursor de reproduccion y repinta el grid.
   * Pasar -1 para ocultar el cursor.
   */
  public void setPlayColumn(final int col) {
    this.playColumn = col;
    if (this.gridPanel != null) this.gridPanel.repaint();
  }

  private void applyScaleInternal(final TrackerScale scale) {
    this.currentScale = scale;
    for (int row = 0; row < ROWS; row++) {
      this.rowActive[row] = scale.isRowActive(this.noteList.get(row).semitone);
    }
    for (int row = 0; row < ROWS; row++) {
      this.noteLabels[row].repaint();
      for (int col = 0; col < COLS; col++) {
        this.cells[row][col].repaint();
      }
    }
  }

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
    this.labelsPanel = new JPanel(new GridLayout(ROWS, 1, 0, 0));
    this.labelsPanel.setBackground(C_BG);
    for (int row = 0; row < ROWS; row++) {
      final NoteLabel lbl = new NoteLabel(row);
      this.noteLabels[row] = lbl;
      this.labelsPanel.add(lbl);
    }

    this.gridPanel = new JPanel(new GridLayout(ROWS, COLS, 0, 0)) {
      private static final long serialVersionUID = 1L;
      @Override
      protected void paintChildren(final Graphics g) {
        super.paintChildren(g);
        final int pc = TrackerPanel.this.playColumn;
        if (pc < 0 || pc >= COLS) return;
        final int cw = getWidth() / COLS;
        final int x  = pc * cw;
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Fondo semitransparente sobre la columna
        g2.setColor(new Color(255, 60, 60, 35));
        g2.fillRect(x, 0, cw, getHeight());
        // Linea roja izquierda de la columna
        g2.setColor(new Color(255, 60, 60, 220));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x + 1, 0, x + 1, getHeight());
        g2.dispose();
      }
    };
    this.gridPanel.setBackground(C_BG);
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        final StepCell cell = new StepCell(row, col);
        this.cells[row][col] = cell;
        this.gridPanel.add(cell);
      }
    }

    area.add(this.labelsPanel, BorderLayout.WEST);
    area.add(this.gridPanel,   BorderLayout.CENTER);
  }

  // -------------------------------------------------------------------------
  // Cabecera (numeros de paso) — almacena refs para poder actualizar el tema
  // -------------------------------------------------------------------------

  private JPanel buildHeaderRow() {
    final JPanel header = new JPanel(new BorderLayout(0, 0));
    header.setBackground(C_HEADER_BG);
    header.setPreferredSize(new Dimension(LABEL_W + COLS * CELL_W, 24));
    this.headerPanel = header;

    final JLabel corner = new JLabel("NOTA", SwingConstants.CENTER);
    corner.setPreferredSize(new Dimension(LABEL_W, 24));
    corner.setForeground(C_HEADER_TEXT);
    corner.setFont(new Font("SansSerif", Font.BOLD, 10));
    corner.setOpaque(true);
    corner.setBackground(C_HEADER_BG);
    this.headerCorner = corner;
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
      lbl.setBackground(isBeat ? C_STEP_BEAT_BG : C_HEADER_BG);
      if (col % 4 == 0 && col > 0) {
        lbl.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, C_BEAT_LINE));
      }
      this.headerStepLabels[col] = lbl;
      nums.add(lbl);
    }
    header.add(nums, BorderLayout.CENTER);
    return header;
  }

  // -------------------------------------------------------------------------
  // Helper
  // -------------------------------------------------------------------------

  private boolean isOctaveSeparator(final int row) {
    if (row >= ROWS - 1) return false;
    return this.noteList.get(row).octave != this.noteList.get(row + 1).octave;
  }

  // -------------------------------------------------------------------------
  // API publica de pasos
  // -------------------------------------------------------------------------

  public void setStep(final int row, final int col, final boolean on) {
    if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
      this.active[row][col] = on;
      this.cells[row][col].repaint();
    }
  }

  public boolean isStepActive(final int row, final int col) {
    return this.active[row][col];
  }

  public void clearAll() {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        this.active[r][c] = false;
        this.cells[r][c].repaint();
      }
    }
  }

  public boolean[][] getSteps() {
    final boolean[][] copy = new boolean[ROWS][COLS];
    for (int r = 0; r < ROWS; r++) {
      System.arraycopy(this.active[r], 0, copy[r], 0, COLS);
    }
    return copy;
  }

  // -------------------------------------------------------------------------
  // NoteInfo
  // -------------------------------------------------------------------------

  public static final class NoteInfo {
    public final int     semitone;
    public final String  name;
    public final int     octave;
    public final boolean isBlack;

    NoteInfo(final int semitone, final String name,
             final int octave,  final boolean isBlack) {
      this.semitone = semitone;
      this.name     = name;
      this.octave   = octave;
      this.isBlack  = isBlack;
    }

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
      this.row         = row;
      this.noteIsBlack = TrackerPanel.this.noteList.get(row).isBlack;
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
        g2.setColor(this.noteIsBlack
            ? new Color(200, 90, 50, 80)
            : new Color(64, 140, 220, 60));
        g2.fillRect(0, 0, 4, getHeight());
        setForeground(this.noteIsBlack ? C_LABEL_BLACK : C_LABEL_WHITE);
      } else {
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
          if (!TrackerPanel.this.rowActive[StepCell.this.row]) return;
          // Notificar al exterior (p.ej. detener el sequencer) antes de editar
          final Runnable cb = TrackerPanel.this.onStepClick;
          if (cb != null) cb.run();
          final boolean nowOn = !TrackerPanel.this.active[StepCell.this.row][StepCell.this.col];
          TrackerPanel.this.active[StepCell.this.row][StepCell.this.col] = nowOn;
          StepCell.this.repaint();
          if (nowOn) {
            final NoteInfo ni = TrackerPanel.this.noteList.get(StepCell.this.row);
            NotePlayer.playNote(ni.semitone, ni.octave);
          }
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
        g2.setColor(C_GHOST_CELL);
        g2.fillRect(0, 0, w, h);
        if (beat && this.col > 0) {
          g2.setColor(C_GHOST_BEAT);
          g2.fillRect(0, 0, 2, h);
        }
        if (octSep) {
          g2.setColor(C_GHOST_BORDER);
          g2.fillRect(0, h - 2, w, 2);
        }
        g2.dispose();
        return;
      }

      final Color bg;
      if (on)           bg = ni.isBlack ? C_CELL_ON_B    : C_CELL_ON_W;
      else if (hovered) bg = ni.isBlack ? C_CELL_HOVER_B : C_CELL_HOVER_W;
      else              bg = ni.isBlack ? C_CELL_OFF_B   : C_CELL_OFF_W;

      g2.setColor(bg);
      g2.fillRoundRect(2, 2, w - 4, h - 4, 5, 5);

      if (on) {
        g2.setColor(C_CELL_SHINE);
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
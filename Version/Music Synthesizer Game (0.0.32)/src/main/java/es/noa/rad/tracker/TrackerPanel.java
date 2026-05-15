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
import javax.swing.BoxLayout;
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
  private static final int CELL_H  = 19;
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

  /** Numero maximo de compases soportados por instancia. */
  public  static final int MAX_BARS = 32;

  /** Pasos activos por compas: [bar][row][col]. */
  private final boolean[][][] barSteps = new boolean[MAX_BARS][ROWS][COLS];

  /** Numero de compases activos (1 al arrancar). */
  private int numBars    = 1;
  /** Compas que se esta editando actualmente. */
  private int currentBar = 0;
  /** Compas que esta reproduciendose (-1 = parado). */
  private int playBar    = -1;

  private final StepCell[][]  cells  = new StepCell[ROWS][COLS];
  private final NoteLabel[]   noteLabels = new NoteLabel[ROWS];
  private TrackerScale currentScale;

  /** Indice del canal MIDI asignado a este panel (0 a NotePlayer.NUM_CHANNELS-1). */
  private int channelIndex = 0;

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

  /** true = solo se muestran las filas activas en la escala actual. */
  private boolean compressedView = false;
  /** Paneles de fila para la columna de etiquetas (uno por fila). */
  private final JPanel[] rowLabelPanels = new JPanel[ROWS];
  /** Paneles de fila para el grid de pasos (uno por fila). */
  private final JPanel[] rowGridPanels  = new JPanel[ROWS];

  /** Panel selector de compases (debajo del grid). */
  private JPanel barSelectorPanel;
  /** Fila interior donde se insertan los botones de compas. */
  private JPanel barButtonRow;
  /** ScrollPane que envuelve barButtonRow para desplazarse al compas activo. */
  private javax.swing.JScrollPane barScrollPane;
  /** Grupo de seleccion exclusiva para los botones de compas. */
  private final javax.swing.ButtonGroup barGroup = new javax.swing.ButtonGroup();
  /** Lista de botones de compas (uno por compas activo). */
  private final List<javax.swing.JToggleButton> barButtons = new ArrayList<>();

  /** Callback: se invoca cuando se anade un nuevo compas via {@link #addBar()}. */
  private Runnable onBarCountChanged;
  /** Callback: se invoca cuando el usuario solicita eliminar un compas por clic derecho. */
  private java.util.function.IntConsumer onDeleteBarRequested;

  /** Registra callback que se invoca al anadir un compas. */
  public void setOnBarCountChanged(final Runnable r) { this.onBarCountChanged = r; }
  /** Registra callback que se invoca al solicitar eliminar un compas (argumento = indice de compas). */
  public void setOnDeleteBarRequested(final java.util.function.IntConsumer h) { this.onDeleteBarRequested = h; }

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

    this.barSelectorPanel = buildBarSelectorPanel();
    add(this.barSelectorPanel, BorderLayout.SOUTH);

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

  /** @return indice del canal MIDI asignado a este panel (0-3) */
  public int getChannelIndex() { return this.channelIndex; }

  /** Asigna el canal MIDI que se usara al previsualizar notas al hacer clic. */
  public void setChannelIndex(final int idx) { this.channelIndex = idx; }

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
   * Establece la posicion de reproduccion (compas + columna) y repinta el grid.
   * Pasar bar=-1 / col=-1 para ocultar el cursor.
   */
  public void setPlayPosition(final int bar, final int col) {
    this.playBar    = bar;
    this.playColumn = col;
    // Auto-seguimiento: cambiar compas visible al que se reproduce
    if (bar >= 0 && bar < this.numBars && bar != this.currentBar) {
      this.currentBar = bar;
      if (bar < this.barButtons.size()) {
        this.barButtons.get(bar).setSelected(true);
      }
    }
    if (bar >= 0) scrollToBar(bar);
    if (this.gridPanel != null) this.gridPanel.repaint();
    updateBarPlayIndicator(bar);
  }

  /**
   * Compatibilidad hacia atras: establece la columna en el compas 0.
   * Llamar con col=-1 para ocultar el cursor.
   */
  public void setPlayColumn(final int col) {
    setPlayPosition(col >= 0 ? 0 : -1, col);
  }

  private void applyScaleInternal(final TrackerScale scale) {
    this.currentScale = scale;
    for (int row = 0; row < ROWS; row++) {
      this.rowActive[row] = scale.isRowActive(this.noteList.get(row).semitone);
    }
    if (this.compressedView) {
      applyCompressedView();
      revalidate();
    }
    for (int row = 0; row < ROWS; row++) {
      this.noteLabels[row].repaint();
      for (int col = 0; col < COLS; col++) {
        this.cells[row][col].repaint();
      }
    }
  }

  /**
   * Activa o desactiva la vista comprimida.
   * En modo comprimido, las filas fantasma (fuera de escala) se ocultan
   * y solo se muestran las notas activas con igual altura visual.
   */
  public void setCompressedView(final boolean compressed) {
    this.compressedView = compressed;
    applyCompressedView();
    revalidate();
    repaint();
  }

  /** @return true si la vista comprimida esta activada */
  public boolean isCompressedView() { return this.compressedView; }

  private void applyCompressedView() {
    for (int row = 0; row < ROWS; row++) {
      final boolean show = !this.compressedView || this.rowActive[row];
      if (this.rowLabelPanels[row] != null) this.rowLabelPanels[row].setVisible(show);
      if (this.rowGridPanels[row]  != null) this.rowGridPanels[row].setVisible(show);
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
    this.labelsPanel = new JPanel();
    this.labelsPanel.setLayout(new BoxLayout(this.labelsPanel, BoxLayout.Y_AXIS));
    this.labelsPanel.setBackground(C_BG);
    for (int row = 0; row < ROWS; row++) {
      final NoteLabel lbl = new NoteLabel(row);
      this.noteLabels[row] = lbl;
      final JPanel rowLbl = new JPanel(new BorderLayout(0, 0));
      rowLbl.setBackground(C_BG);
      rowLbl.setOpaque(false);
      rowLbl.setMaximumSize(new Dimension(LABEL_W, CELL_H));
      rowLbl.setPreferredSize(new Dimension(LABEL_W, CELL_H));
      rowLbl.setMinimumSize(new Dimension(LABEL_W, CELL_H));
      rowLbl.add(lbl);
      this.rowLabelPanels[row] = rowLbl;
      this.labelsPanel.add(rowLbl);
    }

    this.gridPanel = new JPanel() {
      private static final long serialVersionUID = 1L;
      @Override
      protected void paintChildren(final Graphics g) {
        super.paintChildren(g);
        final int pc = TrackerPanel.this.playColumn;
        final int pb = TrackerPanel.this.playBar;
        // Mostrar cursor solo si se esta reproduciendo este compas
        if (pc < 0 || pc >= COLS || pb != TrackerPanel.this.currentBar) return;
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
    this.gridPanel.setLayout(new BoxLayout(this.gridPanel, BoxLayout.Y_AXIS));
    this.gridPanel.setBackground(C_BG);

    for (int row = 0; row < ROWS; row++) {
      final JPanel rowGrid = new JPanel(new GridLayout(1, COLS, 0, 0));
      rowGrid.setBackground(C_BG);
      rowGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, CELL_H));
      rowGrid.setPreferredSize(new Dimension(COLS * CELL_W, CELL_H));
      rowGrid.setMinimumSize(new Dimension(COLS * CELL_W, CELL_H));
      for (int col = 0; col < COLS; col++) {
        final StepCell cell = new StepCell(row, col);
        this.cells[row][col] = cell;
        rowGrid.add(cell);
      }
      this.rowGridPanels[row] = rowGrid;
      this.gridPanel.add(rowGrid);
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
      this.barSteps[this.currentBar][row][col] = on;
      this.cells[row][col].repaint();
    }
  }

  public boolean isStepActive(final int row, final int col) {
    return this.barSteps[this.currentBar][row][col];
  }

  /**
   * Devuelve si el paso (bar, row, col) esta activo — usado por el sequencer.
   */
  public boolean isStepActiveAt(final int bar, final int row, final int col) {
    if (bar < 0 || bar >= this.numBars || row < 0 || row >= ROWS || col < 0 || col >= COLS)
      return false;
    return this.barSteps[bar][row][col];
  }

  public void clearAll() {
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        this.barSteps[this.currentBar][r][c] = false;
        this.cells[r][c].repaint();
      }
    }
  }

  public boolean[][] getSteps() {
    final boolean[][] copy = new boolean[ROWS][COLS];
    for (int r = 0; r < ROWS; r++) {
      System.arraycopy(this.barSteps[this.currentBar][r], 0, copy[r], 0, COLS);
    }
    return copy;
  }

  /**
   * Devuelve todos los compases activos: [bar][row][col].
   * Se usa en WavExporter y MelodyFile.
   */
  public boolean[][][] getAllBarSteps() {
    final boolean[][][] copy = new boolean[this.numBars][ROWS][COLS];
    for (int b = 0; b < this.numBars; b++) {
      for (int r = 0; r < ROWS; r++) {
        System.arraycopy(this.barSteps[b][r], 0, copy[b][r], 0, COLS);
      }
    }
    return copy;
  }

  /** Carga todos los compases desde un array externo (usado en MelodyFile.load). */
  public void setAllBarSteps(final boolean[][][] data) {
    final int bars = Math.min(data.length, MAX_BARS);
    // Asegurarse de que hay suficientes botones
    while (this.numBars < bars) addBar();
    for (int b = 0; b < bars; b++) {
      for (int r = 0; r < ROWS && r < data[b].length; r++) {
        for (int c = 0; c < COLS && c < data[b][r].length; c++) {
          this.barSteps[b][r][c] = data[b][r][c];
        }
      }
    }
    setCurrentBar(0);
  }

  public int getNumBars() { return this.numBars; }
  public int getCurrentBar() { return this.currentBar; }

  /**
   * Cambia el compas que se esta editando y repinta el grid.
   */
  public void setCurrentBar(final int bar) {
    if (bar < 0 || bar >= this.numBars) return;
    this.currentBar = bar;
    // Actualizar seleccion del boton
    if (bar < this.barButtons.size()) {
      this.barButtons.get(bar).setSelected(true);
    }
    if (this.gridPanel != null) this.gridPanel.repaint();
  }

  /**
   * Anade un nuevo compas vacio al final y devuelve su indice.
   * Si ya se alcanzo el maximo, devuelve el ultimo indice existente.
   */
  public int addBar() {
    if (this.numBars >= MAX_BARS) return this.numBars - 1;
    final int newBar = this.numBars++;
    if (this.barButtonRow != null) {
      final javax.swing.JToggleButton btn = createBarButton(newBar);
      this.barButtonRow.add(btn, this.barButtonRow.getComponentCount() - 1); // antes del "+"
      this.barButtonRow.revalidate();
      this.barButtonRow.repaint();
    }
    if (this.onBarCountChanged != null) this.onBarCountChanged.run();
    return newBar;
  }

  // -------------------------------------------------------------------------
  // Gestion avanzada de compases
  // -------------------------------------------------------------------------

  /** @return true si hay algun paso activo en el compas dado. */
  public boolean hasDataInBar(final int bar) {
    if (bar < 0 || bar >= this.numBars) return false;
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        if (this.barSteps[bar][r][c]) return true;
      }
    }
    return false;
  }

  /**
   * Ajusta silenciosamente (sin disparar onBarCountChanged) el numero de
   * compases hasta {@code n}, anadiendo o eliminando los del final.
   */
  public void syncBarCount(final int n) {
    final int target = Math.max(1, Math.min(n, MAX_BARS));
    // Anadir compases vacios
    while (this.numBars < target) {
      if (this.numBars >= MAX_BARS) break;
      final int newBar = this.numBars++;
      if (this.barButtonRow != null) {
        final javax.swing.JToggleButton btn = createBarButton(newBar);
        this.barButtonRow.add(btn, this.barButtonRow.getComponentCount() - 1);
        this.barButtonRow.revalidate();
        this.barButtonRow.repaint();
      }
    }
    // Eliminar compases sobrantes por el final
    while (this.numBars > target) {
      final int last = --this.numBars;
      for (int r = 0; r < ROWS; r++) java.util.Arrays.fill(this.barSteps[last][r], false);
      if (!this.barButtons.isEmpty()) {
        final javax.swing.JToggleButton btn = this.barButtons.remove(this.barButtons.size() - 1);
        this.barGroup.remove(btn);
        if (this.barButtonRow != null) {
          this.barButtonRow.remove(btn);
          this.barButtonRow.revalidate();
          this.barButtonRow.repaint();
        }
      }
    }
    if (this.currentBar >= this.numBars) setCurrentBar(this.numBars - 1);
  }

  /**
   * Elimina el compas en {@code barIdx} (barIdx > 0), desplazando los
   * siguientes y actualizando la UI de botones.
   */
  public void removeBarAt(final int barIdx) {
    if (barIdx <= 0 || barIdx >= this.numBars) return;
    // Desplazar datos
    for (int b = barIdx; b < this.numBars - 1; b++) {
      for (int r = 0; r < ROWS; r++) {
        System.arraycopy(this.barSteps[b + 1][r], 0, this.barSteps[b][r], 0, COLS);
      }
    }
    // Limpiar ultimo compas liberado
    for (int r = 0; r < ROWS; r++) java.util.Arrays.fill(this.barSteps[this.numBars - 1][r], false);
    this.numBars--;
    if (this.currentBar >= this.numBars) this.currentBar = this.numBars - 1;
    rebuildBarButtons();
    if (this.gridPanel != null) this.gridPanel.repaint();
  }

  /**
   * Limpia TODOS los datos de todos los compases de este canal y
   * ajusta el numero de compases a {@code targetNumBars}.
   */
  public void clearChannel(final int targetNumBars) {
    for (int b = 0; b < MAX_BARS; b++) {
      for (int r = 0; r < ROWS; r++) java.util.Arrays.fill(this.barSteps[b][r], false);
    }
    // Resetear a 1 boton
    this.numBars = 1;
    this.currentBar = 0;
    rebuildBarButtons();
    // Expandir hasta el objetivo sin disparar callback
    syncBarCount(Math.max(1, targetNumBars));
    if (this.gridPanel != null) this.gridPanel.repaint();
  }

  /** Reconstruye todos los botones de compas (usado tras eliminar un compas). */
  private void rebuildBarButtons() {
    if (this.barButtonRow == null) return;
    // Quitar todos los botones existentes del panel y del grupo
    for (final javax.swing.JToggleButton b : this.barButtons) {
      this.barGroup.remove(b);
      this.barButtonRow.remove(b);
    }
    this.barButtons.clear();
    // Recrear un boton por compas
    for (int b = 0; b < this.numBars; b++) {
      final javax.swing.JToggleButton btn = createBarButton(b);
      this.barButtonRow.add(btn, b);
    }
    // Seleccionar el compas actual
    if (this.currentBar < this.barButtons.size()) {
      this.barButtons.get(this.currentBar).setSelected(true);
    }
    this.barButtonRow.revalidate();
    this.barButtonRow.repaint();
  }

  /** Desplaza el scroll del selector para que el boton del compas dado sea visible. */
  private void scrollToBar(final int bar) {
    if (bar < 0 || bar >= this.barButtons.size() || this.barScrollPane == null) return;
    final javax.swing.JToggleButton btn = this.barButtons.get(bar);
    final java.awt.Rectangle bounds = javax.swing.SwingUtilities.convertRectangle(
        btn.getParent(), btn.getBounds(), this.barScrollPane.getViewport().getView());
    this.barScrollPane.getViewport().scrollRectToVisible(bounds);
  }

  /** Muestra el menu contextual de clic derecho sobre un boton de compas. */
  private void maybeShowBarPopup(final MouseEvent e, final int barIdx) {
    if (!e.isPopupTrigger() && e.getButton() != MouseEvent.BUTTON3) return;
    if (barIdx == 0) return; // no se puede eliminar el compas 1
    final javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
    final javax.swing.JMenuItem del = new javax.swing.JMenuItem(
        "Eliminar compás " + (barIdx + 1));
    del.addActionListener(ev -> {
      if (this.onDeleteBarRequested != null) this.onDeleteBarRequested.accept(barIdx);
    });
    popup.add(del);
    popup.show(e.getComponent(), e.getX(), e.getY());
  }

  // -------------------------------------------------------------------------
  // Selector de compases (debajo del grid)
  // -------------------------------------------------------------------------

  private JPanel buildBarSelectorPanel() {
    final JPanel outer = new JPanel(new BorderLayout(0, 0));
    outer.setBackground(C_BG);
    outer.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, C_OCT_SEP));

    final JLabel lbl = new JLabel("COMPAS:", SwingConstants.LEFT);
    lbl.setForeground(C_HEADER_TEXT);
    lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
    lbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 8));
    outer.add(lbl, BorderLayout.WEST);

    this.barButtonRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 3));
    this.barButtonRow.setBackground(C_BG);

    // Boton del compas 0 (siempre existe)
    final javax.swing.JToggleButton first = createBarButton(0);
    first.setSelected(true);
    this.barButtonRow.add(first);

    // Boton "+"
    final JLabel addBtn = new JLabel("  +  ");
    addBtn.setForeground(C_STEP_BEAT);
    addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    addBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    addBtn.setOpaque(true);
    addBtn.setBackground(C_HEADER_BG);
    addBtn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(C_BEAT_LINE, 1),
        BorderFactory.createEmptyBorder(2, 6, 2, 6)));
    addBtn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        final int newBar = addBar();
        setCurrentBar(newBar);
      }
      @Override
      public void mouseEntered(final MouseEvent e) {
        addBtn.setBackground(C_LABEL_BG_W);
      }
      @Override
      public void mouseExited(final MouseEvent e) {
        addBtn.setBackground(C_HEADER_BG);
      }
    });
    this.barButtonRow.add(addBtn);

    this.barScrollPane = new javax.swing.JScrollPane(this.barButtonRow,
        javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER,
        javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.barScrollPane.setBorder(null);
    this.barScrollPane.setBackground(C_BG);
    this.barScrollPane.getViewport().setBackground(C_BG);
    this.barScrollPane.setPreferredSize(new Dimension(0, 36));
    outer.add(this.barScrollPane, BorderLayout.CENTER);
    return outer;
  }

  private javax.swing.JToggleButton createBarButton(final int barIdx) {
    final javax.swing.JToggleButton btn =
        new javax.swing.JToggleButton(String.valueOf(barIdx + 1));
    btn.setPreferredSize(new Dimension(32, 22));
    btn.setFont(new Font("Monospaced", Font.BOLD, 10));
    btn.setForeground(C_HEADER_TEXT);
    btn.setBackground(C_HEADER_BG);
    btn.setFocusPainted(false);
    btn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(C_BEAT_LINE, 1),
        BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    this.barGroup.add(btn);
    this.barButtons.add(btn);
    btn.addActionListener(e -> setCurrentBar(barIdx));
    // Clic derecho: menu contextual para eliminar (solo compas > 0)
    if (barIdx > 0) {
      btn.addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(final MouseEvent e)  { maybeShowBarPopup(e, barIdx); }
        @Override public void mouseReleased(final MouseEvent e) { maybeShowBarPopup(e, barIdx); }
      });
    }
    return btn;
  }

  /** Resalta visualmente el boton del compas que se esta reproduciendo. */
  private void updateBarPlayIndicator(final int playingBar) {
    for (int i = 0; i < this.barButtons.size(); i++) {
      final javax.swing.JToggleButton b = this.barButtons.get(i);
      if (i == playingBar && playingBar >= 0) {
        b.setBackground(new Color(180, 50, 50));
        b.setForeground(Color.WHITE);
      } else if (b.isSelected()) {
        b.setBackground(C_STEP_BEAT_BG);
        b.setForeground(C_STEP_BEAT);
      } else {
        b.setBackground(C_HEADER_BG);
        b.setForeground(C_HEADER_TEXT);
      }
    }
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
          final boolean nowOn = !TrackerPanel.this.barSteps[TrackerPanel.this.currentBar][StepCell.this.row][StepCell.this.col];
          TrackerPanel.this.barSteps[TrackerPanel.this.currentBar][StepCell.this.row][StepCell.this.col] = nowOn;
          StepCell.this.repaint();
          if (nowOn) {
            final NoteInfo ni = TrackerPanel.this.noteList.get(StepCell.this.row);
            NotePlayer.playNote(ni.semitone, ni.octave, TrackerPanel.this.channelIndex);
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
      final boolean  on     = TrackerPanel.this.barSteps[TrackerPanel.this.currentBar][this.row][this.col];
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
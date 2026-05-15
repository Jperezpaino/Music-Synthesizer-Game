package es.noa.rad.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.Timer;

import es.noa.rad.synth.SoundEngine;

/**
 * Panel que muestra la forma de onda del osciloscopio en tiempo real.
 */
public class OscilloscopePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color BG_COLOR      = new Color(10, 10, 30);
  private static final Color GRID_COLOR    = new Color(30, 60, 30);
  private static final Color WAVE_COLOR    = new Color(0, 220, 80);
  private static final Color ZERO_COLOR    = new Color(60, 120, 60);

  private final SoundEngine soundEngine;
  private final Timer       refreshTimer;

  public OscilloscopePanel(final SoundEngine soundEngine) {
    this.soundEngine = soundEngine;
    setBackground(BG_COLOR);
    setPreferredSize(new Dimension(0, 120));
    setMinimumSize(new Dimension(100, 80));

    // Refresco cada 30 ms (~33 fps)
    this.refreshTimer = new Timer(30, e -> repaint());
    this.refreshTimer.start();
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final int w = getWidth();
    final int h = getHeight();
    final int midY = h / 2;

    final Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Fondo
    g2.setColor(BG_COLOR);
    g2.fillRect(0, 0, w, h);

    // Rejilla horizontal
    g2.setColor(GRID_COLOR);
    g2.setStroke(new BasicStroke(1f));
    for (int row = 0; row <= 4; row++) {
      final int y = h * row / 4;
      g2.drawLine(0, y, w, y);
    }
    // Rejilla vertical
    for (int col = 0; col <= 8; col++) {
      final int x = w * col / 8;
      g2.drawLine(x, 0, x, h);
    }

    // Línea central (cero)
    g2.setColor(ZERO_COLOR);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawLine(0, midY, w, midY);

    // Forma de onda
    final double[] buffer = this.soundEngine.getOscilloscopeBuffer();
    final int size = SoundEngine.getOscilloscopeSize();
    if (buffer == null || size == 0) {
      return;
    }

    g2.setColor(WAVE_COLOR);
    g2.setStroke(new BasicStroke(2f));

    int prevX = 0;
    int prevY = midY;

    for (int i = 0; i < size; i++) {
      final int x = i * w / size;
      final double sample = buffer[i]; // rango -1.0 a 1.0
      final int y = midY - (int) (sample * (h / 2 - 4));
      if (i > 0) {
        g2.drawLine(prevX, prevY, x, y);
      }
      prevX = x;
      prevY = y;
    }
  }

  /** Detiene el timer de refresco (llamar al cerrar la ventana). */
  public void stop() {
    this.refreshTimer.stop();
  }
}

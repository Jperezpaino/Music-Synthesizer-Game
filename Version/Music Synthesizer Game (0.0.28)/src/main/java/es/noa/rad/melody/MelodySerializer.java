package es.noa.rad.melody;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.noa.rad.synth.Note;
import es.noa.rad.synth.Waveform;

/**
 * Serializa y deserializa una {@link Melody} en formato JSON simple.
 *
 * <p>No usa librerías externas — el JSON se genera y parsea manualmente.
 * Formato del fichero:</p>
 *
 * <pre>
 * {
 *   "version": 1,
 *   "notes": [
 *     { "note": "C4", "waveform": "SINE", "startMs": 0, "durationMs": 500 },
 *     ...
 *   ]
 * }
 * </pre>
 *
 * <p>La extensión recomendada es {@code .synth}.</p>
 */
public final class MelodySerializer {

  /** Extensión de fichero para las melodías guardadas */
  public static final String FILE_EXTENSION = ".synth";

  /** Descripción del filtro de fichero para {@code JFileChooser} */
  public static final String FILE_DESCRIPTION = "Melodía del sintetizador (*" + FILE_EXTENSION + ")";

  private static final String VERSION = "1";

  private MelodySerializer() { }

  // -------------------------------------------------------------------------
  // Guardar
  // -------------------------------------------------------------------------

  /**
   * Guarda una melodía en un fichero JSON.
   *
   * @param melody Melodía a guardar
   * @param file   Fichero de destino
   * @throws IOException Si hay un error de escritura
   */
  public static void save(final Melody melody, final File file) throws IOException {
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write("{\n");
      writer.write("  \"version\": " + VERSION + ",\n");
      writer.write("  \"notes\": [\n");

      final List<MelodyNote> notes = melody.getNotes();
      for (int i = 0; i < notes.size(); i++) {
        final MelodyNote mn = notes.get(i);
        final String comma  = (i < notes.size() - 1) ? "," : "";
        writer.write("    { "
            + "\"note\": \""     + mn.getNote().name()     + "\", "
            + "\"waveform\": \"" + mn.getWaveform().name() + "\", "
            + "\"startMs\": "    + mn.getStartTimeMs()     + ", "
            + "\"durationMs\": " + mn.getDurationMs()
            + " }" + comma + "\n");
      }

      writer.write("  ]\n");
      writer.write("}\n");
    }
  }

  // -------------------------------------------------------------------------
  // Cargar
  // -------------------------------------------------------------------------

  /**
   * Carga una melodía desde un fichero JSON guardado con {@link #save}.
   *
   * @param file Fichero a cargar
   * @return Melodía reconstruida
   * @throws IOException Si hay un error de lectura o el formato es inválido
   */
  public static Melody load(final File file) throws IOException {
    final String json = readFile(file);
    final Melody melody = new Melody();

    // Extraer el array "notes": [ ... ]
    final int arrayStart = json.indexOf('[');
    final int arrayEnd   = json.lastIndexOf(']');
    if (arrayStart < 0 || arrayEnd < 0) {
      throw new IOException("Formato inválido: no se encontró el array de notas.");
    }

    final String arrayContent = json.substring(arrayStart + 1, arrayEnd).trim();
    if (arrayContent.isEmpty()) {
      return melody; // melodía vacía
    }

    // Separar objetos { ... }
    final List<String> objects = splitObjects(arrayContent);
    for (final String obj : objects) {
      final MelodyNote mn = parseNote(obj);
      melody.addNote(mn);
    }

    return melody;
  }

  // -------------------------------------------------------------------------
  // Helpers privados
  // -------------------------------------------------------------------------

  private static String readFile(final File file) throws IOException {
    final StringBuilder sb = new StringBuilder();
    try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * Divide el contenido del array en objetos JSON individuales { ... }.
   */
  private static List<String> splitObjects(final String content) {
    final List<String> result = new ArrayList<>();
    int depth = 0;
    int start = -1;

    for (int i = 0; i < content.length(); i++) {
      final char c = content.charAt(i);
      if (c == '{') {
        if (depth == 0) {
          start = i;
        }
        depth++;
      } else if (c == '}') {
        depth--;
        if (depth == 0 && start >= 0) {
          result.add(content.substring(start, i + 1));
          start = -1;
        }
      }
    }
    return result;
  }

  /**
   * Parsea un objeto JSON de nota: {@code { "note": "C4", "waveform": "SINE", "startMs": 0, "durationMs": 500 }}.
   */
  private static MelodyNote parseNote(final String obj) throws IOException {
    final String noteName  = extractString(obj, "note");
    final String wfName    = extractString(obj, "waveform");
    final long   startMs   = extractLong(obj, "startMs");
    final long   durationMs = extractLong(obj, "durationMs");

    final Note note;
    try {
      note = Note.valueOf(noteName);
    } catch (final IllegalArgumentException e) {
      throw new IOException("Nota desconocida: " + noteName);
    }

    final Waveform waveform;
    try {
      waveform = Waveform.valueOf(wfName);
    } catch (final IllegalArgumentException e) {
      throw new IOException("Forma de onda desconocida: " + wfName);
    }

    final MelodyNote mn = new MelodyNote(note, waveform, startMs);
    mn.setDurationMs(durationMs);
    return mn;
  }

  /**
   * Extrae el valor de un campo String: {@code "key": "value"}.
   */
  private static String extractString(final String json, final String key) throws IOException {
    final String search = "\"" + key + "\": \"";
    final int start = json.indexOf(search);
    if (start < 0) {
      throw new IOException("Campo no encontrado: " + key);
    }
    final int valueStart = start + search.length();
    final int valueEnd   = json.indexOf('"', valueStart);
    if (valueEnd < 0) {
      throw new IOException("Valor malformado para: " + key);
    }
    return json.substring(valueStart, valueEnd);
  }

  /**
   * Extrae el valor de un campo numérico: {@code "key": 1234}.
   */
  private static long extractLong(final String json, final String key) throws IOException {
    final String search = "\"" + key + "\": ";
    final int start = json.indexOf(search);
    if (start < 0) {
      throw new IOException("Campo no encontrado: " + key);
    }
    final int valueStart = start + search.length();
    int valueEnd = valueStart;
    while (valueEnd < json.length()
        && (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '-')) {
      valueEnd++;
    }
    try {
      return Long.parseLong(json.substring(valueStart, valueEnd));
    } catch (final NumberFormatException e) {
      throw new IOException("Número malformado para: " + key);
    }
  }

}

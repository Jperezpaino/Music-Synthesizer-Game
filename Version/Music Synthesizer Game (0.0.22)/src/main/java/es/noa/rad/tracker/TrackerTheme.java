package es.noa.rad.tracker;

/**
 * Estado global del tema visual (oscuro / claro).
 * Clase no instanciable; se accede unicamente mediante metodos estaticos.
 */
public final class TrackerTheme {

  private static boolean dark = true;

  private TrackerTheme() {}

  /** Devuelve true si el tema actual es oscuro. */
  public static boolean isDark() { return dark; }

  /** Cambia al tema contrario. */
  public static void toggle() { dark = !dark; }
}
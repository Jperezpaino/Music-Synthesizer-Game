package es.noa.rad.tracker;

/**
 * Lista de los 128 instrumentos del estandar General MIDI (GM Level 1).
 *
 * <p>El indice del array corresponde directamente al numero de programa MIDI
 * (0-127). Se usan nombres en espanol/intuitivos para facilitar la seleccion
 * en la interfaz.</p>
 */
public final class InstrumentList {

  private InstrumentList() {}

  /**
   * Nombres de los 128 instrumentos GM.
   * {@code NAMES[programNumber]} = nombre legible del instrumento.
   */
  public static final String[] NAMES = {
    // ---- Piano (0-7) ----
    "Piano de cola",           // 0
    "Piano brillante",         // 1
    "Piano electrico",         // 2
    "Piano honky-tonk",        // 3
    "Piano electrico 1",       // 4
    "Piano electrico 2",       // 5
    "Clavicembalo",            // 6
    "Clavicordio",             // 7

    // ---- Percusion cromatica (8-15) ----
    "Celesta",                 // 8
    "Glockenspiel",            // 9
    "Caja musical",            // 10
    "Vibrafono",               // 11
    "Marimba",                 // 12
    "Xilofono",                // 13
    "Campanas tubulares",      // 14
    "Dulcimer",                // 15

    // ---- Organos (16-23) ----
    "Organo Hammond",          // 16
    "Organo percusivo",        // 17
    "Organo de roca",          // 18
    "Organo de iglesia",       // 19
    "Organo de cana",          // 20
    "Acordeon",                // 21
    "Armonica",                // 22
    "Acordeon de tango",       // 23

    // ---- Guitarra (24-31) ----
    "Guitarra acustica (nylon)", // 24
    "Guitarra acustica (acero)", // 25
    "Guitarra electrica (jazz)", // 26
    "Guitarra electrica (limpia)", // 27
    "Guitarra electrica (muda)", // 28
    "Guitarra con overdrive",   // 29
    "Guitarra con distorsion",  // 30
    "Armonicos de guitarra",    // 31

    // ---- Bajo (32-39) ----
    "Bajo acustico",           // 32
    "Bajo electrico (dedos)",  // 33
    "Bajo electrico (pua)",    // 34
    "Bajo sin trastes",        // 35
    "Slap bass 1",             // 36
    "Slap bass 2",             // 37
    "Synth bass 1",            // 38
    "Synth bass 2",            // 39

    // ---- Cuerdas (40-47) ----
    "Violin",                  // 40
    "Viola",                   // 41
    "Cello",                   // 42
    "Contrabajo",              // 43
    "Cuerdas tremolo",         // 44
    "Cuerdas pizzicato",       // 45
    "Arpa orquestal",          // 46
    "Timpano",                 // 47

    // ---- Conjunto (48-55) ----
    "Ensemble de cuerdas 1",   // 48
    "Ensemble de cuerdas 2",   // 49
    "Cuerdas sinteticas 1",    // 50
    "Cuerdas sinteticas 2",    // 51
    "Coro (aahs)",             // 52
    "Voz (oohs)",              // 53
    "Voz sintetica",           // 54
    "Golpe de orquesta",       // 55

    // ---- Viento metal (56-63) ----
    "Trompeta",                // 56
    "Trombon",                 // 57
    "Tuba",                    // 58
    "Trompeta sordina",        // 59
    "Trompa francesa",         // 60
    "Seccion de vientos",      // 61
    "Synth brass 1",           // 62
    "Synth brass 2",           // 63

    // ---- Viento madera (64-71) ----
    "Saxo soprano",            // 64
    "Saxo alto",               // 65
    "Saxo tenor",              // 66
    "Saxo baritono",           // 67
    "Oboe",                    // 68
    "Corno ingles",            // 69
    "Fagot",                   // 70
    "Clarinete",               // 71

    // ---- Flautas (72-79) ----
    "Piccolo",                 // 72
    "Flauta",                  // 73
    "Flauta dulce",            // 74
    "Flauta de pan",           // 75
    "Flauta soplada",          // 76
    "Shakuhachi",              // 77
    "Silbato",                 // 78
    "Ocarina",                 // 79

    // ---- Synth Lead (80-87) ----
    "Lead: onda cuadrada",     // 80
    "Lead: diente de sierra",  // 81
    "Lead: calliope",          // 82
    "Lead: chiff",             // 83
    "Lead: charang",           // 84
    "Lead: voz",               // 85
    "Lead: quintas",           // 86
    "Lead: bajo + lead",       // 87

    // ---- Synth Pad (88-95) ----
    "Pad: nueva era",          // 88
    "Pad: calido",             // 89
    "Pad: polysynth",          // 90
    "Pad: coro",               // 91
    "Pad: arqueado",           // 92
    "Pad: metalico",           // 93
    "Pad: halo",               // 94
    "Pad: sweep",              // 95

    // ---- Synth FX (96-103) ----
    "FX: lluvia",              // 96
    "FX: banda sonora",        // 97
    "FX: cristal",             // 98
    "FX: atmosfera",           // 99
    "FX: brillo",              // 100
    "FX: duendes",             // 101
    "FX: ecos",                // 102
    "FX: ciencia ficcion",     // 103

    // ---- Etnico (104-111) ----
    "Sitar",                   // 104
    "Banjo",                   // 105
    "Shamisen",                // 106
    "Koto",                    // 107
    "Kalimba",                 // 108
    "Gaita",                   // 109
    "Fiddle",                  // 110
    "Shanai",                  // 111

    // ---- Percusion melódica (112-119) ----
    "Campanitas",              // 112
    "Agogo",                   // 113
    "Steel drums",             // 114
    "Woodblock",               // 115
    "Taiko drum",              // 116
    "Tom melodico",            // 117
    "Synth drum",              // 118
    "Platillo invertido",      // 119

    // ---- Efectos de sonido (120-127) ----
    "Ruido de trastes",        // 120
    "Ruido de respiracion",    // 121
    "Oleaje del mar",          // 122
    "Canto de pajaro",         // 123
    "Tono de telefono",        // 124
    "Helicoptero",             // 125
    "Aplausos",                // 126
    "Disparo de pistola",      // 127
  };

  /**
   * Devuelve el nombre del instrumento GM para un numero de programa dado.
   *
   * @param program numero de programa MIDI (0-127)
   * @return nombre legible del instrumento
   */
  public static String nameOf(final int program) {
    if (program < 0 || program >= NAMES.length) return "Desconocido";
    return NAMES[program];
  }
}

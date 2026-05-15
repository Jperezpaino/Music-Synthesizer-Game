# 📖 Documentación del Código — Music Synthesizer Game

> **Para quién es este documento:**  
> Para alguien que quiere entender *cómo está hecho* el programa por dentro,  
> sin necesidad de saber programación ni música.  
> Cada fichero se explica con una analogía del mundo real.

---

## Estructura general del proyecto

Imagina el proyecto como una **fábrica de sonido** dividida en departamentos.  
Cada departamento (carpeta) tiene su función:

```
src/main/java/es/noa/rad/
│
├── Application.java          ← La PUERTA de entrada al programa
│
├── synth/                    ← Departamento de SÍNTESIS (fabrica el sonido)
│   ├── Waveform.java
│   ├── Note.java
│   ├── InstrumentPreset.java
│   ├── PolyVoice.java
│   └── SoundEngine.java
│
├── fx/                       ← Departamento de EFECTOS (modifica el sonido)
│   ├── AudioEffect.java
│   ├── DelayEffect.java
│   ├── ReverbEffect.java
│   ├── DistortionEffect.java
│   └── EffectChain.java
│
├── melody/                   ← Departamento de MELODÍAS (graba y reproduce)
│   ├── MelodyNote.java
│   ├── Melody.java
│   ├── MelodyPlayer.java
│   └── MelodySerializer.java
│
├── sequencer/                ← Departamento del SECUENCIADOR
│   ├── SequencerStep.java
│   └── Sequencer.java
│
├── drumachine/               ← Departamento de la CAJA DE RITMOS
│   ├── DrumVoice.java
│   └── DrumMachine.java
│
└── ui/                       ← Departamento VISUAL (lo que ves en pantalla)
    ├── KeyboardPanel.java
    ├── ControlPanel.java
    ├── OscilloscopePanel.java
    ├── EffectsPanel.java
    ├── MelodyPanel.java
    ├── SequencerPanel.java
    ├── DrumMachinePanel.java
    └── SynthesizerPanel.java
```

---

## 🚪 Application.java — La puerta de entrada

**Analogía:** Es como el *conserje* de un edificio.  
Su único trabajo es abrir la puerta (arrancar el programa) y dejar pasar a los demás.

**Qué hace concretamente:**
- Crea la ventana principal (el marco de la aplicación).
- Pone el título, el tamaño y el tema visual oscuro.
- Conecta el teclado del ordenador al sintetizador:  
  cuando pulsas la letra `A`, la letra `S`, etc., el conserje lo traduce  
  a "toca la nota Do", "toca la nota Re", etc.
- Usa un sistema anti-trampa: si mantienes una tecla pulsada mucho rato,  
  el ordenador la envía varias veces seguidas. El programa lo detecta y  
  hace sonar la nota solo una vez.

**Mapeo de teclas (lo que hace el conserje):**

```
Tecla del ordenador  →  Nota musical
─────────────────────────────────────
A                    →  Do  (C4)
W                    →  Do# (C#4)
S                    →  Re  (D4)
E                    →  Re# (D#4)
D                    →  Mi  (E4)
F                    →  Fa  (F4)
T                    →  Fa# (F#4)
G                    →  Sol (G4)
Y                    →  Sol#(G#4)
H                    →  La  (A4)
U                    →  La# (A#4)
J                    →  Si  (B4)
K                    →  Do  (C5, una octava más aguda)
```

---

## 🔊 Carpeta `synth/` — El Departamento de Síntesis

Este departamento es el **corazón** del programa. Aquí se *fabrica* el sonido  
desde cero, sin muestras de audio grabadas — todo es matemática pura.

---

### Waveform.java — Las formas del sonido

**Analogía:** Imagina que el sonido es agua. La forma de onda es la forma  
que tiene la superficie del agua: puede ser suave como una ola de playa,  
o afilada como las dientes de una sierra.

**Contiene 4 formas:**

| Forma     | Imagen mental                          | Matemática (simplificada)         |
|-----------|----------------------------------------|-----------------------------------|
| SINE      | Ola de mar suave y redonda             | Función seno (la más pura)        |
| SQUARE    | Escalón: arriba, abajo, arriba, abajo  | Alterna entre +1 y -1             |
| SAWTOOTH  | Diente de sierra                       | Sube gradualmente, cae de golpe   |
| TRIANGLE  | Montaña: sube y baja en línea recta    | Zigzag lineal                     |

**Por qué importa:** La forma de la onda determina el *timbre* — por qué un  
violín y una flauta suenan diferente aunque toquen la misma nota.

---

### Note.java — El catálogo de notas

**Analogía:** Es como un **menú de carta** en un restaurante.  
Lista todos los platos disponibles (notas) con su precio (frecuencia en Hz).

Contiene 35 notas que abarcan 3 octavas (de la octava 3 a la 5).  
Para cada nota guarda su **frecuencia en Hz** — el número de vibraciones  
por segundo que produce ese sonido:

```
Do3 (C3)  =  130.81 Hz   (grave, profundo)
La4 (A4)  =  440.00 Hz   (la nota de referencia universal)
Do5 (C5)  =  523.25 Hz   (agudo, brillante)
```

El número sube porque al doblar la frecuencia el sonido sube una octava:  
- La3 = 220 Hz  
- La4 = 440 Hz (el doble → una octava más aguda)  
- La5 = 880 Hz (el doble otra vez)

---

### InstrumentPreset.java — Las personalidades de los instrumentos

**Analogía:** Es como una **receta de cocina**. Cada instrumento tiene  
su propia receta que dice exactamente cómo debe comportarse el sonido.

La receta usa cuatro ingredientes llamados **ADSR**:

| Ingrediente | Nombre técnico | Qué hace en la vida real                              |
|-------------|---------------|-------------------------------------------------------|
| A           | Attack        | Tiempo hasta llegar al volumen máximo al pulsar       |
| D           | Decay         | Tiempo hasta bajar al nivel de sustain                |
| S           | Sustain       | Volumen al que se mantiene mientras pulsas            |
| R           | Release       | Tiempo hasta apagarse al soltar la tecla              |

**Ejemplo — Por qué el piano y el órgano suenan diferente:**

```
PIANO:
  Attack  muy rápido  → el golpe del martillo es instantáneo
  Decay   rápido      → el sonido baja enseguida
  Sustain medio       → se mantiene mientras pulsas
  Release corto       → se apaga rápido al soltar

ORGAN:
  Attack  rápido      → entra con fuerza
  Decay   casi cero   → no decae (el órgano no decae)
  Sustain máximo      → se mantiene al 100% mientras pulsas
  Release muy corto   → se corta en seco al soltar
```

---

### PolyVoice.java — Una "garganta" individual

**Analogía:** Imagina que tienes un coro de 8 cantantes.  
Cada cantante es una `PolyVoice`. Cuando pides una nota, un cantante libre  
empieza a cantar. Si los 8 están cantando y pides una nota más, el cantante  
que lleva más tiempo cantando para y canta la nueva.

**Estados de cada "garganta":**
```
IDLE (en reposo)
  ↓ [se pulsa una tecla]
ATTACK (el sonido sube de volumen)
  ↓
DECAY (baja un poco hasta el nivel normal)
  ↓
SUSTAIN (se mantiene mientras tienes pulsada la tecla)
  ↓ [sueltas la tecla]
RELEASE (el sonido se va apagando suavemente)
  ↓
DONE (terminado, vuelve a estar libre)
```

El fade suave del Release es fundamental: sin él, el sonido se cortaría  
de golpe y escucharías un "clic" desagradable.

---

### SoundEngine.java — El Motor Central

**Analogía:** Es el **director de orquesta** y también la **sala de mezclas**.

Hace tres cosas a la vez, continuamente, en un hilo propio:

1. **Gestiona el coro** de 8 voces (`PolyVoice`): asigna y libera cantantes.
2. **Mezcla** lo que cantan todos: suma sus señales y las normaliza  
   para que no haya distorsión involuntaria.
3. **Envía la mezcla** a los altavoces del ordenador, en bloques de 512 muestras  
   cada vez (aproximadamente cada 11 milisegundos).

**El proceso de mezcla (simplificado):**
```
Voz 1: ─────▄▄─────
Voz 2: ──▄▄─────▄▄─   →  SUMA  →  Normaliza  →  Efectos  →  🔊 Altavoz
Voz 3: ──────▄──────
```

Cuando hay varias voces activas, la suma se divide entre la raíz cuadrada  
del número de voces. Esto evita que al tocar 4 notas a la vez el volumen  
sea el cuádruple de lo esperado.

---

## 🎛️ Carpeta `fx/` — El Departamento de Efectos

Los efectos actúan como **filtros de Instagram para el audio**:  
toman el sonido ya generado y lo modifican antes de que llegue al altavoz.

---

### AudioEffect.java — El contrato común

**Analogía:** Es como la **descripción del trabajo** que debe cumplir  
cualquier efecto. Dice "todo efecto debe saber:  
activarse/desactivarse, procesar un sonido, y decir su nombre".

No hace nada por sí solo — es solo las reglas que los demás deben seguir.

---

### DelayEffect.java — El Eco

**Analogía:** Grita en una cueva. Escuchas tu voz, y unos instantes después  
la escuchas de nuevo (más suave), y quizás otra vez (aún más suave).

**Cómo funciona por dentro:**  
Tiene una **cinta grabadora imaginaria** (un buffer circular de muestras).  
- El sonido que llega se guarda en la cinta.  
- Después de un tiempo (el parámetro `Time`), ese sonido guardado  
  se mezcla de vuelta con el sonido actual.  
- Una parte de ese eco vuelve a grabarse (el parámetro `Feedback`)  
  creando ecos de ecos.

```
Sonido original:  ──█────────────────────────────
Eco 1:            ──────────█──────────────────
Eco 2:            ──────────────────█────────
Eco 3 (tenue):    ──────────────────────────█
```

---

### ReverbEffect.java — La Reverberación

**Analogía:** Toca palmas en una catedral. El sonido rebota en las paredes,  
el suelo y el techo miles de veces antes de apagarse — eso es la reverb.

**Cómo funciona por dentro:**  
Usa el **algoritmo de Schroeder** (inventado en los años 60):
- 4 "filtros peine" (comb filters): cada uno hace un eco a diferente velocidad.
- 2 "filtros pasatodo" (allpass filters): mezclan las reflexiones para  
  que suenen naturales y no metálicas.

El resultado es una nube de ecos tan densa que suena como espacio real.

---

### DistortionEffect.java — La Distorsión

**Analogía:** Pon el volumen de un altavoz pequeño al máximo.  
La membrana no puede moverse tan rápido y "aplana" los picos del sonido.  
Eso es distorsión — y es exactamente lo que buscan las guitarras eléctricas.

**Cómo funciona por dentro:**  
Aplica la función matemática `tanh()` (tangente hiperbólica) al sonido.  
Esta función aplana los picos de manera suave y musical (se llama  
"soft clipping" — recorte suave), a diferencia del corte brusco  
que sonaría como ruido.

```
Sin distorsión:  /\        Con distorsión:  /-\
                /  \                       /   \
───────────────/────\──   ─────────────---/─────\---
               \    /                       \   /
                \  /                         \-/
                 \/
```

---

### EffectChain.java — La Cadena de Efectos

**Analogía:** Como los pedales de efectos en el suelo de un guitarrista.  
El sonido entra por el primero, sale modificado, entra al segundo, y así.

El orden fijo es:
```
Sonido limpio → [Distorsión] → [Delay] → [Reverb] → 🔊 Altavoz
```

El orden importa: si pusieras el Delay antes de la Distorsión,  
los ecos también sonarían distorsionados (puede sonar bien o mal, según el gusto).

---

## 🎼 Carpeta `melody/` — El Departamento de Melodías

---

### MelodyNote.java — Una nota con memoria

**Analogía:** Una entrada del libro de contabilidad.  
Anota: "se tocó la nota X, con la forma Y, empezando a los Z milisegundos,  
y duró W milisegundos".

Guarda todo lo necesario para poder reproducir exactamente esa nota  
más tarde, en el mismo momento relativo.

---

### Melody.java — El libro de melodías

**Analogía:** El **cuaderno de partituras**.  
Es simplemente una lista ordenada de `MelodyNote`.  
Tiene operaciones básicas: añadir nota, borrar todo, contar notas,  
calcular la duración total.

---

### MelodyPlayer.java — El reproductor

**Analogía:** Un músico que lee el cuaderno y toca cada nota en su momento.

Funciona en un hilo separado (para no bloquear la pantalla):  
lee la lista de notas y usa un temporizador para tocar cada una  
exactamente cuando le corresponde, respetando las duraciones originales.

---

### MelodySerializer.java — El guardián de ficheros

**Analogía:** Un escribano que convierte la música en texto escrito  
y luego puede volver a leer ese texto y reconstruir la música.

Guarda las melodías en ficheros con extensión `.synth`  
usando un formato de texto simple (similar a JSON):

```
{
  "notes": [
    {"note":"C4","waveform":"SINE","startMs":0,"durationMs":500},
    {"note":"E4","waveform":"SINE","startMs":600,"durationMs":400}
  ]
}
```

---

## 🔢 Carpeta `sequencer/` — El Departamento del Secuenciador

---

### SequencerStep.java — Un peldaño del secuenciador

**Analogía:** Un interruptor de luz con post-it.  
El interruptor dice si ese momento está encendido o apagado.  
El post-it dice qué nota toca y a qué volumen.

Propiedades:
- **active**: ¿está encendido este paso?
- **note**: qué nota suena si está encendido
- **velocity**: qué tan fuerte (0.0 a 1.0)

---

### Sequencer.java — El Director de compás

**Analogía:** Un metrónomo que también toca notas.

Tiene un hilo propio que avanza de paso en paso a la velocidad del BPM.  
Para cada paso:
1. Si está activo, toca la nota.
2. Avisa a la pantalla de qué paso va (para iluminarlo en amarillo).
3. Espera el tiempo justo antes del siguiente paso.

La duración de cada paso es: `60 segundos ÷ BPM ÷ 2`  
(se divide entre 2 porque cada paso es una corchea, la mitad de un pulso).

---

## 🥁 Carpeta `drumachine/` — La Caja de Ritmos

---

### DrumVoice.java — Los 4 instrumentos de percusión

**Analogía:** 4 "recetas de cocina" para hacer sonidos de batería  
usando solo matemáticas (sin grabar ningún sonido real).

| Voz   | Ingredientes matemáticos                                          |
|-------|-------------------------------------------------------------------|
| KICK  | Seno puro cuya frecuencia baja rápido de 150Hz a 40Hz (pitch bend)|
| SNARE | Mezcla de tono + ruido aleatorio, corta duración                 |
| HIHAT | Solo ruido aleatorio, muy corto (60 ms)                          |
| CLAP  | Tres ráfagas de ruido seguidas muy rápidas, como tres palmadas   |

El **ruido aleatorio** se genera con números aleatorios entre -1 y 1.  
El sonido "blanco" de la estática de la televisión es exactamente eso.

---

### DrumMachine.java — El motor de la batería

**Analogía:** Un baterista robot que sigue instrucciones precisas.

Características:
- Guarda un patrón de 4 filas × 16 columnas de casillas (on/off).
- Cada hit de percusión se reproduce en **su propio hilo daemon**,  
  lo que permite que el bombo y el platillo suenen exactamente a la vez  
  sin que uno tenga que esperar al otro.
- Tiene BPM independiente del secuenciador (puedes tener los dos activos  
  con velocidades distintas si quieres efectos experimentales).

---

## 🖥️ Carpeta `ui/` — El Departamento Visual

Todo lo que ves en pantalla está aquí. Cada fichero es un "panel"  
(un rectángulo de pantalla) con su propio comportamiento.

---

### KeyboardPanel.java — El Teclado Visual

**Analogía:** Una foto de un teclado de piano que también se puede tocar.

Dibuja teclas blancas y negras manualmente pixel a pixel.  
Cuando haces clic en una zona, calcula qué nota corresponde a esa posición  
y llama al `SoundEngine` para que la suene.

Las teclas cambian de color cuando están pulsadas  
(blancas → azul claro; negras → azul más oscuro).

---

### OscilloscopePanel.java — El Osciloscopio

**Analogía:** Una ventana que muestra el "latido" del sonido en tiempo real.

Cada 30 milisegundos (unas 33 veces por segundo) toma los últimos 512 valores  
de la señal de audio y los dibuja como una línea verde.  
Los valores positivos se dibujan arriba y los negativos abajo;  
el silencio es una línea horizontal plana en el centro.

---

### ControlPanel.java — El Panel de Controles

**Analogía:** El salpicadero de un coche.  
Agrupa los controles más usados: preset, forma de onda, volumen y octava.

Cuando cambias el preset, actualiza automáticamente la forma de onda  
para que coincida con la del instrumento elegido.

---

### EffectsPanel.java — El Panel de Efectos

**Analogía:** La pedalera de efectos de un guitarrista, pero con interfaz gráfica.

Crea tres secciones (una por efecto) con:
- Una casilla ☐/☑ para activar/desactivar.
- Sliders (deslizadores) para cada parámetro del efecto.

Cuando mueves un slider, actualiza el efecto en tiempo real  
— escuchas el cambio inmediatamente mientras el sonido está activo.

---

### MelodyPanel.java — El Panel de Grabación

**Analogía:** Una grabadora de cassette con botones.

Implementa la interfaz `NoteListener` del `KeyboardPanel`:  
cuando el teclado dice "se pulsó la nota X", si estamos grabando,  
este panel anota en su cuaderno "nota X, a los N milisegundos".  
Cuando se suelta la nota, apunta la duración.

---

### SequencerPanel.java — El Panel del Secuenciador

Dibuja los 16 botones de paso con un renderizado personalizado  
(no usa botones estándar del sistema sino que los pinta a mano para  
que tengan el aspecto retro iluminado).

Cuando el secuenciador avanza de paso, este panel recibe la notificación  
y pinta el paso actual en amarillo — todo dentro del hilo de interfaz gráfica  
para que no haya parpadeos ni fallos visuales.

---

### DrumMachinePanel.java — El Panel de la Caja de Ritmos

Igual que el `SequencerPanel` pero con 4 filas.  
Cada fila tiene el color característico de su instrumento:

- **KICK** — rojo oscuro
- **SNARE** — naranja
- **HIHAT** — cyan
- **CLAP** — violeta

Los botones tienen tres estados visuales:
- ⬛ Oscuro → apagado
- 🟥 Con color → encendido
- 🟨 Amarillo → encendido Y es el paso actual

---

### SynthesizerPanel.java — El Panel Principal

**Analogía:** El plano del edificio entero.  
Organiza todos los demás paneles en su posición correcta:

```
┌───────────────────────────────────────────┐
│  ControlPanel  │  EffectsPanel            │ NORTE
├───────────────────────────────────────────┤
│  OscilloscopePanel                        │ CENTRO
│  KeyboardPanel (con scroll horizontal)    │
├───────────────────────────────────────────┤
│  MelodyPanel                              │ SUR
│  SequencerPanel                           │
│  DrumMachinePanel                         │
└───────────────────────────────────────────┘
```

---

## 🔄 Cómo fluye el sonido — De principio a fin

```
👆 Usuario pulsa tecla
        ↓
   Application.java detecta la tecla
        ↓
   KeyboardPanel.pressNote(nota)
        ↓
   SoundEngine.startNote(nota)
        ↓
   PolyVoice.trigger(frecuencia, ADSR...)
        ↓
   [Hilo de mezcla — cada 11ms]
   SoundEngine.mixLoop():
     suma los nextSample() de las 8 voces
        ↓
   EffectChain.process(mezcla):
     Distorsión → Delay → Reverb
        ↓
   Convierte a bytes PCM 16-bit
        ↓
   SourceDataLine.write() → 🔊 Altavoz
        ↓
   También guarda en oscilloscopeBuffer
        ↓
   OscilloscopePanel lo dibuja (cada 30ms)

👆 Usuario suelta la tecla
        ↓
   KeyboardPanel.releaseNote(nota)
        ↓
   SoundEngine.stopNote(nota)
        ↓
   PolyVoice.release() → empieza el fade-out
        ↓
   [Hilo de mezcla sigue corriendo]
   La voz pasa por RELEASE → DONE → libre
```

---

## 📐 Datos técnicos (sin matemáticas profundas)

| Concepto               | Valor          | Por qué                                          |
|------------------------|----------------|--------------------------------------------------|
| Frecuencia de muestreo | 44 100 Hz      | Calidad de CD — el estándar de audio digital     |
| Bits por muestra       | 16             | 65 536 niveles de volumen posibles               |
| Canales                | 1 (mono)       | Un solo altavoz virtual, simplifica la síntesis  |
| Voces simultáneas      | 8              | Balance entre calidad y consumo de CPU           |
| Buffer de mezcla       | 512 muestras   | ≈11 ms de latencia — prácticamente imperceptible |
| Buffer del osciloscopio| 512 muestras   | Una "ventana" de unos 12 ms de señal             |
| Refresco del osciloscopio | 33 fps      | Fluido sin consumir demasiada CPU                |

---

## ❓ Preguntas frecuentes sobre el código

**¿Por qué todo el código está en inglés pero los comentarios en español?**  
Los nombres de las clases, métodos y variables siguen la convención internacional  
de Java (inglés). Los comentarios se dejaron en español para este proyecto  
educativo, pero en proyectos profesionales también irían en inglés.

**¿Qué es un "hilo" (Thread)?**  
El ordenador puede hacer varias cosas a la vez si las divide en hilos.  
El programa usa hilos separados para el audio, la interfaz gráfica y la mezcla,  
para que ninguno bloquee a los otros. Es como tener varios cocineros en la cocina.

**¿Por qué no se usan librerías externas?**  
Todo el audio usa `javax.sound.sampled`, que viene incluida en Java.  
Toda la interfaz usa `javax.swing`, también incluida en Java.  
El programa no necesita instalar nada más — funciona en cualquier ordenador  
con Java 21 o superior.

**¿Qué significa "daemon thread"?**  
Un hilo daemon es un ayudante que trabaja en segundo plano.  
Cuando cierras la ventana del programa, Java cierra automáticamente  
todos los hilos daemon sin necesidad de pararlos uno a uno.

---

*Documentación de código — Music Synthesizer Game v1.0*  
*Última actualización: Mayo 2026*

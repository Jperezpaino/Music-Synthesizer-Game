# 🎹 Music Synthesizer Game — Manual de Usuario

> **Para quién es este manual:**  
> Está escrito pensando en alguien que nunca ha tocado un instrumento musical  
> y que tampoco necesita saber nada de programación.  
> Si puedes hacer clic con el ratón y pulsar teclas, puedes usar este programa.

---

## ¿Qué es este programa?

Es un **sintetizador musical** que funciona dentro de tu ordenador.  
Un sintetizador es una máquina que *fabrica* sonidos — igual que los teclados  
electrónicos que ves en los conciertos, pero en tu pantalla.

No necesitas altavoces externos: el sonido sale por los altavoces o auriculares  
que ya tienes conectados al ordenador.

---

## Cómo arrancar el programa

1. Abre una ventana de comandos (PowerShell).
2. Pega este comando y pulsa **Enter**:

```
$env:JAVA_HOME = "C:\Users\x922180\TCN-IDE\eclipse-jee-2022-12-R-win32-x86_64\jdk\Temurin-21.0.9+10"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\x922180\TCN-IDE\eclipse-jee-2022-12-R-win32-x86_64\Maven\bin;$env:PATH"
cd "c:\Users\x922180\Copilot\Music Synthesizer Game"
mvn clean compile -q
Start-Process "$env:JAVA_HOME\bin\javaw" -ArgumentList "-cp","target\classes","es.noa.rad.Application" -WorkingDirectory "c:\Users\x922180\Copilot\Music Synthesizer Game"
```

3. Se abre una ventana con el sintetizador. ¡Listo!

---

## Vista general de la pantalla

```
┌─────────────────────────────────────────────────────────┐
│  [Controles]          [Efectos de sonido]               │  ← Zona NORTE
├─────────────────────────────────────────────────────────┤
│  [Osciloscopio — dibujo de la onda]                     │  ← Zona CENTRO
│  [Teclado de piano]                                     │
├─────────────────────────────────────────────────────────┤
│  [Grabador de melodías]                                 │  ← Zona SUR
│  [Secuenciador de 16 pasos]                             │
│  [Caja de ritmos]                                       │
└─────────────────────────────────────────────────────────┘
```

---

## Sección 1 — Los Controles (arriba a la izquierda)

### 🎸 Instrumento (Preset)
Es un menú desplegable con 6 instrumentos diferentes:

| Nombre   | Cómo suena                              |
|----------|-----------------------------------------|
| PIANO    | Piano clásico, suave y redondo          |
| ORGAN    | Órgano de iglesia, con mucho cuerpo     |
| FLUTE    | Flauta, ligera y suave                  |
| BASS     | Bajo eléctrico, grave y profundo        |
| LEAD     | Sonido de sintetizador brillante        |
| STRINGS  | Cuerdas de orquesta, cálidas y suaves   |

**Cómo cambiarlo:** haz clic en el menú y elige el que quieras.

---

### 〰️ Forma de onda (Waveform)
Controla el *carácter* del sonido, independientemente del instrumento.  
Piénsalo como el color del sonido:

| Nombre   | Cómo suena                                     |
|----------|------------------------------------------------|
| SINE     | Muy suave, puro, como una flauta de cristal    |
| SQUARE   | Con bordes, más agresivo, como un videojuego   |
| SAWTOOTH | Brillante y con muchos armónicos (más rico)    |
| TRIANGLE | A medio camino entre SINE y SQUARE             |

---

### 🔊 Volumen
Mueve el deslizador para subir o bajar el volumen general. Simple.

---

### 🎵 Octava
Sube o baja las notas una **octava**:  
- Una octava arriba → el sonido es más agudo (más chillón).  
- Una octava abajo → el sonido es más grave (más profundo).

---

## Sección 2 — El Teclado de Piano (centro)

Verás un teclado blanco y negro igual al de un piano real.

### Con el ratón
- **Haz clic** sobre una tecla y mantén pulsado → suena la nota.
- **Suelta el clic** → la nota se apaga.

### Con el teclado del ordenador
Puedes tocar con las letras de tu teclado:

```
Teclas blancas:   A  S  D  F  G  H  J  K
Notas:            Do Re Mi Fa Sol La Si Do

Teclas negras (sostenidos):  W  E     T  Y  U
Notas:                       Do# Re#  Fa# Sol# La#
```

💡 **Consejo:** pulsa varias teclas a la vez para tocar **acordes**  
(varios sonidos simultáneos). El programa aguanta hasta 8 notas a la vez.

---

## Sección 3 — El Osciloscopio (la pantalla verde)

Es esa pantalla con una línea verde que se mueve.  
**No tienes que tocarla** — es solo visual.

Muestra la "forma" del sonido que estás produciendo en tiempo real.  
Cuando no suena nada la línea es plana; cuando tocas, forma ondas.

---

## Sección 4 — Efectos de Sonido (arriba a la derecha)

Hay tres efectos que puedes activar o desactivar con una casilla de verificación  
(☑ activado / ☐ desactivado).

---

### 🔁 Delay (Eco)
Repite el sonido poco después de que lo produces, como un eco en una cueva.

| Parámetro  | Qué controla                                          |
|------------|-------------------------------------------------------|
| Time       | Cuánto tiempo tarda en repetirse (más → eco más lento)|
| Feedback   | Cuántas veces se repite (más → más repeticiones)      |
| Mix        | Qué tan fuerte se oye el eco comparado con el original|

---

### 🏛️ Reverb (Reverberación)
Simula que estás tocando en una habitación grande, como una catedral.  
La diferencia con el eco es que la reverberación es continua y difusa.

| Parámetro | Qué controla                                           |
|-----------|--------------------------------------------------------|
| Room Size | El "tamaño" de la sala (más grande → más difuso)       |
| Mix       | Qué tan fuerte se nota el efecto                       |

---

### ⚡ Distorsión
Hace el sonido más sucio y agresivo, como una guitarra eléctrica con amplificador.

| Parámetro | Qué controla                                      |
|-----------|---------------------------------------------------|
| Drive     | Qué tan distorsionado suena (más → más agresivo)  |
| Mix       | Mezcla entre sonido limpio y distorsionado         |

---

## Sección 5 — El Grabador de Melodías

Aquí puedes **grabar lo que tocas** y reproducirlo.

### Botones disponibles

| Botón      | Qué hace                                                  |
|------------|-----------------------------------------------------------|
| ⏺ GRABAR  | Empieza a grabar. Todo lo que toques queda guardado.      |
| ▶ PLAY    | Reproduce lo que grabaste.                                |
| ⏹ STOP    | Para la grabación o la reproducción.                      |
| 🗑 BORRAR  | Borra lo que grabaste (no se puede deshacer).             |
| 💾 GUARDAR | Guarda la melodía en un fichero en tu ordenador.          |
| 📂 ABRIR   | Abre una melodía guardada anteriormente.                  |

### Cómo grabar tu primera melodía
1. Pulsa **⏺ GRABAR** — el botón cambia de color.
2. Toca notas con el teclado o el ratón.
3. Pulsa **⏹ STOP** para terminar.
4. Pulsa **▶ PLAY** para escucharla.

💾 Los ficheros de melodía tienen extensión `.synth` y los puedes mover,  
copiar y compartir con otras personas que tengan el programa.

---

## Sección 6 — El Secuenciador de 16 Pasos

El secuenciador es como una **caja de notas automática**.  
Toca una secuencia de notas en bucle, una tras otra, sin que tengas que hacer nada.

### La cuadrícula
- Hay **16 botones** en fila. Cada botón es un "paso" (un momento en el tiempo).
- Un botón **encendido** (brillante) significa que en ese momento sonará una nota.
- Un botón **apagado** (oscuro) significa silencio en ese momento.

### Cómo usarlo
1. Elige la **nota** que quieres con el menú desplegable de la izquierda.
2. Haz clic en los botones para encenderlos o apagarlos.
3. Pulsa **▶ PLAY** — el secuenciador empieza a avanzar paso a paso.
4. El paso actual se ilumina en **amarillo** para que veas por dónde va.
5. Ajusta el **BPM** (velocidad): más BPM → más rápido; menos BPM → más lento.

**BPM** significa "golpes por minuto". Una marcha lenta son unos 60 BPM;  
una canción de pop normal son unos 120 BPM; la música electrónica rápida puede  
llegar a 160 BPM.

---

## Sección 7 — La Caja de Ritmos 🥁

La caja de ritmos funciona igual que el secuenciador, pero en vez de notas  
produce **sonidos de batería**. No necesitas saber tocar la batería.

### Los cuatro instrumentos

| Nombre | Qué es                                          |
|--------|-------------------------------------------------|
| KICK   | El bombo — el golpe grave que sientes en el pecho |
| SNARE  | El redoblante — el golpe fuerte del centro       |
| HIHAT  | El platillo — ese sonido seco "chk chk chk"      |
| CLAP   | Palmada — un sonido seco y brillante             |

### La cuadrícula
- Hay **4 filas** (una por instrumento) × **16 columnas** (pasos).
- Haz clic en cualquier casilla para encenderla o apagarla.
- El slider de la derecha de cada fila controla el **volumen** de ese instrumento.

### Un ritmo básico para empezar
Enciende estos pasos para conseguir un ritmo de pop sencillo:

```
KICK:   ■ . . . ■ . . . ■ . . . ■ . . .   (pasos 1, 5, 9, 13)
SNARE:  . . . . ■ . . . . . . . ■ . . .   (pasos 5, 13)
HIHAT:  ■ . ■ . ■ . ■ . ■ . ■ . ■ . ■ .  (pasos impares)
CLAP:   . . . . . . . . . . . . . . . .   (apagado)
```
*(■ = encendido, . = apagado)*

---

## Consejos y trucos

### Para hacer música interesante
- Activa el **secuenciador** con un patrón y toca notas encima con el teclado.
- Prueba a activar la **reverberación** para que todo suene más espacioso.
- Combina el **delay** con notas rápidas para efectos de eco rítmico.

### Si no se oye nada
1. Comprueba que el volumen del ordenador no está en cero o silenciado.
2. El slider de **Volumen** en los controles debe estar subido.
3. Cierra y vuelve a abrir el programa.

### Si el sonido suena distorsionado sin querer
- El slider de **Drive** en la Distorsión puede estar muy alto.
- Comprueba que la casilla de **Distorsión** está desactivada (☐).

---

## Glosario rápido

| Palabra       | Explicación sencilla                                              |
|---------------|-------------------------------------------------------------------|
| Nota          | Un sonido con una altura concreta (como Do, Re, Mi...)            |
| Acorde        | Varias notas sonando al mismo tiempo                             |
| Octava        | Una nota que suena el doble de aguda (o grave) que la misma nota |
| BPM           | Velocidad de la música: golpes por minuto                         |
| Sintetizador  | Máquina electrónica que fabrica sonidos artificialmente           |
| Osciloscopio  | Pantalla que muestra la forma visual del sonido                   |
| Eco / Delay   | Repetición del sonido con un pequeño retraso                      |
| Reverb        | Sensación de espacio, como estar en una sala grande               |
| Distorsión    | Efecto que hace el sonido más áspero y agresivo                   |
| Secuenciador  | Máquina que toca notas automáticamente en un orden fijo           |
| Caja de ritmos| Máquina que produce patrones de percusión automáticamente         |
| Preset        | Configuración predefinida de un instrumento                       |
| Forma de onda | La "forma" matemática del sonido; cambia su timbre                |
| Polirítmia    | Capacidad de sonar varias notas a la vez                          |

---

*Manual de usuario — Music Synthesizer Game v1.0*  
*Creado con Java + javax.sound.sampled (sin dependencias externas)*

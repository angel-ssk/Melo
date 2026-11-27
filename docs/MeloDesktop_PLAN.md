# Melo Desktop — Plan técnico (Windows first)

## 1. Objetivo

Crear **Melo para PC**: una app de escritorio nativa, no un port del APK. Vive en el mismo repositorio `angel-ssk/Melo`, en la carpeta `/desktop`, junto a `/app` (Android).

- **Plataforma objetivo inicial:** Windows 10/11 x64.
- **Identidad:** la misma que Melo Android (paleta morada derivada del logo, squircle, Melo Wave, Geom + Nunito, galleta de búsqueda, pentágono de Inicio).
- **Principio rector:** reproductor de música **local**. Sin Last.fm, ListenBrainz, Discord Presence ni servicios de streaming. “Melo GO” no existe en Desktop.
- **Independencia de versiones:** Android se queda en `1.0` (tag `v1.0`); Desktop empieza en `0.1.0` con tags `desktop-v0.1.0`.
- **Publicación:** ningún push/release sin la orden explícita `publicar`.

## 2. Organización en el repositorio

```
Melo/
├── app/                  # Android (intacto)
├── macrobenchmark/       # Android (intacto)
├── desktop/              # NUEVO — proyecto Gradle independiente
│   ├── build.gradle.kts
│   ├── src/main/kotlin/com/melo/desktop/...
│   ├── src/main/resources/fonts/
│   └── README.md
└── docs/MeloDesktop_PLAN.md
```

`desktop/` se registra como **included build** (`includeBuild("desktop")` en `settings.gradle.kts`) para no acoplar sus versiones de Gradle/Kotlin a las del módulo Android. Se compila con:

```
.\gradlew.bat :desktop:run          # desde el build incluido
```

## 3. Stack recomendado

| Capa | Tecnología | Motivo |
|---|---|---|
| Lenguaje/UI | Kotlin + Compose Multiplatform (JVM) | Reutiliza el sistema de diseño Melo y la experiencia Compose |
| JDK | Java 21 LTS + `jlink` | Runtime recortado para el instalador |
| Audio | libVLC embebido vía VLCJ/JNA | Codecs amplios (FLAC, OGG, M4A, OPUS) y dos players para crossfade |
| Metadatos | JAudiotagger | Tags y carátulas incrustadas |
| Persistencia | SQLite + SQLDelight | Biblioteca y estadísticas |
| Ajustes | `kotlinx.serialization` → JSON en `%APPDATA%/Melo` | Simple y portable |
| Imágenes | Coil Multiplatform / decodificado AWT | Carátulas y Melo Wave |
| Concurrencia | Coroutines + Flow | Igual modelo que Android |
| Empaquetado | MSI (jpackage) + ZIP portable | Windows first, sin dependencias externas |

Se descarta JavaFX Media: soporte de codecs insuficiente para una biblioteca local real.

## 4. Estructura de código propuesta

```
com.melo.desktop/
├── MeloApp.kt                 # entrada Compose Desktop
├── ui/
│   ├── theme/                 # Color, Type, Shapes, MeloWave, Motion
│   ├── navigation/            # Inicio / Biblioteca / Reproductor / Ajustes / Perfil
│   ├── home/  library/  player/  lyrics/  settings/  profile/
│   └── components/            # MeloControlButton, cookie search, etc.
├── audio/
│   ├── PlaybackEngine.kt      # libVLC, cola, shuffle/repeat, volumen
│   ├── CrossfadeEngine.kt     # dos players + lead time (mismo contrato que Android)
│   └── PlaybackState.kt       # StateFlow
├── library/
│   ├── LibraryScanner.kt      # escaneo recursivo + WatchService
│   ├── TagReader.kt           # JAudiotagger
│   └── SongRepository.kt      # SQLite
├── data/
│   ├── Models.kt              # Song, Playlist, Lyrics, Stats
│   ├── Database.kt
│   └── SettingsStore.kt       # JSON + basicMode, uiScale, crossfade...
├── lyrics/
│   ├── LrcParser.kt           # LRC + word-by-word
│   └── WordAnimation.kt       # presets DEFAULT/BOUNCE/SLIDE/GLOW/KARAOKE/MINIMAL
├── system/
│   ├── Tray.kt                # bandeja + minimize-to-tray
│   ├── MediaKeys.kt           # teclas multimedia globales (JNA/Win32)
│   └── SingleInstance.kt      # una sola instancia
├── update/                    # GitHub releases filtrando tags desktop-v*
└── util/
```

## 5. Modelo de datos

- **Song**: id, ruta, título, artista, álbum, albumArtist, pista, disco, año, género, duración, tamaño, bitrate, sampleRate, canales, carátula, letra, fecha de añadido, última reproducción, contador, favorito, valoración.
- **Playlist**: id, nombre, canciones, fecha de creación.
- **Estadísticas**: tiempo de escucha, canción más reproducida, artistas top (mismo espíritu de “Melo Estadísticas”).
- **Ajustes**: tema, formas, escala de UI, Modo básico, crossfade, carpetas de biblioteca, proveedores de letras, canal de actualizaciones.

La deduplicación se hace por `ruta + tamaño + mtime`, no solo por título.

## 6. Motor de audio

- Dos instancias de libVLC (A/B) con conmutación por volumen para el **crossfade**, replicando el contrato de `MeloPlayerEngine`/`TransitionController` de Android.
- Manual skip con fade limitado a **1,2 s** ( igual que la regla ya aplicada en Android).
- `PlaybackState` expuesto como `StateFlow`: canción actual, posición, playing, cola, shuffle, repeat, volumen, dispositivo.
- Opciones posteriores: gapless, replay gain, ecualizador, velocidad/tono.
- **Modo básico** desactiva crossfade animado y efectos decorativos, igual que en Android.

## 7. Biblioteca local

- Selección de una o varias carpetas raíz.
- Extensiones: `mp3, flac, m4a, ogg, opus, wav, aac, wma`.
- Escaneo paralelo con dispatcher limitado; `WatchService` para cambios en carpetas.
- Búsqueda y filtros (Canciones, Álbumes, Artistas, Favoritos, Playlists, CD Quality, duración), con índices SQLite.

## 8. UI e identidad

- Tokens copiados del tema Android: morado Melo, surface oscuro/claro, squircle, expressive shapes, tipografías Geom/Nunito.
- Adaptaciones de escritorio: ventana mínima 1024×640, redimensionable, navegación por teclado, menús contextuales, drag & drop de archivos a la cola.
- Pantallas: **Inicio**, **Biblioteca**, **Reproductor**, **Letras a pantalla completa**, **Ajustes**, **Perfil/Estadísticas**.
- Melo Wave como fondo con blur opcional; el Modo básico lo desactiva.
- Keyboard-first: `Espacio` play/pause, `←/→` seek, `Ctrl+F` buscar, `F11` pantalla de letras.

### 8.1 Fidelidad Material 3 Expressive (requisito estricto)

Desktop **no** será un Material 3 “genérico”: debe verse y sentirse como Melo Android.

- Portar literalmente los **tokens** de `Theme.kt` (colores del logo, surface/container, roles primary/secondary/tertiary) y la paleta Melo Predeterminada.
- Portar las **formas expresivas** de `ExpressiveComponents.kt` / `MeloControlButton.kt`: squircle, cookie de búsqueda de 9 piezas, pentágono de Inicio, botones de salto redondeados.
- Portar el **esquema de movimiento** `MeloHighRefreshMotionScheme` (springs cortos, independientes de la tasa de frames) como `CompositionLocal` propio si Compose Desktop no expone `MotionScheme`.
- Respetar **reduced motion** del sistema y el **Modo básico**, que desactiva blur, Melo Wave animado y crossfade visual.
- Misma jerarquía tipográfica: **Geom** para display/headline, **Nunito** para body/label; incluir ambas fuentes en `resources/fonts`.
- Misma semántica de superficies: contenedores `surfaceContainer`, elevaciones suaves y transiciones compartidas entre Biblioteca ↔ Reproductor.

Criterio visual: poner pantallas lado a lado (Android vs Desktop) y que un usuario reconozca Melo sin leer el nombre.

## 9. Integración con Windows

- Bandeja del sistema: play/pause, siguiente, anterior, salir; “minimizar a bandeja”.
- Teclas multimedia globales (JNA/Win32 `RegisterHotKey`).
- Notificación “now playing” opcional.
- Asociación de archivos ( Abrir con Melo ) para extensiones de audio.
- Instancia única: si ya hay un Melo corriendo, los archivos abiertos se le envían y se enfoca la ventana.

## 10. Letras y carátulas

- Parser LRC y word-by-word con el mismo contrato que Android.
- Offset de sincronización persistido y presets de animación portados.
- Proveedores online **local-first** y opcionales;uitar cualquier integración de scrobbling.
- Carátula incrustada primero, luego archivo `folder.jpg`/`cover.jpg`, y sólo después búsqueda online opcional.

## 11. Releases y actualizaciones (mismo repo)

- Tags: `desktop-v0.1.0`, `desktop-v0.2.0`, …
- Assets: `Melo-Desktop-0.1.0.msi` y `Melo-Desktop-0.1.0-portable.zip`.
- El updater de Desktop consulta los releases de `angel-ssk/Melo` y filtra los tags que empiezan por `desktop-v`; el updater de Android sigue usando `v*`.
- README raíz con dos secciones claras: **Melo Android** y **Melo Desktop**.
- Firma con certificado de código (recomendado para evitar SmartScreen); si no hay certificado, se documenta el aviso de Windows.

## 12. Fases y estimación

| Fase | Contenido | Tiempo |
|---|---|---|
| 0 | Scaffold Gradle, includeBuild, tema Melo, ventana, README, CI de compilación | 1–2 sem |
| 1 | Escáner + SQLite + biblioteca + búsqueda/filtros | 3–4 sem |
| 2 | libVLC + cola + crossfade + controles + atajos | 3–4 sem |
| 3 | Carátulas + letras LRC + offset + presets | 2–3 sem |
| 4 | Ajustes persistentes + Modo básico + Perfil/Estadísticas | 2–3 sem |
| 5 | Bandeja, media keys, actualizador, MSI/ZIP, firma | 2 sem |
| 6 | Pulido, accesibilidad, ES/EN completo | 2 sem |

**Total MVP usable:** ~3–5 meses. Windows primero; compartir código con Linux/macOS después es natural porque Compose Multiplatform ya abstrae la UI.

## 13. Riesgos

- Empaquetar los binarios nativos de libVLC (x64) y cumplir su licencia LGPL (compatible con GPL-3.0 de Melo, pero hay que incluir los avisos).
- Rendimiento con bibliotecas muy grandes: usar listas lazy e índices SQLite desde el principio.
- Conflicto de teclas globales con otras apps: permitir reconfigurar atajos.
- SmartScreen sin certificado de firma: mitigarlo con el MSI firmado cuando sea posible.

## 13.1 Optimización (igual o mejor que Android)

El escritorio no puede ser más pesado que el móvil. Se aplica desde la Fase 0:

- **Arranque:** runtime recortado con `jlink`, **AppCDS** generado en la build y `jpackage` para el MSI. Objetivo: arranque en frío < 1,5 s en un PC medio.
- **Perfilado equivalente al macrobenchmark:** JDK Flight Recorder + async-profiler para arranque, escaneo y cambio de canción; JMH para el parser LRC y el cálculo de crossfade. Se guardan trazas junto a cada release.
- **Listas y memoria:** `LazyColumn` con keys estables, paginación del escáner y cachés de carátulas acotadas por tamaño (128/96/64 px) para no repetir decodes, igual que la corrección hecha en Android.
- **Escaner en background:** coroutines con dispatcher limitado y `Flow` incremental; nunca bloquea la UI ni reconstruye la biblioteca completa por un cambio de archivo.
- **Efectos opcionales:** blur de Melo Wave y color derivado de la carátula se desactivan en Modo básico, en equipos con poca RAM y cuando la ventana está minimizada (equivalente a `MeloApplication.isForeground`).
- **Crossfade:** el motor A/B usa dos players, pero sin exponer un segundo vídeo/Canvas innecesario; Modo básico lo reduce a cambio directo.
- **Target medible:** 10.000 canciones < 30 s, pausas de GC < 10 ms en reproducción, consumo idle comparable al de un reproductor local ligero.

Si el PC tiene más recursos que un gama baja, se aprovecha para **más fluidez**, no para añadir efectos obligatorios.

## 14. Criterios de aceptación del MVP

1. Escanear ~10.000 archivos en menos de 30 s.
2. Reproducir MP3, FLAC y M4A con crossfade correcto.
3. Buscar y filtrar la biblioteca sin bloquear la UI.
4. Persistir biblioteca, ajustes y estadísticas entre reinicios.
5. Bandeja y teclas multimedia funcionando.
6. Instalador MSI y ZIP portable generados desde CI.
7. Interfaz en español completa (y english fallback).

## 15. Explícitamente fuera de alcance

- Last.fm / ListenBrainz / scrobbling.
- Discord Presence.
- Servicios de streaming o “Melo GO”.
- Cambios en el release 1.0 de Android antes de `publicar`.

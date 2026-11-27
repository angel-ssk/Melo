# Melo — cambios del proyecto

## 1. Reinicio del fork (versión 1.0)

- Base: **v5.6.506.1360-stable** de Rhythm (última estable, publicada el 24 nov 2026), descargada como código fuente desde el tag del repo oficial.
- Se eliminaron las 8 releases anteriores del repositorio `angel-ssk/Melo`.
- `namespace` y `applicationId`: **`com.melo.player`**. Paquetes Java/Kotlin renombrados de `chromahub.rhythm.app.*` a `com.melo.player.*` (incluye tests y macrobenchmark).
- `rootProject.name = "Melo"`, `app_name = "Melo"`, `versionCode = 1000000`, `versionName = "1.0"`.
- Renombrado global: clases, archivos, rutas, recursos, documentación, CI, fastlane y scripts. **No queda ninguna referencia a “Rhythm”** en nombres ni contenidos; se conservan las cabeceras SPDX originales para cumplir GPL-3.0-or-later.

## 2. Identidad visual

- Icono nuevo aportado por el mantenedor, sin alterar su apariencia: squircle morado con la ola M.
- Icono adaptativo: fondo `#523B8D`, foreground con la ola M y capa `monochrome` para los temas del sistema (Pixel). Iconos legacy PNG generados para todas las densidades.
- Splash y logos internos usan los mismos recursos.

## 3. Tipografía

- **Geom** para display/headline/titleLarge y **Nunito** (variable) para titleMedium/Small, body y label.
- `Type.kt` mantiene la API pública (`getTypographyForFont`, `getFontFamilyByName`). Licencia SIL OFL de Nunito en `docs/fonts/`.

## 4. Funciones portadas del fork anterior

- **Cuadrícula de artistas**: ordenación sincrónica y deduplicada; sin `animateItem()` ni animación de escala que duplicara tarjetas al desplazarse.
- **Letras sincronizadas**: el índice activo solo puede ser una línea con texto/palabras; auto-scroll directo (`scrollToItem`) sin pre-posicionamiento ni animaciones canceladas que devolvieran la vista a la línea anterior.
- **Imágenes de artistas en línea**: nueva opción en Ajustes → Integraciones (`online_artist_artwork`). Deezer + YouTube Music + respaldo iTunes Search sin clave API.
- **Traducción ES**: la pestaña “Liked” se muestra como **Favoritos**.

## 5. Correcciones y optimizaciones posteriores

- **Crash de arranque**: los logos internos se cargaban con `painterResource` desde `layer-list`, que Compose no soporta. Se usan PNG directos vía Coil; el squircle queda solo para launcher y «Acerca de», y la ola M sin fondo para onboarding, carga y pantallas internas.
- **Notificaciones**: `ic_notification` reemplazado por la ola M (antes seguía el icono del proyecto original).
- **Tema**: `Esquema personalizado` con **Melo Predeterminado** es el valor inicial; los colores dinámicos de Material You vienen desactivados para que el morado se aplique desde el primer arranque sin cambiar de modo.
- **Sliders**: corregido el patrón que recreaba el `SliderState` en cada cambio de preferencia (el de crossfade volvía a 0,5 s al arrastrar). Aplica a los 14 archivos de ajustes que usaban el mismo patrón; el slider de progreso externo solo sigue a la reproducción cuando no se está arrastrando.
- **Rendimiento**: cachés de imágenes y concurrencia adaptadas al tipo de dispositivo (`isLowRam` / `memoryClass`); blur de fondos desactivado en dispositivos con poca RAM en letras, álbum y artista. Build flags ya optimizados (paralelo, caché, configuración y R8).

## 6. Traducción y movimiento

- **Español completo**: 2911 de 2911 cadenas traducidas (antes faltaban 485; 336 recuperadas del fork anterior y 149 traducidas).
- **Alta tasa de refresco**: nueva opción en Modo básico → Movimiento, activa por defecto, con `MeloHighRefreshMotionScheme` (springs de corta duración e independientes de la tasa de fotogramas).
- **Actualizaciones**: el updater consulta `angel-ssk/Melo`, muestra versión y novedades del release y descarga el APK dentro de la app.

## 7. Perfil y créditos

- **Acerca de**: solo Anjishnu Nandi (creador de Rhythm) y Angel (creador de Melo, con avatar de GitHub); eliminados el resto de colaboradores.
- **Perfil**: nueva pantalla accesible desde el logo de la esquina superior izquierda. Foto de perfil (selector de galería), nombre editable y Melo estadísticas con tiempo de escucha, canción más reproducida, canciones y sesiones en Hoy/Semana/Mes/Año. Diseño tipo tarjeta con fondo difuminado en Material 3 Expressive.
- Añadido el rango `YEAR` al repositorio de estadísticas.

## 8. Firma

- Keystore en `.config/melo-release.jks` (alias `melo`), configurado en `.config/keystore.properties`. Ambos ignorados por Git.

## 9. Melo 1.0 — cierre de functions y rendimiento

- **Perfil**: se accede desde el avatar de la esquina superior derecha; el logo de la izquierda ya no navega.
- **Animaciones en segundo plano**: `MeloApplication` observa el ciclo de vida y expone `foregroundFlow`. La galleta de búsqueda, el Canvas del reproductor y las aureolas de letras se detienen al salir de la app.
- **Experiencia ligera (Modo básico)**: nuevo interruptor en Ajustes → Modo básico → Movimiento que desactiva blur, carátulas animadas, Canvas y movimiento decorativo incluso en dispositivos potentes; se suma a la detección automática por RAM.
- **Letras — presets y sincronización**: implementados `DEFAULT / BOUNCE / SLIDE / GLOW / KARAOKE / MINIMAL` en `WordByWordLyricsView`, seleccionables en Ajustes → Letras. El desfase manual ±0,5 s se guarda ahora en `AppSettings` y también se puede ajustar desde Ajustes → Letras.
- **Arranque diferido**: tras la primera frame se inicializan CrashReporter, NetworkClient, LeakCanary/ANR y el recorte de caché; el tema se aplica antes para evitar parpadeos.
- **Melo GO eliminado de la experiencia**: el onboarding ya no ofrece modo streaming ni pasos de servicio; el interruptor de Go Mode desapareció de Funciones experimentales y `setAppMode()` fuerza siempre `LOCAL`. La app es exclusivamente reproductor local.
- **Baseline profile**: el generador cubre ahora inicio, Inicio, Biblioteca, búsqueda y reproductor, con selectores en español e inglés. Nuevos benchmarks `LowEndJourneyBenchmarks` (startup / búsqueda / reproductor) para ejecutar en los flavors `github` y `fdroid`.
- **Perfil regenerado y benchmarks ejecutados en MuMu (API 35)**, sin fallos:
  - `BaselineProfileGenerator` con 6 recorridos (inicio, Inicio, Biblioteca, búsqueda, reproductor y vuelta). Perfil resultante: 15.197 reglas, copiado a `app/src/main/baseline-prof/baseline-prof.txt`.
  - `LowEndJourneyBenchmarks` en `github`: TTID mediano **731 ms** (713–763); P95 de frame: búsqueda **61,6 ms**, reproductor **43,0 ms**.
  - `LowEndJourneyBenchmarks` en `fdroid`: TTID mediano **790 ms**; P95 de frame: búsqueda **51,9 ms**, reproductor **48,2 ms**.
  - Nota: MuMu emula x86; los valores sirven como referencia comparativa, no como medida de un Tecno Spark Go real.
- **Corrección verificada en MuMu**: con Modo básico activo, una canción con Canvas animado descargado online (Moonlight) muestra carátula estática; no se crea el ExoPlayer ni se consulta el proveedor.

## 10. Melo 1.1 — mejoras portadas de Rhythm (solo aplicables a local)

La mayoría de los fixes de Rhythm 5.6 ya estaban presentes en la base; se cerraron las brechas residuales:

- **Biblioteca**: normalización recursiva de carpetas `CD/Disc/Disk N` para álbumes multi-disco (#692); cola del explorador con canciones de subcarpetas (#708); filtro de whitelist también en el fallback de MediaStore (#703).
- **Observador de MediaStore**: `isLibraryStale()` se ejecuta en `Dispatchers.IO`, no en Main (#646).
- **Letras**: umbral de progreso monótono alineado a 1000 ms en ambas vistas (#685) y el broadcast Bluetooth/widget usa la posición ya acotada; lectura de letras incrustadas vía Uri `content://` con TagLib (#687).
- **Reproducción**: búsqueda/filtros ya no amplían la cola a toda la biblioteca (#684); iconos de repeat del widget corregidos con constantes de `Player` (#671); pools de buffers de audio liberados al cerrar el engine (#686/697); logs de transición tras `BuildConfig.DEBUG` (#697); E/S de estadísticas fuera del lock en memoria (#701).
- **Red**: circuit breaker con TTL de 10 min para búsquedas de letras fallidas (#656).
- **Playlists**: ids UUID para evitar colisiones por milisegundo (#414); el servicio ya no sobrescribe el JSON legacy de favoritos, deja que el ViewModel/Room sea la única fuente.
- **Búsqueda**: filtros (canciones/álbumes/artistas/playlists) persistidos con `rememberSaveable` (#688).
- **Ya presentes y verificados** (sin cambios): miniplayer morphing + carousel, pantalla externa en bottom sheet, escalado de widgets por altura, scoping de géneros, deduplicación de álbumes y persistencia de consulta.
- **No portados**: ViPER4Android/Clarity/DDC/Convolver (coste CPU/RAM incompatible con gama baja) y todo lo de Rhythm Go/streaming (Melo es local-only).
- **Cierre adicional de 1.1**: reproducción externa con vista de letras completa (word-by-word/LRC con salto al tocar); cachés negativas de carátulas con TTL de 30 min; `loadSummary()` de Melo Guard en `Dispatchers.IO`.

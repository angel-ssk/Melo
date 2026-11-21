# Rhythm Fork — 120 Hz + Perfil (Bienestar Digital)

Rama de trabajo: `rhythm-120hz-profile` (basada en `main`, release 5.5.500.1306).

## Qué se ha añadido

### 1. Perfil «Bienestar Digital»
- Nueva ruta `profile` en `LocalNavigation` y avatar en la **esquina superior izquierda**
  del Inicio (`HomeScreen` → `CollapsibleHeaderScreen`).
- `ProfileScreen.kt`: cabecera con foto/nombre editables (selector de imagen que copia
  el archivo a `filesDir/profile/avatar.jpg`), anillo de «días escuchando música»,
  selector **Día / Semana / Mes / Año** y tarjetas de *Rhythm estadísticas*
  (tiempo, canciones, artistas, sesiones, racha).
- Persistencia en `AppSettings` (SharedPreferences + StateFlow): `profile_name`,
  `profile_photo_path`, `profile_join_date`, `profile_daily_goal_minutes`.
- `StatsTimeRange.YEAR` nuevo, con rango desde el 1 de enero. El recuento de días
  combina el historial de reproducción con `dailyListeningStats` (más duradero),
  por lo que los días siguen siendo correctos aunque el JSON de eventos expire a 90 días.
- «Rhythm estadísticas» también gana la pestaña **Este año**.

### 2. Rendimiento / 120 Hz+
- `RhythmMotion.kt`: tokens de movimiento basados en **springs** (no duraciones
  fijas), con umbrales de visibilidad muy bajos y animaciones sólo de
  `alpha/scale/translation` mediante `graphicsLayer` (sin re-medición).
- Transiciones complejas por estado: `updateTransition`, `AnimatedContent` con
  `ContentTransform` de tres propiedades solapadas (fade + scale + slide),
  animaciones de color y peso de pestañas.
- Aplicado a navegación raíz, cabeceras colapsibles y pantalla de perfil.
- Ajuste **Ajustes → Rendimiento → Movimiento ultra fluido (120 Hz+)** que alterna
  el `MotionScheme` expresivo (por defecto) con el estándar.

### 3. Nueva función incluida
- **Meta diaria de escucha** estilo Bienestar Digital, con indicador de progreso
  y deslizador configurable (15–240 min) dentro del Perfil.

### 4. Segunda iteración
- **Mapa de calor de escucha**: cuadrícula 52×7 de los últimos 12 meses dibujada
  en un único `Canvas` (364 celdas sin 364 composables), alimentada por
  `dailyListeningStats`.
- **Rhythm Wrap**: tarjeta mensual compartible generada con `android.graphics.Canvas`
  en un bitmap 720×1280 que se recicla inmediatamente; se comparte mediante
  `FileProvider` (ya existente en el manifest).
- **Detección real de tasa de refresco** (`DisplayRefreshRate.kt`): observa
  `Display.refreshRate` con `DisplayManager.DisplayListener` y muestra el valor
  en Ajustes → Rendimiento. Con «Adaptar velocidad a la tasa de refresco» se
  incrementa la velocidad hasta ×1,6 en paneles de 120 Hz.
- **Movimiento más rápido**: los springs pasan de rigidez 220-420 a 550-900 con
  amortiguamiento crítico, y se añade `RhythmMotionScheme` que sustituye el
  `MotionScheme.expressive()` de Material 3 (transiciones de 400-500 ms) por
  springs cortos. También hay un control **Velocidad de animación (0,75×–1,75×)**.
- Optimización de memoria: la foto de perfil se decodifica a 256 px en vez de
  su tamaño original.

### 5. Rebrand a Melo
- Nombre de aplicación **Melo** en `app_name` de las 26 localizaciones, etiquetas
  del manifest, títulos visibles y cadenas de accesibilidad del logo.
- Logotipos nuevos **sin editar las imágenes entregadas**:
  - `drawable/melo_mark.png` — la «M» (imagen 1), usada en el header colapsable
    del Inicio y teñida en tiempo de ejecución con `MaterialTheme.colorScheme.primary`
    (que ya refleja el color del álbum en reproducción o el color elegido por el usuario).
  - `drawable/melo_app_icon.png` — icono azul (imagen 2), usado globalmente en
    splash, About, cargadores, notificaciones y launcher adaptativo (fondo `#1946B5`,
    foreground con la «M», monocromo para iconos temáticos).
  - `drawable/dayner_avatar.jpg` — foto del creador (imagen 3), usada tal cual.
- `Acerca de`: se eliminaron mantenedores, colaboradores y diseñadores; quedan
  el creador de Rhythm (Anjishnu Nandi) y **Dayner Cabarca** como creador de Melo.
- **Identificador de paquete:** `namespace` y `applicationId` ahora son
  `com.melo.app` (incluidos flavors, referencias `R`/`BuildConfig`, perfil base
  y benchmarks). Los nombres de paquete del código fuente se mantienen para no
  romper las reglas de ProGuard.

### 6. Versión y build
- `versionName = "1.0"`, `versionCode = 1000000`; se quitaron los sufijos de
  versión de los flavors para que Release muestre exactamente `1.0`.
- `gradle.properties`: heap reducido a 3 GB y `org.gradle.parallel=false` para
  que la compilación funcione en equipos con poca RAM disponible.

### 7. Fix de arranque tras el cambio de paquete
Al cambiar el `namespace` a `com.melo.app`, los nombres relativos del manifest
(`.RhythmApplication`, `.activities.MainActivity`, etc.) pasaron a resolverse
como `com.melo.app.*`, mientras las clases siguen en `chromahub.rhythm.app.*`.
Eso provocaba `ClassNotFoundException` y cierre inmediato. Se sustituyeron las
12 referencias por nombres fully-qualified. Se añadió también un APK x86_64
para emuladores como MuMuPlayer.

### 8. Desvinculación total del proyecto original
- Todas las cadenas visibles en los 26 idiomas pasan a «Melo» (Melo Stats,
  Melo Guardia, Melo Go, notificaciones, etc.). Se conserva únicamente el
  crédito histórico «Rhythm Creator» del creador original.
- Actualizaciones OTA desactivadas: `updatesEnabled` y `autoCheckForUpdates`
  nacen en `false`, `AppUpdaterViewModel.checkForUpdates()` y
  `UpdateNotificationWorker` tienen una guardia que impide contactar cualquier
  repositorio. Los enlaces a issues/Discord/Telegram del proyecto original se
  eliminaron de «Acerca de».
- Splash y pantallas de carga usan la «M» transparente teñida con el color
  dinámico (mismo dibujo, sin fondo); el icono azul completo sigue siendo el
  logo global. En «Acerca de» el icono azul se redujo de 82 dp a 52 dp.

### 9. Repositorio propio y OTA
- Repositorio oficial: https://github.com/angel-ssk/Melo
- `AppSettings` guarda `update_repo_owner` / `update_repo_name`, con valores
  por defecto `angel-ssk` / `Melo`. Se pueden cambiar en
  Ajustes → Actualizaciones.
- `AppUpdaterViewModel` y `UpdateNotificationWorker` usan esos valores; si
  están vacíos no se realiza ninguna petición. El check OTA quedó activado
  (defaults originales restaurados) ahora que el repo existe.
- «Acerca de» incluye un acceso «Ver código fuente» al repo de Melo.
- `README.md` reescrito con identidad Melo y créditos a Rhythm y a su creador
  Anjishnu Nandi (@cromaguy).

### 10. Corrección de sliders y textos finales
- El slider de crossfade (y otros 29 sliders de ajustes) recreaba su
  `SliderState` en cada cambio de valor, lo que cancelaba el arrastre y dejaba
  el valor fijo en 0,5 s. Ahora el estado se mantiene durante el gesto.
- Opción «Rhythm» → «Melo» en el tema del reproductor y en los valores por
  defecto de widgets y notificaciones.
- El error «No releases found on GitHub» ya no se muestra mientras el repo no
  tenga releases publicadas.
- Icono adaptativo del launcher con la «M» completa dentro de la zona segura;
  degradado del header conservado.

### 11. Traducción completa y pantalla de actualizaciones
- Se añadieron 369 cadenas al español: la traducción es del 100 % de los
  recursos traducibles (los 8 restantes son `translatable="false"`).
- Textos de la pantalla de actualizaciones traducidos y con «Melo» en lugar de
  «Rhythm»; enlaces de release apuntan a `angel-ssk/Melo`.
- El panel de configuración del repositorio se eliminó de la UI: Melo viene
  configurado por defecto con `angel-ssk/Melo`.
- El parseo de notas de release acepta encabezados en español («Novedades»,
  «Problemas conocidos») y, si no los encuentra, muestra igualmente los
  puntos de la release como novedades. El peso del APK se muestra junto a la
  versión.

### 12. Ajustes finales de UI
- El título del estado «al día» es ahora corto («¡Estás al día!») y lleva
  tres galletas de 12 piezas girando como elipsis; el mensaje largo queda
  sólo como descripción en gris.
- Corregida la codificación de «Tu música, tu Melo» en español.
- `PresetDefaultSeed` (esquema personalizado) pasa del morado de Rhythm al
  azul Melo `#1946B5`, el mismo del icono.

### 13. Easter Egg del creador
- `MeloDevMode.isEnabled(name)` detecta el nombre exacto «Angel Dev» (o el flag
  `-PdevBuild=true`). Al escribirlo en el Perfil aparecen la foto de GitHub del
  creador y una corona vectorial propia de 20 dp sobre el avatar del Inicio y
  del Perfil. Se desactiva cambiando el nombre.
- Las notas de la release v1.1.5 son deliberadamente crípticas: el descubrimiento
  forma parte del Easter Egg.

## Verificación
- `./gradlew compileGithubDebugKotlin` ✅
- `./gradlew assembleGithubDebug` ✅
- Nota local: el SDK de esta máquina tenía `build-tools 36.0.0` corrupto; se
  compiló usando `37.0.0` temporalmente y se revirtió el cambio de Gradle.

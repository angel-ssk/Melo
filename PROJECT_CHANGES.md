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

## 7. Firma

- Keystore en `.config/melo-release.jks` (alias `melo`), configurado en `.config/keystore.properties`. Ambos ignorados por Git.

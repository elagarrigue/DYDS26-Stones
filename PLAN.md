# PLAN - Etapa 1 (Base de arquitectura para futuras features)

Este documento define una guía reutilizable para ejecutar una refactorización incremental, controlada y verificable.

## Objetivo

Reorganizar `edu.dyds.movies` en una arquitectura por capas, respetando SOLID y Clean Code, sin introducir cambios funcionales en esta etapa:

- `data`: solo datos persistentes/infraestructura de datos.
- `di`: ensamblado de dependencias.
- `domain`: lógica de negocio (`entity`, `repository`, `usecase`).
- `presentation`: lógica de interfaz (`detail`, `home`, `utils`) y `App.kt`.
- `main.kt` dentro de `edu.dyds.movies`.

## Reglas de ejecución obligatorias

1. Ejecutar **solo 1 paso por iteración**.
2. Esperar aprobación explícita antes del siguiente paso.
3. Al terminar cada paso: detenerse, reportar y validar contra su criterio.
4. Si un paso no valida: corregir solo ese paso y volver a validar.
5. No introducir cambios de comportamiento en etapa 1 (solo estructura/organización/imports).
6. Marcar en este archivo cuando un paso quede completado.

## Pasos (pequeños, verificables y autocontenidos)

### 1) Levantar inventario actual de archivos y responsabilidades

- Identificar en `edu.dyds.movies` todos los archivos/símbolos relevantes (modelos, viewmodels, DI, pantallas, navegación, utilidades, entrypoint).
- Registrar el rol actual de cada archivo para evitar mover código "a ciegas".

**Criterio de validación**
- Existe un inventario explícito y revisable con archivo -> responsabilidad principal.
- Se identifican al menos: `Movie`, `MoviesViewModel`, `MoviesDependencyInjector`, `HomeScreen`, `DetailScreen`, `Navigation`, `CommonComposables`, `App.kt`, `main.kt`.

**Inventario actual (archivo/símbolo -> responsabilidad principal)**
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/Movie.kt`
  - `Movie`: modelo de dominio usado por UI.
  - `QualifiedMovie`: wrapper para clasificar película "buena/mala" en UI.
  - `RemoteMovie`, `RemoteResult`: modelos remotos serializables de TMDB.
  - `toDomainMovie()`: mapper de remoto a dominio.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/MoviesViewModel.kt`
  - `MoviesViewModel`: coordina carga de populares/detalle, estado de UI y cache en memoria.
  - `MoviesUiState`, `MovieDetailUiState`: estados observables consumidos por pantallas.
  - Aplica orden por `voteAverage` y regla de clasificación con `MIN_VOTE_AVERAGE`.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/MoviesDependencyInjector.kt`
  - `MoviesDependencyInjector`: crea/configura `HttpClient` (JSON, timeout, API key) y provee `MoviesViewModel` para Compose.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/HomeScreen.kt`
  - `HomeScreen`: dispara carga inicial, observa listado y decide entre loading/resultados/sin resultados.
  - `MovieGrid`, `GoodMovieItem`, `BadMovieItem`: render de grilla, navegación a detalle y diálogo para películas no recomendadas.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/DetailScreen.kt`
  - `DetailScreen`: solicita detalle por id, observa estado y muestra loading/detalle/reintento.
  - `MovieDetail`, `DetailTopBar`: composición de detalle, metadata y navegación de regreso.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/Navigation.kt`
  - `Navigation`: define `NavHost` con rutas `home` y `detail/{movieId}` y cablea eventos entre pantallas.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/CommonComposables.kt`
  - `LoadingIndicator`, `NoResults`: componentes UI reutilizables para carga y estado vacío con retry.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/App.kt`
  - `App`: raíz composable de la aplicación, delega en `Navigation`.
- `composeApp/src/desktopMain/kotlin/edu/dyds/movies/main.kt`
  - `main()`: entrypoint desktop Compose; crea ventana y monta `App`.

**Estado:** COMPLETADO (inventario validado el 2026-04-12)

### 2) Definir mapa destino por capas y reglas de ubicación

- Establecer el árbol objetivo en `edu.dyds.movies`:
  - `data`
  - `di`
  - `domain/entity`
  - `domain/repository`
  - `domain/usecase`
  - `presentation/detail`
  - `presentation/home`
  - `presentation/utils`
  - `presentation/App.kt`
- Definir reglas de pertenencia por capa (qué puede depender de qué).

**Criterio de validación**
- Existe tabla/decisión clara de "archivo actual -> destino".
- No hay ambigüedad en responsabilidades de cada carpeta.

**Estado:** PENDIENTE

### 3) Crear estructura de carpetas y mover `domain` (entity/repository/usecase)

- Crear carpetas de `domain`.
- Mover entidades de negocio a `domain/entity`.
- Crear o ubicar contratos de repositorio en `domain/repository`.
- Crear base de `usecase` (aunque sea mínima, con nombres y propósito claros).

**Criterio de validación**
- Existen `domain/entity`, `domain/repository`, `domain/usecase`.
- Entidades de negocio no quedan mezcladas con UI o networking.
- Compila tras ajustar imports (si aplica en este paso).

**Estado:** PENDIENTE

### 4) Mover capa `data` (persistencia/infraestructura de datos)

- Reubicar modelos remotos, resultados de API, mappers y acceso a datos en `data`.
- Mantener interoperabilidad con `domain` vía contratos/mappers.

**Criterio de validación**
- `data` contiene solo componentes de acceso/persistencia de datos.
- No hay dependencias directas desde `domain` hacia `presentation`.
- Compilación exitosa después del ajuste de imports.

**Estado:** PENDIENTE

### 5) Mover `di` y centralizar instanciación de dependencias

- Reubicar inyector/fábrica de dependencias a `di`.
- Alinear creación de ViewModel, repositorio y servicios según responsabilidades.

**Criterio de validación**
- `di` concentra la creación/ensamblado de dependencias.
- Las pantallas no crean dependencias concretas directamente.
- Compilación exitosa.

**Estado:** PENDIENTE

### 6) Reorganizar `presentation` (`home`, `detail`, `utils`) y `App.kt`

- Mover pantallas y navegación a `presentation`.
- Ubicar utilidades composables comunes en `presentation/utils`.
- Dejar `App.kt` en `presentation`.

**Criterio de validación**
- Existen y se usan `presentation/home`, `presentation/detail`, `presentation/utils`.
- `App.kt` está en `presentation` y sigue orquestando la UI.
- Compila tras ajustes de package/imports.

**Estado:** PENDIENTE

### 7) Verificar `main.kt` en `edu.dyds.movies` y consistencia del entrypoint

- Confirmar ubicación final de `main.kt` dentro de `edu.dyds.movies`.
- Verificar que `mainClass` en Gradle apunte correctamente a `edu.dyds.movies.MainKt`.

**Criterio de validación**
- `main.kt` está en el paquete/ubicación esperada.
- Configuración de arranque consistente.
- Compilación/ejecución sin cambios funcionales observables.

**Estado:** PENDIENTE

### 8) Limpieza final de arquitectura (SOLID/Clean Code) sin cambiar comportamiento

- Eliminar acoplamientos innecesarios y ordenar responsabilidades.
- Revisar nombres, cohesión, y separación entre capas.
- Asegurar que la lógica de negocio quede en `domain` y la UI en `presentation`.

**Criterio de validación**
- No hay referencias cruzadas indebidas entre capas.
- Código más modular y legible sin alteración funcional.
- Suite de compilación/tests existente en verde.

**Estado:** PENDIENTE

## Bitácora de ejecución incremental

- Iteración actual: **Paso 1 completado**. Esperando aprobación explícita para ejecutar **solo el Paso 2**.
- Regla activa: no avanzar al siguiente paso sin confirmación explícita del usuario.


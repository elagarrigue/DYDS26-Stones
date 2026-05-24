# Plan de Resolución — Etapa 3: Cambios (Broker TMDB + OMDB)

> **Instrucciones para el agente:** Seguí este plan en orden estricto. Antes de avanzar al siguiente paso, verificá que los tests del paso actual pasen. No modifiques código que no esté indicado explícitamente.

---

## Herramientas disponibles y cuándo usarlas

| Herramienta | Cuándo usarla |
|---|---|
| `read_file` | Antes de modificar cualquier archivo — nunca edites a ciegas |
| `str_replace` / `edit_file` | Para modificaciones quirúrgicas en archivos existentes |
| `create_file` | Solo para archivos nuevos (clases, interfaces, tests) |
| `run_tests` / `bash` | Después de cada paso para verificar que los tests pasan |
| `search_in_files` / `grep` | Para encontrar todos los usos de un símbolo antes de renombrarlo |
| `list_directory` | Para entender la estructura del proyecto antes de empezar |

---

## Paso 0 — Relevamiento inicial — **Completados** ✅

**Objetivo:** Entender la estructura del proyecto antes de tocar nada.

```
ACCIONES:
1. list_directory en la raíz del proyecto
2. list_directory en src/ (o el directorio principal de código)
3. read_file de la interfaz MoviesExternalSource
4. read_file de TMDBMoviesExternalSource
5. read_file del archivo de tests existentes de MoviesExternalSource
6. read_file de la configuración de inyección de dependencias (DI container / módulo)
```

**Resultado esperado:** Mapa mental de dónde vive cada clase y qué métodos existen actualmente.

**Condición para avanzar:** Haber leído todos los archivos relevantes. No escribir código aún.

---

## Paso 1 — Reemplazar `getMovieDetails(id: Int)` por `getMovieDetails(title: String)` en `RemoteMoviesDataSource` — **Completados** ✅

**Objetivo:** Cambiar el método de búsqueda por ID a búsqueda por título en TMDB.

### 1.1 Modificar la interfaz `RemoteMoviesDataSource`

```
ACCIONES:
1. read_file `RemoteMoviesDataSource` (interfaz)
2. Reemplazar la firma:
   - ANTES:  suspend fun getMovieDetails(id: Int): Movie
   - DESPUÉS: suspend fun getMovieDetails(title: String): Movie
3. Guardar el archivo
```

### 1.2 Modificar `RemoteMoviesDataSourceImpl`

```
ACCIONES:
1. read_file `RemoteMoviesDataSourceImpl`
2. search_in_files "getMovieDetails" para encontrar todos los call sites
3. Cambiar la implementación para usar el endpoint de TMDB Search Movie:
   - Endpoint: GET /search/movie?query={title}
   - Tomar el primer resultado de la lista
   - Con el id del resultado, llamar a GET /movie/{id} para obtener el detalle completo
4. Asegurarse de que la construcción del objeto Movie siga siendo correcta
5. Guardar el archivo
```

### 1.3 Actualizar tests de `RemoteMoviesDataSource`

```
ACCIONES:
1. read_file del test de `RemoteMoviesDataSource`
2. Reemplazar todos los casos de test que usen getMovieDetails(id: Int)
   por getMovieDetails(title: String)
3. Actualizar los mocks/stubs del endpoint para que respondan al nuevo parámetro
4. run_tests — deben pasar TODOS los tests de `RemoteMoviesDataSource` antes de continuar
```

**Condición para avanzar:** `run_tests` verde en los tests de TMDB.

---

## Paso 2 — Separar `MoviesExternalSource` en dos interfaces — **Completados** ✅

**Objetivo:** Respetar el DIP — OMDB solo devuelve un resultado, no una lista, por lo que no puede implementar la misma interfaz que TMDB.

### 2.1 Crear la nueva interfaz `SingleMovieExternalSource`

```
ACCIONES:
1. create_file SingleMovieExternalSource.kt (o .java / .ts según el lenguaje del proyecto)
2. Contenido mínimo:

   interface SingleMovieExternalSource {
       suspend fun getMovieByTitle(title: String): Movie?
   }

3. Ubicarla en el mismo paquete/directorio que MoviesExternalSource
```

### 2.2 Verificar que `MoviesExternalSource` conserve sus métodos

```
ACCIONES:
1. read_file MoviesExternalSource
2. Confirmar que tiene al menos:
   - suspend fun getPopularMovies(): List<Movie>
   - suspend fun getMovieDetails(title: String): Movie?
3. Si falta alguno, agregarlo
```

### 2.3 Actualizar tests de la interfaz

```
ACCIONES:
1. read_file de los tests de MoviesExternalSource
2. Ajustar cualquier test que haga referencia a la firma anterior
3. run_tests — verde antes de continuar
```

### 2.4 Mover `RemoteMoviesDataSourceImpl` a `data/external/tmdb` y renombrarlo a `TMDBMoviesExternalSource`

```
ACCIONES:
1. read_file de `RemoteMoviesDataSourceImpl`
2. Mover el archivo a `data/external/tmdb`
3. Renombrar la clase a `TMDBMoviesExternalSource`
4. Mantener la implementación de `RemoteMoviesDataSource` sin cambiar comportamiento
5. search_in_files "RemoteMoviesDataSourceImpl" para encontrar dependencias
6. Actualizar imports, constructores e inyección de dependencias para usar `TMDBMoviesExternalSource`
7. run_tests de `desktopTest` — verde antes de continuar
```

**Condición para avanzar:** Ambas interfaces existen, los tests pasan.

---

## Paso 3 — Implementar `OMDBMoviesExternalSource`

**Objetivo:** Crear la implementación concreta que consulta la API de OMDB.

### 3.1 Crear la clase

```
ACCIONES:
1. create_file OMDBMoviesExternalSource.kt
2. Debe implementar SingleMovieExternalSource
3. Configurar el cliente HTTP:
   - Base URL: http://www.omdbapi.com
   - API Key: leer de variable de entorno OMDB_API_KEY (NO hardcodear)
   - Endpoint: GET /?t={title}&apikey={key}
4. Implementar getMovieByTitle(title: String):
   - Si la respuesta tiene "Response": "True" → mapear al modelo Movie
   - Si la respuesta tiene "Response": "False" → retornar null
5. Mapear los campos de OMDB al modelo Movie del dominio:
   - Title → title
   - Year → releaseDate (o el campo correspondiente)
   - Plot → overview
   - Poster → posterPath
   - imdbRating → voteAverage (convertir a escala correcta si es necesario)
   - (cualquier otro campo que use el modelo Movie)
```

### 3.2 Escribir los tests de `OMDBMoviesExternalSource`

```
ACCIONES:
1. create_file OMDBMoviesExternalSourceTest.kt
2. Casos obligatorios:
   a. getMovieByTitle con película existente → retorna Movie correctamente mapeada
   b. getMovieByTitle con película inexistente → retorna null
   c. Error de red / timeout → lanza excepción o retorna null (según la estrategia del proyecto)
3. Usar mocks del cliente HTTP (no hacer llamadas reales en tests unitarios)
4. run_tests — verde antes de continuar
```

**Condición para avanzar:** Tests de OMDB en verde.

---

## Paso 4 — Implementar el Broker

**Objetivo:** Crear la clase que orquesta TMDB y OMDB y expone una sola interfaz al cliente.

### 4.1 Crear la clase `MoviesBroker` (o `BrokerMoviesExternalSource`)

```
ACCIONES:
1. create_file MoviesBroker.kt
2. Debe implementar MoviesExternalSource
3. Recibir por constructor (inyección de dependencias):
   - tmdb: TMDBMoviesExternalSource (o MoviesExternalSource)
   - omdb: OMDBMoviesExternalSource (o SingleMovieExternalSource)

4. Implementar getPopularMovies():
   - Delegar directamente a tmdb.getPopularMovies()

5. Implementar getMovieDetails(title: String):
   - Llamar en paralelo (o secuencial) a:
       val tmdbResult = tmdb.getMovieDetails(title)
       val omdbResult = omdb.getMovieByTitle(title)
   - Aplicar la lógica de combinación (ver tabla abajo)
```

### Lógica de combinación de resultados

| TMDB | OMDB | Resultado |
|------|------|-----------|
| ✅ retorna | ✅ retorna | Combinar propiedades en un único objeto `Movie` |
| ✅ retorna | ❌ null | Retornar resultado TMDB con `overview += " TMDB"` |
| ❌ null | ✅ retorna | Retornar resultado OMDB con `overview += " OMDB"` |
| ❌ null | ❌ null | Retornar null / vacío |

```
NOTA: "Combinar" significa que los campos de un servicio completan los campos
vacíos/nulos del otro. Definir prioridad: TMDB tiene precedencia sobre OMDB
salvo que el campo esté vacío.
```

**Condición para avanzar:** La clase compila sin errores.

---

## Paso 5 — Tests del Broker

**Objetivo:** Cubrir los 4 casos de la lógica de combinación.

```
ACCIONES:
1. create_file MoviesBrokerTest.kt
2. Mockear tanto tmdb como omdb (no instanciar los clientes HTTP reales)
3. Escribir un test por cada caso de la tabla del Paso 4:

   CASO 1 — Ambos retornan resultado
   - Mock: tmdb.getMovieDetails("Inception") → Movie(title="Inception", overview="A dream...", ...)
   - Mock: omdb.getMovieByTitle("Inception") → Movie(title="Inception", imdbRating=8.8, ...)
   - Assert: el objeto resultante tiene campos de ambos servicios combinados

   CASO 2 — Solo TMDB retorna
   - Mock: tmdb.getMovieDetails("X") → Movie(overview="Some plot")
   - Mock: omdb.getMovieByTitle("X") → null
   - Assert: overview contiene " TMDB"

   CASO 3 — Solo OMDB retorna
   - Mock: tmdb.getMovieDetails("X") → null
   - Mock: omdb.getMovieByTitle("X") → Movie(overview="Some plot")
   - Assert: overview contiene " OMDB"

   CASO 4 — Ninguno retorna
   - Mock: tmdb.getMovieDetails("X") → null
   - Mock: omdb.getMovieByTitle("X") → null
   - Assert: resultado es null o lista vacía (según contrato)

4. run_tests — los 4 casos deben estar en verde
```

**Condición para avanzar:** Los 4 tests del Broker pasan.

---

## Paso 6 — Wiring: registrar el Broker en el contenedor de DI

**Objetivo:** Que el resto de la aplicación use el Broker en lugar de TMDB directamente.

```
ACCIONES:
1. read_file del módulo/configuración de inyección de dependencias
2. search_in_files "TMDBMoviesExternalSource" para encontrar dónde se registra
3. Reemplazar el binding de MoviesExternalSource:
   - ANTES: MoviesExternalSource → TMDBMoviesExternalSource
   - DESPUÉS: MoviesExternalSource → MoviesBroker
4. Registrar también OMDBMoviesExternalSource como dependencia del Broker
5. Asegurarse de que la API Key de OMDB se lea de variables de entorno
6. read_file del controlador/router que usa MoviesExternalSource
   - Confirmar que no importa TMDBMoviesExternalSource directamente
   - Si lo hace, reemplazar la referencia por la interfaz MoviesExternalSource
```

**Condición para avanzar:** El proyecto compila sin errores de DI.

---

## Paso 7 — Verificación final

```
ACCIONES:
1. run_tests (suite completa — todos los tests del proyecto)
2. Si algún test falla:
   a. read_file del test que falla
   b. Identificar la causa (firma cambiada, mock desactualizado, etc.)
   c. Corregir únicamente lo necesario
   d. Volver a run_tests
3. Confirmar que el diagrama de dependencias es:

   Client
     └── MoviesBroker  (implements MoviesExternalSource)
           ├── TMDBMoviesExternalSource
           └── OMDBMoviesExternalSource  (implements SingleMovieExternalSource)
```

**Condición de éxito:** 100% de tests en verde, sin warnings de compilación relacionados a los cambios realizados.

---

## Checklist final para el agente

- [ ] `MoviesExternalSource` usa `getMovieDetails(title: String)` (no `id: Int`)
- [ ] Existe la interfaz `SingleMovieExternalSource` con `getMovieByTitle(title: String)`
- [ ] `OMDBMoviesExternalSource` implementa `SingleMovieExternalSource`
- [ ] `MoviesBroker` implementa `MoviesExternalSource` e inyecta ambas fuentes
- [ ] Los 4 casos de combinación están cubiertos por tests
- [ ] El contenedor de DI apunta a `MoviesBroker`, no a `TMDBMoviesExternalSource`
- [ ] La API Key de OMDB viene de variables de entorno (`OMDB_API_KEY`)
- [ ] Todos los tests del proyecto pasan

---

> **Regla de oro:** Si en algún paso encontrás algo que no coincide con lo descripto aquí
> (nombre de clase diferente, lenguaje distinto, estructura de carpetas inesperada),
> **adaptá los nombres pero respetá la lógica**. No inventes funcionalidad extra.

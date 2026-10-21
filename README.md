# SMA8Puzzle

Sistema Multiagente en **JADE** que resuelve el **8-Puzzle**. Los agentes se comunican con mensajes **FIPA-ACL** y exploran el espacio de estados con **Greedy Best-First Search**, usando tres heurísticas distintas. Al final se comparan gráficamente.

> **Estado actual:** fase de diseño (demo). Todavía no hay código; este documento fija los acuerdos para que cada integrante pueda implementar su parte sin depender de los demás.

## Equipo
| Parte | Responsable |
|---|---|
| Agente Generador del espacio de soluciones, Agente GUI y diseño | Wilberth Mancinas |
| Agentes Solucionadores (uno por heurística) | Compañeros del equipo |

## Agentes
| Agente | Responsabilidad |
|---|---|
| **GUI** | Recibe el puzzle del usuario, se lo entrega al generador y muestra las soluciones y la gráfica comparativa. No valida el puzzle. |
| **Generador** | Evalúa si el puzzle tiene solución (paridad de inversiones) y genera los nodos siguientes de cada estado que le pidan. Registra lo explorado por cada heurística. |
| **Solucionador 1, 2, 3** | Cada uno aplica Greedy Best-First con su propia heurística: elige qué nodo expandir y reporta su resultado a la GUI. |

## Decisiones de diseño preliminares

| | **Decisión 1: Representación de estados** | **Decisión 2: Generación bajo demanda** |
|---|---|---|
| **Qué decidimos** | Cada estado es un arreglo de 9 enteros leído por filas, con `0` como espacio vacío. Ejemplo: `[1,2,3,4,0,6,7,5,8]` | El generador no envía el grafo completo: manda los nodos siguientes, el solucionador elige uno con su heurística y el generador genera los siguientes de ese nodo, y así sucesivamente |
| **Cómo funciona** | Estado → `int[9]` · Nodos siguientes → 2, 3 o 4 arreglos · Meta → `[1,2,3,4,5,6,7,8,0]` | Generador → `INFORM` (nodos siguientes) · Solucionador → `REQUEST` (nodo elegido) · Puzzle sin solución → `REFUSE` |
| **Por qué** | Viaja directo en el mensaje con `setContentObject(...)` (los `int[]` ya son serializables) sin crear clases compartidas | Solo se genera del 0.1 % al 2 % de los 181,440 estados alcanzables; además, el generador registra lo que exploró cada heurística |
| **Evita dependencias porque…** | Todos usan el mismo formato desde el inicio, sin esperar a que alguien programe una clase `Estado` | Cada quien programa y prueba su agente por separado, simulando a los demás con el Dummy Agent de JADE |
| **Alternativa descartada** | Objeto Java propio: obliga a que todos compartan exactamente la misma clase | Enviar el grafo completo: mensaje enorme, estados que nunca se usan y esperar a que termine la generación |

### Contrato de mensajes
| Momento | De → A | Performativa | Contenido |
|---|---|---|---|
| Entregar puzzle | GUI → Generador | `REQUEST` | estado inicial (`int[9]`) |
| Puzzle sin solución | Generador → GUI | `REFUSE` | `"puzzle sin solución"` |
| Puzzle aceptado | Generador → GUI | `AGREE` | — |
| Nodos siguientes | Generador → Solucionador | `INFORM` | 2, 3 o 4 estados (`int[9]` cada uno) |
| Nodo elegido | Solucionador → Generador | `REQUEST` | estado elegido (`int[9]`) |
| Fin de la búsqueda | Generador → Solucionador | `INFORM` | `"meta alcanzada"` |
| Resultado | Solucionador → GUI | `INFORM` | heurística, camino, nodos expandidos, tiempo |
| Exploración | Generador → GUI | `INFORM` | subgrafo explorado por cada heurística |

> **Importante para los solucionadores:** el nodo elegido debe ser el mejor de **toda** su lista abierta (todos los nodos recibidos y aún no expandidos), no solo de los 2–4 que acaban de llegar. Si no, el algoritmo se convierte en *hill climbing* y puede atorarse sin llegar a la meta.

## Diagrama de secuencia

```mermaid
sequenceDiagram
    actor U as Usuario
    participant GUI as Agente GUI
    participant GEN as Agente Generador
    participant S as Solucionador i<br/>(Heurística i)

    U->>GUI: Ingresa el puzzle inicial
    GUI->>GEN: REQUEST (puzzle inicial)
    Note over GEN: Evalúa si tiene solución<br/>(paridad de inversiones)

    alt Puzzle sin solución
        GEN-->>GUI: REFUSE ("puzzle sin solución")
        GUI->>U: Muestra aviso
    else Puzzle con solución
        GEN-->>GUI: AGREE
        Note over GEN,S: Se repite en paralelo para los 3 solucionadores
        GEN->>S: INFORM (nodos siguientes: 2, 3 o 4 arreglos)
        loop hasta que el nodo elegido sea la meta
            Note over S: Evalúa los nodos con su heurística
            S->>GEN: REQUEST (nodo elegido)
            Note over GEN: Genera los movimientos del nodo elegido
            GEN-->>S: INFORM (nodos siguientes)
        end
        GEN-->>S: INFORM ("meta alcanzada")
        S->>GUI: INFORM (camino, nodos expandidos, tiempo)
        GEN->>GUI: INFORM (subgrafo explorado por cada heurística)
        GUI->>U: Muestra soluciones y gráfica comparativa
    end
```

## Datos del espacio de estados
| Dato | Valor |
|---|---|
| Estados alcanzables desde la meta | 181,440 (la mitad de 9! = 362,880) |
| Movimientos del hueco | 2 en esquina · 3 en orilla · 4 en el centro |
| Solución óptima más larga | 31 movimientos |
| Distancia óptima promedio | ≈ 22 movimientos |
| ¿Tiene solución? | Sí, si el número de inversiones (sin contar el 0) es **par** |

## Cómo abrirlo
1. IntelliJ IDEA → *File → Open* → carpeta del repo.
2. JDK 21 (*File → Project Structure → SDK*).
3. JADE 4.6.0 ya viene incluido en `lib/jade.jar` como librería `jade`.
4. Ejecutar la configuración **JADE Plataforma** (`jade.Boot -gui`).

> Si sale `No ICP active`, ya hay otra plataforma JADE usando el puerto 1099: ciérrala primero.

## Solucionador 3 (esta rama)

Una carpeta por agente, cada una con su percepción, razonamiento y actuación:

| Carpeta | Agente | Nombre en JADE |
|---|---|---|
| `src/AgenteEuclidiano/` | Solucionador con Greedy Best-First y heurística **euclidiana** | `solucionador3` |
| `src/GUISolucionador/` | GUI que anima la solución del solucionador 3 | `gui3` |

- El solucionador reporta su resultado a **`gui`** (la GUI principal, que compara a los 3) **y** a **`gui3`** (que anima la solución).
- El tiempo reportado es **tiempo de procesador**: suma solo lo que tarda cada turno del solucionador, sin la espera de red ni los `println`. Se envía con decimales (`tiempoMs=37.354`).
- La terminal muestra pocas líneas: los 3 primeros nodos elegidos, uno cada 100 y el resumen al llegar a la meta.
- `gui3` recibe el puzzle del generador al presionar **Resolver** y anima el camino a 0.25 s por movimiento.
- `lib/` trae JavaFX 21.0.2 para Windows (`-win`) y Linux (`-linux`).

### Ejecutar en red
Configuración **JADE Red (contenedor)** (arranca primero la computadora principal):
```
-container -host 10.21.26.144 -port 1099 -local-host 10.21.40.37 -local-port 1100 -agents "solucionador3:AgenteEuclidiano.AgenteEuclidiano;gui3:GUISolucionador.AgenteGUISolucionador"
```
Si cambian las IPs, actualizar `-host` (principal) y `-local-host` (esta computadora).

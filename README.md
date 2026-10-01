# PEPSE — Procedural 2D World

A Java exploration game that builds a block-based landscape as the player moves.
The world combines noise-generated terrain, trees with animated leaves, collectible
fruit, and a day–night cycle. A following camera keeps the avatar in view while
nearby world chunks are created and distant chunks are removed.

Developed by **Yonatan Algamil** for Exercise 4 in the Object-Oriented Programming
course at the Hebrew University of Jerusalem. This repository preserves the
submitted gameplay implementation and presents it in a standalone project layout.

## Features

- **Procedural terrain:** a height function backed by `NoiseGenerator` creates
  columns of 30 × 30 blocks, with 20 blocks of depth per column.
- **Chunk-based horizontal exploration:** terrain and vegetation are generated
  in 600-pixel chunks; distant chunks and their game objects are unloaded.
- **Reproducible world layout:** a fixed seed reproduces terrain heights and
  tree/leaf/fruit placement when a range is generated again. Decorative color
  variations use a separate, unseeded random generator.
- **Animated avatar:** separate idle, running, and airborne sprite animations,
  with horizontal image flipping according to movement direction.
- **Energy-based movement:** standing still on the ground restores energy;
  ground running and jumps consume it. The energy display stays fixed on screen.
- **Conditional double jump:** one extra airborne jump is available while
  falling, provided the avatar has enough energy.
- **Trees and fruit:** trunks are solid, leaves do not block the avatar, and
  fruit restores 10 energy, disappears on collection, and respawns after 30 seconds
  while its chunk remains loaded.
- **Animated environment:** leaves sway and change width; the sun and its halo
  move through a 30-second cycle while a translucent night overlay darkens the scene.

## Controls

| Input | Action |
| --- | --- |
| Left arrow / Right arrow | Move horizontally |
| Space | Jump from the ground, spending 20 energy |
| Release Space, then press it while falling | Perform one additional jump, spending 50 energy |
| Release the movement keys while on the ground | Restore energy |
| Escape | Exit the window using the engine's default exit control |

Holding Space does not repeatedly trigger jumps. Energy stays in the range 0–100.
Ground running costs 2 energy per update and standing still restores 1 per update;
these rates follow the original frame-based implementation. Airborne horizontal
movement does not incur the running cost.

## Architecture

`PepseGameManager` is the composition point: it creates the world factories,
avatar, camera, UI, environmental objects, and collision-layer configuration.
It also owns a `Map<Integer, List<GameObject>>` that tracks each loaded chunk.

| Package / class | Responsibility |
| --- | --- |
| `pepse.PepseGameManager` | Entry point, scene initialization, object layers, chunk loading and unloading |
| `pepse.world.Terrain` / `Block` | Ground height calculation, terrain columns, and immovable collision blocks |
| `pepse.world.Sky` | Background that stays in camera coordinates |
| `pepse.world.avatar.Avatar` | Input, velocity, jumping rules, energy, and movement state |
| `AvatarState` / `AvatarAnimation` | `IDLE`, `RUN`, and `JUMP` states; loading and switching sprite animations |
| `EnergyDisplay` | Screen-fixed text updated through an energy callback |
| `pepse.world.trees.Flora` | Tree, leaf, and fruit generation using a supplied ground-height function |
| `Fruit` | Avatar collision, energy reward, hiding, and timed respawning |
| `pepse.world.daynight` | Sun orbit, sun halo, and night-opacity transitions |
| `pepse.utils` | Terrain noise and color variation helpers |

Terrain and trunk blocks use the static layer, leaves use a separate nonblocking
layer, and fruit shares the avatar's default layer. Background and foreground
layers arrange the sky, sun, halo, and night effect; the energy text uses the UI layer.

## OOP and design concepts

- **Inheritance and polymorphism:** `PepseGameManager` extends the engine's
  `GameManager`; `Avatar`, `Block`, `EnergyDisplay`, and `Fruit` specialize
  `GameObject`. Overridden update and collision hooks provide object-specific behavior.
- **Encapsulation:** avatar energy and jump flags are private; energy changes pass
  through a clamping method and public interaction methods.
- **Separation of responsibilities:** avatar behavior, animation, and energy UI
  are separate classes; terrain and vegetation generation are separate factories.
- **Composition and delegation:** the avatar owns an `AvatarAnimation`; the manager
  coordinates factories and game objects rather than implementing all their behavior.
- **Dependency injection through functions:** `Flora` accepts a
  `Function<Float, Float>` and receives `terrain::groundHeightAt`, so it does not
  need a direct dependency on `Terrain`.
- **Callbacks:** a `Consumer<Integer>` connects energy changes to
  `energyDisplay::updateEnergy` without making `Avatar` depend on the display class.
- **Lambdas and method references:** scheduled fruit respawns, leaf movement,
  sun transitions, and halo-following behavior use the engine's callback/component APIs.
- **Explicit state representation:** an enum names the avatar's movement states;
  helper methods enforce the transitions and movement rules.

## Project structure

```text
pepse/
├── assets/                         # 14 avatar animation frames
├── lib/
│   ├── DanoGameLab.jar             # Course game engine dependency
│   └── README.md                   # Dependency information
├── scripts/
│   ├── build.ps1                   # Windows PowerShell build
│   ├── run.ps1                     # Windows PowerShell build and run
│   ├── build.sh                    # Bash build
│   └── run.sh                      # Bash build and run
├── src/
│   └── pepse/
│       ├── PepseGameManager.java
│       ├── utils/
│       │   ├── ColorSupplier.java
│       │   └── NoiseGenerator.java
│       └── world/
│           ├── Block.java
│           ├── Sky.java
│           ├── Terrain.java
│           ├── avatar/
│           │   ├── Avatar.java
│           │   ├── AvatarAnimation.java
│           │   ├── AvatarState.java
│           │   └── EnergyDisplay.java
│           ├── daynight/
│           │   ├── Night.java
│           │   ├── Sun.java
│           │   └── SunHalo.java
│           └── trees/
│               ├── Flora.java
│               └── Fruit.java
├── .gitattributes
├── .gitignore
└── README.md
```

Build output is created locally under `build/` and ignored by Git.

## Technologies

- Java; build target: **Java 17**
- DanoGameLab: external course game engine for the loop, objects, rendering,
  input, collisions, camera, components, scheduled tasks, and transitions
- Java AWT types for colors and key codes
- Java Collections, functional interfaces, lambdas, and method references
- PNG sprite assets
- JDK command-line tools, Bash, and Windows PowerShell; no Maven or Gradle required

## Build and run

### Prerequisites

1. Install **JDK 17** and ensure `java` and `javac` are on `PATH`.
2. Clone this repository.
3. Use a desktop environment to open the game window.

The compatible **`DanoGameLab.jar`** used by this project is included under
`lib/`, so no additional game-engine dependency needs to be downloaded manually.

Run commands from the repository root. The scripts also select this directory as
the working directory so the existing `assets/...` image paths resolve correctly.

### Windows PowerShell

Build and launch:

```powershell
.\scripts\run.ps1
```

Build without opening the game:

```powershell
.\scripts\build.ps1
```

If PowerShell blocks script execution, run this command for that invocation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run.ps1
```

To use an existing JAR outside the repository instead of copying it into `lib/`:

```powershell
$env:DANOGL_JAR = 'C:\path\to\DanoGameLab.jar'
.\scripts\run.ps1
```

### Linux / macOS / Bash

Build and launch:

```bash
bash scripts/run.sh
```

Build without opening the game:

```bash
bash scripts/build.sh
```

Use an external local JAR, if preferred:

```bash
export DANOGL_JAR='/absolute/path/to/DanoGameLab.jar'
bash scripts/run.sh
```

### Manual commands

After building, the game can also be launched directly from the repository root:

```powershell
# Windows PowerShell
java -cp 'build/classes;lib/DanoGameLab.jar' pepse.PepseGameManager
```

```bash
# Linux / macOS
java -cp 'build/classes:lib/DanoGameLab.jar' pepse.PepseGameManager
```

For a manual compilation, create `build/classes` and a file named
`build/sources.txt` containing one source path per line, then run:

```text
javac --release 17 -encoding UTF-8 -cp lib/DanoGameLab.jar -d build/classes "@build/sources.txt"
```

### IntelliJ IDEA

1. Open the `pepse` directory and select JDK 17 as the project SDK.
2. Mark `src/` as **Sources Root**.
3. Add `lib/DanoGameLab.jar` as a module library under
   **File → Project Structure → Modules → Dependencies**.
4. Run `pepse.PepseGameManager.main` with the working directory set to the
   repository root, where `assets/` is located.

IntelliJ configuration remains local and is ignored by Git.

### Troubleshooting

| Symptom | Check |
| --- | --- |
| `Missing DanoGameLab.jar` | Place the dependency in `lib/`, or set `DANOGL_JAR` to its absolute path |
| `javac` is not recognized / not found | Install a full JDK and check `PATH`; a runtime alone is insufficient |
| `package danogl does not exist` | Include the compatible engine JAR on the compiler classpath |
| `Could not find or load main class` | Build first and include both `build/classes` and the JAR in the runtime classpath |
| Images cannot be loaded | Run from the repository root, or use the provided run scripts |
| `HeadlessException` | Launch from a graphical desktop rather than a headless server |

## Implementation scope

This is an exploration simulation. Chunk unloading retains the procedural recipe,
not persistent gameplay state: recreated fruit starts uneaten, and visual effects
restart. Energy recovery and ground-running cost are frame-based, and the current
ground check uses zero vertical velocity. These original implementation choices
are preserved in this repository.

## Verification

The 15 source files compile with Java 17 against the supplied course JAR. Their
package declarations match the `src/` directory layout. Source files and assets
are unchanged, and the compiled classes match the original build byte for byte.

Packaging checks exercised the Bash build and the ignore rules. Temporary checks
using the actual game classes and engine JAR verified sprite loading, reproducible
terrain/vegetation geometry, avatar input and energy rules, energy notifications,
fruit collection and timed respawning, chunk unloading, and offscreen rendering
of sprites and environmental objects. This temporary harness is not a committed
test suite.

The graphical window/bootstrap and text UI updating/rendering could not be tested
in this headless environment. The PowerShell scripts were reviewed but require
verification on Windows; a full interactive desktop run remains to be performed.

## Credits and external dependency

**This work was made using the DanoGameLab library by Dan Nirel.**

DanoGameLab is an external course dependency and is not part of this project's
source code. The compiled `DanoGameLab.jar` required to build and run the project
is included in `lib/` with redistribution permission. No DanoGameLab source code,
examples, or generated engine documentation are included.

The original `ColorSupplier` author attribution is retained in its source file.
The project's helper code and sprite assets are preserved as supplied; this
repository does not add a blanket license for third-party material.

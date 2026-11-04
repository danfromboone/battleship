# Classic Battleship Console Application

A classic, console-based **Battleship** game built in **Java** strictly adhering to **Domain-Driven Design (DDD)** principles and Hexagonal / Layered Architecture.

---

## 🎯 Features

- **Classic Rules & Fleet**:
  - Standard 10×10 ocean grid (columns `A`–`J`, rows `1`–`10`).
  - Standard 5-ship fleet:
    - **Carrier** (size 5)
    - **Battleship** (size 4)
    - **Cruiser** (size 3)
    - **Submarine** (size 3)
    - **Destroyer** (size 2)
  - Horizontal & Vertical placement.
  - Sinking announcements: *"BOOM! You sank the Battleship!"*
- **Game Modes**:
  1. **Single Player (Human vs. Smart Computer)**: Play against a tactical AI.
  2. **Two Players (Local Hotseat)**: Pass-and-play with screen clearing to protect hidden fleets.
  3. **AI vs AI Simulation**: Watch two autonomous AI commanders battle in real time.
- **Smart AI Strategy**:
  - Implements **Hunt-and-Target** mode.
  - In **Hunt Mode**: Uses parity/checkerboard targeting (targeting cells where `(row + col) % 2 == 0`) since ships are length $\ge 2$, pruning moves that cannot fit valid ships.
  - In **Target Mode**: When an enemy ship is hit, it targets adjacent orthogonal neighbors. When 2+ hits form a line, it identifies collinear endpoints until the ship is destroyed.
- **Console UI & Rendering**:
  - Side-by-side presentation of **Your Fleet (Defense)** and **Radar Tracking (Offense)**.
  - ANSI color-coded grid cells (`~` Water, `S` Ship, `X` Hit, `O` Miss, `#` Sunk).
  - Real-time fleet health bars showing intact segments vs damages for each ship type.
  - Robust coordinate input parsing (`B4`, `b4`, `4B`, `J10`) with bounds and duplicate shot checking.
  - Battle statistics (turn count, hit count, accuracy percentage).

---

## 📐 Domain-Driven Design (DDD) Architecture

The application strictly separates domain logic from application workflow, infrastructure, and presentation:

```
com.battleship
├── domain                              # Pure business logic & invariants
│   ├── model
│   │   ├── coordinate
│   │   │   ├── Coordinate              # Value Object (row, col, parsing, validation, neighbors)
│   │   │   ├── GridDimension           # Value Object (rows, cols)
│   │   │   └── Orientation             # Enum (HORIZONTAL, VERTICAL)
│   │   ├── ship
│   │   │   ├── ShipType                # Enum (Carrier, Battleship, Cruiser, Submarine, Destroyer)
│   │   │   ├── Ship                    # Entity (tracks occupied cells, hits, sunk status)
│   │   │   ├── ShipPlacement           # Value Object (start coordinate, orientation, segment list)
│   │   │   └── Fleet                   # Aggregate / Entity (validates fleet completeness & no-overlaps)
│   │   ├── board
│   │   │   ├── CellStatus              # Enum (WATER, SHIP, HIT, MISS, SUNK)
│   │   │   ├── ShotStatus              # Enum (MISS, HIT, SUNK)
│   │   │   ├── ShotResult              # Value Object (outcome, hit/sunk details, messaging)
│   │   │   ├── OceanBoard              # Entity (manages placements and incoming fire)
│   │   │   └── TargetBoard             # Entity (tracks outgoing shots against enemy)
│   │   ├── player
│   │   │   ├── PlayerId                # Value Object
│   │   │   ├── PlayerType              # Enum (HUMAN, COMPUTER)
│   │   │   └── Player                  # Entity
│   │   └── game
│   │       ├── GameId                  # Value Object
│   │       ├── GameState               # Enum (PLACING_SHIPS, IN_PROGRESS, FINISHED)
│   │       ├── TurnRecord              # Value Object (history entry)
│   │       └── Game                    # AGGREGATE ROOT (enforces state, turns, victory)
│   ├── event                           # Domain Events
│   │   ├── DomainEvent                 # Interface
│   │   ├── DomainEventPublisher        # In-process publisher
│   │   ├── ShipPlacedEvent             # Event
│   │   ├── ShotFiredEvent              # Event
│   │   ├── ShipHitEvent                # Event
│   │   ├── ShipSunkEvent               # Event
│   │   └── GameOverEvent               # Event
│   ├── policy                          # Domain Services
│   │   └── FleetPlacementService       # Placement algorithm ensuring valid random deployments
│   └── exception                       # Explicit Domain Exceptions
│       ├── BattleshipException
│       ├── InvalidCoordinateException
│       ├── OutOfBoundsException
│       ├── ShipOverlapException
│       ├── CellAlreadyAttackedException
│       └── InvalidGameStateException
├── application                         # Application Use Cases & Orchestration
│   ├── BattleshipApplicationService    # Orchestrates commands to domain aggregates
│   └── command                         # Commands (CreateGameCommand, PlaceShipCommand, FireShotCommand)
├── infrastructure                      # Technical implementations & adapters
│   ├── ai
│   │   ├── AiStrategy                  # Port / Interface
│   │   └── HuntAndTargetAiStrategy     # Parity & Adjacency AI strategy
│   └── persistence
│       ├── GameRepository              # Repository Port
│       └── InMemoryGameRepository      # In-Memory Storage Adapter
└── presentation                        # User Interface Adapters
    ├── console                         # Console Presentation Layer
    │   ├── AnsiColor                   # ANSI color styling
    │   ├── ConsoleRenderer             # Side-by-side grid & status renderer
    │   └── ConsoleGameController       # Interactive menu & game loop
    └── swing                           # Swing GUI Presentation Layer
        ├── SwingTheme                  # Dark naval palette, fonts, styled widgets
        ├── BoardGridPanel              # 10x10 custom-rendered grid with radar & hover previews
        ├── FleetStatusPanel            # Live fleet health bars & damaged segment dashboard
        ├── CombatLogPanel              # Real-time event log with hit/miss/sunk styling
        ├── MainMenuPanel               # Mission type selection, briefing, player setup
        ├── PlacementViewPanel          # Interactive fleet deployment (hover, 'R' rotate, auto-deploy)
        ├── BattleViewPanel             # Dual-board battle center (Radar offense vs Fleet defense)
        ├── HotseatPassDialog           # Security shielding dialog for 2-player local play
        ├── GameOverDialog              # Victory fanfare, battle statistics, accuracy analysis
        └── BattleshipSwingApp          # Main JFrame window & lifecycle orchestrator
```

### DDD Tactical Patterns Employed:
1. **Aggregates & Aggregate Roots**:
   - `Game` is the aggregate root that governs match lifecycle, active turns, firing authorization, victory conditions, and state transitions.
   - `Fleet` encapsulates ship collection integrity, preventing overlapping placements and verifying complete fleets.
2. **Value Objects**:
   - `Coordinate`, `GridDimension`, `Orientation`, `ShipPlacement`, `ShotResult`, `PlayerId`, `GameId`, `TurnRecord` are immutable and have value-based equality and self-validation.
3. **Domain Events**:
   - `ShotFiredEvent`, `ShipHitEvent`, `ShipSunkEvent`, `GameOverEvent`, and `ShipPlacedEvent` decouple game events from notifications and external listeners.
4. **Domain Services**:
   - `FleetPlacementService` encapsulates algorithm-heavy placement logic without bloating aggregate boundaries.
5. **Repositories (Ports & Adapters)**:
   - `GameRepository` separates persistence concerns from the domain layer.

---

## 🚀 Getting Started

### Prerequisites
- Java 21+ (Java 27 tested and supported)

### Running the Application

**Swing Graphical Interface (Default on systems with display)**:
```bash
# Launches the Swing GUI
./run.sh

# Or directly via Gradle
./gradlew run

# Or launch BattleshipSwingApp directly
./gradlew run --args="--gui"
```

**Console Terminal Interface**:
```bash
# Run in console mode
./gradlew run --args="--console"
```

Or running the pre-built distribution binary:
```bash
./gradlew installDist
./build/install/battleship/bin/battleship
```

### Windows Portable Distribution (.zip)

Build a self-contained Windows package:

```bash
# Using Gradle
./gradlew packageWindows

# Or using the package script directly
./package-windows.sh
```

The resulting archive is generated in:
- `build/distributions/Battleship-1.0.0-Windows.zip`
- Staged folder: `build/windows/Battleship/`

**To run on Windows:**
1. Extract `Battleship-1.0.0-Windows.zip`.
2. Double-click `Battleship.vbs` (or `Battleship.bat`) to launch the Swing GUI.
3. *(Optional)* Run `CreateShortcut.vbs` to automatically add a shortcut with the Battleship icon to your Desktop.
4. Or run `Battleship-Console.bat` for terminal play.

The Windows distribution includes:
- `Battleship.vbs`: Silent windowless launcher that executes `javaw` without a command prompt popup.
- `Battleship.bat`: Batch launcher with automatic Java 21+ detection and download guidance.
- `CreateShortcut.vbs`: VBScript that creates a Desktop shortcut configured with `icon/battleship.ico`.
- `installer.nsi` & `setup.iss`: Installer build scripts for NSIS and Inno Setup to create `Battleship-Setup.exe`.
- Multi-resolution Windows icon (`battleship.ico` containing 16×16 to 256×256 icon resources).

### macOS Application Bundle (.app & .zip)

Build a native macOS application bundle (`Battleship.app`) and distribution archive:

```bash
# Using Gradle
./gradlew packageMac

# Or using the package script directly
./package-macos.sh
```

The resulting archives are generated in:
- `build/distributions/Battleship-1.0.0-macOS.zip`
- `build/distributions/Battleship-1.0.0-macOS.tar.gz`
- Staged bundle: `build/macos/Battleship.app`

**To run on macOS:**
1. Extract `Battleship-1.0.0-macOS.zip`
2. Move `Battleship.app` to `/Applications` (or keep in Downloads/Desktop)
3. Double-click `Battleship.app` to launch!
   *(Or launch from Terminal with: `open Battleship.app`)*

The macOS bundle features:
- Retina multi-resolution Apple icon (`battleship.icns` supporting 16×16 to 1024×1024)
- Native macOS menu bar and dock integration (`-Dapple.laf.useScreenMenuBar=true`, `-Xdock:name="Battleship"`)
- Smart Java 21+ detection (Homebrew, `java_home`, Adoptium) with a native macOS graphical error prompt if a compatible JRE is missing.

### Debian Package (.deb)

Build a Debian package for Ubuntu, Debian, Linux Mint, and derivatives:

```bash
# Using Gradle
./gradlew packageDeb

# Or using the package script directly
./package-deb.sh
```

The resulting package will be in `build/distributions/battleship_1.0.0_all.deb`.

Install it system-wide:
```bash
sudo apt install ./build/distributions/battleship_1.0.0_all.deb
```

This installs:
- `/usr/bin/battleship` command in your `$PATH`
- Desktop entry in application launchers (`/usr/share/applications/battleship.desktop`)
- Scalable SVG icon (`/usr/share/icons/hicolor/scalable/apps/battleship.svg`)
- Documentation in `/usr/share/doc/battleship/`

### Running Tests
Execute the unit test suite:
```bash
./gradlew test
```
All unit tests cover coordinates, ships, fleet validation, ocean boards, game lifecycle, AI targeting, and application services.

---

## 🎮 How to Play

1. Start the game and select **1** for Single Player (or **2** for Two Player).
2. Choose between **Automatic Placement** (instant setup) or **Manual Placement** (placing each of the 5 ships by entering coordinates like `B3 H` or `E5 V`).
3. Take turns entering coordinates to fire upon the opponent (e.g., `B4`, `J10`).
4. Watch the side-by-side radar and status screens to track hits and sink all 5 enemy vessels!

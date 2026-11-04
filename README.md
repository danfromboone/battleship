# Fleet Strike - Tactical Naval Warfare

**Fleet Strike** is a modern, tactical naval combat simulation game built in **Java 21** strictly adhering to **Domain-Driven Design (DDD)** principles and Hexagonal / Layered Architecture.

Inspired by the classic World War I paper-and-pencil game *Salvo* and tactical naval strategy, Fleet Strike features both a rich **Swing GUI presentation layer** and a full-featured **ANSI console terminal interface**, cross-platform packaging (Debian, RPM, macOS, Windows), and an intelligent **Hunt-and-Target AI**.

---

## 🎯 Features

- **Classic Naval Rules & Fleet**:
  - Standard 10×10 ocean grid (columns `A`–`J`, rows `1`–`10`).
  - Standard 5-ship fleet:
    - **Carrier** (size 5)
    - **Battleship** (size 4)
    - **Cruiser** (size 3)
    - **Submarine** (size 3)
    - **Destroyer** (size 2)
  - Horizontal & Vertical placement with live hover collision detection.
  - Sinking announcements: *"BOOM! Direct hit! Cruiser has been sunk!"*
- **Game Modes**:
  1. **Single Player (Human vs. Smart Computer)**: Engage a tactical AI using hunt-and-target probability grids.
  2. **Two Players (Local Hotseat)**: Pass-and-play with screen shielding to protect confidential fleet formations.
  3. **AI vs AI Simulation**: Watch two autonomous AI commanders battle in real time with speed control and turn stepping.
- **Smart AI Strategy**:
  - Implements **Hunt-and-Target** mode.
  - In **Hunt Mode**: Uses parity/checkerboard targeting (targeting cells where `(row + col) % 2 == 0`) since ships are length $\ge 2$, pruning moves that cannot fit valid ships.
  - In **Target Mode**: When an enemy ship is hit, it targets adjacent orthogonal neighbors. When 2+ hits form a line, it identifies collinear endpoints until the ship is destroyed.
- **Swing Graphical Presentation Layer**:
  - Deep tactical dark naval aesthetic.
  - Side-by-side **Target Radar (Offense)** and **Your Fleet (Defense)** grids.
  - Live damage segment health bars for each warship class.
  - Real-time combat log tracking shots, hits, and sinkings.
  - Pass-and-play turn privacy shields.
  - Post-game victory fanfare with detailed accuracy and shot statistics.
- **Console UI & Rendering**:
  - Side-by-side terminal presentation of Defense and Offense grids.
  - ANSI color-coded grid cells (`~` Water, `S` Ship, `X` Hit, `O` Miss, `#` Sunk).
  - Robust coordinate input parsing (`B4`, `b4`, `4B`, `J10`).

---

## 📐 Domain-Driven Design (DDD) Architecture

The application strictly separates domain logic from application workflow, infrastructure, and presentation:

```
com.fleetstrike
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
│       ├── FleetStrikeException
│       ├── InvalidCoordinateException
│       ├── OutOfBoundsException
│       ├── ShipOverlapException
│       ├── CellAlreadyAttackedException
│       └── InvalidGameStateException
├── application                         # Application Use Cases & Orchestration
│   ├── FleetStrikeApplicationService   # Orchestrates commands to domain aggregates
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
        └── FleetStrikeApp              # Main JFrame window & lifecycle orchestrator
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21+

### Running the Application

**Swing Graphical Interface (Default on systems with display)**:
```bash
# Launches the Swing GUI
./run.sh

# Or directly via Gradle
./gradlew run

# Or launch FleetStrikeApp directly
./gradlew run --args="--gui"
```

**Console Terminal Interface**:
```bash
# Run in console mode
./gradlew run --args="--console"
```

---

## 📦 Cross-Platform Packages

### Debian / Ubuntu / Linux Mint (.deb)
```bash
./gradlew packageDeb
# or ./package-deb.sh
```
Output: `build/distributions/fleetstrike_1.0.0_all.deb`
```bash
sudo apt install ./build/distributions/fleetstrike_1.0.0_all.deb
```

### Red Hat / Fedora / CentOS / openSUSE (.rpm)
```bash
./gradlew packageRpm
# or ./package-rpm.sh
```
Output: `build/distributions/fleetstrike-1.0.0-1.noarch.rpm`
```bash
# Fedora / RHEL
sudo dnf install ./build/distributions/fleetstrike-1.0.0-1.noarch.rpm

# openSUSE
sudo zypper install ./build/distributions/fleetstrike-1.0.0-1.noarch.rpm
```

### macOS Application Bundle (.app & .zip)
```bash
./gradlew packageMac
# or ./package-macos.sh
```
Output: `build/distributions/FleetStrike-1.0.0-macOS.zip` (contains `FleetStrike.app`).
Extract and double-click `FleetStrike.app` to launch.

### Windows Portable Distribution (.zip)
```bash
./gradlew packageWindows
# or ./package-windows.sh
```
Output: `build/distributions/FleetStrike-1.0.0-Windows.zip`.
Extract and double-click `FleetStrike.vbs` (or `FleetStrike.bat`) to launch.

---

## 🧪 Running Tests

Execute the unit test suite:
```bash
./gradlew test
```
All unit tests cover coordinates, ships, fleet validation, ocean boards, game lifecycle, AI targeting, application services, and Swing presentation panels.

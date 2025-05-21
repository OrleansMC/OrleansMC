# OrleansMC

OrleansMC is a Minecraft Paper plugin developed in Java, originally created for the OrleansMC server network. The plugin
provides a comprehensive set of features for managing player data, server state, cross-server communication, custom join
messages, and more. This repository is being released for transparency and community use following the shutdown of the
original OrleansMC server.

> **Note:** This project was not originally designed as an open source project. As such, it may lack full modularity,
> polish, or comprehensive documentation. Some features may be incomplete or tightly coupled to the original OrleansMC
> infrastructure.

---

## Table of Contents

- [Project Purpose and Scope](#project-purpose-and-scope)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Development Notes](#development-notes)
- [Limitations](#limitations)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## Project Purpose and Scope

OrleansMC was developed to power the OrleansMC Minecraft server, providing advanced player management, server state
synchronization, and cross-server features. The plugin is now open-sourced to allow the community to learn from, adapt,
or extend its functionality for their own server networks.

The scope of the project includes:

- Player data management (alerts, invites, custom join messages/sounds, recent deaths)
- Server state tracking and communication via Redis
- Discord webhook integration for player events and server actions
- Cross-server teleportation and server switching
- Integration with external plugins (e.g., HuskHomes, PlaceholderAPI, LuckPerms)
- Administrative commands for server operators

---

## Key Features

- **Player Data Management:** Stores and manages player-specific data such as alerts, invites, and custom join settings
  in MongoDB.
- **Server State Synchronization:** Uses Redis to broadcast and synchronize server state and player lists across
  multiple servers.
- **Cross-Server Teleportation:** Supports teleporting players between servers using HuskHomes API.
- **Custom Join Messages and Sounds:** Allows players (with permission) to set personalized join messages and sounds.
- **Discord Webhook Integration:** Sends notifications to Discord for player join/leave events and administrative
  actions.
- **Administrative Tools:** Includes commands for managing player data and server state.
- **Extensible Event System:** Utilizes Bukkit and Velocity event systems for extensibility.

---

## Technologies Used

- **Java 17+**
- **Paper API 1.21.1**
- **Velocity API (for proxy support)**
- **Redis (via Jedis)**
- **MongoDB (via Lucko's Helper library)**
- **HuskHomes API** (for teleportation)
- **LuckPerms** (for permissions and chat prefixes)
- **PlaceholderAPI** (for dynamic placeholders)
- **Discord Webhooks**

---

## Installation

1. **Requirements:**
    - Minecraft server running [Paper 1.21.1](https://papermc.io/downloads)
    - Java 17 or newer
    - Redis server (for cross-server features)
    - MongoDB server (for player data)
    - [Lucko's Helper library](https://github.com/lucko/helper) (for MongoDB integration)
    - [HuskHomes](https://github.com/WiIIiam278/HuskHomes) (for teleportation)
    - [LuckPerms](https://luckperms.net/)
      and [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (for permissions and placeholders)

2. **Build the Plugin:**
    - Clone this repository.
    - Run `./gradlew build` to generate the plugin JAR.
    - The compiled JAR will be located in the `build/libs` directory.

3. **Install the Plugin:**
    - Place the JAR file into your server's `plugins` directory.
    - Ensure all required dependencies are also installed.

4. **Start the Server:**
    - Start or restart your Paper server to load the plugin.

---

## Usage

- The plugin will automatically create a default configuration file (`config.yml`) in the plugin's data folder on first
  run.
- Administrative commands are available for managing player data and server state. See in-game `/help` or the source
  code for available commands.
- Players with the appropriate permissions can set custom join messages and sounds using `/join-message` and
  `/join-sound`.

---

## Configuration

The main configuration file is `config.yml`. Key settings include:

- **Proxy Mode:** Enable if running in a Velocity proxy environment.
- **Server Identity:** Set `server-name`, `server-type`, and `server-ip` to uniquely identify each server instance.
- **Redis Settings:** Configure `redis-host`, `redis-port`, and `redis-password` for Redis connectivity.
- **MongoDB Settings:** Managed via Lucko's Helper library; ensure the MongoDB provider is available.
- **Discord Webhooks:** Set webhook URLs for player join/leave and server actions.

Example `config.yml`:

```yaml
proxy: false
server-name: "my-server-name"
server-type: "REALMS_SPAWN"
server-ip: "192.168.1.10"
redis-host: "redis"
redis-port: 6379
redis-password: ""
server-state-interval: 1
server-state-timeout: 3
in-out-webhook-url: "https://discord.com/api/webhooks/..."
actions-webhook-url: "https://discord.com/api/webhooks/..."
```

---

## Development Notes

- **Original Design:** The codebase was not originally intended for open source release. Some parts may be tightly
  coupled, lack modularity, or have minimal documentation.
- **Extending Functionality:** Contributions are welcome, but please be aware of the architectural limitations.
- **Code Quality:** Some features may be incomplete or require refactoring for broader use.
- **Dependencies:** The project relies on several external plugins and services. Ensure all dependencies are installed
  and configured.

---

## Limitations

- **Not Fully Modular:** Some features may be hardcoded or not easily configurable for other server networks.
- **Documentation:** Inline code documentation may be sparse or missing.
- **Feature Completeness:** Certain features may be incomplete or specific to the original OrleansMC server.
- **No Official Support:** This project is provided as-is, with no guarantee of support or updates.

---

## License

Unless otherwise specified, this project is licensed under the Apache License 2.0.  
See the [`LICENSE`](./LICENSE.txt) file for details.

## Acknowledgements

- [Lucko's Helper](https://github.com/lucko/helper)
- [HuskHomes](https://github.com/WiIIiam278/HuskHomes)
- [LuckPerms](https://luckperms.net/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
- The original OrleansMC community

---

For questions, issues, or contributions, please use the GitHub Issues and Pull Requests features.

---
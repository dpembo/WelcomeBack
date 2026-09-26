# WelcomeBack

A Spigot/Paper plugin that lets players welcome newly-joined (or returning)
players with a chat command, optionally rewarding both sides with
console-run commands.

This is [dpembo](https://github.com/dpembo)'s updated fork of
[Hadimhz/WelcomeBack](https://github.com/Hadimhz/WelcomeBack)
([SpigotMC resource page](https://www.spigotmc.org/resources/welcome-back-%E2%9C%85-increase-retention-%E2%9C%A8player-rewards.112720/)).
The plugin's behavior and config format are unchanged from upstream; this
fork replaces internal debug/error output with proper logging and simplifies
the config-loading code. See [Changes in this fork](#changes-in-this-fork)
below for specifics.

## How it works

1. When a player joins, `PlayerListener` records the join and starts a
   welcome window that lasts `duration` seconds (config, default `15`).
2. If the player isn't vanished, and hasn't rejoined within
   `onlyWelcomeAfterXDelay` seconds of their last quit, the plugin broadcasts
   `onPlayerJoin` to every other online player.
3. Any other online player can then run `/welcome` (alias `/wb`) during that
   window to welcome them. Running it:
   - Fails with `welcomePartyExpired` if the window has closed, or the
     joining player has since gone offline.
   - Fails with `welcomeYourselfError` if you try to welcome yourself.
   - Fails with `alreadyWelcomed` if you've already welcomed this player
     during the current window.
   - Otherwise picks a random line from `firstJoin` (if the player has never
     played on the server before) or `joinBack` (if they have), sends it as
     if the welcoming player typed it in chat, and runs the console commands
     in `CommandsToExecuteOnFirstJoin` or `commandsToExecute` respectively
     (typically used to hand out an economy reward — see
     [Configuration](#configuration)).
   - `displayFirstXWelcomeMessages` caps how many of those chat messages get
     shown per join window; further welcomes within the same window still
     register (and still run the reward commands) but stay silent once the
     cap is hit. `-1` means no cap.
4. Each welcome is tracked per player-UUID for that join window, so the same
   player can't be welcomed twice by the same person before the window
   resets on their next join.

There's no permission node — any player can run `/welcome`/`/wb`.

## Configuration

Config is stored as JSON (not YAML) at `plugins/WelcomeBack/config.json`,
created with these defaults on first run:

| Key | Type | Default | Meaning |
|---|---|---|---|
| `duration` | int (seconds) | `15` | How long the welcome window stays open after a player joins. |
| `onlyWelcomeAfterXDelay` | int (seconds) | `30` | If a player rejoins within this many seconds of their last quit, the join broadcast/welcome window is skipped entirely (stops reconnect spam). `-1` disables this check. |
| `displayFirstXWelcomeMessages` | int | `-1` | Caps how many welcome chat messages are shown per join window. `-1` = unlimited. |
| `onPlayerJoin` | string | `"&7A player has joined run the command &d/wb &7to welcome them!"` | Broadcast to all other online players when the window opens. Supports `&`-color codes and `%player%`. |
| `welcomePartyExpired` | string | `"That welcome party has expired!"` | Sent to a player running `/welcome` after the window has closed. |
| `welcomeYourselfError` | string | `"You cannot welcome yourself!"` | Sent if a player tries to welcome themselves. |
| `alreadyWelcomed` | string | `"You've already welcomed this player!"` | Sent if the player already welcomed this join. |
| `firstJoin` | list\<string\> | `["Welcome %player%! Hope you enjoy your stay"]` | Chosen at random and sent as the welcoming player's chat message, for a player's first-ever join. Supports `%player%`. |
| `joinBack` | list\<string\> | `["Welcome back %player%!"]` | Same, for a returning player. |
| `CommandsToExecuteOnFirstJoin` | list\<string\> | `["eco give %player% 100"]` | Console commands run (as the server console) when a first-time joiner is welcomed. Supports `%player%`. |
| `commandsToExecute` | list\<string\> | `["eco give %player% 50"]` | Console commands run when a returning player is welcomed. Supports `%player%`. |

Editing `config.json` while the server is stopped (or running `/reload`, if
you use one) picks up changes on next load. Any key you remove from the
file reverts to its code-defined default rather than an empty/null value —
see [Changes in this fork](#changes-in-this-fork).

Color codes use the standard `&` format (e.g. `&7` grey, `&d` pink);
`%player%` is replaced with the relevant player's name wherever it appears
above.

## Commands

| Command | Aliases | Who | Description |
|---|---|---|---|
| `/welcome` | `/wb` | Any player | Welcome the most recently joined player, per the rules above. |

## Building

```
./gradlew build
```

Produces a shaded jar (via the `shadow` plugin) under `build/libs/`. Built
against `spigot-api 1.16.1-R0.1-SNAPSHOT` (`api-version: 1.13` in
`plugin.yml`), so it should run on that version and reasonably close
neighbours; it hasn't been verified against newer Paper/Spigot releases.

> **Note:** `build.gradle.kts` also declares
> `implementation(files("./libs/config.jar"))`, but the config classes
> (`ConfigRegistry`, `JsonConfigRegistry`, etc.) already live under
> `src/main/java/.../util/config/` in this repo, and `libs/config.jar` isn't
> checked in. This looks like a leftover from before those classes were
> vendored into the source tree rather than something introduced by this
> fork — flagging it here in case a clean checkout fails to build for you.

## Changes in this fork

Compared to upstream `Hadimhz/WelcomeBack`, this fork:

- **Replaced debug `System.out.println` calls** in `PlayerListener` with
  proper logging via the plugin's own `java.util.logging.Logger`
  (`plugin.getLogger()`), at `FINE` level so they're silent unless the
  server's logging is turned up.
- **Replaced `e.printStackTrace()` calls** in the config-loading code
  (`ConfigRegistry`, `AbstractConfigRegistry`, `JsonConfigRegistry`) with
  logger calls at `SEVERE`/`WARNING`, so failures show up in the server log
  with context instead of a bare, unlabelled stack trace on stdout.
- **Replaced manual try/finally + `addSuppressed` boilerplate** in
  `JsonConfigRegistry` (the pattern `javac` generates when desugaring
  try-with-resources, complete with synthetic `var5`/`var7`/`var8`/`var9`
  names) with actual `try-with-resources` syntax — same behavior, about
  half the code.
- **Simplified config loading**: `AbstractConfigRegistry#register()` used to
  load the file into a throwaway object and then manually copy each field
  onto the caller's instance via reflection. Since the config classes have
  an accessible no-arg constructor, Gson already runs it (and the field
  initializers) when deserializing, so any key missing from the JSON file
  already keeps its code-defined default — the manual copy was redundant,
  and less correct than Gson (it missed `static`/`volatile` fields and
  wouldn't reach fields on a superclass). `register()` now just uses
  whatever Gson loaded, falling back to the default instance if the file
  didn't parse. The `reload()` method this replaced has been removed from
  `ConfigRegistry` (it had no other callers). Config file contents and
  defaults are unaffected.

None of these changes alter the plugin's external behavior — the config
format, commands, messages, and reward logic all work exactly as in
upstream.
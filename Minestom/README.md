# Easyconomy - Minestom

This is the Minestom port of the Easyconomy plugin for Bukkit.
It is a standalone application and currently does not need any other extension as a dependency.

## Features
 * Easy-to use economy API
 * Thread-safe default economy implementation
 * Provides `/baltop` and `/bal` as commands.

## Dependencies / Requirements
 * Java 11 or above (which you'd need anyways)
 * A Minestom server
   - Implementing Minestom API ed46bd0dc2 or later
   - UUIDs HAVE to be consistent with in the implementation in order for saving to work properly.
 * No further dependencies outside of those provided by the extension.json

## API-Usage
As of now the Easyconomy-Minestom artifact needs to be used, the economy can be obtained via `EasyconomyAdvanced.getEconomy()` as of now, but we'll likely change this so you'll only have to use the Easyconomy-API artifact when the time comes for API usage, however we are waiting for Minestom to add the required objects to be able to use that (such as a ServiceProvider or similar).

## Support
There currently is no particular discord support server, however feel free to DM me at `tristellar#9022` via discord.

## Compiling
The project can be easily compiled using `mvn install` at the project root, however your IDE might automise that. Additionally you'll need to have the Easyconomy-API artifact built.

## Contributing
Everyone is welcome to contribute to the repository by creating issues or by creating Pull Requests. Every help that the project gets makes it more stable for the future.

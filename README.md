# EasyconomyAdvanced

## About
EasyconomyAdvanced is a fork of the Easyconomy plugin that adds a lot of obscure features like
 * Bleeding edge V2 reading compliance for the binary player storage format (for Minestom it's even write compliance)
 * Concurrency safety
 * Backups are asynchronous
 * Allow to run command prompt commands when doing backups (allows to compress, ship to offsite, etc them)
 * Banks are now implemented
 * Force-uses banks for invalid playernames (-> sort-of fixed unsighly baltops)
 * Bundling FIO operations
 * No static abuse
 * **Updated to Java 11**
 * Minestom support (Still WIP though)
  - Cross-plattform API

## Features
The main policy of the plugin is that it only includes core economy features.
* Easy setup (Drag plugin in and reload)
* Perfect for first-time users
* Easy To Use - Has all features a regular economy plugin has
  - /money
  - /eco give|take|set|backup
  - /pay
  - /baltop
  - /givemoney
  - /takemoney
* All messages customizable
* All settings customizable

## Why NOT use the fork?
* It makes use of Java 11 - if you don't have it it won't run.
* This allows for unsafe commandline operations - don't give anyone write acess to the config file!

## Dependencies
* Bukkit
   - 1.16.4 or above
   - The [Vault Plugin](https://dev.bukkit.org/projects/vault) implementing vault-api 1.7 or beyond
* Minestom
   - A more recent version
* Java
   - 11 or higher. This is meant to be used for serious production use, if you are still running Java 8 you are out of luck

## Support
There currently is no particular discord support server, however feel free to DM me at `tristellar#9022` via discord.

## Compiling
The project can be easily compiled using `mvn install` at the project root, however your IDE might automise that. Additionally if you only need to make use of the bukkit implementation you can remove the Minestom folder and vice-versa

## Contributing
Everyone is welcome to contribute to the repository by creating issues or by creating Pull Requests. Every help that the project gets makes it more stable for the future.

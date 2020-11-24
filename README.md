# EasyConomyAdvanced

## About
This is a production-grade for of the EasyConomy plugin that has more features than the parent project as well as having a no-tolerance bug policy, if you find a bug report it so it can be dealt with accordingly.
It was originally developed for the `mc.geolykt.de` server but now also contains features meant for the general user, however due to multiple reasons is incompatible with the parent plugin if you are using it's yaml storage format. (It can be converted rather easily though - so if you want to switch - just ask me).

## Features
The main policy of the plugin is that it only includes core economy features.
* Easy setup (Drag plugin in and reload)
* Easy To Use (/money, /eco give/take/set/backup, /pay & /baltop)
* Perfect for first-time users
* All messages customizable
* All settings customizable

The goal is to take the plugin and implementing more advaced options.
Our changes:
* Bleeding-edge V2 reading compliance for the binary storage format
* Concurrency-safe
* Backups are asynchronous
* Allow to run command prompt commands when doing backups (allows to compress, ship to offsite, etc them)
* Banks are implemented
* Force-uses banks for invalid playernames (-> sort-of fixed unsighly baltops)
* Bundling FIO operations
* No static abuse
* Updated to Java 11

## Why NOT use the fork?
* It makes use of Java 11 - if you don't have it it won't run.
* This allows for unsafe commandline operations - don't give anyone write acess to the config file!

## Dependencies
* A Bukkit/Spigot/Paper server with 1.16 or above
* The [Vault Plugin](https://dev.bukkit.org/projects/vault) implementing vault-api 1.7 or beyond (Basically a bridge between plugins)
* Java 11 or higher. This is meant to be used for serious production use, if you are still running Java 8 you are out of luck

## Support
There currently is no particular discord support server, however feel free to DM me at `tristellar#9022` via discord.

## Getting the plugin
You can only compile the plugin via `gradlew build` as of now, however once the plugin is an acceptably finished state it will be released on other sources.

## Contributing
Everyone is welcome to contribute to the repository by creating issues or by creating Pull Requests. Every help that the project gets makes it more stable for the future.

This branch is the jre8 compatible adaption of what is used within the geolykt-prod branch.
It includes several changes to optimize the plugin, remove static abuse and to make it more compatible with other plugins. In short it adds more compact storage files and bank support.
However due to this, this is not a drop-in replacement for the upstream plugin and cannot be used interchangeably.

# EasyConomy
EasyConomy is an economy plugin that integrates into Vault and focusses about a small selection of core features.

## Features
* Easy setup (Drag plugin in and reload)
* Easy To Use (/money, /eco give/take/set, /pay AND THAT'S IT)
* Perfect for first-time users
* All messages customizable
* All settings customizable

## Dependencies
* A Bukkit/Spigot/Paper server with 1.16 or above
* The [Vault Plugin](https://dev.bukkit.org/projects/vault) implementing vault-api 1.7 or beyond (Basically a bridge between plugins)
* Java 8 or higher. If you are using Java 11 we recommend checking out geolykt-prod instead.

## Support
There currently is no particular discord support server, however feel free to DM me at `tristellar#9022` via discord.

## Getting the plugin
You can only compile the plugin via `gradlew build` as of now, however once the plugin is an acceptably finished state it will be released on other sources.

## Contributing
Everyone is welcome to contribute to the repository by creating issues or by creating Pull Requests. Every help that the project gets makes it more stable for the future.

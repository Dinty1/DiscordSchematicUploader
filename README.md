[![Discord](https://discord.com/api/guilds/830198696204566607/widget.png)](https://dinty1.github.io/plugins-discord)
[![Latest release](https://img.shields.io/github/release/Dinty1/DiscordSchematicUploader.svg)](https://github.com/Dinty1/DiscordSchematicUploader/releases/latest)
[![GitHub downloads](https://img.shields.io/github/downloads/Dinty1/DiscordSchematicUploader/total.svg)](https://github.com/Dinty1/DiscordSchematicUploader/releases/latest)
[![License](https://img.shields.io/github/license/Dinty1/DiscordSchematicUploader.svg)](https://github.com/Dinty1/DiscordSchematicUploader/blob/master/LICENSE)
[![](https://img.shields.io/bstats/servers/11934)](https://bstats.org/plugin/bukkit/DiscordSchematicUploader/11934)
[![](https://img.shields.io/bstats/players/11934)](https://bstats.org/plugin/bukkit/DiscordSchematicUploader/11934)
[![CodeFactor](https://www.codefactor.io/repository/github/dinty1/discordschematicuploader/badge)](https://www.codefactor.io/repository/github/dinty1/discordschematicuploader)

DiscordSchematicUploader allows users to upload and download WorldEdit schematics from Discord using DiscordSRV.

## Installing
DiscordSchematicUploader requires WorldEdit (or one of its forks) and DiscordSRV to function. You will need these before going any further.

Installation itself is easy, simply drag and drop the plugin into your plugins folder. Once you have done this, restart your server.

The plugin should create a config.yml file which you can edit to change some settings and messages for the plugin. One of the first things you will need to do is add to the list of allowed roles for the upload and download commands. The comments and examples in the config will explain how to do this.

## Commands
`!upload [-o]` with an attached file: Upload a schematic to your server. Use the `-o` flag to overwrite an existing schematic.

![](https://i.imgur.com/bF6vVcI.png)

`!download <name>`: Download a schematic from the server.

![](https://i.imgur.com/hJ9GoD4.png)

## Schematic Upload Channels
This plugin allows you to designate channels where any schematic file that is uploaded will be automatically processed, regardless of whether a command is run.

![image](https://user-images.githubusercontent.com/67452089/147857856-9e1c01af-9f89-4dec-bcf7-fa1d721fe6e2.png)

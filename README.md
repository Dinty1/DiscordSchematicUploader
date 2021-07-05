[![Discord](https://discord.com/api/guilds/830198696204566607/widget.png)](https://dinty1.github.io/plugins-discord)
[![Latest release](https://img.shields.io/github/release/Dinty1/DiscordSchematicUploader.svg)](https://github.com/Dinty1/DiscordSchematicUploader/releases/latest)
[![GitHub downloads](https://img.shields.io/github/downloads/Dinty1/DiscordSchematicUploader/total.svg)](https://github.com/Dinty1/DiscordSchematicUploader/releases/latest)
[![License](https://img.shields.io/github/license/Dinty1/DiscordSchematicUploader.svg)](https://github.com/Dinty1/DiscordSchematicUploader/blob/master/LICENSE)

DiscordSchematicUploader allows users to upload and download WorldEdit schematics from Discord using DiscordSRV.

## Installing
DiscordSchematicUploader requires WorldEdit and DiscordSRV to function. You will need these before going any further.

Installation itself is simple, simply drag and drop the plugin into your plugins folder. Once you have done this, restart your server.

The plugin should create a config.yml file which you can edit to change some settings about the plugin. One of the first things you will need to do is add to the list of allowed roles for the upload and download commands. The comments and examples in the config will explain how to do this.

## Commands
`!upload` with an attached file: Upload a schematic to your server.

![](https://i.imgur.com/bF6vVcI.png)

`!download <name>`: Download a schematic from the server.

![](https://i.imgur.com/hJ9GoD4.png)

Note: These commands will only work in channels that are included in DiscordSRV's `Channels` option.

# BlueMap Custom Skin Provider

[![GitHub Total Downloads](https://img.shields.io/github/downloads/TechnicJelle/BlueMapCustomSkinProvider/total?color=success&label=Downloads "Click here to download the plugin")](https://github.com/TechnicJelle/BlueMapCustomSkinProvider/releases/latest)
[![Servers using this plugin](https://img.shields.io/bstats/servers/18368?label=Servers)](https://bstats.org/plugin/bukkit/BlueMap%20Custom%20Skin%20Provider/18368)

Little Minecraft Paper plugin for the people who want to use a custom skin server for [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap).

Compatible with Paper 1.13+, just like BlueMap itself.

SkinsRestorerAPI V15 is supported if using that integration.

To reload this plugin, just reload BlueMap itself with `/bluemap reload`.

## Config
There's just one config option, the URL of the skin server:
```yaml
# Define as a URL that is reachable as the server, use placeholders to get player specific information.
# Available placeholders: {UUID} and {USERNAME}
# Set to "skinsrestorer" to enable skinrestorerAPI V15 support

url: "https://minotar.net/skin/{UUID}"
#url: skinsrestorer
```

## [Click here to download!](../../releases/latest)

## Support

To get support with this plugin, join the [BlueMap Discord server](https://bluecolo.red/map-discord)
and ask your questions in [#3rd-party-support](https://discord.com/channels/665868367416131594/863844716047106068). You're welcome to ping me, @TechnicJelle.

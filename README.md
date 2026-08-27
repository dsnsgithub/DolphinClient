# Dolphin Client

Dolphin Optimized is an **insanely** optimized Minecraft client/mod collection for Hypixel **1.8.9**. 

Download [Dolphin Optimized Pink.zip](https://github.com/dsnsgithub/AxolotlClient-Fork/releases), then drag it into Prism Launcher to run.

<img width="2560" height="1440" alt="2026-08-26_15 20 15" src="https://github.com/user-attachments/assets/6bfbac4f-85e6-42da-98d7-ef623f73ca2e" />

Open the client menu in-game with **Right Shift**.
<img width="1548" height="938" alt="image" src="https://github.com/user-attachments/assets/b525cfba-501c-45a5-9a09-98d920e68135" />

### Performance
When I say **insanely optimized**, I mean it. The combination of backporting modern optimizations + the relatively lightweight 1.8 Minecraft version means it **smokes** every other popular 1.8.9 client.
<img width="2560" height="1440" alt="2026-08-26_18 26 41" src="https://github.com/user-attachments/assets/dca9b006-ef06-4f96-8f7d-8ea288e372dd" />

Tested Dolphin Optimized on a Ryzen 7 5800x, RTX 3070:
- 800 FPS on Lunar Client => ~3000 fps in Housing (/visit DSNS)
- ~200 FPS on Lunar Client => ~600 fps in Duels Lobby
- ~200 FPS on Lunar Client => ~1000 fps in Housing lobby

(0.1% lows increase proportionally, detailed measurements coming soon)

## Credits

Dolphin Client is a fork of [AxolotlClient](https://codeberg.org/AxolotlClient/AxolotlClient-mod) (with rewritten configuration + tweaks), while Dolphin Optimized also includes a potent combination of optimization mods.

For those nerdy and curious to care, Dolphin Optimized is built on:

- [Argentum](https://github.com/rhysdh540/argentum) (Sodium for 1.8.9)
- [Argentum Extras](https://github.com/rhysdh540/argentum) (Sodium Tweaks for 1.8.9)
- [Async Logger](https://modrinth.com/mod/asynclogger) (improved Minecraft logging)
- [Cera](https://github.com/rhysdh540/argentum) (OptiFine compatability)
- DolphinClient (Lunar Client HUD replacement: Hypixel mods, clean GUI, 1.7 Animations, and so much more)
- [Legacy LWJGL3](https://modrinth.com/mod/moehreag-legacy-lwjgl3) (upgraded from LWJGL2 to modern LWJGL3, performance boost and compatability)
- [NettyFix](https://github.com/moehreag/nettyfix) (allow multiplayer connection with newest Java)
- [Ornithe](https://ornithemc.net/) + [Ornithe Standard Libraries](https://modrinth.com/mod/osl) (required for Ornithe, which is backported Fabric for 1.8.9)
- [SoundFix](https://modrinth.com/mod/soundfix) (brings new sound system to 1.8.9)
- [Zirconium](https://github.com/Coccocoahelper/Zirconium-Ornithe) (brings modern optimizations to 1.8.9)


---

<details>

<summary>
Licensing (from AxolotlClient)
</summary>

This mod is licensed under the LGPL-3.0 License.
For more information see the [LICENSE](LICENSE) file.

```
AxolotlClient-mod
Copyright (C) 2021-present moehreag + Contributors

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
```
</details>

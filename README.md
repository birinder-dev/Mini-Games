<p align="center">
  <img src="art/banner.png" alt="Mini-Games: Tavern Gambling & 3D Cards" width="100%" />
</p>

<p align="center">
  <strong>Wager your emeralds, play with cards like never before, and test your luck against the Gambler.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg" alt="Minecraft 1.21.1" />
  <img src="https://img.shields.io/badge/Fabric-Loader%200.16%2B-blue.svg" alt="Fabric Loader" />
  <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java 21" />
</p>

---

## Quick Start & Testing

To test the mod immediately inside the prepared showcase room:

```powershell
git clone https://github.com/birinder-dev/Mini-Games.git
cd Mini-Games
.\gradlew runClient --args="--quickPlaySingleplayer DemoWorld"
```

If you are already in an existing world, execute `/function tavern:setup` to instantly construct the room, spawn the Gambler NPC, and populate the betting barrels.

To test normally without presets in regular survival or creative worlds, place down a Gambling Table block, set two Gambling Stools adjacent to the table facing directly across from one another, spawn a Gambler nearby, and sit on the opposite stool with emeralds in hand.

> **Performance Note**: Running the game through the development client (`runClient`) incurs heavy background debugging overhead that consumes extra system resources for lower visual performance. For actual survival playthroughs, using the compiled `.jar` file placed in your regular `.minecraft/mods` directory is strongly recommended.

---

## Overview

Mini-Games introduces an authentic tavern card gambling experience into Minecraft Fabric 1.21.1. Pull up a stool, wager emeralds on an interactive table against the Gambler NPC, and handle 3D cards that can be fanned in hand, placed flat onto surfaces, or flung across the room.

<p align="center">
  <img src="art/player-with-gambler.png" alt="Player with Gambler at Table" width="85%" />
  <br>
  <em>The Gambler seated at the table ready for a match.</em>
</p>

---

# Tavern Gambling Table

### Gambler & Furniture

The Gambler is a custom librarian villager variant programmed with autonomous seating AI that actively searches for nearby stools. For a match to function, the player must sit on the stool directly opposite to the Gambler's stool, with both stools placed immediately adjacent to the Gambling Table block.

<p align="center">
  <img src="art/area_render_1.png" alt="Gambler and Furniture Area Render" width="75%" />
  <br>
  <em>Stool alignment: two adjacent stools positioned directly across from each other.</em>
</p>

### Playing Mechanism & Table HUD

The gambling minigame operates through three dedicated screen tabs: Rules, Advanced Configuration, and the Game Table itself.

#### 1. Rules

Matches are contested through sequential card reveals against the Gambler. Each round, both participants place one card on the felt. Every suit carries an explicit win condition, with Sword cards taking absolute priority over Shield cards regardless of numeric value. When both cards are Swords, the higher number cuts down the opponent and takes the point.

<p align="center">
  <img src="art/rules-page1.png" alt="Rules Tab Page 1" width="85%" />
  <br>
  <em>Rules Page 1: Suit priority hierarchy and Sword combat resolution.</em>
</p>

When both played cards are Shields, the rule inverts: the lowest numeric value parries the heavier strike and wins the round. If both participants reveal cards with the exact same suit and identical numeric value, the round results in a tie and both sides draw replacement cards.

<p align="center">
  <img src="art/rules-page2.png" alt="Rules Tab Page 2" width="85%" />
  <br>
  <em>Rules Page 2: Shield parry mechanics and tie-break rules.</em>
</p>

#### 2. Advanced Settings

The Advanced tab configures match parameters before wagering. Players can adjust the match length between 3, 5, 7, or 9 rounds, adjust the Gambler's AI difficulty between Easy, Medium, and Hard, and select the emerald wager. The emerald block texture on this slider was directly inspired by SevTech: Ages.

<p align="center">
  <img src="art/advanced.png" alt="Advanced Settings Tab" width="85%" />
  <br>
  <em>Advanced menu featuring the SevTech-inspired emerald wagering meter.</em>
</p>

#### 3. Game Table

The primary Game tab provides a full view of your current deck, the Gambler's remaining cards, and the confirmed wager amount before the first card is laid down.

<p align="center">
  <img src="art/game-table.png" alt="Game Table Screen" width="85%" />
  <br>
  <em>Game tab displaying live hand count and match stakes.</em>
</p>

#### 4. In-Game HUD & Table Deck Pile

Once a match begins, an in-game HUD displays real-time round counts, your current score against the Gambler, and the active emerald wager. While outside of an active game, players can right-click the deck pile resting on top of the gambling table to retrieve fresh cards into their inventory. Winning a match awards double the wagered emeralds (`2 * bet`) directly into your inventory, while losing forfeits the pot to the Gambler.

<p align="center">
  <img src="art/ingame-hud.png" alt="In-Game HUD and Table Deck" width="85%" />
  <br>
  <em>Real-time match HUD tracking round progress and pot size.</em>
</p>

---

# Throwable Cards

Holding Tavern Cards in your inventory while outside of a table game and pressing Shift + Right-Click transitions you into the card state, dynamically fanning out your deck in full 3D perspective across your hand. Pressing Shift + Right-Click again exits the state and returns your hotbar to normal.

<p align="center">
  <img src="art/card-fanned.png" alt="3D Cards Fanned in Hand" width="75%" />
  <br>
  <em>3D deck fanning toggled via Shift + Right-Click.</em>
</p>

While inside the card state, holding Right-Click charges up throw velocity before releasing a high-speed 3D projectile into the air. Note that thrown cards currently do not deal damage.

<p align="center">
  <img src="art/card-thrown.png" alt="Card Thrown Projectile" width="75%" />
  <br>
  <em>Charging and launching cards as projectile entities.</em>
</p>

Tapping Right-Click directly onto any block face places the card flat against the surface as a decorative item on floors, walls, or tables.

<p align="center">
  <img src="art/card-on-ground.png" alt="Cards Placed Flat on Ground" width="75%" />
  <br>
  <em>Cards placed flat against world surfaces with a single right-click tap.</em>
</p>

---

# Server Config Features

Server administrators can inspect and modify runtime configurations in-game or through the console using `/minigames` or `/minigames help`.

The base command `/minigames config` supports the following management subcommands:

`/minigames get [option]` checks the current runtime value of any setting.

`/minigames set [option] [val]` changes configuration values on the fly without requiring a server reboot.

`/minigames reload` reloads all values from the configuration file on disk.

`/minigames reset` restores all configuration options back to factory defaults.

The available configuration options are:

`despawn <seconds>` sets the time before placed or thrown cards despawn, or -1 to never despawn.

`damage <amount>` configures the damage dealt by thrown card projectiles.

`pickup <true/false>` toggles whether thrown cards can be retrieved from blocks.

`gambler <true/false>` enables or disables Gambler NPC spawning behavior.

`crafting <true/false>` enables or disables survival recipes for tables, stools, and cards.

`maxcards <1-9>` limits the maximum cards allowed per hand in gambling matches.

<p align="center">
  <img src="art/server-config1.png" alt="Server Config Command Output" width="80%" />
  <br>
  <em>In-game output listing active server settings.</em>
</p>

<p align="center">
  <img src="art/server-config2.png" alt="Server Config Value Modification" width="80%" />
  <br>
  <em>Modifying server configuration parameters via chat commands.</em>
</p>

---

# Creative Tab Addition

A dedicated Mini Games tab has been registered to the creative menu on Page 1 (slot 0) for rapid access to all mod items, furniture blocks, and spawn eggs, represented by a custom 3D Tavern Card icon.

<p align="center">
  <img src="art/creative-tab-minigames.png" alt="Mini Games Creative Tab" width="80%" />
  <br>
  <em>The dedicated Mini Games creative tab positioned on Page 1.</em>
</p>

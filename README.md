# eTamagotchi
## Author
Maverick Peppers "TheMaverickProgrammer"

## License
MIT commercial free use - open source means you can take, edit, and contribute

## Language
java version "1.8.0_131"
Java(TM) SE Runtime Environment (build 1.8.0_131-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.131-b11, mixed mode)

## Compiling
`javac eTamagotchi.java`

`java eTamagotchi`

Doesn't get any simpler than that

## Description
Inspired by Digimon "Digivices" tamagotchis circa 1990's. A good foundation for a more advanced virtual tamagotchi or "learning" material.

# Features
* A random digimon tile is selected and random stats are generated.

* eTamagotchi runs as both a host (timeout=60 seconds) and a client depending on the menu item selected.
![menu](./screens/menu.png)

* The digimon moves side to side when healthy (HP > 1)
![after-battle](./screens/after-battle.png)

* P2P battles.
![P2P](./screens/P2P.png)

* Battles are computed similarly to the originals: one of the devices
decides who would win before the effects took place on screen. The battles were simulated.
For eTamagotchi, the Host requests stat data from the Client and simulates
a battle by reducing each monsters stats by eachother's attack power at the same time.
Whoever has the most before K.O. wins. The client is updated with HP from the resulting battle.
![combat](./screens/combat.png)

* View stats from toolbar
![stats](./screens/stats.png)

# Room For Improvement
Lots of it. I hacked this together in 8 hours.

## Code Improvement
* The logic is in the main thread which happens to be the render loop. Bad.
Decouple the rendering from the main thread.

* Create a Monster class that represents the stats, tileID, and pass that back
and forth the BattleThread and the Render instances instead of how I'm doing it.
Divide the BattleThread into two different classes HostBattleThread and ClientBattleThread.

* Too many battle flags managing the thread states... speaking of threads...

* Unsure of the thread saftey. Each time a host or client is established a new thread is made. This can't be good.

* Creating new graphics each render call in P2P for opponent has to go

## Gameplay Improvement
* Read from a source the correct digimon names, stats, etc...

* Save the tamagotchi state data to a file and load

* Reward battling monsters with EXP points (e.g. winners +3 EXP, losers +1 EXP)

* Decrease health after X hours to reinforce feeding

* Add mood (hidden) stat

* 💩

* Evolution trees

* Animations

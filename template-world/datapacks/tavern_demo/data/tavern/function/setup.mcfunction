gamerule doMobSpawning false
gamerule doDaylightCycle false
gamerule doWeatherCycle false
time set noon
difficulty easy
setworldspawn -4 101 1

kill @e[type=minigames:gambler]
kill @e[type=minigames:seat]
kill @e[x=-9,y=99,z=-6,dx=18,dy=8,dz=12,type=!player]
tag @a remove tavern_setup

fill -8 100 -5 8 105 5 spruce_planks outline
fill -7 101 -4 7 104 4 air
fill -7 100 -4 7 100 4 dark_oak_planks
fill -7 105 -4 7 105 4 dark_oak_planks
fill 0 101 -4 0 104 -4 stripped_dark_oak_log
fill 0 101 4 0 104 4 stripped_dark_oak_log
fill 0 104 -3 0 104 3 stripped_dark_oak_log

setblock -4 104 0 lantern[hanging=true]
setblock -4 104 -3 lantern[hanging=true]
setblock -4 104 3 lantern[hanging=true]
setblock 4 104 0 lantern[hanging=true]
setblock 4 104 -3 lantern[hanging=true]
setblock 4 104 3 lantern[hanging=true]

setblock -4 101 0 minigames:gambling_table[facing=north]
setblock -4 101 -1 minigames:gambling_stool
setblock -4 101 1 minigames:gambling_stool

summon minigames:gambler -4 101.5 -1

setblock -6 101 0 barrel[facing=east]{Items:[{Slot:0b,id:"minecraft:emerald",count:64},{Slot:1b,id:"minecraft:emerald",count:64},{Slot:2b,id:"minigames:tavern_card",count:9}]}

setblock -7 102 1 oak_wall_sign[facing=east]{front_text:{color:"gold",has_glowing_text:1b,messages:['{"text":"[GAMBLING TABLE]","bold":true}','{"text":"1. Sit on stool"}','{"text":"2. Click table"}','{"text":"3. Set bet & Play"}']}}
setblock -7 102 -1 oak_wall_sign[facing=east]{front_text:{color:"green",has_glowing_text:1b,messages:['{"text":"[BETTING POT]","bold":true}','{"text":"Take emeralds"}','{"text":"from the barrel"}','{"text":"to wager bets"}']}}

setblock 6 101 0 stripped_spruce_log
setblock 6 102 0 target
summon armor_stand 5.5 101 0 {NoBasePlate:1b,ShowArms:1b,ArmorItems:[{},{},{},{id:"minecraft:zombie_head",count:1}]}
setblock 6 101 -2 barrel[facing=west]{Items:[{Slot:0b,id:"minigames:tavern_card",count:9},{Slot:1b,id:"minigames:tavern_card",count:9},{Slot:2b,id:"minigames:tavern_card",count:9}]}

setblock 7 103 0 oak_wall_sign[facing=west]{front_text:{color:"yellow",has_glowing_text:1b,messages:['{"text":"[CARD THROW]","bold":true}','{"text":"Right-click card"}','{"text":"Throws card"}','{"text":"as projectile!"}']}}
setblock 7 103 -2 oak_wall_sign[facing=west]{front_text:{color:"aqua",has_glowing_text:1b,messages:['{"text":"[CARD FANNING]","bold":true}','{"text":"Shift + Right-click"}','{"text":"Fans out deck"}','{"text":"in hand"}']}}
setblock 7 103 2 oak_wall_sign[facing=west]{front_text:{color:"light_purple",has_glowing_text:1b,messages:['{"text":"[TIE RULE]","bold":true}','{"text":"Same suit & val"}','{"text":"Ties the round"}','{"text":"Draw replacement!"}']}}

execute as @a run function tavern:player_setup

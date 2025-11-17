--------
Preface:
--------
Adding support for RTSAssist currently requires a very basic understanding of java and the StarSector API.
Using the ship testing tools does not.

******************
  RTSAssist API:
******************

* Include RTSAssist JAR as source in intellij: Project Structure/library. 
* add >import API.RTS_API;<
* See zzzTestMod(zzzTestMod\src\data\scripts\testModBasePlugin.java) for an example on how to use the API.
* zzzTestMod is a standalone mod and can be pasted into the mods folder and will provide a working example.

***********************
  Ship Testing Tools:
***********************

* A set of tools for testing ships.
* Use either LunaLib or the RTSAssist config file to enable this feature.
		- Set enableShipTestSuite to true.
		- modID, if left blank, will test vanilla ships, else set to the ID, found in modinfo.json, of the mod you want to test.
		- findAllShips: By default RTSAssist only list ships with systems that dont exist in vanilla SS. Set to true to find all ships.
		- Fighters are not listed.
* Keys:
		- M -> Set selected ship as test ship. If no ship is selected, reverts back to default.
		- N -> Spawn test ship.
		- H -> Remove selected ships.
		- J -> print ship details to the console.
		- K -> spawn next ship from base list.
		- Y -> Add ship to test list.
		- U -> Remove ship from test list.
		- I -> spawn next ship from test list.
* All ships added to the test list will be removed from the base list.
* If a test ship is selected, RTSAssist will attempt to replace the player ship with the test ship on the first frame of combat.
* Selected test ship and test list persist between game restarts.
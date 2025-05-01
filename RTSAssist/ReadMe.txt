Mod thread: https://fractalsoftworks.com/forum/index.php?topic=32007.0

-----------------
  Installation:
-----------------
This is installed the same way as a normal mod. Simply extract the zip into
your mods folder and tag it in the launcher.

------------
  Removal:
------------
Removing/untagging this mod WILL BREAK YOUR SAVE. However, in the config file it can be
set to be disabled which will(should...) restore vanilla gameplay as if the mod were not
installed/tagged.

-------------
  Updating:
-------------
Just delete the old version and copy over the new one. Your stored ship arrangemnts
will be lost.


*****************
  Introduction:
*****************

Quickstart: It plays just like an RTS. Drag select and give ships orders. Cancel
previous orders with the X hotkey. Hold Alt to see all orders. Enjoy!!
Use Capslock to toggle between vanilla and RTS style gameplay.

RTSAssist is a mod that gives some assistance in controlling ships during battle.
Ships can be moved to a position, ordered to attack an enemy ship etc.

This can be considered a feature overhaul in that the player loses control of a single ship
and instead receives extended command over the entire fleet as well as the camera.

It does not change or modify the AI but rather restricts it. For example: A ship
is ordered to hold position. The AI is still in full control except that it must stay
at the position that its order commands. What it chooses to attack, when it uses its
systems, how it raises it's shields etc., are all the same, with some exceptions.
When not given an order ships will behave exactly as they do in the vanilla game.

In order to make some ships more wieldy, some ships will have there system use either
disabled or there usecases modified WHEN following a command.

Hotkeys can be changed in the hotkeys.ini file found in this folder.

Finally, I hope you enjoy it. It has been a project of mine for a long, long time. If
you are having trouble micromanaging ships remember the AI is fully capable without you
and sometimes just a little influence makes all the difference.

IMPORTANT: During the first frame of combat, combat is paused. Ships can be arranged
before the battle starts similar to Total War games. During this time ships must be given
commands on an individual basis.


**********
  Usage:
**********

(Some) Hotkeys can be changed in Hotkeys.ini

------------
** SELECT **
------------
LEFT CLICK/ LEFT CLICK and DRAG:
	Select one or more ships.

-----------------------------
** ADD/REMOVE TO SELECTION **
-----------------------------
HOLD SHIFT and LEFT CLICK/ LEFT CLICK and DRAG:
	Add (select unselected ships) or remove (select selected ships) ships from the current selection.

----------------------
** MOVE AND RELEASE **
----------------------
RIGHT CLICK on open space:
	Move selected ships to the clicked position and then release them when they reach it.

-------------------
** MOVE AND HOLD **
-------------------
Double RIGHT CLICK on open space:
	Move selected ships to the clicked position and order them to hold the position.

-----------------------------------------
** MOVE AND HOLD WITH RETREAT POSITION **
-----------------------------------------
RIGHT CLICK(point A) and HOLD on open space and DRAG the mouse to point B and RELEASE(point B):
	Move a single ship to a point between point A and B based on flux level i.e., at 0% flux the
	ship will stay at point A, at 100% flux the ship will stay at point B and at 50% flux the
	ship will stay at a point exactly between point A and B.

---------------------------------------
** ATTACK ENEMY ENGAGING ALL WEAPONS **
---------------------------------------
RIGHT CLICK on an enemy:
	Order one or more ships to face selected enemy and move to a position that is close enough
	to selected enemy such that ALL weapons can be engaged (except point defense and missiles).

-------------------------------------------------
** ATTACK ENEMY ENGAGING LONGEST RANGE WEAPONS **
-------------------------------------------------
Double RIGHT CLICK on an enemy:
	Order one or more ships to face selected enemy and move to a position that is close enough
	to selected enemy such that only the LONGEST range weapon/s can be engaged.
	(except point defense and missiles).

-----------------------------
** ATTACK ENEMY FROM ANGLE **
-----------------------------
RIGHT CLICK(point A) and HOLD on an enemy and DRAG the mouse to an open space and RELEASE(point B):
	Order a single ship to face selected enemy and maintain a position that is at the same
	angle (A to B) and distance (A to B) as when the command was issued. Distance is limited
	such that at least one weapon can be engaged (except point defense and missiles).

-----------------
** FOCUS ENEMY **
-----------------
HOLD LEFT CONTROL and LEFT CLICK on enemy:
	Order selected ships to face a chosen enemy. Ships must already have an assignment.

--------------------------------------
** MOVE TO POSITION AND FOCUS ENEMY **
--------------------------------------
RIGHT CLICK(point A) and HOLD on open space and DRAG the mouse to an enemy and RELEASE:
	Move to point A and face the selected enemy.

------------------------
** CANCEL ASSIGNMENTS **
------------------------
X:
	Cancel all assignments for the selected ships.

--------------------------
** CANCEL FOCUSED ENEMY **
--------------------------
HOLD LEFT CONTROL and X:
	Stop all selected ships from focusing on an enemy.

--------------------------
** SHOW ALL ASSIGNMENTS **
--------------------------
HOLD ALT:
	Show all assignments given.

---------------------------
** TRANSLATE ASSIGNMENTS **
---------------------------
HOLD ALT and LEFT CLICK(point A) and DRAG and RELEASE(point B):
	Translate all assignments (excluding targeted enemys) a distance and angle defined by
	point A and B.

------------------------
** ROTATE ASSIGNMENTS **
------------------------
HOLD ALT and HOLD LEFT CONTROL and LEFT CLICK(point A) and DRAG and RELEASE(point B):
	Rotate all assignments (excluding targeted enemys) an angle defined by point A and B
	around a pivot defined by their collective centre.

----------
** VENT **
----------
C (Default)
	Vent all selected ships.

--------------------------
** CREATE CONTROL GROUP **
--------------------------
HOLD LEFT CONTROL and {1,2,3,4,5}
	Create a control group.

----------------------
** SAVE ASSIGNMENTS **
----------------------
Q (Default):
	Save all assignments.

----------------------
** LOAD ASSIGNMENTS **
----------------------
W (Default):
	Load all assignments centered around the current mouse position.

---------------------
** TOGGLE RTS MODE **
---------------------
Capslock (Default)
	Toggle between normal and RTS camera and controls.


**********************************
  Still in DEVELOPMENT for 0.1.4
**********************************

* Waypoints.
* UI scaling bugs.
* Mod compatibility.

Waypoints offer the player the ability to queue actions together.

Several users have indicated that when nvidia UI scaling is enabled, UI elements bug out.
This needs to be fixed asap as nvidia ui scaling seems quite ubiquitous for people on larger
displays. 

Occasionally, ships from mod packs may have systems that cause them to not behave well when
given orders. They also may need to broadside to effectively attack. Contained is an API for
making ships compatible (still in development). I will be working to make some of the more popular ship packs
compatible. Next is Knights of Ludd.

There is more that will be developed, mainly UI enhancements and more controls.
The goal for 1.0 is a well rounded RTS adaption that looks clean and feels vanilla.


****************
  For MODDERS:
****************

Ships may have systems that may activate when a player might not want them to. Some ships may also
need to broadside to attack. Contained is a tutorial guide and example of how to tell this mod that
a given ship hull system should be disabled or if that ship hull needs to broadside (still under
development).

Im not too familiar with the various mods that are available and what functionality they add that
might conflict with this mod. If you are aware of something problematic please let me know and Ill
get to it asap.

If you would like your ships to be compatible but do not have the knowledge to be able to make
it so, contact me on the STARSECTOR forumns I'll be more than willing to do it for you when I
get the chance.


---
END
---
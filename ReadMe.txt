Mod thread: https://fractalsoftworks.com/forum/index.php?topic=32007.0

-----------------
  Installation:
-----------------
This is installed the same way as a normal mod. Simply extract the zip into
your mods folder and tag it in the launcher.
Can be added to an existing playthrough.

------------
  Removal:
------------
Safe to remove from an existing save. 

-------------
  Updating:
-------------
Just delete the old version and copy over the new one. Hotkeys and Settings
adjusted using LunaLib will persist, otherwise hotkeys will be reset on update.
Can also be updated through TriOS.

*****************
  Introduction:
*****************

Quickstart: It plays just like an RTS. Drag select and give ships orders. Cancel
previous orders with the X hotkey. Hold Alt to see all orders.
Use Capslock to toggle between vanilla and RTS style gameplay.

RTSAssist is a RTS conversion for Starsector. It does, however, allow switching between RTS
and Vanilla control schemes. Ships can be moved to a position, ordered to attack an enemy ship etc.

RTSAssist does not change or modify the AI but rather restricts it. For example: A ship
is ordered to hold position. The AI is still in full control except that it must stay
at the position that its order commands. What it chooses to attack, when it uses its
systems, how it raises it's shields etc., are all the same, with some exceptions.
When not given an order ships will behave exactly as they do in the vanilla game.

In order to make some ships more wieldy, some ships will have there system use either
disabled or there usecases modified WHEN following a command. On the mod page is a full list
of which ship packs are compatible.

Hotkeys can be changed in the hotkeys.ini file found in this folder or using LunaLib. 

Finally, I hope you enjoy it. It has been a project of mine for a long, long time. If
you are having trouble micromanaging ships remember the AI is fully capable without you
and sometimes just a little influence makes all the difference.

IMPORTANT: During the first frame of combat, combat is paused. Ships can be arranged
before the battle starts similar to Total War games. During this time ships must be given
commands on an individual basis.


**********
  Usage:
**********

Hotkeys can be changed in Hotkeys.ini

---------------------
** TOGGLE RTS MODE **
---------------------
Capslock:
	Toggle between normal and RTS camera and controls.

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

->(MOVE AND HOLD and MOVE AND RELEASE can be switched in the config file)<-

----------------------
** QUEUE ASSIGNMENT **
----------------------
HOLD SHIFT and give ships assignments:
	Queues an assignment. Assignments that are indefinite can not be queued after.

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
	to selected enemy such that ALL weapons can be engaged (except point defense and missiles, unless
	these are the only weapons).

-------------------------------------------------
** ATTACK ENEMY ENGAGING LONGEST RANGE WEAPONS **
-------------------------------------------------
Double RIGHT CLICK on an enemy:
	Order one or more ships to face selected enemy and move to a position that is close enough
	to selected enemy such that only the LONGEST range weapon/s can be engaged.
	(except point defense and missiles, unless these are the only weapons).

-----------------------------
** ATTACK ENEMY FROM ANGLE **
-----------------------------
RIGHT CLICK(point A) and HOLD on an enemy and DRAG the mouse to an open space and RELEASE(point B):
	Order a single ship to face selected enemy and maintain a position that is at the same
	angle (A to B) and distance (A to B) as when the command was issued. Distance is limited
	such that at least one weapon can be engaged (except point defense and missiles, unless
	these are the only weapons).

-----------------
** FOCUS ENEMY **
-----------------
HOLD LEFT CONTROL and LEFT CLICK on enemy:
	Order selected ships to face and target a chosen enemy. Ships must already have an assignment.

--------------------------------------
** MOVE TO POSITION AND FOCUS ENEMY **
--------------------------------------
RIGHT CLICK(point A) and HOLD on open space and DRAG the mouse to an enemy and RELEASE:
	Move to point A and face the selected enemy.

-----------------------------------
** ATTACK MOVE TO POSITION/ENEMY **
-----------------------------------
C:
	Move selected ships to either : (A) If the ships already has an assignment, convert that
	assignment to an attack move assignment : (B) Move to a position defined by the mouse pointer.
	Ships given an attack move assignent will move to the designated position engaging enemies on
	the way at a range that allows all weapons to fire. Can also target enemys.

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

-----------------------------------
** CANCEL LAST QUEUED ASSIGNMENT **
-----------------------------------
HOLD LEFT SHIFT and X:
	Cancle the most recently queued assignment for a given ship.

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

-------------------
** MOVE TOGETHER **
-------------------
R:
	When given to a group of ships, their speeds will be adjusted so that they will arrive
	at their destinations at the same time. For Example: This is useful when giving ships
	that are in formation a translate command. If R is pressed after the translate command
	they will move to their new positions maintaining formation.
	Pressing R again on the group will cancle this command. Pressing R on an individual ship
	will release it allowing it to move at maximum speed. Adding another ship to the selection
	and then pressing R will add that ship to the group. 

----------
** VENT **
----------
V:
	Vent all selected ships.

----------------
** USE SYSTEM **
----------------
F:
	All ships will attempt to use their systems.

----------------------------
** ADD BROADSIDE MODIFIER **
----------------------------
B:
	Adds a permanent broadside modfierthat is used when the ship is ordered to attack another
	ship. Persistant between battles. Single tap adds strict modifier. Double tap will have
	the ship use the most appropriate side.

--------------------------
** CREATE CONTROL GROUP **
--------------------------
HOLD LEFT CONTROL and {1,2,3,4,5}
	Create a control group.

--------------------------
** PAN TO CONTROL GROUP **
--------------------------
DOUBLE TAP {1,2,3,4,5}
	Pan the camera to the selected control group

----------------------
** SAVE ASSIGNMENTS **
----------------------
Q:
	Save all assignments.

----------------------
** LOAD ASSIGNMENTS **
----------------------
E:
	Load all assignments centered around the current mouse position.



****************************
  Still in DEVELOPMENT 0.2
****************************

The current user interface for RTSAssist is temporary and looks like ass.
Version 0.2 will aim to add a user interface that not only feels genuine,
but will also add more information to the screen in such a way that is not
invasive and also allows the player to make better descisions with his APM.
This will include a reworked HUD, custom minimap, reworked audio and upgraded
selection circles, target indicators etc. If you have any additional ideas,
drop a post on the forumn.

The goal for 1.0 is a well rounded RTS adaption that looks clean and feels vanilla.


****************
  For MODDERS:
****************

Ships may have systems that may activate when a player might not want them to. Some ships may also
need to broadside to attack. Contained is a tutorial guide and example of how to tell this mod that
a given ship system should have its behaviour modified or if that ship needs to broadside (First
iteration is complete).

Im not too familiar with the various mods that are available and what functionality they add that
might conflict with this mod. If you are aware of something problematic please let me know and Ill
get to it asap.

If you would like your ships to be compatible but do not have the knowledge to be able to make
it so, contact me on the STARSECTOR forumns I'll be more than willing to do it for you when I
get the chance.


---
END
---
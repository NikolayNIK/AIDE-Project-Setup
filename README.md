# AIDE Project Setup App
## Why
The AIDE is a great app providing android programming for android itself.
For now it can create application projects by one template ignoring "use tabs" options and using damn space instead!
This app is a powerfull tool to provide projects for AIDE for your choise and your requests.
Flexible template structure lets third party developers build their own packs and share it.
Another big argument is that this app can provides easier usage of [Design Support Library](https://developer.android.com/training/material/design-library.html).
It isn't just provides compatibility with older android versions but adds new features (such as [Navigation Drawer](https://material.io/guidelines/patterns/navigation-drawer.html)).

## Development
App uses [Lua](https://www.lua.org) as a base for templates. It lets developers to make templates that not only can create fancy app projects but add something to it (for example activity).
Files in template are described by:
* "@/filename" - for internal template files (read-only).
* "#/filename" - for project destination files (read-write).

Script @/create.lua is executed on UI thread when template is first created.
As an any UI thread method its execution should not take more that 5 seconds or the application will be considered as not responding.
To communicate with users templates may (or may not) use graphical items (pre-defined group of android views). Items can be created and modified by **UI thread only**. Getters may be safely called by any other.

### Function *findByID*(arg0)
* arg0 **string** - id of an item to return.
* return - lua representation of item that was found by passed id.

### Function *new__Item__*(arg0, arg1)
* arg0 **string** - id of a new item.
* arg1 **string** - title for new item (may be *nil*).
* return - lua representation of new item.

Available items:
1. Text
1. EditText
1. Destination
1. Check
1. MultiChoose
1. SingleChoose

`//TODO Docs for every one`

### Function *finish*()
Finishes template's activity. Not recommended to use.

### Function *runOnUiThread*(arg0)
* arg0 **function** - a function to be called on UI thread.

### Function *runOnNewThread*(arg0)
* arg0 **function** - a function to be called on a new thread.

### Function *setEmptyRecomended*(arg0)
* arg0 **boolean** - if false (default is true) the destination is not empty dialog would not be shown.

### Function *enableFab*()
Makes it possible for user to click generate button.

### Function *disableFab*()
Makes it impossible for user to click generate button.

### Function *setTitle*(arg0)
UI thread only.
* arg0 **string** - a title of activity to be shown (default is name of template).

### Function *splitCard*()
makes any items created after appear on a new card.

### Function *snackbar*(arg0, arg1, arg2)
* arg0 **string** - snackbar message.
* arg1 **string** - snackbar duration (0 — long, -1 — short, -2 — indefinite).
* arg2 **array of 2**: (may be nil)
	* item1 **string** - action text.
	* item2 **function** - function that will called when user clicks an action.

### Function *sleep*(arg0)
* arg0 **long** - number of milliseconds for thread to be suspended by.

# **To be continued...**
`//TODO Fix misspellings`

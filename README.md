# Hudify - A Spotify integration for CustomHud

## Requires [CustomHud](https://modrinth.com/mod/customhud)


You'll need to authorize the third party app Blockify to your Spotify account to use this mod.   
Some features are locked behind a Spotify Premium subscription, which is out of my control  
You'll be prompted to authorize when you first press the 'toggle play/pause' key

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps

Tested, working versions
If you use on other versions of Minecraft or CustomHud, please lmk whether it works or doesnt
- Minecraft 1.20.4 with CustomHud 3.3.0
- Minecraft 1.20.4 with CustomHud 4.0 Beta 23 (found on [Jakob's discord](https://discord.gg/eYf7DDHhvN))


### Variables added:

#### String variables:  
`spotify_track` / `sp_track` - song's title  
`spotify_artists` / `sp_artists` - If multiple artists, they are combined into one string  
`spotify_progress` / `sp_prog` - song progress in MM:SS notation  
`spotify_duration` / `sp_dur`  - song duration in MM:SS notation  

`spotify_url` - a long, unclickable url link to your current track  
`spotify_volume` - your volume from 1-100. Spotify's built-in volume, not your devices' volume

### Example CustomHud Config:
```
=if: ({spotify_track} = "-" | {spotify_track} = "Status Code: 400")=
Spotify not loaded/initialized/whatnot

If this is your first time using this mod, 
    auth yourself via pressing the 'toggle playback' key (unbound by default)

If you've already authed, please report this. 
It's not an error exactly, but I want to understand when/why it happens, to make handling it cleaner
=else=
{spotify_track}
{spotify_artists}
{spotify_progress} / {spotify_duration}

// you probably won't need these
URL: {spotify_url}
Volume: {spotify_volume}
=endif=
```
#### Known issues:
- sometimes progress var skips a second
- progress doesn't always update progress on first skip of session
- when skipping, a slight delay before hud updates


#### Todo list:  
- change volume var from string to integer. Or just remove it?
- add more variables
- add more playback controls, maybe
- maybe? make customhud a soft dependency
- add screenshots to readme

#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work
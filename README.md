# Hudify - A Spotify integration for CustomHud

## Requires [CustomHud](https://modrinth.com/mod/customhud)


You'll need to authorize the third party app Blockify to your Spotify account to use this mod.   
Some features are locked behind a Spotify Premium subscription, which is out of my control  
You'll be prompted to authorize when you first press the 'toggle play/pause' key

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps

Tested, working versions
If you use on other versions of Minecraft or CustomHud, please lmk whether it works or doesnt
- Minecraft 1.20.4 with CustomHud 3.3.0
- Minecraft 1.20.4 with CustomHud 4.0 Beta 23 (found on Jakob's discord)


### Variables added:

#### String variables:
`{sp_track}` - song's title  
`{sp_album}` - album of current track
`{sp_artists}` - All artists combined into one string
`{sp_first_artist}` - The very first artist listed
`{sp_context_type}` - Where the track is playing from. Can be "artist", "playlist", "album", "show"
`{sp_context_name}` - Name of context's artist/playlist/album/show

`{spotify_progress}` / `{sp_prog}` - song progress in MM:SS notation  
`{spotify_duration}` / `{sp_dur}`  - song duration in MM:SS notation  


### Example CustomHud Config:
```
todo
```
#### Known issues:
- progress can get thrown off after unpausing
- variables doesn't always update on first skip of session
- when skipping tracks, a slight but noticeable delay before hud updates
- at launch, variables are often empty

#### Todo list:
- create my own spotify dev app
- add more playback controls, maybe
- maybe? make customhud a soft dependency
- add screenshots to readme

#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work
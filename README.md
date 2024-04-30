## Requires https://modrinth.com/mod/customhud


You'll need to authorize the app Blockify to use this mod.   
You'll be prompted to authorize once you use the first play/pause action

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps


### Variables added:

String variables:  
`spotify_track` / `sp_track` - song's title  
`spotify_artists` / `sp_artists` - If multiple artists, they are combined into one string  
`spotify_progress` / `sp_prog` - song progress in MM:SS notation  
`spotify_duration` / `sp_dur`  - song duration in MM:SS notation  

`spotify_url` - a long, not-clickable url link to your current track  
`spotify_volume` - your volume from 1-100. Spotify's built-in volume, not your devices' volume

### Example CustomHud Config:
```
=if: {spotify_track} = "-"=
spotify not loaded/initialized/whatnot
If this is your first time using this mod, auth yourself via pressing the play/pause key (unbound by default)
=else=
{spotify_track}
{spotify_artists}
{spotify_progress} / {spotify_duration}

// you probably won't need these
URL: {spotify_url}
Volume: {spotify_volume}
=endif=
```

Todo list:  
- turn volume to integer
- more variables
- maybe? make customhud a soft dependency

Credit:  
a huge thank you to erruqie and https://github.com/erruqie/Blockify and Jakob for their fantastic work

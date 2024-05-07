# Hudify - A Spotify integration for CustomHud

## Requires [CustomHud](https://modrinth.com/mod/customhud)


You'll need to authorize the third party app Hudify to your Spotify account to use this mod.   
Some features are locked behind a Spotify Premium subscription, which is out of my control  
You'll be prompted to authorize when you first press the 'toggle play/pause' key

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps


### Variables added:

#### String variables:
`{sp_track}` - Song's title  
`{sp_album}` - Album of current track
`{sp_artists}` - All artists combined into one string
`{sp_first_artist}` - The very first artist listed
`{sp_context_type}` - Where the track is playing from. Can be "artist", "playlist", "album", "show"
`{sp_context_name}` - Name of context's artist/playlist/album/show. Unavoidably buggy, see below

`{spotify_progress}` / `{sp_prog}` - Song progress in MM:SS notation  
`{spotify_duration}` / `{sp_dur}`  - Song duration in MM:SS notation  

`{sp_shuffle_state}` - Boolean. True if shuffling, false if shuffle is off
`{sp_repeat_state}` - "off", "track", or "all"
`{sp_media_type}` - "track" or "episode" - should i remove this in favor of `is_podcast`?
`{sp_is_podcast}` - Boolean. True if podcast, false if not

### Example CustomHud Config:
```
todo
```
#### Known issues:
- progress can get thrown off after unpausing
- at launch, variables are often empty
- 'context' doesnt update if playing from queue or search results. 
  - Most likely a limitation of [Spotify's api](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback)

#### Todo list:
is playing/ is valid / is app closed etc vars
- figure out refreshActiveSession

- "program message" variable
- truncate long variables
- scrub "remastered"s and other unnecessary stuff appended to track titles
- add screenshots to readme

#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work
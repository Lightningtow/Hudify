# Hudify - A Spotify integration for CustomHud
## Requires the 4.0 beta of [CustomHud](https://modrinth.com/mod/customhud), found on the CustomHud discord


You'll need to authorize the third party app Hudify to your Spotify account to use this mod.   
Some features are locked behind a Spotify Premium subscription, which is out of my control  
You'll be prompted to authorize when you first press a control key

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps


### Variables added:
#### String variables:
`{sp_song}` / `{sp_track}` - Song's title  
`{sp_album}` - Album of current track  
`{sp_artist}` / `{sp_artists}` - All artists combined into one string  
`{sp_first_artist}` - The very first artist listed  
`{sp_context_type}` - Where the track is playing from. Can be "artist", "playlist", "album", "show"  
`{sp_context_name}` - Name of context's artist/playlist/album/show   
`{sp_media_type}` - "track" or "episode" - should i remove this in favor of `is_podcast`?  
`{sp_repeat}` - "off", "track", or "all"  
`{sp_message}` / `{sp_msg}` - Program messages. Put here instead of in Minecraft chat, for compatibility with more Minecraft versions  

#### Special variables:
`{sp_progress}` / `{sp_prog}` - String: Song progress in MM:SS notation. Number: song progress in seconds  
`{sp_duration}` / `{sp_dur}`  - String: Song duration in MM:SS notation  Number: song duration in seconds  

#### Boolean variables:
`{sp_shuffle}` - Boolean. True if shuffling, false if shuffle is off  
`{sp_is_podcast}` - Boolean. True if podcast, false if not  

### Example CustomHud Config:
```
=if: sp_track=
{sp_track}
{sp_album}
{sp_artists}
{sp_progress} / {sp_duration}

Shuffle: {sp_shuffle}
Repeat: {sp_repeat}
=endif=
```
#### Known issues:
- progress can get thrown off after unpausing
- at launch, variables are often empty
- context hitting rate limits, needs to locally cache api-name pairs
- 'context' doesnt update if playing from queue or search results. 
  - Most likely a limitation of [Spotify's api](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback)

#### Todo list:
- is playing/ is valid / is app closed etc vars
- figure out refreshActiveSession
- "program message" variable
- truncate long variables
- scrub "remastered"s and other unnecessary stuff appended to track titles
- add screenshots to readme

#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work



# Hudify - A Spotify integration for CustomHud
## Requires the 4.0 beta of [CustomHud](https://modrinth.com/mod/customhud), found on the CustomHud discord


You'll need to authorize the third party app Hudify to your Spotify account to use this mod.   
Some features are locked behind a Spotify Premium subscription, which is out of my control  
You'll be prompted to authorize when you first press a control key

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps

Program messages are displayed in the `{sp_message}` variable rather than in Minecraft chat, for compatibility with more Minecraft versions.

### Variables added:
#### String variables:
`{sp_track}` / `{sp_song}` - Song/episode's title  
`{sp_fancy_track}` - Song title with stuff like "remastered", "bonus track" etc scrubbed out  
`{sp_album}` - Album of current track. Blank if podcast  
`{sp_artist}` / `{sp_artists}` - All artists combined into one string  
`{sp_first_artist}` - The very first artist listed  
`{sp_context_type}` - Where the track is playing from. Can be `artist`, `playlist`, `album`, or `show`  
`{sp_context_name}` - Name of the artist/playlist/album/show you're playing from  
`{sp_repeat}` - `off`, `track`, or `all`  

#### Special variables:
`{sp_progress}` / `{sp_prog}` - String: progress in MM:SS format. Number: number of seconds. Boolean: If num > 0  
`{sp_duration}` / `{sp_dur}`  - Song duration. Formatted same as `sp_progress` above  
`{sp_message}` / `{sp_msg}` -  String: the message. Number: seconds remaining till the message is cleared. Boolean: whether a message is currently being displayed.  
#### Boolean variables:
`{sp_shuffle}` - Boolean. True if shuffle is on (including smart shuffle), false if not  
`{sp_is_podcast}` - Boolean. True if currently listening to a podcast, false if not  

### Example CustomHud Config:
```
=if: sp_track=
{sp_track}
{sp_album}
{sp_artists}
{sp_progress} / {sp_duration}
Playing from {sp_context_name} ({sp_context_type})

Shuffle: {sp_shuffle}
Repeat: {sp_repeat}

{{sp_message, "{sp_msg} {$0, sp_msg}"}}
=endif=
```
#### Known issues:
- progress can get thrown off after unpausing
- at launch, variables are often empty
- context doesn't update if playing from queue or search results. 
  - Most likely a limitation of [Spotify's api](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback). I don't know if I can fix this

#### Todo list:
- is playing/ is valid / is app closed etc vars
- figure out refreshActiveSession
- truncate long variables
- add screenshots to readme

#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work



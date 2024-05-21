# Hudify - A Spotify integration for CustomHud
## Requires [CustomHud](https://modrinth.com/mod/customhud)


You'll need to authorize the third party app Hudify to your Spotify account to use this mod.   
Playback controls are locked behind a Spotify Premium subscription, which is out of my control  
You'll be prompted to authorize on your first startup

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps

Program messages are displayed in the `{sp_message}` variable rather than in Minecraft chat, for compatibility with more Minecraft versions.

### Variables added:
#### String variables:
`{sp_track}` / `{sp_song}` - Song/episode's title  
`{sp_fancy_track}` - Track title with stuff like "remastered", "bonus track" etc scrubbed out.  
`{sp_album}` - Album of current track. Blank if podcast  
`{sp_artist}` / `{sp_artists}` - All artist(s)  
`{sp_first_artist}` - The very first artist listed  
`{sp_context_type}` - Where the track is playing from. Can be `artist`, `playlist`, `album`, or `show`  
`{sp_context_name}` - Name of the artist/playlist/album/show you're playing from  
`{sp_repeat}` / `{sp_repeat_state}` - One of `track`, `off`, or `all`  

#### Special variables:
These return a string, a number, or a boolean depending on how they are used within the CustomHud profile.  
`{sp_progress}` / `{sp_prog}` - String: progress in MM:SS format. Number: number of seconds. Boolean: If num > 0  
`{sp_duration}` / `{sp_dur}`  - Song duration. Formatted same as `{sp_progress}` above  
`{sp_message}` / `{sp_msg}` -  String: the message. Number: seconds remaining till the message is cleared. Boolean: whether a message is currently being displayed.  
`{sp_message_duration}` / `{sp_msg_dur}` - Number variable. Seconds remaining till the message clears  

#### Boolean variables:
All string variables can be used as a boolean. They return false if the string is empty, otherwise they're true  
`{sp_shuffle}` -  True if shuffle (or smart shuffle) is enabled, false if not  
`{sp_is_podcast}` - True if currently listening to a podcast, false if not  
`{sp_is_playing}` - Whether Spotify is playing. Returns true if actively playing music, returns false if paused, app shut, unauthorized etc.  
(a different way of checking if it's loaded is using `sp_track` as a boolean, see below for example. This checks whether `sp_track` exists, which is useful because it returns true while paused)    
<details>
<summary>Internal variables</summary>

These were added by me for debugging, I can't think of any reason you'd need them, but you can use them if you like   
`{sp_device_id}` - String. ID of device. Gibberish string of random digits  
`{sp_device_name}` - String. What you named the device you're playing from  
`{sp_device_is_active}` - String. Whether the device is active  

`{sp_status_code}` - Number. Status code from the header of the most recent Spotify API call.  
`{sp_status_string}` - String. Description of the status code of latest "get playback info" call, according to [Spotify's docs](https://developer.spotify.com/documentation/web-api/concepts/api-calls)  
`{sp_is_authorized}` - Boolean. Whether currently authed with Spotify   

</details>



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

{{sp_message, "{sp_msg} {sp_msg_dur}"}}
=endif=
```
#### Known issues:
- playback controls can be finicky
- at launch, variables are empty if app is open but paused
- context doesn't update if playing from queue or search results. 
  - This is a limitation of [Spotify's api](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback). I don't believe I can fix this


#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work



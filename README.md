# Hudify - A Spotify integration for CustomHud
## Requires [CustomHud](https://modrinth.com/mod/customhud). Please read the installation instructions below!  

<details>
<summary>Installation</summary>

You'll need to create a Spotify developer app to use this mod.  
Why? In short, because it's easier for everyone. Since Spotify's API ratelimits are per app, not per user,
if everyone has their own app, Hudify can poll the API much more frequently without worrying about hitting ratelimits.
This allows Hudify to display far more accurate info, with much less risk of getting desynchronized.  
Note: Playback controls are locked behind a Spotify Premium subscription, which is out of my control.  
The CustomHud variables should work fine without a subscription.   
### Installation instructions:
1) Create a Spotify app according to [this](https://developer.spotify.com/documentation/web-api/tutorials/getting-started#create-an-app).  
2) Set your app name and description to anything you want, and be sure to set your Redirect URI to `http://localhost:8001/callback`.  
3) Get your Client ID from your newly created app  
4) Put your Client ID in Hudify's config, via ModMenu.  
5) Press a Hudify hotkey to initialize, and that's it!. No need to request an access token like the docs prompt you to, because Hudify takes care of that for you.

To uninstall, also be sure to also disconnect the app from https://www.spotify.com/us/account/apps

</details>



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
{{sp_context_name, "Playing from {sp_context_name} ({sp_context_type})"}}

Shuffle: {sp_shuffle}
Repeat: {sp_repeat}

{{sp_message, "{sp_msg} {sp_msg_dur}"}}
=endif=
```
#### Known issues:
- playback controls can be finicky. Don't mash the keybinds, it'll make things worse  
- at launch, variables are empty if app is open but paused
- context doesn't update if playing from queue or search results. 
  - This is a limitation of [Spotify's api](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback). I don't believe I can fix this


#### Credit:  
a huge thank you to erruqie's [Blockify](https://github.com/erruqie/Blockify) and Jakob for their fantastic work



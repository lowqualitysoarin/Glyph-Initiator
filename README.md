# Glyph Initiator
Glyph Initiator is an application made specifically for Nothing Phones that allows the user to control the phone's Glyph Interface without needing root
via automation apps (e.g. Macrodroid, Tasker, Automate), by sending intents to this application.

### What can I do with it?
It allows your automations to control your Glyph Interface, e.g. like a Glyph Alarm, Glyph Charge Indicator, Glyph Bluetooth Connection effect, etc...

### How do I get started?
Since I don't have the API key yet, you need to enable the glyph debugger in order for this app to function properly... Go to your Device's Settings/System/Developer options/ and scroll down a bit until you see Glyph Interface Debug Mode and enable it. If you can't see the setting you have to enable it via ADB, follow [Nothing's setup instructions](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit?tab=readme-ov-file#setup-instructions) on how to enable it via ADB commands.

Download the app on the releases page here in this repository and install the apk on your phone, launch the app and accept permissions, confirm the app is marked as an active app by opening your quick settings menu and tapping on the icon mostly showed as (1) to the left of the settings icon. Once that is confirmed, you can close the app or stay in it if you want to add glyph composition entry.

If you have Macrodroid you can download these [Action Blocks](https://drive.google.com/file/d/1owAWLgtwWdtXnma2T-MqKazWTJKeH1ux/view?usp=sharing) to give you an idea on how to get this up and working.
If you don't, in the mean time. Here is a little documentation how to setup the intent you are going to send to this application

- Intent Type: _Service_
- Package Name: _com.lowqualitysoarin.glyphinitiator_
- Action Name: _com.lqs.glyph.intent.action.[Action - Replace this with the desired action below.]_
- Class: _com.lowqualitysoarin.glyphinitiator.[Class - Replace this with the desired class below.]_

## Action List

| Action / Class Name | Description | Parameters (Add the Parameters in your Intent's Extras. These are case-sensitive.) |
| ------ | ----------- | ---------- |
| [**Action**] START_GLYPH_SESSION<br/> [**Class**] .services.GlyphStartSessionService | Starts the glyph session, recommended to always call first before calling BUILD_GLYPH_CHANNEL or DISPLAY_GLYPH_PROGRESS. | None. |
| [**Action**] STOP_GLYPH_SESSION<br/> [**Class**] .services.GlyphStopSessionService | Stops the glyph session, always call this after when you are done sending actions through the app. Else essential notifications, and music visualization. won't work after calling your automation. | None. |
| [**Action**] PLAY_GLYPH_ACTION<br/> [**Class**] .services.GlyphActionService | Plays a glyph action. | [**String**] *actionKey* : null,<br/> [**Boolean**] *noAudio* : false,</br> [**Boolean**] *allowOverride* : false
| [**Action**] STOP_GLYPH_ACTION<br/> [**Class**] .services.GlyphStopActionService | Stops the currently playing glyph action. | None. |
| [**Action**] DISPLAY_GLYPH_PROGRESS<br/> [**Class**] .services.GlyphDisplayProgressService | Displays the given progress to the glyph interface. | [**String**] *channel* : null,<br/> [**Integer**] *progress* : 0,<br/> [**Boolean**] *reversed* : false |
| [**Action**] BUILD_GLYPH_CHANNEL<br/> [**Class**] .services.GlyphBuildChannelService | Toggles the given glyph channel from the intent. | [**String**] *channel* : null,<br/> [**Boolean**] *noAnimate* : false,<br/> [**Integer**] *interval* : 10,<br/> [**Integer**] *cycles* : 1,<br/> [**Integer**] *period* : 3000 |
| [**Action**] GLYPH_OFF<br/> [**Class**] .services.GlyphOffService | Different from STOP_GLYPH_SESSION, this just turns off all the lit up glyphs called by BUILD_GLYPH_CHANNEL DISPLAY_GLYPH_PROGRESS but doesn't stop the session. | None. | 

## Parameter Help
[**String**] *actionKey* : This is the target action that you want to play when calling PLAY_GLYPH_ACTION, make sure the name that you are going to provide in this parameter corresponds to the existing entry you added in the app.<br/><br/>
[**Boolean**] *allowOverride* : This is an option specifically for PLAY_GLYPH_ACTION whether you want the current intent to overridable by another PLAY_GLYPH_ACTION call. If enabled the the glyph action you are going to play can be overridable by another glyph action call, if not your glyph action won't be overridable by another glyph action call and will just ignore the other glyph action calls.

[**String**] *channel* : This is the channel that you want to use, check [Nothing's GDK](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit?tab=readme-ov-file#glyph) on github to view the glyph channels of your phone. You don't need to include the numbers on the zone from the documentation, example if you want to use the Channel "A" just put "A" instead of "A1".<br/>

[**Integer**] *interval* : [Nothing's buildInterval documentation.](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit#:~:text=buildInterval(int%20interval))\
[**Integer**] *cycles* : [Nothing's buildCycles documentation.](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit#:~:text=buildCycles(int%20cycles))\
[**Integer**] *period* : [Nothing's buildPeriod documentation.](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit#:~:text=buildPeriod(int%20period))

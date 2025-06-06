# Glyph Initiator
Glyph Initiator is an application made specifically for Nothing Phones that allows the user to control the phone's Glyph Interface without needing root
via automation apps (at the moment its confirmed to work on Macrodroid), by sending intents to this application.

### What can I do with it?
It allows you to create automations that interacts with your Glyph Interface, e.g. Glyph Alarm, Glyph Charge Indicator, Glyph Bluetooth Connection effect, etc...

### How do I get started?
If you have Macrodroid you can download these Action Blocks to give you an idea on how to get this up and working.
If you don't, in the mean time. Here is a little documentation how to setup the intent you are going to send to this application

- Intent Typhe: _Service_
- Package Name: _com.lowqualitysoarin.glyphinitiator_
- Action Name: _com.lqs.glyph.intent.action.[Action]_

## Action List

| Action | Description | Parameters (Add the Parameters in your Intent's Extras. These are case-sensitive.) |
| ------ | ----------- | ---------- |
| START_GLYPH_SESSION | Starts the glyph session, recommended to always call first before calling BUILD_GLYPH_CHANNEL or DISPLAY_GLYPH_PROGRESS. | None. |
| STOP_GLYPH_SESSION | Stops the glyph session, always call this after when you are done sending actions through the app. Else essential notifications, and music visualization. won't work after calling your automation. | None. |
| PLAY_GLYPH_ACTION | Plays a glyph action. | [String] actionKey : null,<br/> [Boolean] noAudio : false
| STOP_GLYPH_ACTION | Stops the currently playing glyph action. | None. |
| DISPLAY_GLYPH_PROGRESS | Displays the given progress to the glyph interface. | [String] channel : null,<br/> [Integer] progress : 0,<br/> [Boolean] reversed : false |
| BUILD_GLYPH_CHANNEL | Toggles the given glyph channel from the intent. | [String] channel : null,<br/> [Integer] interval : 10,<br/> [Integer] cycles : 1,<br/> [Integer] period : 3000 |

## Parameter Help
[String] channel : This is the channel that you want to use, check [Nothing's GDK](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit?tab=readme-ov-file#glyph) on github to view the glyph channels of your phone.

[Integer] interval : [Nothing's buildInterval documentation.](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit#:~:text=buildInterval(int%20interval))\
[Integer] cycles : [Nothing's buildCycles documentation.](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit#:~:text=buildCycles(int%20cycles))\
[Integer] period : [Nothing's buildPeriod documentation.](https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit#:~:text=buildPeriod(int%20period))

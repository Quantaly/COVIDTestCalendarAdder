# COVIDTestCalendarAdder

Colorado School of Mines and also some other places use a website called PrimaryBio to schedule COVID-19 tests. They send you a text message after you schedule it. This app watches for those text messages and adds the appointments to your calendar so you don't forget to go for like, a week, not that I'm speaking from experience or anything.

## Installation

Unfortunately, using SMS permissions for anything other than a messaging app violates the policies of the Google Play Store, so in order to use the app, you will need to manually "sideload" it. One option is to compile it yourself using Android Studio. Another, potentially easier option is to download and install the provided APK file:

1. Download `app-release.apk` from the [latest release](https://github.com/Quantaly/COVIDTestCalendarAdder/releases/latest). You might get a scary prompt about how harmful APK files can be, but I promise this one is harmless. If you are paranoid (which is completely valid), you can [review the source code](https://github.com/Quantaly/COVIDTestCalendarAdder/) and/or compile it yourself using Android Studio.
2. Open and install the APK file. You will probably get more scary warnings; again, I promise this app doesn't do anything bad! You might also need to give one of your apps (probably your web browser or file browser) permission to install other apps. I recommend that you revoke this permission from the settings after you've finished with it.
3. Open the app, grant it SMS and calendar permissions, and choose a calendar to add events to.
4. From now on, whenever you [schedule a test through PrimaryBio](https://www.primarybio.com/a/mines), it will be automatically added to your calendar!

## Caveats

I have only tried the app with scheduling tests at Colorado School of Mines. Other locations may work, but have not been verified.

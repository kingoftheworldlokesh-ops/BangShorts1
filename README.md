# BangShorts

BangShorts is an Android focus helper that blocks selected short-form destinations without closing the host app.

Current behavior
- YouTube is not blocked as a whole.
- Normal YouTube home, search, subscriptions, and long-form videos remain usable.
- When focus mode or an active schedule is enabled, recognized Shorts player/feed indicators trigger Back to leave the Shorts destination.
- Instagram Reels, Facebook Reels, and supported browser URLs use similarly narrow rules.

Important limitation
Accessibility text differs between app versions, devices, and languages. If a Shorts player is not recognized, the app leaves it untouched rather than risking closing the entire host app.

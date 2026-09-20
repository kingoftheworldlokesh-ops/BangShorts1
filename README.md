# BangShorts

BangShorts is an Android focus helper that blocks selected short-form destinations without closing the host app.

Behavior
- Normal YouTube navigation and long-form videos remain usable.
- The first confidently recognized Shorts/Reels video is allowed once per Accessibility Service session.
- Later distinct Shorts/Reels videos are blocked while Focus mode or an active schedule is enabled.
- Repeated accessibility events for the allowed first video do not block it.

Accessibility text differs between app versions, devices, and languages. If a screen cannot be confidently identified, BangShorts leaves it untouched rather than risking the host app.

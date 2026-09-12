# Cozy Candy Bloom

A calm, cozy match-3 game (Candy-Crush style) for Android, forked from
[CandyCozy](https://github.com/TomasThrawat/CandyCozy) and reskinned with
game assets generated live by [GameAssetMCP](https://github.com/TomasThrawat/GameAssetMCP)
(the `engine mcp` custom MCP server) instead of emoji glyphs.

## What's different from CandyCozy
- New default skin **"Cozy Bloom (MCP)"** — six candy flavors rendered as
  flat-vector SVG icons (heart, chest, star, gem, potion, coin), generated
  procedurally by the `generate_icon` tool on the GameAssetMCP server and
  converted to Android `VectorDrawable`s (`res/drawable/candy_*.xml`).
- `CandyVisual` gained an optional `iconRes` field; `GameView.drawCandyAt()`
  now draws a cached bitmap of the vector icon when one is set, falling back
  to the original emoji-drawing path for every other skin (so all the
  original skins — Fruit Salad, Bright Shapes, Bakery Box, etc. — still work
  unchanged).
- Everything else — the pure `Match3Engine` (8x8 grid, swap/match/cascade
  logic), the synthesized on-device sound effects, the shop/boosters/levels
  system — is untouched from CandyCozy.

## Building the APK
1. Install Android Studio (Koala or newer recommended).
2. File -> Open and select this folder.
3. Let Android Studio sync Gradle (needs internet to download SDK 34 +
   Kotlin 1.9.24 + AGP 8.4.0, all free).
4. Build -> Build Bundle(s) / APK(s) -> Build APK(s), or press Run to install
   straight onto a connected/emulated device.
5. The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow (`.github/workflows/build-apk.yml`) also builds the
debug APK automatically on every push to `main` (and on manual dispatch),
uploading it as a build artifact and attaching it to a release.

## Notes
- `minSdk 24`, `targetSdk 34` / `compileSdk 34`.
- No animation-scale system settings are touched or required — the 60fps
  loop is driven entirely by the app's own render loop.

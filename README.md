# 1141 App

An unofficial native Android app for reading manga from [senkuro.me](https://senkuro.me).
Package: `me.senkuro.unofficial`. Kotlin + Jetpack Compose; data comes from the GraphQL API at `https://api.senkuro.me/graphql`.

## Features

- Home: latest updates, popular today, new titles, recommendations
- Catalog: search, sorting (popularity, rating, chapter upload date, title creation date; direction can be toggled), filters (type, format, status, translation status, age rating, labels; include/exclude)
- Title page: description, labels, translation branches, chapter list, "Continue reading"
- Reader: vertical page strip, jump to the previous/next chapter
- Account notifications (bell icon on the home screen with a counter, "New" and "Read" tabs)
- Site login (via WebView, with captcha) and server-side bookmarks: a "Bookmarks" tab, collections, adding/removing from the title page (star)
- Site bookmarks replace local favorites and history; read chapters are marked on the device
- Streamer mode (a separate switch in settings, available after login, independent of the theme): blurs the logged-in user's nickname, avatar, level, points and credits on the home screen, in the profile, tops, rankings and comments. Before Android 12 a placeholder is drawn instead of a blur

## Design

The styling is adapted from the Figma UI kit "Webtoon App UI (Free UI Kit)": light pages (home, search, library) with text `#3C3C3C`,
gray `#8C8C8C`, lines `#E0E0E0` and field background `#F8F8F8`; the title page and the reader are dark (`#161616`, chapter rows `#2C2C2C`, lines `#464545`);
the accent color is green `#00DC64`. Home: numbered "Trending" rows, three per column, and poster rows with a corner radius of 4;
the bottom bar is white with a line on top. Title page: cover under a dark overlay, genre, title, author, a pill-shaped button, chapter rows with a thumbnail and "#N".
Images and covers from the kit were not copied into the app, and the system font is used instead of Arial.

## GraphQL queries

All queries are located in [`app/src/main/assets/graphql`](app/src/main/assets/graphql) and are loaded by file name.

The list of queries and their purpose is in [docs/graphql.md](docs/graphql.md).

The queries are the same ones the senkuro.me website runs (taken from its JS). Client headers and addresses are in
[`SenkuroConfig.kt`](app/src/main/java/me/senkuro/unofficial/data/SenkuroConfig.kt).

## Building

Manual only: **Actions → Android build → Run workflow**, then choose `debug` or `release` in the `build_type` field.
The APK appears in the run's artifacts (`me.senkuro.unofficial-debug` / `me.senkuro.unofficial-release`; debug is installed as `me.senkuro.unofficial.debug` named "Debug", release as `me.senkuro.unofficial` named "Senkuro Unofficial").

### Debug tools (debug builds only)

In the debug build, the "Library" tab has a button "Debug: intercept site requests". It opens senkuro.me
in a built-in browser and records all GraphQL calls (operation, query, variables, response).
Besides GraphQL, it captures non-GET requests to senkuro hosts (login, OAuth) and page navigations; the `senkuro.me` and `sso.senkuro.net` buttons open the chosen domain in the browser. Queries already implemented in the app (names from `assets/graphql`) are hidden and shown as a counter; a switch turns them back on. Passwords, tokens, `code`/`state` and cookies are stripped. The log can be copied or sent as a file ("Share").

The release build does not include this:
- the "Files" tab of the interceptor: static site files (js, css, json, maps, fonts) are downloaded by the app and saved individually or as a ZIP archive; the "Find GraphQL in js" button extracts query texts from the bundles;
- the interception code lives in `app/src/debug`, and `app/src/release` contains only an empty stub;
- `BuildConfig.DEBUG_TOOLS` is `true` in debug and `false` in release, and the button is hidden;
- the `androidx.webkit` library is included only as `debugImplementation`;
- for `release`, the workflow inspects the APK and fails if it finds interception code in it.

### Persistent debug signing

All builds (debug, and release without your own secrets) are signed with the `app/debug.keystore` key from the repository (password `android`, alias `androiddebugkey`).
The key is the same on every Actions build, so a new version can be installed over the old one without uninstalling.
Note: the key is public and is not suitable for publishing the app.

### Release signing (optional)

If the repository secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and
`ANDROID_KEY_PASSWORD` are set, release is signed with your key. Without them it is signed with the persistent debug key.

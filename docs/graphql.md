# GraphQL queries

All queries are located in [`app/src/main/assets/graphql`](../app/src/main/assets/graphql) and are loaded by file name
(`SenkuroApi.execute(operation, file, variables, serializer)`). The queries were taken from the senkuro.me website's JS; operation names
and fragments match the site's. Client headers and domain addresses are in
[`SenkuroConfig.kt`](../app/src/main/java/me/senkuro/unofficial/data/SenkuroConfig.kt).

## Documented queries

| File | Purpose |
| --- | --- |
| `fetchMangas` | catalog and search, as on the site (`mangas`, cursor pagination, 20 per page): sorting, all filters, the title rating in the response, the server-side "Bookmark status"; also used for the character, card and author pages |
| `fetchMangaFilters` | catalog label groups (`allLabels`: roots such as "Main cast", "Traits"…, popular labels first) |
| `fetchManga` | title page (`manga(slug)`): names, description (Tiptap), labels, branches and teams, license and region block |
| `fetchMangaChapters` | chapters of a branch (`mangaChapters`, 100 per page with a cursor); branches come from `fetchManga` |
| `fetchMangaChapter` | chapter pages by slug (`mangaChapter`, `cdnQuality: "auto"`; fields `original` and the compressed version `compress`) |
| `fetchViewer` | account and bookmark collections |
| `fetchViewerUnreadNotifications`, `fetchNotificationFeeds` | notifications |
| `bulkReadNotifications`, `bulkDeleteNotifications` | "Mark all as read", deleting notifications |
| `fetchUser`, `fetchUserAchievements` | profile screen (avatar in the header) and achievements |
| SSO `/api/v1/captcha-config`, `/api/v1/auth/login`, `/oauth/authorize`, `/oauth/token` | native login: email/password/2FA form, captcha in a WebView, OAuth+PKCE performed by the app (the SSO host depends on the domain: `sso.senkuro.net` for senkuro.me, `sso.senkuro.org` for senkuro.com) |
| `fetchThreadComments`, `createComment`, `updateComment`, `deleteComment` | page comments in the reader |
| `fetchConversations`, `fetchConversationMessages`, `createConversationMessage`, `readConversationMessage`, `bulkDeleteConversationMessages` | private messages (Settings → Messages) |
| `fetchGuilds`, `fetchGuild`, `fetchGuildMembers` | guilds (Settings → Guilds) |
| `fetchUserCards` | my cards (Settings → My cards) |
| `fetchCards`, `fetchCard`, `viewCard` | card catalog (Settings → Card catalog), card page |
| `fetchCharacter` | character page: titles and cards (tap a character in a title or card) |
| `fetchUserTasks` | tasks (Settings → Tasks) |
| `fetchLevelUsers` | levels page (header → your account's "Lv.") |
| `redeemRedemption` | promo code (Settings → My account → Promo code) |
| `fetchCardOffers`, `acceptCardOffer`, `closeCardOffer` | card trades (Settings → Card trades) |
| `fetchMainUserTops` | daily tops feed on the home screen |
| `fetchMainAd`, `viewAd` | thin ad banner on the home screen under "Popular today" |
| `voteComment`, `deleteCommentVote` | like/dislike a comment |
| `fetchMangaBookmarkChapter` | the "To bookmark" button in a title (`viewerBookmark`) |
| `createUserBookmarkCollection`, `updateUserBookmarkCollection`, `deleteUserBookmarkCollection` | custom bookmark collections |
| `search` | site search: titles, characters, people, teams, guilds, users |
| `fetchTeams`, `fetchTeam`, `fetchTeamMembers`, `viewTeam` | translator teams |
| `fetchPerson`, `fetchMangaStaff` | title authors and the author page |
| `fetchPosts`, `fetchPost`, `viewPost`, `fetchFeed` | posts and the new-chapters feed |
| `voteCard`, `deleteCardVote` | vote for a card |
| `viewGuild`, `requestGuildMember`, `deleteGuildMember`, `depositGuildPoints` | guild actions |
| `updateConversationMessage` | edit your own message |
| `viewManga` | counts a title view |
| `voteManga`, `fetchMangaViewerState` | title rating 1–10 ("★ Rating" on the title screen) |
| `deleteMangaVote` | "Remove rating" |
| `fetchMangaRelations`, `fetchMangaCharacters` | the "Related" and "Characters" blocks on the title screen |
| `fetchPopularTodaySearchQueries` | popular queries as chips in the catalog |
| `readMangaChapter`, `updateMangaReaderSetting` | reading progress: chapter read, end of chapter, comments opened |
| `fetchMangaBookmarkNextChapters` | "Continue reading" on the home screen |
| `fetchUserBookmarks`, `updateUserBookmark`, `deleteUserBookmark` | bookmarks on the site |
| `homeLatest`, `homeNew`, `homePopular`, `homeRecommendations` | home screen sections |

## Queries without a detailed description

These files exist in `assets/graphql` but are not yet in the table above. The names are grouped by meaning, based on the file
names; check the code (`CardsRepository`, `SocialRepository`, `EcosystemRepository`, etc.) for details.

- **Decks, packs, forge, shop:** `fetchCardDecks`, `fetchCardDeck`, `createCardDeck`, `deleteCardDeck`, `voteCardDeck`,
  `deleteCardDeckVote`, `fetchCardPacks`, `fetchCardPack`, `openCardPack`, `bulkChooseCardPackOpen`, `fetchPendingPackOpens`,
  `fetchPendingCardContest`, `votePendingCard`, `fetchForgeCardsSlots`, `forgeCards`, `fetchCollectibles`,
  `equipUserCollectible`, `createCollectibleOrder`
- **Notification settings:** `fetchUserNotificationPreferences`, `updateUserNotificationPreference`,
  `deleteUserNotificationPreference`, `fetchNotificationFeedsBasic`, `fetchNotificationFeedsRich`
- **Profile:** `fetchActivities`, `fetchUserRanks`, `fetchUserAppearance`, `fetchUserProfileExtra`, `fetchViewerBalance`
- **Other:** `fetchMangaPage`, `fetchShortsMangaChapters`, `homePopularPeriod`

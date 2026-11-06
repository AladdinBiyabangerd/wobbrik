# Wobbrik Android alış adapteri

Kotlin + Compose hostuna qoşulan iki kitabxana modulu: `core` və `android`. Host oyun tətbiqi/APK, real SKU, credential və Console qurulumu yaradılmır. Backend HTTPS origin, host packageName və Firebase sessiyası sonradan verilir.

## Axın

1. Daimi oyun hesabı ilə login; anonim hesabla alış yoxdur.
2. Authenticated backend catalog paket adını, məhsul ID-sini və `durable`/`consumable` növünü verir. Sikkə miqdarını client seçmir.
3. Təzə Play ProductDetails/offer və yerli qiymət alınır. Checkout backend account identity-si ilə açılır.
4. PURCHASED tokeni əvvəl UID-yə bağlı Keystore/AES-GCM jurnalına commit edilir, sonra backend verify çağırılır. PENDING hüquq və sikkə vermir.
5. Backend alış, kredit ledger-i və finalization job-u atomik saxlayır. Ack/consume yalnız serverdə çatdırılmadan sonra olur; client bunları çağırmır.
6. `billing/state` vahid server snapshot-ı qaytarır: daimi hüquqlar və `purchased_coins`. Lokal cache bu snapshot ilə **əvəz edilir**; callback-də `+500` və ya başqa artım əməliyyatı yoxdur.

## Bərpa və təkrarlar

- Daimi kosmetika server hüquq siyahısından bərpa olunur; Play-də mövcud non-consumable-lar da yenidən yoxlanır.
- Consumable artıq Play-dən silinmiş/consumed olsa belə, yeni cihaz serverdən mövcud balansı alır; əvvəlki paket yenidən verilmir.
- Server commit-dən sonra cavab itərsə eyni token retry edilir. Backend eyni tokeni ikinci dəfə kreditləşdirmir.
- Play köhnə consumed tokeni artıq qaytarmırsa, yalnız həmin UID + məhsul + token üzrə əvvəlki purchase/ledger qəbzi `delivery_confirmed=true` verə bilər. Bu, tarixi çatdırılma təsdiqidir; yeni grant və refund siyasəti deyil.
- Consumed token üçün serverdə kredit qəbzi yoxdursa avtomatik kredit verilmir. Köhnə client-only alışları miqrasiya etmək ayrıca işdir.
- Proses ölərsə queue saxlanır və foreground-da retry olur. Network xətası cache/queue-nu silmir.
- Hesab və ya login epoch dəyişərsə gec cavab yeni hesaba tətbiq edilmir. Cache oxunarkən logout olsa belə əvvəlki hesabın balansı qaytarılmır.
- Onlayn uğurlu refresh cache-i tam əvəz edir; backup/cloud union köhnə hüquqları və sikkələri geri qaytarmır.

## Host inteqrasiyası

```kotlin
val sessions = AccountSessions { expectedUid ->
    val user = firebaseAuth.currentUser ?: throw AccountChanged()
    if (user.uid != expectedUid || user.isAnonymous) throw AccountChanged()
    user.getIdToken(false).await().token ?: throw AccountChanged() // Hostun Task/coroutine adapteri.
}
val journal = SecurePurchaseJournal(applicationContext)
val api = AppBackendClient(backendHttpsOrigin, sessions)
val billing = BillingCoordinator(sessions, api, journal, applicationContext.packageName)
val play = PlayBillingBridge(applicationContext, billing)
```

Firebase auth listener-i hər giriş/çıxışda `sessions.onAuthChanged(nonAnonymousUidOrNull)` çağırmalıdır. Foreground-da `play.onForeground()`; mağazada `play.offers(productId)` və seçilmiş cari offer üçün `play.buy(activity, productId, offerToken)` istifadə edilir. `play.notices` nəticəsində UI `billing.activeSnapshot()`-ı yenidən oxuyur. Qiymət Play-in formattedPrice mətnidir.

`activeSnapshot().purchasedCoins` legacy adlı read-only cache-dir: eyni server wallet-ində alış və gameplay kreditləri saxlandığı üçün cari ümumi balansı göstərir. Snapshot cache-i əvəz edir; bu dəyəri Firestore və ya başqa wallet-ə `+delta` kimi köçürmə. Billing krediti gameplay revision-unu artırmır; eyni revision yeni balansı atmaq üçün əsas deyil. Gameplay API/read/sync müqaviləsi AppBackend `docs/wobbrik-game-api.md` faylındadır; Android gameplay client-i və bazar handler-ləri hələ hazır deyil.

Hesab silmə server baryeri təsdiqlənəndən sonra `clearDeletedAccount(uid)` çağırır; bu lokal təmizləmə tam server deletion coordinator-u deyil. Alışın başqa hesaba transferi və post-deletion refund siyasəti bu işlə seçilmir.

## Yaddaş və SDK

`wobbrik_billing.xml` şifrələnmiş SharedPreferences, `wobbrik.billing.v1` Keystore açarı, UID-yə bağlı AAD. Token və Firebase ID tokeni log edilmir; ID token saxlanmır. Bir app prosesi üçün bir journal/bridge instance istifadə et.

Hostun mövcud backup qaydalarına `wobbrik_billing_backup_rules.xml` və API31+ `wobbrik_billing_data_extraction_rules.xml` istisnalarını birləşdir. Kitabxana hostun digər backup qaydalarını overwrite etmir. KeyStore/ciphertext xətası səssiz reset edilmir.

Kotlin 2.1.10, Gradle 8.13, AGP 8.13.0, Play Billing 9.1.0, compileSdk36. 4 noyabr 2026 məhsul qərarı: minimum Android 7.0 (minSdk24), hədəf Android 16 (targetSdk36). Billing modulunda tətbiq edilib; gələcək application host eyni hədləri saxlamalıdır.

## Yoxlama

JDK21 və Android SDK36 ilə:

```sh
./gradlew :core:test :android:assembleDebug :android:lintDebug --no-daemon
```

19 core ssenarisi: kosmetika restore, təkrar coin callback, consumed token olmayan yeni cihaz, itmiş cavab/process restart, pending, disk/network xətası, account/epoch dəyişməsi, logout zamanı cache, mənfi balansın rəddi və revoked cache. AAR/lint sınağı real Google alışını və cihaz KeyStore davranışını təsdiqləmir; onlar real dəyişənlər veriləndə ayrıca yoxlanacaq.

### Android application auth bootstrap

The app now owns one `AuthenticatedRecoveryOwner`, disk journal and Firebase Auth
listener per process. Existing Firebase sessions are observed; this does not create
an anonymous account or add sign-in UI. ID tokens come from the current Firebase
user on each request (SDK refresh enabled), with account/epoch guards around I/O.
Process foreground retries recovery; Activity recreation does not create writers.

Supply the actual AppBackend HTTPS origin through the Gradle property
`wobbrik.backendOrigin` (for example in user-local `~/.gradle/gradle.properties`).
Only an HTTPS origin is accepted; no path, credentials, query or fragment. No
production endpoint is assumed. The existing git-excluded `app/google-services.json`
selects Firebase. Without either configuration the application reports
`MissingFirebase` / `MissingBackendOrigin` through `WobbrikApplication.bootstrap`
and does not start recovery. Invalid nonempty origins fail explicitly.

Guest → Google link uses Credential Manager and `linkWithCredential` only (same UID;
no `signInWithCredential` fallback). Provide the Web OAuth client ID via
`wobbrik.googleWebClientId`, or ensure `google-services.json` generates
`default_web_client_id`. Enabling the Google provider in Firebase Console remains a
separate explicit step; this repo does not change Console settings.

`MainActivity` hosts `OnlineGame` on the application-owned `AcceptedEngineHost`.
Offline `PracticeGame` remains debug/fixture-only. Restart/abandon stay unbound.
Live auth, process death and backend connectivity still need configured device
verification.
Firebase listener semantics: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
Google account linking: https://firebase.google.com/docs/auth/android/account-linking

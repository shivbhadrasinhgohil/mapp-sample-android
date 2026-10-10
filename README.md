# mapp-sample-android

A tiny Android app that is **insecure on purpose**, built to show what the
[MAPP Scan GitHub Action](https://github.com/shivbhadrasinhgohil/MAPP) finds in an APK.

> ## WARNING: INTENTIONALLY VULNERABLE. DEMO MATERIAL FOR A SECURITY SCANNER.
>
> Every weakness in this repository is deliberate. The code, manifest, network
> security config and assets exist only so that a static scanner has something
> to find.
>
> - **Never ship this app**, publish it to any store, or reuse any of its code or settings.
> - **Never install it on a device that holds real data.** It is debuggable, exports
>   components with no permission and trusts every TLS certificate.
> - **Every secret in it is fake.** The AWS values are Amazon's documented
>   example credentials, the Google and "secret" values are made-up strings, and the
>   Stripe value is a test-mode placeholder. Nothing here is, or ever was, a live credential.
> - Hosts are `example.com` names that this project does not operate.

## What the Action does

[MAPP](https://github.com/shivbhadrasinhgohil/MAPP) (Mobile App Protection Platform) is a
static APK security scanner. Its Scan module reads a built APK (manifest, network security
config, string resources, assets and DEX bytecode), runs rules mapped to OWASP MASVS v2, and
writes a report. The **MAPP Scan** GitHub Action wraps that:

1. picks a `mapp` binary for the runner (or builds it from source when no release exists yet),
2. runs `mapp scan <apk>`,
3. uploads the findings to **GitHub code scanning** as SARIF,
4. attaches the report to the workflow run, and
5. can fail the job at a severity threshold (`fail-on`).

This repository's workflow, [`.github/workflows/mapp-scan.yml`](.github/workflows/mapp-scan.yml),
builds the debug APK with Gradle and then runs the Action on it:

```yaml
- uses: shivbhadrasinhgohil/MAPP@main
  with:
    apk: app/build/outputs/apk/debug/app-debug.apk
    format: sarif
    fail-on: none        # report everything, never fail the demo job
    upload-sarif: 'true'
```

After a run, open **Security > Code scanning** to see the alerts. The SARIF report and the
APK are also attached to the run as artifacts (`mapp-report` and `mapp-sarif` hold the
report, `app-debug-apk` the APK).

Notes on the first run:

- Until MAPP publishes a release, the Action falls back to building `mapp` from source,
  which adds a few minutes. The log says so.
- Pull requests from forks cannot upload SARIF (the token has no `security-events: write`),
  so the alerts appear only for pushes and same-repository pull requests.

## The app

Package `com.example.mappsample`, Kotlin, plain `LinearLayout` UI, one module, AGP 8.9.1,
Kotlin 2.1.20, compileSdk/targetSdk 35, minSdk 24, JDK 17. The only libraries are AndroidX
`appcompat` and `core-ktx`. Each weak spot carries a `MAPP demo: <rule id>` comment in the
source, the manifest or the resource files, so you can grep for it:

```sh
grep -rn "MAPP demo:" app/src
```

## Intended finding, rule id, where

"Observed" is what `mapp scan` reported on a locally assembled approximation of this app (the
Kotlin was compiled and dexed by hand against `android.jar` and packaged with `aapt2`, not by
Gradle), using the MAPP source as of this commit. Your CI run uses the real Gradle build and the
real AndroidX libraries, so counts and Info-level noise can differ slightly.

### Manifest and resources

| Intended finding | Rule id | Where | Observed |
|---|---|---|---|
| App is debuggable | `MANIFEST-DEBUGGABLE` | `AndroidManifest.xml`, `android:debuggable="true"` | High |
| Backups allowed | `MANIFEST-ALLOW-BACKUP` | `AndroidManifest.xml`, `android:allowBackup="true"` | Medium |
| Cleartext traffic allowed | `NSC-CLEARTEXT-PERMITTED` (and `MANIFEST-CLEARTEXT-TRAFFIC`, see note 2) | `usesCleartextTraffic="true"`, `res/xml/network_security_config.xml` `base-config` | High |
| User-installed CAs trusted | `NSC-USER-TRUST-ANCHORS` | `res/xml/network_security_config.xml`, `<certificates src="user" />` | High |
| Single certificate pin, no backup pin | `NSC-PIN-SET-WEAK` | `res/xml/network_security_config.xml`, `<pin-set>` (the digest is fake) | Medium |
| Web deep link not verified | `MANIFEST-DEEPLINK-NOT-VERIFIED` | `DeepLinkActivity` intent filter, http/https VIEW, no `autoVerify` | Medium |
| Exported activity without permission (bonus) | `MANIFEST-EXPORTED-ACTIVITY` | `DeepLinkActivity` | Medium |
| Exported provider without permission | `MANIFEST-EXPORTED-PROVIDER` | `NotesProvider`, `grantUriPermissions="true"` | High |
| Exported receiver without permission | `MANIFEST-EXPORTED-RECEIVER` | `DemoReceiver` | Medium |
| Exported service without permission | `MANIFEST-EXPORTED-SERVICE` | `SyncService` | High |
| Dangerous permissions | `MANIFEST-DANGEROUS-PERMISSIONS` | `READ_SMS`, `ACCESS_FINE_LOCATION` | Info |
| Google API key in a resource | `SECRET-GOOGLE-API-KEY` | `res/values/strings.xml` `maps_api_key`, referenced by the `com.google.android.geo.API_KEY` meta-data | Low |
| Credential-named string resource | `SECRET-RESOURCE-SECRET-NAME` | `res/values/strings.xml` `api_secret` | Medium (low confidence) |
| AWS secret access key in an asset | `SECRET-AWS-SECRET-KEY` | `assets/config.properties` (Amazon's documented example key, see note 1) | High (low confidence) |
| AWS access key ID in an asset | `SECRET-AWS-ACCESS-KEY` | `assets/config.properties` | **Not reported, by design** (see note 1) |

### Code (DEX)

| Intended finding | Rule id | Where | Observed |
|---|---|---|---|
| Weak cipher (DES/ECB) | `DEX-WEAK-CIPHER` | `InsecureCrypto.encrypt` | High |
| Weak cipher (RSA with PKCS#1 v1.5 padding, never called) | `DEX-WEAK-CIPHER` | `InsecureCrypto.rsa` | Medium |
| Weak hash (MD5) | `DEX-WEAK-HASH` | `InsecureCrypto.hash` | Low |
| Hard-coded encryption key | `DEX-STATIC-IV-OR-KEY` | `InsecureCrypto.encrypt`, `SecretKeySpec("12345678"...)` | High |
| Predictable random number for a session token | `DEX-INSECURE-RANDOM` | `MainActivity.newSessionToken`, `java.util.Random` | Medium |
| Logging a sensitive-looking string | `DEX-LOGGING-PRESENT` | `MainActivity.onCreate`, `Log.d(..., "password=...")` | Medium, plus an Info count |
| Password in SharedPreferences | `DEX-SHAREDPREFS-SENSITIVE` | `InsecureStorage.savePassword` | Medium |
| Data written to external storage | `DEX-EXTERNAL-STORAGE-WRITE` | `InsecureStorage.savePassword`, `getExternalFilesDir` | Info |
| SQL built by string concatenation | `DEX-SQL-RAW-QUERY` | `NotesDb.findByTitle`, `NotesProvider.query` | Medium |
| Command built from an intent extra | `DEX-RUNTIME-EXEC` | `MainActivity.onCreate`, `Runtime.exec("ping -c 1 " + host)` | Medium |
| World-readable preferences file | `DEX-WORLD-ACCESSIBLE-FILES` | `MainActivity.readLegacyPrefs`, `MODE_WORLD_READABLE` | High |
| WebView JavaScript enabled | `DEX-WEBVIEW-JS-ENABLED` | `DeepLinkActivity.onCreate` | Low |
| JavaScript bridge | `DEX-WEBVIEW-JS-INTERFACE` | `DeepLinkActivity.onCreate`, `addJavascriptInterface(DemoBridge(), "Android")` | Medium |
| WebView file access | `DEX-WEBVIEW-FILE-ACCESS` | `DeepLinkActivity.onCreate`, `allowFileAccess`, `allowUniversalAccessFromFileURLs` | Medium and High |
| TLS errors ignored | `DEX-TRUST-ALL-TLS` | `DeepLinkActivity`, `onReceivedSslError` calls `proceed()` | High |
| Intent data loaded into a WebView | `DEX-INTENT-EXTRA-TO-WEBVIEW` | `DeepLinkActivity.onCreate`, `loadUrl(intent data)` | High |
| Intent redirection | `DEX-INTENT-REDIRECT` | `DeepLinkActivity.onCreate`, `DemoReceiver.onReceive` | High |
| Dynamic receiver without permission | `DEX-DYNAMIC-RECEIVER-UNPROTECTED` | `MainActivity.onResume`, `registerReceiver` | Medium |
| Dynamic code loading | `DEX-DYNAMIC-CODE-LOADING` | `SyncService.onStartCommand`, `DexClassLoader` | Medium |
| Cleartext `http://` URL | `DEX-HTTP-URL-LITERAL` | `SyncService` thread, `URL("http://demo.example.com/api")`; `DeepLinkActivity` fallback URL | Medium |

### Notes

1. **AWS keys.** `assets/config.properties` holds Amazon's *documented example* credentials
   (`AKIAIOSFODNN7EXAMPLE` and its example secret). MAPP deliberately ignores access key IDs
   that end in `EXAMPLE` or look like placeholders, so `SECRET-AWS-ACCESS-KEY` stays quiet,
   which is the correct behaviour for a placeholder. The example secret key still trips
   `SECRET-AWS-SECRET-KEY`, because its shape and its `aws_secret_access_key=` name look like
   a real credential. The Stripe value in the same file is a test-mode placeholder and is
   ignored on purpose.
2. **Cleartext.** The manifest sets `usesCleartextTraffic="true"`. With `minSdk 24` and a
   readable network security config, MAPP lets the `NSC-*` rules decide and does not also
   emit `MANIFEST-CLEARTEXT-TRAFFIC`; the manifest flag only matters below API 24. The
   cleartext weakness is therefore reported once, as `NSC-CLEARTEXT-PERMITTED`.
3. **Extra Info findings.** The scan also reports Info-level facts (no root, debugger or
   emulator checks, no `FLAG_SECURE`, no obfuscation, SDK inventory). They are expected for a
   demo app and are not weaknesses planted on purpose.
4. **Exported launcher.** `MainActivity` is exported because it is the launcher entry point,
   which is not a finding.

## Run the scan locally

Build the APK (needs JDK 17 and the Android SDK) or download `app-debug-apk` from a workflow run:

```sh
./gradlew assembleDebug
# app/build/outputs/apk/debug/app-debug.apk
```

Build and run the `mapp` CLI from the MAPP repository (needs a stable Rust toolchain):

```sh
git clone https://github.com/shivbhadrasinhgohil/MAPP
cd MAPP/engine
cargo build --release -p mapp-cli

# human-readable report
./target/release/mapp scan /path/to/mapp-sample-android/app/build/outputs/apk/debug/app-debug.apk

# machine-readable, and fail (exit code 1) on anything High or worse
./target/release/mapp scan app-debug.apk --format sarif -o mapp-report.sarif --fail-on high

# every rule the scanner knows
./target/release/mapp rules
```

## Repository layout

```
.github/workflows/mapp-scan.yml   build + MAPP Scan Action
app/build.gradle.kts              single Android application module
app/src/main/AndroidManifest.xml  exported components, debuggable, backup, cleartext
app/src/main/res/xml/             network_security_config.xml
app/src/main/res/values/          strings.xml (fake keys)
app/src/main/assets/              config.properties (documented example credentials)
app/src/main/java/com/example/mappsample/   the deliberately weak Kotlin code
```

## Licence

MIT, copyright 2026 Shivbhadrasinh Gohil. See [`LICENSE`](LICENSE).
The licence covers this demo only; it is not a statement that any of this code is fit for use.

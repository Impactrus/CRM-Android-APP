# Android Security Audit Report — CRM-OC

**Data audytu:** 2026-03-15
**Audytor:** Claude Opus 4.6 (AI Security Audit)
**Wersja aplikacji:** 1.0 (versionCode 1)
**Zakres:** Kompletny przeglad bezpieczenstwa aplikacji Android
**Ocena ogolna: D (niewystarczajacy)**

---

## 1. Executive Summary

Aplikacja Android CRM-OC zawiera **5 krytycznych** i **6 wysokich** podatnosci bezpieczenstwa, ktore wymagaja natychmiastowej naprawy przed wdrozeniem produkcyjnym. Najwazniejsze problemy:

1. **Komunikacja HTTP plaintext** — caly ruch API odbywa sie przez nieszyfrowane HTTP (nie HTTPS), co umozliwia przechwycenie tokenow JWT i danych biznesowych w sieci
2. **Token JWT w plaintext SharedPreferences** — brak szyfrowania danych sesji na urzadzeniu
3. **ProGuard/R8 wylaczony** — binarki release nie sa zaciemniane, co ulatwia reverse engineering
4. **Brak certificate pinning** — podatnosc na ataki MITM nawet po migracji na HTTPS
5. **Backup danych wlaczony** — atakujacy z fizycznym dostepem moze wydobyc tokeny i dane sesji

---

## 2. Tabela Findings

| ID | Severity | Opis | Plik:linia | OWASP Ref |
|----|----------|------|------------|-----------|
| SEC-01 | **CRITICAL** | API base URL uzywa HTTP (plaintext) | `build.gradle:19` | M5 |
| SEC-02 | **CRITICAL** | `usesCleartextTraffic="true"` w Manifest | `AndroidManifest.xml:14` | M5 |
| SEC-03 | **CRITICAL** | Token JWT w plaintext SharedPreferences | `SessionManager.kt:7-8` | M9 |
| SEC-04 | **CRITICAL** | ProGuard/R8 minifyEnabled=false w release | `build.gradle:23-24` | M7 |
| SEC-05 | **CRITICAL** | Brak network_security_config.xml | brak pliku | M5, M8 |
| SEC-06 | **HIGH** | `allowBackup="true"` — dane sesji w backupie | `AndroidManifest.xml:8` | M9 |
| SEC-07 | **HIGH** | Brak certificate pinning (OkHttp/NSC) | `RetrofitClient.kt` | M5 |
| SEC-08 | **HIGH** | HttpLoggingInterceptor BODY w debug — tokeny w logcat | `RetrofitClient.kt:110-116` | M9 |
| SEC-09 | **HIGH** | Brak session timeout — token nigdy nie wygasa lokalnie | `SessionManager.kt` | M3 |
| SEC-10 | **HIGH** | Hardcoded IP serwera w build.gradle | `build.gradle:19` | M8 |
| SEC-11 | **HIGH** | Rola i userId trzymane w plaintext SharedPreferences | `SessionManager.kt:24-27` | M9 |
| SEC-12 | **MEDIUM** | Brak walidacji inputow przed wyslaniem do API | `NewRequestActivity.kt:111-166` | M4 |
| SEC-13 | **MEDIUM** | Brak proguard-rules.pro w repozytorium | brak pliku | M7 |
| SEC-14 | **MEDIUM** | Brak sprawdzania integralnosci tokena (claims) po stronie klienta | `MainActivity.kt:27-29` | M3 |
| SEC-15 | **MEDIUM** | Logout nie czeka na odpowiedz serwera | `AuthRepository.kt:13-21` | M3 |
| SEC-16 | **MEDIUM** | Brak root/jailbreak detection | brak implementacji | M8 |
| SEC-17 | **LOW** | Brak biometric authentication jako opcja dodatkowa | brak implementacji | M1 |
| SEC-18 | **LOW** | userId przekazywany w request body zamiast z tokena | `DashboardActivity.kt:126` | M3 |
| SEC-19 | **LOW** | Error messages wyswietlane w Toast moga ujawniac informacje o infrastrukturze | `DashboardActivity.kt:155` | M4 |
| SEC-20 | **INFO** | Brak test frameworka (brak testow bezpieczenstwa) | `build.gradle` | M8 |
| SEC-21 | **INFO** | compileSdk/targetSdk 34 — aktualne, OK | `build.gradle:8,12` | — |

---

## 3. Szczegolowy opis findings

### SEC-01 [CRITICAL] — HTTP Plaintext Communication

**Plik:** `Android/app/build.gradle:19`

```kotlin
buildConfigField "String", "API_BASE_URL", "\"http://10.0.1.216\""
```

Caly ruch sieciowy (wlacznie z loginem i tokenami JWT) odbywa sie przez nieszyfrowane HTTP. Atakujacy w tej samej sieci (WiFi, LAN) moze przechwycic:
- Loginy i hasla (POST /api/auth/login)
- Tokeny JWT (kazdy request)
- Dane biznesowe (wnioski, limity kredytowe, dane kontrahentow)

**Rekomendacja:** Zmienic base URL na `https://` i skonfigurowac certyfikat TLS na serwerze 10.0.1.216. Jesli to siec wewnetrzna, nadal wymagane jest TLS — ataki ARP spoofing/MITM sa trywialne w sieciach LAN.

---

### SEC-02 [CRITICAL] — Cleartext Traffic Allowed

**Plik:** `Android/app/src/main/AndroidManifest.xml:14`

```xml
android:usesCleartextTraffic="true"
```

Explicite zezwolenie na nieszyfrowany ruch HTTP. Od Android 9 (API 28) cleartext jest domyslnie blokowany — ta flaga jawnie wylacza te ochrone.

**Rekomendacja:** Ustawic `android:usesCleartextTraffic="false"`. Jesli HTTP jest tymczasowo potrzebne do dewelopmentu, uzyc `network_security_config.xml` z domena wyjatku tylko dla debug buildu.

---

### SEC-03 [CRITICAL] — JWT Token in Plaintext SharedPreferences

**Plik:** `Android/app/src/main/java/com/ossadkowski/app/data/SessionManager.kt:7-8`

```kotlin
private val prefs: SharedPreferences =
    context.getSharedPreferences("crm_session", Context.MODE_PRIVATE)
```

Token JWT, rola uzytkownika, userId i username sa trzymane w standardowym SharedPreferences w postaci plaintext. Na zrootowanym urzadzeniu lub przy wlaczonym backupie, te dane sa latwo dostepne.

Plik XML na urzadzeniu: `/data/data/com.ossadkowski.app/shared_prefs/crm_session.xml`

**Rekomendacja:** Uzyc `EncryptedSharedPreferences` z biblioteki `androidx.security:security-crypto:1.1.0-alpha06`:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val prefs = EncryptedSharedPreferences.create(
    context,
    "crm_session_encrypted",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

### SEC-04 [CRITICAL] — ProGuard/R8 Disabled in Release

**Plik:** `Android/app/build.gradle:23-24`

```groovy
release {
    minifyEnabled false
```

Binarki release nie sa zaciemniane. Atakujacy moze latwo zdekompilowac APK (np. przez jadx/apktool) i przeanalizowac:
- Logike autentykacji i token refresh
- Wszystkie endpointy API
- Strukture danych i modele
- Logike biznesowa

**Rekomendacja:** Wlaczyc minification i shrinking:

```groovy
release {
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
}
```

Dodac reguly ProGuard dla Retrofit, Gson i modeli danych.

---

### SEC-05 [CRITICAL] — Missing network_security_config.xml

Brak pliku `res/xml/network_security_config.xml`. Bez tego pliku:
- Nie ma certificate pinning
- Nie ma kontroli nad zaufanymi CA
- Nie mozna ograniczyc cleartext do debug-only

**Rekomendacja:** Utworzyc `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <domain-config>
        <domain includeSubdomains="true">10.0.1.216</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">BASE64_ENCODED_PIN</pin>
        </pin-set>
    </domain-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

Dodac w Manifest: `android:networkSecurityConfig="@xml/network_security_config"`

---

### SEC-06 [HIGH] — Backup Enabled

**Plik:** `AndroidManifest.xml:8`

```xml
android:allowBackup="true"
```

ADB backup (`adb backup com.ossadkowski.app`) pozwala wydobyc cale SharedPreferences (w tym token JWT) z urzadzenia bez roota.

**Rekomendacja:** Ustawic `android:allowBackup="false"` lub dodac `android:fullBackupContent="@xml/backup_rules"` z wykluczeniem `shared_prefs/crm_session.xml`.

---

### SEC-07 [HIGH] — No Certificate Pinning

**Plik:** `RetrofitClient.kt` — OkHttpClient builder

Brak `CertificatePinner` w OkHttpClient. Po migracji na HTTPS, atakujacy z zainstalowanym wlasnym CA na urzadzeniu moze nadal przechwycic ruch (np. przez narzedzia proxy jak mitmproxy/Burp Suite).

**Rekomendacja:** Dodac certificate pinning w OkHttpClient:

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("10.0.1.216", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
```

---

### SEC-08 [HIGH] — HTTP Logging Exposes Tokens in Debug

**Plik:** `RetrofitClient.kt:110-116`

```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
```

W trybie debug, BODY level loguje pelne request/response headers i body, w tym:
- `Authorization: Bearer <JWT_TOKEN>` — w kazdym uwidocznionym requeście
- Haslo w POST /auth/login body
- Dane biznesowe

Te dane trafiaja do logcat, ktory jest dostepny dla innych aplikacji na starszych Androidach (< 4.1) i zawsze dla ADB.

**Rekomendacja:** Uzyc `Level.HEADERS` zamiast `Level.BODY` w debug, z redaction headerow:

```kotlin
HttpLoggingInterceptor { message ->
    Log.d("HTTP", message.replace(Regex("Bearer [^ ]+"), "Bearer [REDACTED]"))
}.apply {
    level = if (BuildConfig.DEBUG) Level.HEADERS else Level.NONE
    redactHeader("Authorization")
}
```

---

### SEC-09 [HIGH] — No Local Session Timeout

**Plik:** `SessionManager.kt`

Brak jakiegokolwiek mechanizmu wygasania sesji po stronie klienta. Jesli uzytkownik nie wyloguje sie recznie, token pozostaje w SharedPreferences bezterminowo (az do wygasniecia JWT po stronie serwera).

Brak tez sprawdzania, czy token nie wygas przed jego uzyciem — aplikacja po prostu sprawdza `token != null` (linia 41).

**Rekomendacja:** Dodac timestamp logowania i sprawdzac waznosc lokalna:

```kotlin
fun saveSession(...) {
    prefs.edit()
        .putLong("login_timestamp", System.currentTimeMillis())
        // ... reszta
        .apply()
}

val isSessionValid: Boolean get() {
    val loginTime = prefs.getLong("login_timestamp", 0)
    val elapsed = System.currentTimeMillis() - loginTime
    return token != null && elapsed < SESSION_TIMEOUT_MS // np. 8h
}
```

---

### SEC-10 [HIGH] — Hardcoded Server IP in Build Config

**Plik:** `build.gradle:19`

```groovy
buildConfigField "String", "API_BASE_URL", "\"http://10.0.1.216\""
```

IP serwera jest hardcoded w kodzie. Po dekompilacji APK atakujacy zna dokladny adres serwera API w sieci wewnetrznej. Brak rozroznienia miedzy srodowiskami (dev/staging/prod).

**Rekomendacja:** Uzyc roznych buildTypes/flavors:

```groovy
buildTypes {
    debug {
        buildConfigField "String", "API_BASE_URL", "\"https://dev-api.ossadkowski.com\""
    }
    release {
        buildConfigField "String", "API_BASE_URL", "\"https://api.ossadkowski.com\""
    }
}
```

---

### SEC-11 [HIGH] — Sensitive User Data in Plaintext SharedPreferences

**Plik:** `SessionManager.kt:19-27`

Oprócz tokena JWT, w plaintext SharedPreferences trzymane sa takze: userId, rola, username, dzial, employeeCacheId. Manipulacja tymi wartosciami (np. zmiana roli na "Admin") moze byc wykorzystana do ominiecia kontroli dostepu po stronie klienta.

**Rekomendacja:** Migracja na EncryptedSharedPreferences (patrz SEC-03). Dodatkowo, nigdy nie ufac roli/userId z klienta — serwer powinien weryfikowac z tokena JWT.

---

### SEC-12 [MEDIUM] — Insufficient Input Validation

**Plik:** `NewRequestActivity.kt:111-166`

Walidacja inputow ogranicza sie do sprawdzenia, czy pola nie sa puste. Brak:
- Walidacji formatu dat (mozliwe wstrzykniecie zlych wartosci)
- Ograniczenia dlugosci opisu/powodu
- Sanityzacji danych przed wyslaniem do API
- Walidacji, ze data koncowa >= data poczatkowa

**Rekomendacja:** Dodac walidacje:
- Regex na format daty `yyyy-MM-dd`
- Sprawdzenie `endDate >= startDate`
- Limit dlugosci stringow (np. 1000 znakow)
- Escapowanie specjalnych znakow

---

### SEC-14 [MEDIUM] — No Client-side Token Integrity Check

**Plik:** `MainActivity.kt:27-29`

```kotlin
if (session.isLoggedIn) {
    navigateToDashboard()
    return
}
```

Sprawdzenie logowania polega wylacznie na `token != null`. Brak:
- Dekodowania JWT i sprawdzenia `exp` claim
- Walidacji czy token nie jest uszkodzony/zmodyfikowany
- Proaktywnego refresh jesli token jest bliski wygasniecia

**Rekomendacja:** Dodac dekodowanie JWT (bez weryfikacji sygnatury — to robi serwer) i sprawdzanie `exp`:

```kotlin
fun isTokenExpired(): Boolean {
    val token = token ?: return true
    val parts = token.split(".")
    if (parts.size != 3) return true
    val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
    val exp = JSONObject(payload).optLong("exp", 0)
    return System.currentTimeMillis() / 1000 > exp
}
```

---

### SEC-15 [MEDIUM] — Logout Ignores Server Response

**Plik:** `AuthRepository.kt:13-21`

```kotlin
suspend fun logout(): NetworkResult<Any> {
    return safeApiCall {
        try {
            RetrofitClient.apiService.logout()
        } catch (e: Exception) {
            // Ignore logout failures
        }
    }
}
```

Logout po stronie serwera moze sie nie powiesc (np. brak sieci), ale klient i tak czysci sesje. Oznacza to, ze token JWT nadal jest wazny na serwerze po "wylogowaniu" po stronie klienta.

**Rekomendacja:** Informowac uzytkownika jesli logout po stronie serwera sie nie powiodl. Rozwazyc token blacklisting po stronie serwera.

---

### SEC-16 [MEDIUM] — No Root/Jailbreak Detection

Brak jakiejkolwiek detekcji zrootowanego urzadzenia. Na zrootowanym urzadzeniu atakujacy ma pelny dostep do SharedPreferences, pamieci procesu i ruchu sieciowego.

**Rekomendacja:** Dodac detekcje root (np. biblioteka rootbeer lub SafetyNet/Play Integrity API). Ostrzegac uzytkownika lub blokowac dzialanie na zrootowanych urzadzeniach.

---

### SEC-18 [LOW] — userId from Client Instead of Token

**Plik:** `DashboardActivity.kt:126`

```kotlin
"wnioski" -> viewModel.loadWnioski(session.userId)
```

userId jest pobierany z lokalnego SessionManager i wysylany w request body. Atakujacy moze zmodyfikowac userId w SharedPreferences lub request, by zobaczyc wnioski innego uzytkownika. Serwer powinien ignorowac userId z ciala requestu i uzywac wylacznie wartosci z JWT.

**Rekomendacja:** Usunac userId z request body. Backend powinien wyciagac userId wylacznie z tokena JWT.

---

## 4. OWASP Mobile Top 10 (2024) — Mapowanie

| OWASP | Status | Findings |
|-------|--------|----------|
| **M1: Improper Credential Usage** | FAIL | SEC-03, SEC-11 — tokeny w plaintext, brak biometrics |
| **M2: Inadequate Supply Chain Security** | WARN | SEC-20 — brak testow, ale zależnosci sa aktualne |
| **M3: Insecure Authentication/Authorization** | FAIL | SEC-09, SEC-14, SEC-15, SEC-18 — brak timeout, brak walidacji tokena |
| **M4: Insufficient Input/Output Validation** | WARN | SEC-12, SEC-19 — minimalna walidacja inputow |
| **M5: Insecure Communication** | FAIL | SEC-01, SEC-02, SEC-05, SEC-07 — HTTP plaintext, brak TLS, brak pinning |
| **M6: Inadequate Privacy Controls** | WARN | Dane uzytkownikow nie sa szyfrowane na urzadzeniu |
| **M7: Insufficient Binary Protections** | FAIL | SEC-04, SEC-13 — brak ProGuard/R8, brak obfuscation |
| **M8: Security Misconfiguration** | FAIL | SEC-02, SEC-05, SEC-06, SEC-10, SEC-16 — cleartext, backup, hardcoded IP |
| **M9: Insecure Data Storage** | FAIL | SEC-03, SEC-06, SEC-08, SEC-11 — plaintext prefs, backup, logcat |
| **M10: Insufficient Cryptography** | FAIL | Zero szyfrowania danych lokalnych |

**Wynik: 7/10 kategorii FAIL, 2 WARN, 1 OK**

---

## 5. Rekomendacje naprawcze (priorytetyzowane)

### Priorytet 1 — NATYCHMIAST (przed jakimkolwiek wdrozeniem)

| # | Akcja | Effort | Findings |
|---|-------|--------|----------|
| 1 | Migracja API na HTTPS + wylaczenie cleartext | 2-4h (serwer + app) | SEC-01, SEC-02 |
| 2 | Zamiana SharedPreferences na EncryptedSharedPreferences | 1-2h | SEC-03, SEC-11 |
| 3 | Wlaczenie ProGuard/R8 w release + reguly dla Retrofit/Gson | 2-3h | SEC-04, SEC-13 |
| 4 | Ustawienie `allowBackup="false"` | 5 min | SEC-06 |

### Priorytet 2 — W ciagu 2 tygodni

| # | Akcja | Effort | Findings |
|---|-------|--------|----------|
| 5 | Dodanie network_security_config.xml z certificate pinning | 1-2h | SEC-05, SEC-07 |
| 6 | Dodanie session timeout (np. 8h) | 1h | SEC-09 |
| 7 | Redakcja tokenow w HttpLoggingInterceptor | 30 min | SEC-08 |
| 8 | Osobne base URL per buildType (dev/prod) | 30 min | SEC-10 |

### Priorytet 3 — W ciagu miesiaca

| # | Akcja | Effort | Findings |
|---|-------|--------|----------|
| 9 | Walidacja inputow (daty, dlugosci, formaty) | 2-3h | SEC-12 |
| 10 | Kliencka walidacja JWT exp claim | 1h | SEC-14 |
| 11 | Root detection (rootbeer lub Play Integrity) | 2-3h | SEC-16 |
| 12 | Usunac userId z request body — serwer z JWT | 4-6h (backend change) | SEC-18 |

### Priorytet 4 — Nice to have

| # | Akcja | Effort | Findings |
|---|-------|--------|----------|
| 13 | Biometric auth (odblokowanie sesji) | 4-6h | SEC-17 |
| 14 | Testy bezpieczenstwa (unit + instrumented) | 8-16h | SEC-20 |
| 15 | Sanityzacja error messages | 1h | SEC-19 |

---

## 6. Pozytywne aspekty

Nalezy odnotowac elementy, ktore sa poprawnie zaimplementowane:

1. **Token refresh z synchronizacja** — `RetrofitClient.kt:54` uzyta bloku `synchronized` do unikniecia race conditions przy odswiezaniu tokena. Poprawne sprawdzenie czy inny watek juz odswiezyl token.
2. **Redirect na login po 401** — poprawne czyszczenie sesji i przekierowanie na ekran logowania gdy refresh sie nie powiedzie.
3. **Logging warunkowy** — `HttpLoggingInterceptor` jest ustawiony na `NONE` w release (choc BODY w debug jest nadal ryzykowne).
4. **MODE_PRIVATE** — SharedPreferences uzywa trybu prywatnego (choc to nie zastepuje szyfrowania).
5. **Aktualne zaleznosci** — OkHttp 4.12.0, Retrofit 2.9.0, compileSdk 34 sa relatywnie aktualne.
6. **Minimalne uprawnienia** — tylko `INTERNET` permission w Manifest.

---

## 7. Ocena ogolna

| Kategoria | Ocena |
|-----------|-------|
| Przechowywanie danych | F |
| Komunikacja sieciowa | F |
| Autentykacja | C |
| Input validation | D |
| Konfiguracja buildu | D |
| **OGOLNA** | **D** |

Aplikacja w obecnym stanie **NIE powinna byc wdrazana produkcyjnie**. Krytyczne problemy z komunikacja HTTP plaintext i przechowywaniem tokenow w nieszyfrowanych SharedPreferences stanowia bezposrednie zagrozenie dla bezpieczenstwa danych uzytkownikow i danych biznesowych firmy. Szacowany czas naprawy Priorytetu 1: **5-9 godzin pracy dewelopera**.

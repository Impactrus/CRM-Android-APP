# CRM-Android-APP

Nowoczesny, zaawansowany system mobilny CRM dedykowany dla handlowców oraz menedżerów pracujących w terenie. Aplikacja została zbudowana od podstaw w języku Kotlin z myślą o stabilności, wysokiej wydajności oraz wsparciu pracy w trybie offline.

Projekt stanowi kompletny system kliencki integrujący się z serwerami AX/CRM za pomocą interfejsu REST API, oferując bogaty zestaw funkcjonalności optymalizujących codzienne procesy biznesowe.

---

## 🚀 Główne Funkcjonalności

### 1. Panel Handlowy (Sales Coverage)
* **Analiza pokrycia rynku**: Wizualny wskaźnik realizacji planów sprzedażowych oraz pokrycia kluczowych kontrahentów w regionie.
* **Statusy dostaw**: Monitorowanie postępu realizacji zamówień (od etapu pakowania po dostawę).
* **Szczegóły sprzedaży**: Dedykowane widoki z historycznymi danymi zakupowymi kontrahentów oraz limitami kredytowymi.

### 2. Logistyka i Zamówienia (Branża Nawozowa)
* **Koszyk zakupowy**: Mechanizm offline-first umożliwiający zbieranie zamówień u klienta w miejscach o słabym zasięgu i ich późniejszą synchronizację.
* **Integracja z bazami handlowymi**: Pobieranie aktualnych stanów magazynowych bezpośrednio z systemów centralnych.

### 3. Moduł Administracyjny & HR
* **Delegacje i Plany Urlopowe**: Wygodne składanie wniosków urlopowych bezpośrednio z poziomu aplikacji z systemem akceptacji dla kadry menedżerskiej.
* **Obieg dokumentów i wiadomości**: Wbudowany wewnętrzny komunikator oraz system powiadomień.

---

## 🛠️ Architektura i Technologie

Aplikacja wykorzystuje nowoczesne standardy architektury systemów mobilnych Android:

* **Język**: Kotlin (100%)
* **Architektura**: MVVM (Model-View-ViewModel) ze ścisłą separacją warstw (Data -> Domain -> Presentation).
* **Baza danych (Lokalny Cache)**: Android Room (wykorzystywany jako lokalna baza offline ze zoptymalizowanym czasem życia TTL dla zapytań).
* **Komunikacja sieciowa**: Retrofit 2 + OkHttp 4 (z mechanizmem tokenów autoryzacyjnych JWT i automatycznym odświeżaniem sesji przez `Authenticator`).
* **Asynchroniczność**: Kotlin Coroutines & Flow (zapewniające płynne działanie interfejsu UI podczas operacji dyskowych i sieciowych).
* **Zarządzanie sesją**: `EncryptedSharedPreferences` / standardowe bezpieczne SharedPreferences do ochrony tokenów autoryzacyjnych.

---

## 🔌 Ekosystem i Integracja (Geofencing)

Aplikacja współpracuje bezpośrednio z dedykowanym, autonomicznym modułem **CRM-Geofacing-APP** działającym w tle:
* **Udostępnianie sesji**: Współdzielenie autoryzacji (tokenu sesji) za pomocą bezpiecznego `sharedUserId`.
* **Geofencing & Organizer**: Rejestracja obecności u klienta za pomocą lokalizacji GPS, automatyczne powiadomienia oraz panel wprowadzania notatek z wizyt terenowych.

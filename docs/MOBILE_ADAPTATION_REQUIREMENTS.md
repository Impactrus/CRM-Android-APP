# Mobile Adaptation Requirements — CRM-OC Android

> Data: 2026-03-15 | Wersja: 1.0
> Dokument opisuje wymagania dostosowania aplikacji Android do aktualnego stanu backendu i frontendu.

---

## Spis tresci

- [A. Mapowanie API endpoints](#a-mapowanie-api-endpoints)
- [B. Wymagania per ekran](#b-wymagania-per-ekran)
- [C. Zmiany w istniejacych ekranach](#c-zmiany-w-istniejacych-ekranach)
- [D. Nowe modele danych](#d-nowe-modele-danych)
- [E. Priorytety biznesowe](#e-priorytety-biznesowe)

---

## A. Mapowanie API endpoints

Legenda: **Impl** = zaimplementowany w Android (TAK/NIE/CZESCIOWO), **P** = priorytet (P1-P4).

### A.1 Auth (`/auth`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 1 | POST | `/auth/login` | Logowanie | CZESCIOWO | P1 |
| 2 | POST | `/auth/register` | Rejestracja | NIE | P4 |
| 3 | GET | `/auth/profile` | Profil z JWT claims | NIE | P1 |
| 4 | POST | `/auth/refresh` | Odswiezenie tokenu | NIE | P1 |
| 5 | POST | `/auth/logout` | Wylogowanie (blacklist JTI) | NIE | P1 |

**Login Request DTO:**
```
LoginRequest { username: string [required, 3-100], password: string [required, 6-200] }
```

**Login Response (AuthResponse):**
```
{
  success: boolean,
  message: string,
  userId: int?,
  username: string?,
  token: string?,
  role: string?,
  dzial: string?,
  employeeCacheId: int?,
  claims: string[]?,
  claimsVersion: int?
}
```

**Profile Response:**
```
{
  userId: int,
  username: string,
  role: string,
  dzial: string,
  employeeCacheId: int?,
  claims: string[],
  claimsVersion: int
}
```

**Refresh Response:**
```
{ success: boolean, token: string }
```

**Logout Response:**
```
{ success: boolean, message: string }
```

### A.2 Wnioski (`/wnioski`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 6 | GET | `/wnioski/typy` | Slownik typow wnioskow | NIE | P1 |
| 7 | GET | `/wnioski/rodzaje-urlopu` | Slownik rodzajow urlopu | NIE | P1 |
| 8 | GET | `/wnioski/{id}` | Szczegoly wniosku | NIE | P2 |
| 9 | PUT | `/wnioski/{id}` | Edycja wniosku | NIE | P2 |
| 10 | DELETE | `/wnioski/{id}?userId=X` | Usuniecie wniosku | TAK | P2 |
| 11 | POST | `/wnioski` | Utworzenie wniosku | NIE | P1 |
| 12 | GET | `/wnioski/uzytkownicy` | Lista uzytkownikow (zastepstwa) | NIE | P1 |
| 13 | POST | `/wnioski/{id}/akceptacja-manager` | Akceptacja managera | TAK | P2 |
| 14 | POST | `/wnioski/{id}/akceptacja-hr` | Akceptacja HR | TAK | P2 |
| 15 | POST | `/wnioski/{id}/wyslij` | Wyslanie wniosku | TAK | P2 |
| 16 | POST | `/wnioski/{id}/wyslij-ponownie` | Ponowne wyslanie | TAK | P2 |
| 17 | POST | `/wnioski/{id}/pliki` | Upload pliku (multipart) | NIE | P3 |
| 18 | GET | `/wnioski/{id}/pliki` | Lista plikow | NIE | P3 |
| 19 | GET | `/wnioski/pliki/{plikId}/pobierz` | Pobranie pliku | NIE | P3 |
| 20 | DELETE | `/wnioski/pliki/{plikId}` | Usuniecie pliku | NIE | P3 |
| 21 | POST | `/wnioski/list` | Lista wnioskow (paginowana) | TAK | P1 |
| 22 | POST | `/wnioski/approvals` | Lista do akceptacji | TAK | P1 |
| 23 | GET | `/wnioski/adres/{userId}` | Adres uzytkownika | NIE | P3 |
| 24 | PUT | `/wnioski/adres/{userId}` | Zapis adresu | NIE | P3 |
| 25 | GET | `/wnioski/zamrozenia/check?userId&od&do_` | Sprawdzenie zamrozenia | NIE | P3 |
| 26 | GET | `/wnioski/zamrozenia` | Lista zamrozen | NIE | P3 |
| 27 | GET | `/wnioski/zamrozenia/miesiac?rok&miesiac` | Zamrozenia per miesiac | NIE | P3 |
| 28 | POST | `/wnioski/zamrozenia` | Utworzenie zamrozenia | NIE | P4 |
| 29 | PUT | `/wnioski/zamrozenia/{id}` | Edycja zamrozenia | NIE | P4 |
| 30 | DELETE | `/wnioski/zamrozenia/{id}?userId=X` | Usuniecie zamrozenia | NIE | P4 |

**CreateWniosekRequest:**
```
{
  userId: int,
  typ: string,
  rodzajUrlopu: string?,
  odDo: string,        // format "DD.MM.YYYY - DD.MM.YYYY"
  godziny: int?,
  powod: string,
  iloscDni: int,
  dokumenty: int?,
  zastepstwoUserId: int?
}
```

**WniosekDto (response):**
```
{
  id: int, userId: int, managerId: int?, hrId: int?,
  typ: string, rodzajUrlopu: string?, odDo: string,
  godziny: int?, powod: string, iloscDni: int, dokumenty: int?,
  status: string,
  managerApprovedAt: string?, hrApprovedAt: string?,
  createdAt: string?, zastepstwoUserId: int?, zastepstwoUsername: string?,
  komentarzManager: string?, komentarzHr: string?, username: string?
}
```

**ManagerApprovalRequest:**
```
{ managerId: int, approved: boolean, komentarz: string?, data: datetime }
```

**HrApprovalRequest:**
```
{ hrId: int, approved: boolean, komentarz: string?, data: datetime }
```

### A.3 Tasks V2 (`/api/tasks`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 31 | GET | `/api/tasks/typy` | Slownik typow zadan | NIE | P2 |
| 32 | POST | `/api/tasks` | Utworzenie zadania | NIE | P2 |
| 33 | GET | `/api/tasks?page&pageSize&search&status&typ` | Lista zadan | NIE | P2 |
| 34 | GET | `/api/tasks/{id}` | Szczegoly zadania | NIE | P2 |
| 35 | PUT | `/api/tasks/{id}/status` | Zmiana statusu | NIE | P2 |
| 36 | POST | `/api/tasks/{id}/comments` | Dodanie komentarza | NIE | P2 |
| 37 | GET | `/api/tasks/{id}/comments` | Lista komentarzy | NIE | P2 |
| 38 | POST | `/api/tasks/{id}/files` | Upload pliku (multipart) | NIE | P3 |
| 39 | GET | `/api/tasks/{id}/files` | Lista plikow | NIE | P3 |
| 40 | GET | `/api/tasks/{id}/files/{fileId}` | Pobranie pliku | NIE | P3 |
| 41 | DELETE | `/api/tasks/{id}/files/{fileId}` | Usuniecie pliku | NIE | P3 |
| 42 | GET | `/api/tasks/{id}/historia` | Historia zadania | NIE | P3 |
| 43 | POST | `/api/tasks/{id}/observers` | Dodanie obserwatorow | NIE | P3 |
| 44 | GET | `/api/tasks/{id}/observers` | Lista obserwatorow | NIE | P3 |
| 45 | DELETE | `/api/tasks/{id}/observers/{userId}` | Usuniecie obserwatora | NIE | P3 |
| 46 | GET | `/api/tasks/notifications?unreadOnly` | Powiadomienia | NIE | P2 |
| 47 | PUT | `/api/tasks/notifications/{id}/read` | Oznacz jako przeczytane | NIE | P2 |
| 48 | PUT | `/api/tasks/notifications/read-all` | Oznacz wszystkie | NIE | P2 |

**CreateTaskRequest:**
```
{
  typ: string [required, max 50],
  tytul: string [required, max 300],
  opis: string?,
  termin: string?,          // yyyy-MM-dd
  kontrahentNazwa: string? [max 200],
  assignedToIds: int[] [required, min 1]
}
```

**TaskListItemDto:**
```
{
  id: int, templateId: int, typ: string, tytul: string,
  kontrahentNazwa: string?, termin: string?,
  assignedToName: string, assignedTo: int,
  status: string, isOverdue: boolean,
  createdAt: string, createdByName: string
}
```

**TaskDetailDto (extends TaskListItemDto):**
```
{
  ...TaskListItemDto,
  opis: string?, createdBy: int,
  totalInstances: int, completedInstances: int,
  startedAt: string?, completedAt: string?
}
```

**TaskCommentDto:**
```
{ id: int, userId: int, username: string, tresc: string, createdAt: string }
```

**TaskNotificationDto:**
```
{ id: int, instanceId: int, typ: string, tresc: string, przeczytane: boolean, createdAt: string }
```

### A.4 Tasks V1 (legacy) (`/tasks`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 49 | POST | `/tasks/list` | Stara lista zadan | TAK | P4 |

> Uwaga: Dashboard uzywa starego `/tasks/list`. Docelowo migrowac na `/api/tasks`.

### A.5 Employee (`/employee`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 50 | GET | `/employee/vacation-info?employeeId&employeeStatus` | Info urlopowe | NIE | P3 |
| 51 | GET | `/employee/cache` | Cache pracownikow | NIE | P3 |
| 52 | GET | `/employee/profile/{userId}` | Profil pracownika | TAK | P2 |
| 53 | POST | `/employee/sync` | Synchronizacja | NIE | P4 |
| 54 | GET | `/employee/sync-status` | Status synchronizacji | NIE | P4 |

### A.6 Kontrahenci (`/kontrahenci`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 55 | GET | `/kontrahenci?search&nip&adres&nrAx` | Wyszukiwanie kontrahentow | NIE | P2 |
| 56 | GET | `/kontrahenci/{accountNum}/finanse` | Dane finansowe z AX | NIE | P2 |

**Kontrahent search response (item):**
```
{ id: string (AccountNum), nazwa: string, obecnyLimit: decimal, nip: string, adres: string }
```

**KontrahentFinanseDto:**
```
{
  accountNum: string, nazwa: string, adres: string, nip: string,
  nrAx: string, rodzina: string,
  niewyplacalny: boolean, zamrozony: boolean, niebezpieczny: boolean,
  dka: string, dkz: string, custGroup: string,
  obecnyLimit: decimal, saldo: decimal, zamowione: decimal,
  pozostalyKredyt: decimal, wartoscZabezpieczen: decimal,
  nakladyPoprzedni: decimal, nakladyBiezacy: decimal,
  przychodyPoprzedni: decimal, przychodyBiezacy: decimal,
  poprzedniOkres: string, biezacyOkres: string,
  zadluzeniePrzeterminowane: decimal, procentGwarancji: int, memo: string
}
```

### A.7 Limity Kredytowe (`/limity-kredytowe`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 57 | POST | `/limity-kredytowe` | Utworzenie wniosku o limit | NIE | P2 |
| 58 | GET | `/limity-kredytowe?page&pageSize&status&search&tab` | Lista wnioskow | NIE | P2 |
| 59 | GET | `/limity-kredytowe/{id}` | Szczegoly wniosku | NIE | P2 |
| 60 | POST | `/limity-kredytowe/sync/{accountNum}` | Reczna synchronizacja AX | NIE | P3 |
| 61 | GET | `/limity-kredytowe/{id}/viewers` | Lista obserwatorow | NIE | P3 |
| 62 | POST | `/limity-kredytowe/{id}/viewers` | Dodanie obserwatora | NIE | P3 |
| 63 | DELETE | `/limity-kredytowe/{id}/viewers/{userId}` | Usuniecie obserwatora | NIE | P3 |
| 64 | GET | `/limity-kredytowe/users/search?q=X` | Wyszukiwanie uzytkownikow | NIE | P3 |

**CreateLimitKredytowyRequest:**
```
{
  userId: int,
  kontrahentAccountNum: string [required],
  wnioskowanyLimit: decimal [required, >0],
  terminZabezpieczen: datetime?,
  opisZabezpieczen: string?,
  noweZabezpieczenia: string?,
  dodatkoweDochody: string?,
  zobowiazania: string?,
  uwagi: string?,
  potwierdzonePrzeterminowane: boolean,
  rozliczeniePlonami: boolean
}
```

**Lista response:**
```
{
  data: [{ id, user_id, kontrahent_account_num, kontrahent_nazwa,
           obecny_limit, wnioskowany_limit, status, ax_sync, created_at, created_by }],
  total: int, page: int, pageSize: int
}
```

### A.8 Transport Ceny (`/transport-ceny`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 65 | GET | `/transport-ceny?page&pageSize&status&search&tab` | Lista wnioskow | NIE | P2 |
| 66 | GET | `/transport-ceny/{id}` | Szczegoly wniosku + historia | NIE | P2 |
| 67 | POST | `/transport-ceny` | Nowy wniosek | NIE | P2 |
| 68 | POST | `/transport-ceny/{id}/review` | Zatwierdzenie/odrzucenie | NIE | P2 |
| 69 | GET | `/transport-ceny/archiwum?page&pageSize&search` | Archiwum tras | NIE | P3 |
| 70 | GET | `/transport-ceny/ax-kontrakty?search=X` | Wyszukiwanie kontraktow AX | NIE | P2 |

**CreateTransportPriceRequest:**
```
{
  axVendContractId: string?,
  axCustContractId: string?,
  kontrahentNazwa: string [required],
  towar: string?,
  iloscTon: decimal?,
  adresZaladunku: string?,
  odbiorca: string?,
  adresOdbioru: string?,
  szacowanyKosztTransportu: decimal [required, >0],
  komentarzHandlowiec: string?,
  sklad: string (default "Glowny")
}
```

**ReviewTransportPriceRequest:**
```
{ approved: boolean, zatwierdzonyKoszt: decimal?, komentarz: string? }
```

**AxVendContractDto:**
```
{
  ltVendContractId: string, vendAccount: string?, vendName: string?,
  itemId: string?, qty: decimal?, price: decimal?,
  estimatedTransportCost: decimal?, deliveryAddress: string?,
  ltCustContractId: string?, contractDate: string?, dueDate: string?, status: int?
}
```

### A.9 Transport (`/transport`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 71 | POST | `/transport/route` | Kalkulacja trasy | NIE | P3 |
| 72 | GET | `/transport/vehicles` | Lista pojazdow (Webfleet) | NIE | P3 |
| 73 | GET | `/transport/vehicles/{objectNo}` | Szczegoly pojazdu | NIE | P3 |
| 74 | GET | `/transport/webfleet-config` | Konfiguracja Webfleet | NIE | P4 |
| 75 | POST | `/transport/webfleet-config` | Zapis konfiguracji | NIE | P4 |

### A.10 Delegacja (`/delegacja`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 76 | GET | `/delegacja/szukaj-adres?query=X` | Wyszukiwanie adresu (TomTom) | NIE | P3 |
| 77 | POST | `/delegacja` | Utworzenie delegacji | NIE | P3 |
| 78 | GET | `/delegacja/{wniosekId}` | Delegacja per wniosek | NIE | P3 |
| 79 | PUT | `/delegacja/{id}` | Edycja delegacji | NIE | P3 |
| 80 | POST | `/delegacja/{id}/rozliczenie` | Rozliczenie delegacji | NIE | P3 |
| 81 | GET | `/delegacja/{id}/pdf` | Pobranie PDF | NIE | P4 |

### A.11 AX (`/ax`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 82 | GET | `/ax/handlowcy` | Lista handlowcow | NIE | P3 |
| 83 | GET | `/ax/towary?search&magazyn&grupa` | Lista towarow | NIE | P3 |
| 84 | GET | `/ax/towary/grupy` | Grupy towarowe | NIE | P3 |
| 85 | GET | `/ax/towary/magazyny` | Lista magazynow | NIE | P3 |
| 86 | GET | `/ax/kontrahenci?search&nip` | Kontrahenci z AX | NIE | P3 |

### A.12 Claims Admin (`/api/admin/claims`)

| # | Metoda | Endpoint | Opis | Impl | P |
|---|--------|----------|------|------|---|
| 87 | GET | `/api/admin/claims/definitions` | Definicje uprawien | NIE | P4 |
| 88 | GET | `/api/admin/claims/users` | Uzytkownicy z claims | NIE | P4 |
| 89 | GET | `/api/admin/claims/user/{userId}` | Claims uzytkownika | NIE | P4 |
| 90 | PUT | `/api/admin/claims/user/{userId}` | Ustawienie claims | NIE | P4 |
| 91 | POST | `/api/admin/claims/user/{userId}/toggle` | Przelaczenie claim | NIE | P4 |
| 92 | POST | `/api/admin/claims/user/{userId}/section/{code}` | Przelaczenie sekcji | NIE | P4 |

---

## B. Wymagania per ekran

### B.1 MainActivity (Login) — ISTNIEJACY, wymaga zmian

**Endpointy:** `POST /auth/login`

**Funkcjonalnosc:**
- Formularz logowania (username + password)
- Automatyczne przekierowanie do Dashboard jesli token istnieje
- Zapis danych sesji do SharedPreferences

**Wymagane zmiany:** Patrz sekcja [C.1](#c1-mainactivity-login)

---

### B.2 DashboardActivity — ISTNIEJACY, wymaga zmian

**Endpointy:** `GET /employee/profile/{userId}`, `POST /tasks/list`, `POST /wnioski/list`, `POST /wnioski/{id}/wyslij`, `GET /auth/profile`

**Funkcjonalnosc:**
- Karta profilu (imie, stanowisko, telefon, email)
- Dwie zakladki: Zadania / Wnioski
- Zadania: lista z paginacja i wyszukiwaniem
- Wnioski: lista z paginacja, akcja "Wyslij", przycisk "Nowy wniosek"
- Top bar z nazwa uzytkownika i menu burger
- Navigation Drawer z linkami

**Wymagane zmiany:** Patrz sekcja [C.2](#c2-dashboardactivity)

---

### B.3 ApprovalActivity — ISTNIEJACY, wymaga zmian

**Endpointy:** `POST /wnioski/approvals`, `POST /wnioski/{id}/akceptacja-manager`, `POST /wnioski/{id}/akceptacja-hr`

**Funkcjonalnosc:**
- Lista wnioskow do akceptacji z wyszukiwaniem i paginacja
- Przyciski Akceptuj / Odrzuc na kazdym wniosku
- Stan pusty gdy brak wnioskow

**Wymagane zmiany:** Patrz sekcja [C.3](#c3-approvalactivity)

---

### B.4 NewRequestActivity — ISTNIEJACY, wymaga pelnej przebudowy

**Endpointy:** `POST /wnioski`, `GET /wnioski/typy`, `GET /wnioski/rodzaje-urlopu`, `GET /wnioski/uzytkownicy`, `GET /wnioski/zamrozenia/check`

**Funkcjonalnosc:**
- Formularz tworzenia wniosku urlopowego
- Typ wniosku ladowany z API (spinner)
- Rodzaj urlopu ladowany z API (spinner, widoczny gdy typ = Urlop)
- Zastepstwo ladowane z API (spinner)
- Date picker z formatem DD.MM.YYYY
- Walidacja: typ wymagany, daty wymagane, powod wymagany, iloscDni >= 1
- Sprawdzenie zamrozenia dat przed wyslaniem
- Upload zalacznikow (opcjonalny)

**Wymagane zmiany:** Patrz sekcja [C.4](#c4-newrequestactivity)

---

### B.5 EditRequestActivity — NOWY

**Endpointy:** `GET /wnioski/{id}`, `PUT /wnioski/{id}`, `POST /wnioski/{id}/wyslij-ponownie`, `GET /wnioski/{id}/pliki`, `POST /wnioski/{id}/pliki`, `DELETE /wnioski/pliki/{plikId}`

**Funkcjonalnosc (na podstawie EditRequestView.vue):**
- Zaladowanie istniejacego wniosku
- Edycja pol (formularz identyczny jak NewRequest)
- Ponowne wyslanie jesli status = Odrzucony
- Podglad i zarzadzanie plikami (upload, usuwanie)
- Podglad komentarzy managera i HR

**Elementy UI:**
- Formularz jak NewRequest, ale pre-wypelniony danymi
- Sekcja "Komentarze" (komentarzManager, komentarzHr) - readonly
- Sekcja "Pliki" - lista z akcjami
- Przycisk "Zapisz" / "Wyslij ponownie"

**Walidacje:**
- Edycja mozliwa tylko gdy status = Szkic lub Odrzucony
- Te same walidacje co NewRequest

**Stany:** Loading (skeleton), Error (toast + retry), Empty (brak wniosku = 404)

**Nawigacja:** DashboardActivity (wnioski tab) -> EditRequestActivity | Back button -> finish()

---

### B.6 TasksListActivity — NOWY

**Endpointy:** `GET /api/tasks?page&pageSize&search&status&typ`, `GET /api/tasks/typy`

**Funkcjonalnosc (na podstawie TasksListView.vue):**
- Lista zadan z paginacja
- Filtrowanie: wyszukiwanie tekstowe, filtr statusu, filtr typu
- Kazde zadanie wyswietla: tytul, typ, termin, przypisany, status, overdue badge
- Klikniecie -> TaskDetailActivity

**Elementy UI:**
- Toolbar z tytul "Zadania" i back button
- SearchBar (EditText)
- Spinner/Chips: filtr statusu (Nowe, W trakcie, Zakonczone)
- Spinner: filtr typu (z API /typy)
- RecyclerView z item_task.xml
- Paginacja (prev/next + info)
- FAB "+" do tworzenia zadania (opcjonalnie)

**Stany:** Loading (ProgressBar), Empty ("Brak zadan"), Error (toast)

**Nawigacja:** Drawer -> TasksListActivity | Item click -> TaskDetailActivity

---

### B.7 TaskDetailActivity — NOWY

**Endpointy:** `GET /api/tasks/{id}`, `PUT /api/tasks/{id}/status`, `GET /api/tasks/{id}/comments`, `POST /api/tasks/{id}/comments`, `GET /api/tasks/{id}/files`, `GET /api/tasks/{id}/historia`, `GET /api/tasks/{id}/observers`

**Funkcjonalnosc (na podstawie TaskDetailView.vue):**
- Szczegoly zadania: tytul, typ, opis, termin, status, tworca, przypisany
- Zmiana statusu (dropdown/buttons)
- Komentarze: lista + dodawanie nowych
- Pliki: lista z mozliwoscia pobrania
- Historia zmian
- Obserwatorzy

**Elementy UI:**
- ScrollView z sekcjami:
  - Naglowek: typ badge, tytul, status chip
  - Karta detali: termin, tworca, przypisany, postep (X/Y instancji)
  - Zakladki: Komentarze | Pliki | Historia | Obserwatorzy
  - Akcja: zmiana statusu (button lub spinner)

**Stany:** Loading (skeleton), Error (toast + retry), 404 (finish + toast)

**Nawigacja:** TasksListActivity -> TaskDetailActivity | Back button -> finish()

---

### B.8 CalendarActivity — NOWY

**Endpointy:** `GET /wnioski/zamrozenia/miesiac?rok&miesiac`, `GET /wnioski/zamrozenia`

**Funkcjonalnosc (na podstawie CalendarView.vue):**
- Widok kalendarza miesiecznego
- Zamrozenia (blokady urlopowe) oznaczone kolorami
- Nawigacja miedzy miesiacami

**Elementy UI:**
- Toolbar "Kalendarz" + back button
- Nawigacja: < miesiac/rok >
- Siatka 7x6 (dni tygodnia)
- Legenda kolorow zamrozen

**Stany:** Loading, Error

**Nawigacja:** Drawer -> CalendarActivity

---

### B.9 HandlowcyActivity — NOWY

**Endpointy:** `GET /ax/handlowcy`

**Funkcjonalnosc (na podstawie HandlowcyView.vue):**
- Lista handlowcow z AX
- Wyszukiwanie po nazwisku

**Elementy UI:**
- Toolbar "Handlowcy" + back
- SearchBar
- RecyclerView z lista handlowcow

**Stany:** Loading, Empty, Error, AX niedostepny

**Nawigacja:** Drawer -> HandlowcyActivity

---

### B.10 TransportActivity — NOWY

**Endpointy:** `GET /transport/vehicles`, `GET /transport/vehicles/{objectNo}`, `POST /transport/route`

**Funkcjonalnosc (na podstawie TransportView.vue):**
- Mapa z pojazdami Webfleet (Google Maps / MapView)
- Lista pojazdow z lokalizacja i statusem
- Kalkulacja trasy

**Elementy UI:**
- Toolbar "Flota" + back
- MapView z markerami pojazdow
- Dolny panel: lista pojazdow (swipe up)
- Klikniecie pojazdu: nazwa, kierowca, pozycja, predkosc, czas

**Stany:** Loading, Error, Webfleet niedostepny

**Nawigacja:** Drawer -> TransportActivity

> Uwaga: Wymaga integracji Google Maps SDK. Mozna odlozyc (P3).

---

### B.11 LimitKredytowyActivity (formularz nowego) — NOWY

**Endpointy:** `GET /kontrahenci?search=X`, `GET /kontrahenci/{accountNum}/finanse`, `POST /limity-kredytowe`

**Funkcjonalnosc (na podstawie LimitKredytowyView.vue):**
- Wyszukanie kontrahenta (autocomplete z AX)
- Automatyczne pobranie danych finansowych
- Formularz wniosku o limit kredytowy
- Podglad danych z AX (readonly): obecny limit, saldo, zamowione, itp.
- Pola edytowalne: wnioskowany limit, termin zabezpieczen, opis, uwagi, checkboxy

**Elementy UI:**
- Toolbar "Nowy wniosek o limit" + back
- Sekcja "Kontrahent": autocomplete search
- Sekcja "Dane z AX" (readonly, 12+ pol numerycznych)
- Sekcja "Wniosek": wnioskowany limit, termin zabezpieczen, opis zabezpieczen, nowe zabezpieczenia, dodatkowe dochody, zobowiazania, uwagi
- Checkboxy: potwierdzone przeterminowane, rozliczenie plonami
- Przycisk "Zapisz"

**Walidacje:**
- Kontrahent wymagany
- WnioskowanyLimit > 0

**Stany:** Loading, Error, AX niedostepny (mozna zapisac bez danych AX)

**Nawigacja:** Drawer -> LimitKredytowyActivity

---

### B.12 LimityKredytoweListActivity — NOWY

**Endpointy:** `GET /limity-kredytowe?page&pageSize&status&search&tab`

**Funkcjonalnosc (na podstawie LimityKredytoweListView.vue):**
- Lista wnioskow o limity kredytowe
- Zakladki: Wszystkie / Moje
- Filtrowanie: wyszukiwanie, filtr statusu
- Paginacja

**Elementy UI:**
- Toolbar "Limity kredytowe" + back
- Zakladki: Wszystkie | Moje (tab bar)
- SearchBar + filtr statusu (Szkic, W trakcie, Zaakceptowany, Odrzucony)
- RecyclerView: item wyswietla kontrahent, nr AX, limit obecny, limit wnioskowany, status, data
- Paginacja

**Stany:** Loading, Empty, Error

**Nawigacja:** Drawer -> LimityKredytoweListActivity | Item click -> LimitKredytowyDetailActivity

---

### B.13 LimitKredytowyDetailActivity — NOWY

**Endpointy:** `GET /limity-kredytowe/{id}`, `POST /limity-kredytowe/sync/{accountNum}`, `GET /limity-kredytowe/{id}/viewers`, `POST /limity-kredytowe/{id}/viewers`, `DELETE /limity-kredytowe/{id}/viewers/{userId}`

**Funkcjonalnosc (na podstawie LimitKredytowyDetailView.vue):**
- Pelne dane wniosku (readonly)
- Dane finansowe z AX
- Przycisk "Synchronizuj AX" (manualna synchronizacja)
- Lista obserwatorow (dodawanie/usuwanie)

**Elementy UI:**
- Toolbar z nazwa kontrahenta + back
- Status badge
- Sekcja "Dane kontrahenta" (AccountNum, Nazwa, NIP, itp.)
- Sekcja "Dane finansowe" (obecny limit, saldo, zamowione, itp.)
- Sekcja "Wniosek" (wnioskowany limit, opis, uwagi, checkboxy)
- Sekcja "Obserwatorzy" (lista + dodawanie)
- Przycisk "Synchronizuj AX" (jesli ma uprawnienia)

**Stany:** Loading, Error, 404

**Nawigacja:** LimityKredytoweListActivity -> LimitKredytowyDetailActivity | Back -> finish()

---

### B.14 TransportCenyListActivity — NOWY

**Endpointy:** `GET /transport-ceny?page&pageSize&status&search&tab`

**Funkcjonalnosc (na podstawie TransportCenyListView.vue):**
- Lista wnioskow o ceny transportu zboz
- Zakladki: Wszystkie / Moje (widocznosc zalezna od roli — logistyka widzi wszystkie)
- Filtrowanie: wyszukiwanie, filtr statusu (PENDING/APPROVED/REJECTED/COMPLETED)
- Klikniecie -> TransportCenyDetailActivity

**Elementy UI:**
- Toolbar "Ceny transportu" + back
- Zakladki: Wszystkie | Moje
- SearchBar + Spinner filtr statusu
- RecyclerView z item: kontrahent, towar, iloscTon, adresZaladunku -> adresOdbioru, koszt szacowany, status badge, data
- Paginacja
- FAB "+" do nowego wniosku

**Statusy i kolory:**
- PENDING = zolty/pomaranczowy ("Oczekujacy")
- APPROVED = zielony ("Zatwierdzony")
- REJECTED = czerwony ("Odrzucony")
- COMPLETED = szary ("Zakonczony")

**Stany:** Loading, Empty, Error

**Nawigacja:** Drawer -> TransportCenyListActivity | Item -> Detail | FAB -> TransportCenyNewActivity

---

### B.15 TransportCenyNewActivity — NOWY

**Endpointy:** `POST /transport-ceny`, `GET /transport-ceny/ax-kontrakty?search=X`

**Funkcjonalnosc (na podstawie TransportCenyNewView.vue):**
- Wyszukiwanie kontraktu AX (opcjonalnie)
- Autouzupelnianie pol z kontraktu (kontrahent, towar, ilosc, adres, koszt)
- Reczne wypelnienie pol
- Wyslanie wniosku

**Elementy UI:**
- Toolbar "Nowy wniosek o cene transportu" + back
- Sekcja "Kontrakt AX" (opcjonalny):
  - Autocomplete search kontraktow
  - Wynik: ltVendContractId, vendName, itemId, qty, price, deliveryAddress
- Sekcja "Dane wniosku":
  - kontrahentNazwa (EditText, required)
  - towar (EditText)
  - iloscTon (number)
  - adresZaladunku (EditText)
  - odbiorca (EditText)
  - adresOdbioru (EditText)
  - szacowanyKosztTransportu (number, required, >0)
  - komentarzHandlowiec (multiline EditText)
  - sklad (Spinner: "Glowny", "Ryki", "Warka", ...)
- Przycisk "Wyslij wniosek"
- Success message + auto-redirect

**Walidacje:**
- kontrahentNazwa: wymagane
- szacowanyKosztTransportu: wymagane, > 0

**Stany:** Loading (submitting), Error (toast), Success (snackbar + redirect)

**Nawigacja:** TransportCenyListActivity -> TransportCenyNewActivity | Success -> back to list

---

### B.16 TransportCenyDetailActivity — NOWY

**Endpointy:** `GET /transport-ceny/{id}`, `POST /transport-ceny/{id}/review`

**Funkcjonalnosc (na podstawie TransportCenyDetailView.vue):**
- Szczegoly wniosku o cene transportu
- Dane: kontrahent, towar, iloscTon, adresy, koszt szacowany, koszt zatwierdzony
- Historia zmian
- Panel review (tylko dla logistyki): zatwierdzenie/odrzucenie z komentarzem i kosztem

**Elementy UI:**
- Toolbar z kontrahentNazwa + back
- Status badge
- Karta danych: towar, ilosc, adresy, sklad, koszty, komentarze
- Sekcja "Historia" (lista akcji)
- Panel review (widoczny jesli rola = Logistyka/Admin/Manager ORAZ status = PENDING):
  - Zatwierdz / Odrzuc (radio buttons lub 2 buttony)
  - zatwierdzonyKoszt (number, widoczny jesli zatwierdz)
  - komentarz (EditText)
  - Przycisk "Wyslij decyzje"

**Stany:** Loading, Error, 404, Forbid (brak dostepu)

**Nawigacja:** TransportCenyListActivity -> TransportCenyDetailActivity | Back -> finish()

---

### B.17 OfertaActivity — NOWY

**Endpointy:** Brak dedykowanych endpointow (widok demo/placeholder)

**Funkcjonalnosc:** Placeholder view.

**Priorytet:** P4 (skip)

---

### B.18 ProfileActivity — NOWY (opcjonalny, funkcjonalnosc jest w DashboardActivity)

**Endpointy:** `GET /auth/profile`, `GET /employee/profile/{userId}`

**Funkcjonalnosc (na podstawie ProfileView.vue):**
- Pelny profil uzytkownika (imie, stanowisko, dzial, email, telefon)
- Lista claims/uprawien
- Zmiana hasla (opcjonalnie)

**Priorytet:** P3 (profil juz jest w Dashboard, osobna strona opcjonalna)

---

## C. Zmiany w istniejacych ekranach

### C.1 MainActivity (Login)

| # | Zmiana | Priorytet | Opis |
|---|--------|-----------|------|
| C1.1 | **LoginResponse model update** | P1 | Aktualny `LoginResponse` ma 4 pola (token, userId, role, username). Backend zwraca `AuthResponse` z 10 polami (success, message, token, userId, username, role, dzial, employeeCacheId, claims[], claimsVersion). Trzeba zaktualizowac model i obsluge `success` wrapper. |
| C1.2 | **SessionManager — nowe pola** | P1 | Dodac: `dzial: String`, `employeeCacheId: Int?`, `claims: Set<String>`, `claimsVersion: Int`. Metoda `saveSession()` musi zapisywac wszystkie nowe pola. |
| C1.3 | **Claims-based access control** | P1 | Po zalogowaniu zapisac claims. Uzyc claims do kontrolowania widocznosci pozycji w drawer menu i dostepu do ekranow. |
| C1.4 | **Error handling** | P2 | Sprawdzac `success` field w response. Wyswietlac `message` z backendu zamiast generycznego "Blad logowania". |

### C.2 DashboardActivity

| # | Zmiana | Priorytet | Opis |
|---|--------|-----------|------|
| C2.1 | **Drawer menu — nowe pozycje** | P1 | Dodac linki: Zadania (TasksListActivity), Limity Kredytowe (LimityKredytoweListActivity), Ceny Transportu (TransportCenyListActivity), Kalendarz (CalendarActivity), Transport/Flota (TransportActivity), Handlowcy (HandlowcyActivity). Widocznosc uzalezniona od claims. |
| C2.2 | **Logout — call /auth/logout** | P1 | Przed wyczyszczeniem sesji wyslac `POST /auth/logout` (fire-and-forget). |
| C2.3 | **Profile — uzyc /auth/profile** | P2 | Dodatkowo pobierac profil z `/auth/profile` dla claims/dzial. |
| C2.4 | **Token refresh** | P2 | Implementacja automatycznego odswiezania tokenu przed wygasnieciem (np. w onResume lub przed kazdym requestem sprawdzac exp claim). |

### C.3 ApprovalActivity

| # | Zmiana | Priorytet | Opis |
|---|--------|-----------|------|
| C3.1 | **ManagerApprovalRequest — pole komentarz** | P2 | Backend oczekuje `komentarz` i `data`. Android wysyla tylko `managerId` i `approved`. Dodac dialog z polem komentarza przed akceptacja/odrzuceniem. |
| C3.2 | **HrApprovalRequest — pole komentarz** | P2 | Analogicznie — dodac pole komentarza i date. |
| C3.3 | **Statusy — pelna mapa** | P2 | Statusy wnioskow: Szkic, Wyslany, Do akceptacji (M), Do akceptacji (HR), Zaakceptowany, Odrzucony. Upewnic sie ze adapter mapuje wszystkie poprawnie. |

### C.4 NewRequestActivity

| # | Zmiana | Priorytet | Opis |
|---|--------|-----------|------|
| C4.1 | **Ladowanie typow z API** | P1 | Zamiast hardcoded `["Urlop", "Delegacja", "Praca zdalna"]` — `GET /wnioski/typy` |
| C4.2 | **Ladowanie rodzajow urlopu z API** | P1 | Zamiast hardcoded `["Wypoczynkowy", "Na zadanie", "Okolicznosciowy"]` — `GET /wnioski/rodzaje-urlopu` |
| C4.3 | **Ladowanie uzytkownikow (zastepstwo) z API** | P1 | Zamiast hardcoded `["Jan Nowak", "Anna Kowalska"]` — `GET /wnioski/uzytkownicy` |
| C4.4 | **Integracja z API POST /wnioski** | P1 | Wyslanie CreateWniosekRequest zamiast Toast. |
| C4.5 | **Date format fix** | P1 | Format `YYYY-MM-DD` z zero-padding (aktualnie `YYYY-M-D`). |
| C4.6 | **ViewModel + Repository** | P1 | Dodac NewRequestViewModel i WniosekRepository. |
| C4.7 | **Sprawdzanie zamrozenia** | P2 | Przed wyslaniem sprawdzic `GET /wnioski/zamrozenia/check`. |
| C4.8 | **Upload plikow** | P3 | Dodac sekcje upload plikow (multipart). |

---

## D. Nowe modele danych

### D.1 Zmiany w istniejacych modelach (Models.kt)

```kotlin
// ZMIANA: LoginResponse -> AuthResponse (pelny)
data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val userId: Int?,
    val username: String?,
    val token: String?,
    val role: String?,
    val dzial: String?,
    val employeeCacheId: Int?,
    val claims: List<String>?,
    val claimsVersion: Int?
)

// ZMIANA: ManagerApprovalRequest — dodac komentarz i date
data class ManagerApprovalRequest(
    val managerId: Int,
    val approved: Boolean,
    val komentarz: String? = null,
    val data: String? = null       // ISO datetime
)

// ZMIANA: HrApprovalRequest — dodac komentarz i date
data class HrApprovalRequest(
    val hrId: Int,
    val approved: Boolean,
    val komentarz: String? = null,
    val data: String? = null
)
```

### D.2 Nowe modele

```kotlin
// ── Auth ──
data class RefreshResponse(val success: Boolean, val token: String?)
data class LogoutResponse(val success: Boolean, val message: String?)
data class ProfileResponse2(
    val userId: Int, val username: String, val role: String,
    val dzial: String, val employeeCacheId: Int?,
    val claims: List<String>, val claimsVersion: Int
)

// ── Wnioski (nowe/zmienione) ──
data class CreateWniosekRequest(
    val userId: Int, val typ: String, val rodzajUrlopu: String?,
    val odDo: String, val godziny: Int?, val powod: String,
    val iloscDni: Int, val dokumenty: Int?, val zastepstwoUserId: Int?
)

data class WniosekDetailDto(
    val id: Int, val userId: Int, val managerId: Int?, val hrId: Int?,
    val typ: String, val rodzajUrlopu: String?, val odDo: String,
    val godziny: Int?, val powod: String, val iloscDni: Int, val dokumenty: Int?,
    val status: String,
    val managerApprovedAt: String?, val hrApprovedAt: String?,
    val createdAt: String?, val zastepstwoUserId: Int?, val zastepstwoUsername: String?,
    val komentarzManager: String?, val komentarzHr: String?, val username: String?
)

data class SlownikItem(val id: Int, val nazwa: String)

data class WniosekPlikDto(
    val id: Int, val wniosekId: Int, val nazwaPliku: String, val createdAt: String?
)

data class UserAdresDto(
    val adresUlica: String?, val adresNumer: String?,
    val adresMiasto: String?, val adresKod: String?
)

data class ZamrozenieDto(
    val id: Int, val dzial: String, val dataOd: String,
    val dataDo: String, val opis: String?
)

// ── Tasks V2 ──
data class CreateTaskRequest(
    val typ: String, val tytul: String, val opis: String?,
    val termin: String?, val kontrahentNazwa: String?,
    val assignedToIds: List<Int>
)

data class TaskListItemDto(
    val id: Int, val templateId: Int, val typ: String, val tytul: String,
    val kontrahentNazwa: String?, val termin: String?,
    val assignedToName: String, val assignedTo: Int,
    val status: String, val isOverdue: Boolean,
    val createdAt: String, val createdByName: String
)

data class TaskDetailDto(
    val id: Int, val templateId: Int, val typ: String, val tytul: String,
    val kontrahentNazwa: String?, val termin: String?,
    val assignedToName: String, val assignedTo: Int,
    val status: String, val isOverdue: Boolean,
    val createdAt: String, val createdByName: String,
    val opis: String?, val createdBy: Int,
    val totalInstances: Int, val completedInstances: Int,
    val startedAt: String?, val completedAt: String?
)

data class TaskCommentDto(
    val id: Int, val userId: Int, val username: String,
    val tresc: String, val createdAt: String
)

data class TaskNotificationDto(
    val id: Int, val instanceId: Int, val typ: String,
    val tresc: String, val przeczytane: Boolean, val createdAt: String
)

data class TaskTypDto(val id: Int, val kod: String, val nazwa: String)

data class TaskHistoriaDto(
    val id: Int, val username: String, val akcja: String,
    val staryWartosc: String?, val nowyWartosc: String?,
    val szczegoly: String?, val createdAt: String
)

data class TaskFileDto(
    val id: Int, val nazwaPliku: String,
    val createdAt: String, val uploadedBy: String
)

data class TaskObserverDto(
    val id: Int, val userId: Int, val username: String,
    val dzial: String?, val addedByName: String, val createdAt: String
)

// ── Kontrahenci ──
data class KontrahentSearchResult(
    val id: String, val nazwa: String, val obecnyLimit: Double?,
    val nip: String?, val adres: String?
)

data class KontrahentFinanseDto(
    val accountNum: String, val nazwa: String, val adres: String, val nip: String,
    val nrAx: String, val rodzina: String,
    val niewyplacalny: Boolean, val zamrozony: Boolean, val niebezpieczny: Boolean,
    val dka: String, val dkz: String, val custGroup: String,
    val obecnyLimit: Double, val saldo: Double, val zamowione: Double,
    val pozostalyKredyt: Double, val wartoscZabezpieczen: Double,
    val nakladyPoprzedni: Double, val nakladyBiezacy: Double,
    val przychodyPoprzedni: Double, val przychodyBiezacy: Double,
    val poprzedniOkres: String, val biezacyOkres: String,
    val zadluzeniePrzeterminowane: Double, val procentGwarancji: Int, val memo: String
)

// ── Limity Kredytowe ──
data class CreateLimitKredytowyRequest(
    val userId: Int, val kontrahentAccountNum: String,
    val wnioskowanyLimit: Double,
    val terminZabezpieczen: String?, val opisZabezpieczen: String?,
    val noweZabezpieczenia: String?, val dodatkoweDochody: String?,
    val zobowiazania: String?, val uwagi: String?,
    val potwierdzonePrzeterminowane: Boolean, val rozliczeniePlonami: Boolean
)

data class LimitKredytowyListItem(
    val id: Int, val user_id: Int, val kontrahent_account_num: String,
    val kontrahent_nazwa: String, val obecny_limit: Double?,
    val wnioskowany_limit: Double, val status: String,
    val ax_sync: Boolean, val created_at: String, val created_by: String?
)

// Paginated response for limity/transport-ceny (different format than wnioski)
data class PagedResponse<T>(
    val data: List<T>, val total: Int, val page: Int, val pageSize: Int
)

// ── Transport Ceny ──
data class CreateTransportPriceRequest(
    val axVendContractId: String?, val axCustContractId: String?,
    val kontrahentNazwa: String, val towar: String?,
    val iloscTon: Double?, val adresZaladunku: String?,
    val odbiorca: String?, val adresOdbioru: String?,
    val szacowanyKosztTransportu: Double,
    val komentarzHandlowiec: String?, val sklad: String
)

data class ReviewTransportPriceRequest(
    val approved: Boolean, val zatwierdzonyKoszt: Double?,
    val komentarz: String?
)

data class TransportPriceListItem(
    val id: Int, val user_id: Int, val username: String?,
    val ax_vend_contract_id: String?, val ax_cust_contract_id: String?,
    val kontrahent_nazwa: String, val towar: String?, val ilosc_ton: Double?,
    val adres_zaladunku: String?, val odbiorca: String?, val adres_odbioru: String?,
    val szacowany_koszt_transportu: Double, val zatwierdzony_koszt: Double?,
    val sklad: String?, val status: String,
    val reviewed_by: Int?, val reviewed_by_username: String?,
    val reviewed_at: String?, val komentarz_logistyka: String?,
    val komentarz_handlowiec: String?, val created_at: String?
)

data class TransportPriceDetailResponse(
    val request: TransportPriceListItem,
    val history: List<TransportPriceHistoryItem>
)

data class TransportPriceHistoryItem(
    val id: Int, val akcja: String, val stary_status: String?,
    val nowy_status: String?, val komentarz: String?,
    val created_at: String?, val username: String?
)

data class AxVendContractDto(
    val ltVendContractId: String, val vendAccount: String?, val vendName: String?,
    val itemId: String?, val qty: Double?, val price: Double?,
    val estimatedTransportCost: Double?, val deliveryAddress: String?,
    val ltCustContractId: String?, val contractDate: String?,
    val dueDate: String?, val status: Int?
)

// ── Transport (Webfleet) ──
data class WebfleetVehicleDto(
    val objectNo: String, val objectName: String, val objectUid: String,
    val latitude: Double?, val longitude: Double?,
    val positionText: String?, val positionTime: String?,
    val speed: Int?, val driverName: String?
)

// ── Delegacja ──
data class DelegacjaCreateDto(
    val wniosekId: Int, val celMiejscowosc: String, val celAdres: String?,
    val celDelegacji: String, val srodekLokomocji: String,
    val pojazdSluzbowy: Boolean, val nrRejestracji: String?,
    val zaliczkaKwota: Double?
)

data class DelegacjaDto(
    val id: Int, val wniosekId: Int, val celMiejscowosc: String,
    val celAdres: String?, val celDelegacji: String, val srodekLokomocji: String,
    val pojazdSluzbowy: Boolean, val nrRejestracji: String?,
    val zaliczkaKwota: Double?, val nrDokumentu: String?,
    val createdAt: String?, val trasy: List<DelegacjaTrasaDto>,
    val koszty: DelegacjaKosztyDto?
)

data class DelegacjaTrasaDto(
    val id: Int, val delegacjaId: Int,
    val wyjazdMiejscowosc: String?, val wyjazdData: String?, val wyjazdGodzina: String?,
    val przyjazdMiejscowosc: String?, val przyjazdData: String?, val przyjazdGodzina: String?,
    val srodekLokomocji: String?, val koszt: Double?
)

data class DelegacjaKosztyDto(
    val id: Int, val delegacjaId: Int,
    val ryczaltyDojazdy: Double, val dojazdyUdokumentowane: Double,
    val diety: Double, val noclegiRachunki: Double, val noclegiRyczalt: Double,
    val inneWydatki: Double, val ogolem: Double
)

// ── AX ──
data class HandlowiecDto(
    // Pola zwracane przez /ax/handlowcy — dynamiczne, min:
    val accountNum: String?, val name: String?
)

data class TowarDto(
    val itemId: String, val itemName: String, val itemGroupId: String,
    val grupaTowarowa: String?, val dzial: String?, val dzialNazwa: String?,
    val podkat: String?, val dostepne: Double, val stanFizyczny: Double,
    val magazyn: String?
)

// ── Employee ──
data class EmployeeCacheDto(
    val id: Int, val employee: Int, val emplStatus: Int,
    val name: String, val fName: String, val depart: String,
    val branch: String, val departmentName: String,
    val xWyrgrp1: String, val xWyrgrp2: String, val workpost: String,
    val prevlimitd: Double, val vacdays: Double, val addlimitd: Double,
    val limitconsd: Double, val restlimitd: Double,
    val superior: String, val mdTryb: Int, val mdAmount: Double,
    val initials: String
)
```

### D.3 Zmiany w SessionManager

```kotlin
class SessionManager(context: Context) {
    // Istniejace:
    // KEY_TOKEN, KEY_USER_ID, KEY_ROLE, KEY_USERNAME

    // NOWE:
    private const val KEY_DZIAL = "dzial"
    private const val KEY_EMPLOYEE_CACHE_ID = "employeeCacheId"
    private const val KEY_CLAIMS = "claims"           // JSON array stored as string
    private const val KEY_CLAIMS_VERSION = "claimsVersion"

    fun saveSession(response: AuthResponse) { /* save all fields */ }

    val dzial: String get() = ...
    val employeeCacheId: Int? get() = ...
    val claims: Set<String> get() = ...  // parsed from JSON
    val claimsVersion: Int get() = ...

    fun hasClaim(claim: String): Boolean = claims.contains(claim)
    fun updateToken(newToken: String) { /* only update token */ }
    fun updateClaims(claims: List<String>, version: Int) { /* update claims */ }
}
```

### D.4 Nowe endpointy w ApiService.kt

```kotlin
interface ApiService {
    // ── ISTNIEJACE (do modyfikacji) ──
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse  // zmiana typu

    // ── NOWE — Auth ──
    @GET("api/auth/profile")
    suspend fun getAuthProfile(): ProfileResponse2

    @POST("api/auth/refresh")
    suspend fun refreshToken(): RefreshResponse

    @POST("api/auth/logout")
    suspend fun logout(): LogoutResponse

    // ── NOWE — Wnioski ──
    @GET("api/wnioski/typy")
    suspend fun getTypyWnioskow(): List<SlownikItem>

    @GET("api/wnioski/rodzaje-urlopu")
    suspend fun getRodzajeUrlopu(): List<SlownikItem>

    @GET("api/wnioski/uzytkownicy")
    suspend fun getUzytkownicy(): List<Any>  // dynamic

    @POST("api/wnioski")
    suspend fun createWniosek(@Body request: CreateWniosekRequest): Any

    @GET("api/wnioski/{id}")
    suspend fun getWniosek(@Path("id") id: Int): WniosekDetailDto

    @PUT("api/wnioski/{id}")
    suspend fun updateWniosek(@Path("id") id: Int, @Body request: CreateWniosekRequest): Any

    @GET("api/wnioski/{id}/pliki")
    suspend fun getWniosekPliki(@Path("id") id: Int): List<WniosekPlikDto>

    @Multipart
    @POST("api/wnioski/{id}/pliki")
    suspend fun uploadWniosekPlik(@Path("id") id: Int, @Part file: MultipartBody.Part): Any

    @GET("api/wnioski/zamrozenia/check")
    suspend fun checkZamrozenie(
        @Query("userId") userId: Int,
        @Query("od") od: String,
        @Query("do_") doDate: String?
    ): Any

    // ── NOWE — Tasks V2 ──
    @GET("api/tasks/typy")
    suspend fun getTaskTypes(): List<TaskTypDto>

    @GET("api/tasks")
    suspend fun getTasksV2(
        @Query("page") page: Int, @Query("pageSize") pageSize: Int,
        @Query("search") search: String?, @Query("status") status: String?,
        @Query("typ") typ: String?
    ): PagedResponse<TaskListItemDto>  // note: uses TaskPageResponse<TaskListItemDto>

    @GET("api/tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): TaskDetailDto

    @PUT("api/tasks/{id}/status")
    suspend fun changeTaskStatus(@Path("id") id: Int, @Body request: ChangeTaskStatusRequest): Any

    @POST("api/tasks/{id}/comments")
    suspend fun addTaskComment(@Path("id") id: Int, @Body request: AddTaskCommentRequest): Any

    @GET("api/tasks/{id}/comments")
    suspend fun getTaskComments(@Path("id") id: Int): List<TaskCommentDto>

    @GET("api/tasks/{id}/historia")
    suspend fun getTaskHistoria(@Path("id") id: Int): List<TaskHistoriaDto>

    @GET("api/tasks/{id}/files")
    suspend fun getTaskFiles(@Path("id") id: Int): List<TaskFileDto>

    @GET("api/tasks/notifications")
    suspend fun getTaskNotifications(@Query("unreadOnly") unreadOnly: Boolean): List<TaskNotificationDto>

    @PUT("api/tasks/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): Any

    @PUT("api/tasks/notifications/read-all")
    suspend fun markAllNotificationsRead(): Any

    // ── NOWE — Kontrahenci ──
    @GET("api/kontrahenci")
    suspend fun searchKontrahenci(
        @Query("search") search: String?, @Query("nip") nip: String?,
        @Query("adres") adres: String?, @Query("nrAx") nrAx: String?
    ): List<KontrahentSearchResult>

    @GET("api/kontrahenci/{accountNum}/finanse")
    suspend fun getKontrahentFinanse(@Path("accountNum") accountNum: String): KontrahentFinanseDto

    // ── NOWE — Limity Kredytowe ──
    @POST("api/limity-kredytowe")
    suspend fun createLimitKredytowy(@Body request: CreateLimitKredytowyRequest): Any

    @GET("api/limity-kredytowe")
    suspend fun getLimityKredytowe(
        @Query("page") page: Int, @Query("pageSize") pageSize: Int,
        @Query("status") status: String?, @Query("search") search: String?,
        @Query("tab") tab: String?
    ): PagedResponse<LimitKredytowyListItem>

    @GET("api/limity-kredytowe/{id}")
    suspend fun getLimitKredytowy(@Path("id") id: Int): Any  // dynamic

    @POST("api/limity-kredytowe/sync/{accountNum}")
    suspend fun syncLimitKredytowy(@Path("accountNum") accountNum: String): Any

    // ── NOWE — Transport Ceny ──
    @GET("api/transport-ceny")
    suspend fun getTransportCeny(
        @Query("page") page: Int, @Query("pageSize") pageSize: Int,
        @Query("status") status: String?, @Query("search") search: String?,
        @Query("tab") tab: String?
    ): PagedResponse<TransportPriceListItem>

    @GET("api/transport-ceny/{id}")
    suspend fun getTransportCenaDetail(@Path("id") id: Int): TransportPriceDetailResponse

    @POST("api/transport-ceny")
    suspend fun createTransportPrice(@Body request: CreateTransportPriceRequest): Any

    @POST("api/transport-ceny/{id}/review")
    suspend fun reviewTransportPrice(@Path("id") id: Int, @Body request: ReviewTransportPriceRequest): Any

    @GET("api/transport-ceny/ax-kontrakty")
    suspend fun searchAxContracts(@Query("search") search: String): List<AxVendContractDto>

    @GET("api/transport-ceny/archiwum")
    suspend fun getTransportCenyArchiwum(
        @Query("page") page: Int, @Query("pageSize") pageSize: Int,
        @Query("search") search: String?
    ): PagedResponse<TransportPriceListItem>

    // ── NOWE — Transport (Webfleet) ──
    @GET("api/transport/vehicles")
    suspend fun getVehicles(): List<WebfleetVehicleDto>

    @GET("api/transport/vehicles/{objectNo}")
    suspend fun getVehicle(@Path("objectNo") objectNo: String): WebfleetVehicleDto

    // ── NOWE — AX ──
    @GET("api/ax/handlowcy")
    suspend fun getHandlowcy(): List<Any>  // dynamic response

    @GET("api/ax/towary")
    suspend fun getTowary(
        @Query("search") search: String?,
        @Query("magazyn") magazyn: String?,
        @Query("grupa") grupa: String?
    ): List<TowarDto>

    // ── NOWE — Delegacja ──
    @POST("api/delegacja")
    suspend fun createDelegacja(@Body dto: DelegacjaCreateDto): Any

    @GET("api/delegacja/{wniosekId}")
    suspend fun getDelegacja(@Path("wniosekId") wniosekId: Int): DelegacjaDto

    @PUT("api/delegacja/{id}")
    suspend fun updateDelegacja(@Path("id") id: Int, @Body dto: DelegacjaUpdateDto): Any

    // ── NOWE — Zamrozenia (kalendarz) ──
    @GET("api/wnioski/zamrozenia/miesiac")
    suspend fun getZamrozeniaMiesiac(
        @Query("rok") rok: Int, @Query("miesiac") miesiac: Int
    ): List<ZamrozenieDto>

    @GET("api/wnioski/zamrozenia")
    suspend fun getZamrozenia(): List<ZamrozenieDto>
}
```

> UWAGA: Endpointy backendu uzyja roznych prefixow route:
> - Controllery z `[Route("[controller]")]` mapuja na nazwe klasy (np. `WnioskiController` -> `/wnioski/...`)
> - Ale Vite proxy stripuje `/api` prefix, wiec frontend uzywa `/api/wnioski/...`
> - Android powinien uzywac `api/` prefix (Retrofit base URL konczy sie na `/`) — tak jak jest teraz w istniejacym `ApiService.kt`.

---

## E. Priorytety biznesowe

### P1 — Krytyczne (sprint 1, 2 tygodnie)

| # | Zadanie | Uzasadnienie |
|---|---------|-------------|
| 1 | **LoginResponse update + SessionManager** (C1.1-C1.3) | Login jest zlamany — brakuje `success` wrapper, claims, dzial. Blokuje wszystko. |
| 2 | **NewRequestActivity — pelna integracja API** (C4.1-C4.6) | Jedyny formularz jest hardcoded. Uzytkownicy nie moga tworzyc wnioskow. |
| 3 | **Drawer menu — nowe pozycje** (C2.1) | Nawigacja do nowych ekranow. |
| 4 | **Logout — call /auth/logout** (C2.2) | Bezpieczenstwo — token blacklisting. |
| 5 | **Claims-based access control** (C1.3) | Kontrola dostepu do ekranow i menu. |

### P2 — Wazne (sprint 2-3, 4 tygodnie)

| # | Zadanie | Uzasadnienie |
|---|---------|-------------|
| 6 | **TasksListActivity + TaskDetailActivity** (B.6, B.7) | Zadania to core CRM. Dashboard juz pokazuje liste (V1), potrzeba pelnego widoku V2. |
| 7 | **TransportCenyListActivity + DetailActivity + NewActivity** (B.14-B.16) | Nowy modul biznesowy — transport zboz. Aktywnie uzywany. |
| 8 | **LimityKredytoweListActivity + DetailActivity + FormActivity** (B.11-B.13) | Wnioski o limity kredytowe — uzywane przez windykacje. |
| 9 | **EditRequestActivity** (B.5) | Edycja wnioskow — potrzebna do pelnego flow. |
| 10 | **ApprovalActivity — komentarze** (C3.1-C3.2) | Brak komentarzy w akceptacji = niekompletne dane. |
| 11 | **Token refresh** (C2.4) | Sesje wygasaja — uzytkownicy sa wylogowywani. |
| 12 | **Kontrahenci — wyszukiwanie i finanse** (A.6) | Potrzebne do limitow i transportu. |

### P3 — Przydatne (sprint 4-5, 4 tygodnie)

| # | Zadanie | Uzasadnienie |
|---|---------|-------------|
| 13 | **CalendarActivity** (B.8) | Kalendarz zamrozen — wygoda planowania urlopow. |
| 14 | **TransportActivity (Webfleet)** (B.10) | Mapa floty — wymaga Google Maps SDK. |
| 15 | **HandlowcyActivity** (B.9) | Lista handlowcow — informacyjny widok. |
| 16 | **Upload/download plikow** (wnioski + zadania) | Multipart upload — dodatkowa funkcjonalnosc. |
| 17 | **Delegacja** (B.5 + endpoints 76-81) | Obsluga delegacji — zlozony formularz. |
| 18 | **ProfileActivity** (B.18) | Rozszerzony profil — opcjonalny bo jest w Dashboard. |
| 19 | **Transport ceny archiwum** (endpoint 69) | Archiwum tras — nice to have. |

### P4 — Niska priorytet (backlog)

| # | Zadanie | Uzasadnienie |
|---|---------|-------------|
| 20 | **Admin Claims** (B.12 + endpoints 87-92) | Panel administracyjny — rzadko uzywany na mobile. |
| 21 | **OfertaActivity** (B.17) | Placeholder, brak dedykowanego backendu. |
| 22 | **Zamrozenia CRUD** (endpoints 28-30) | Tworzenie zamrozen — admin feature. |
| 23 | **Rejestracja** (endpoint 2) | Nie uzywana na mobile — konta tworzy admin. |
| 24 | **Webfleet config** (endpoints 74-75) | Konfiguracja — admin-only. |

---

## Podsumowanie liczbowe

| Kategoria | Ilosc |
|-----------|-------|
| Endpointow w backendzie (total) | ~92 |
| Zaimplementowanych w Android | ~10 (11%) |
| Do zaimplementowania (P1+P2) | ~50 |
| Istniejace ekrany | 4 |
| Nowe ekrany (P1+P2) | 8 |
| Nowe ekrany (P3) | 4 |
| Nowe modele danych | ~35 data classes |
| Szacowany naklad (P1) | 2 tygodnie / 1 developer |
| Szacowany naklad (P1+P2) | 6 tygodni / 1 developer |
| Szacowany naklad (all) | 10-12 tygodni / 1 developer |

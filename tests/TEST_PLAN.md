# CRM-OC Android App - Test Plan

## Overview
Test plan for the Android app (Kotlin, Activities + XML layouts, MVVM) mirroring the CRM-OC Vue.js frontend.
Package: `com.ossadkowski.app`. Project root: `Android/`.

---

## Implementation Status

| # | Vue View | Android Activity | Status | Notes |
|---|----------|-----------------|--------|-------|
| 1 | LoginView.vue | MainActivity | DONE | Login + session redirect |
| 2 | DashboardView.vue | DashboardActivity | DONE | Profile, tabs, tasks, wnioski |
| 3 | NewRequestView.vue | NewRequestActivity | PARTIAL | Hardcoded spinners, no API integration, no file upload |
| 4 | ApprovalView.vue | ApprovalActivity | DONE | Approve/reject, search, pagination |
| 5 | EditRequestView.vue | EditRequestActivity | MISSING | |
| 6 | NewOrderView.vue | NewOrderActivity | MISSING | |
| 7 | TasksListView.vue | TasksListActivity | MISSING | |
| 8 | TaskDetailView.vue | TaskDetailActivity | MISSING | |
| 9 | CalendarView.vue | CalendarActivity | MISSING | |
| 10 | HandlowcyView.vue | HandlowcyActivity | MISSING | |
| 11 | TransportView.vue | TransportActivity | MISSING | |
| 12 | LimitKredytowyView.vue | LimitKredytowyActivity | MISSING | |
| 13 | LimityKredytoweListView.vue | LimityKredytoweListActivity | MISSING | |
| 14 | LimitKredytowyDetailView.vue | LimitKredytowyDetailActivity | MISSING | |
| 15 | TransportCenyListView.vue | TransportCenyListActivity | MISSING | |
| 16 | TransportCenyDetailView.vue | TransportCenyDetailActivity | MISSING | |
| 17 | TransportCenyNewView.vue | TransportCenyNewActivity | MISSING | |
| 18 | OfertaView.vue | OfertaActivity | MISSING | |
| 19 | ProductListView.vue | N/A | SKIP | Demo view |
| 20 | KnowledgeBase.vue | KnowledgeBaseActivity | MISSING | Placeholder |

---

## Existing Code Issues Found

### CRITICAL
1. ~~**RetrofitClient uses API Key instead of Bearer token**~~ ✅ **FIXED** — RetrofitClient now uses `Authorization: Bearer {token}` from SessionManager. `API_KEY` removed from build.gradle.

2. **LoginResponse model mismatch** (`Models.kt:8-12`). Vue's auth endpoint returns `{ success, message, token, userId, username, role, dzial, employeeCacheId }`. Android expects `{ token, userId, role, username }` without the `success` wrapper -- Gson will silently get null values if the API actually returns the `success` wrapper.

### HIGH
3. **NewRequestActivity has no API integration** (`NewRequestActivity.kt`). Spinners are hardcoded with static arrays instead of loading from `/api/wnioski/typy`, `/api/wnioski/rodzaje-urlopu`, `/api/wnioski/uzytkownicy`. Submit just shows a Toast without calling the API.

4. ~~**No 401 handling / token refresh**~~ ✅ **FIXED** — Added `unauthorizedInterceptor` in RetrofitClient that clears session and redirects to login on 401.

5. **Missing ApiService endpoints**. Only 10 of ~30+ needed endpoints are defined. Missing: wnioski CRUD (create, update, get single), tasks list/detail, zamrozenia, limity-kredytowe, ax/kontrahenci, ax/towary, transport, transport-ceny, handlowcy, employee sync, file upload/download.

### MEDIUM
6. **Duplicate SessionManager vs TokenStorage pattern**. Android uses `SharedPreferences`-based `SessionManager` while `Mobile/` (separate project) has `DataStore`-based `TokenStorage`. Only Android `SessionManager` is used.

7. **No drawer menu items for missing screens**. DashboardActivity drawer only has "Panel" and "Approvals" links. Missing: Tasks, Calendar, Handlowcy, Oferta, Transport, Transport Ceny, Limity Kredytowe.

8. **Date format in DatePicker** (`NewRequestActivity.kt:74`). Outputs `YYYY-M-D` instead of `YYYY-MM-DD` (no zero-padding). Vue expects `DD.MM.YYYY` format for display.

---

## Test Cases

### 1. Authentication (MainActivity)

**Happy Path:**
- [ ] TC-AUTH-01: Enter valid username + password, tap "Zaloguj" -> navigates to Dashboard
  - Prereqs: Valid credentials, network available
  - Steps: 1) Launch app 2) Enter username 3) Enter password 4) Tap "Zaloguj"
  - Expected: Dashboard opens, no errors
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-02: Token, userId, role, username, dzial, employeeCacheId saved to SharedPreferences
  - Prereqs: Successful login
  - Steps: 1) Login 2) Check SharedPreferences values
  - Expected: All fields from AuthResponse stored: token, userId, username, role, dzial, employeeCacheId, claims, claimsVersion
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-03: Loading state: button disabled, text changes to "Logowanie..."
  - Prereqs: None
  - Steps: 1) Enter credentials 2) Tap "Zaloguj" 3) Observe button state
  - Expected: Button disabled and text changes during API call
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-AUTH-04: Button re-enables on error
  - Prereqs: Invalid credentials or network error
  - Steps: 1) Enter invalid creds 2) Tap "Zaloguj" 3) Wait for error
  - Expected: Button re-enabled after error Toast
  - Priority: P2 | Status: NOT TESTED

**Validation & Errors:**
- [ ] TC-AUTH-10: Empty username or password -> Toast "Wprowadz login i haslo"
  - Prereqs: None
  - Steps: 1) Leave fields empty 2) Tap "Zaloguj"
  - Expected: Toast with validation message, no API call
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-11: Invalid credentials -> Toast with error message
  - Prereqs: Network available
  - Steps: 1) Enter wrong password 2) Tap "Zaloguj"
  - Expected: Toast "Invalid credentials." (from 401 response)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-12: Network error -> Toast with exception message
  - Prereqs: Network unavailable (airplane mode)
  - Steps: 1) Enable airplane mode 2) Enter credentials 3) Tap "Zaloguj"
  - Expected: Toast with network error message
  - Priority: P1 | Status: NOT TESTED

**Session:**
- [ ] TC-AUTH-20: App launch with existing token -> auto-redirect to Dashboard
  - Prereqs: Previously logged in, token in SharedPreferences
  - Steps: 1) Kill app 2) Relaunch
  - Expected: Skips login, opens Dashboard directly
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-21: Logout clears session and returns to login
  - Prereqs: Logged in
  - Steps: 1) Open drawer 2) Tap "Wyloguj"
  - Expected: Session cleared, login screen shown
  - Priority: P1 | Status: NOT TESTED

- [x] TC-AUTH-22: ~~**BUG**~~ ✅ FIXED: Verify Bearer token sent (API Key removed, Bearer token implemented)

**Logout (NEW):**
- [ ] TC-AUTH-30: Logout calls POST /auth/logout before clearing session
  - Prereqs: Logged in with valid token
  - Steps: 1) Tap logout 2) Monitor network calls
  - Expected: POST /auth/logout called with Bearer token, then session cleared. Token blacklisted server-side (JTI stored in token_blacklist table)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-31: Logout succeeds even if /auth/logout API fails (network error)
  - Prereqs: Logged in, network unavailable
  - Steps: 1) Disable network 2) Tap logout
  - Expected: Local session cleared, login screen shown (graceful degradation — backend returns 200 even on error)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-32: Blacklisted token rejected on subsequent API calls
  - Prereqs: Token was used for logout
  - Steps: 1) Logout 2) Manually use old token for API call
  - Expected: 401 Unauthorized (token JTI in blacklist)
  - Priority: P2 | Status: NOT TESTED

**Token Refresh (NEW):**
- [ ] TC-AUTH-40: Automatic token refresh on 401 response
  - Prereqs: Logged in, token near expiry or expired
  - Steps: 1) Wait for token to expire 2) Perform any API action
  - Expected: RetrofitClient intercepts 401, calls POST /auth/refresh, retries original request with new token
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-41: Token refresh prevents infinite loop when refresh endpoint returns 401
  - Prereqs: Token expired AND refresh also fails
  - Steps: 1) Corrupt token 2) Attempt API call
  - Expected: After first refresh attempt fails with 401, redirect to login (no retry loop)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-42: Successful refresh updates stored token in SharedPreferences
  - Prereqs: Token refreshed successfully
  - Steps: 1) Trigger refresh 2) Check SharedPreferences
  - Expected: New token stored, response has { success: true, token: "new_jwt" }
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-AUTH-43: Concurrent requests during refresh — only one refresh call made
  - Prereqs: Multiple simultaneous API calls, token expired
  - Steps: 1) Trigger multiple API calls at once with expired token
  - Expected: Single refresh call, all waiting requests retried with new token
  - Priority: P2 | Status: NOT TESTED

**Profile (NEW):**
- [ ] TC-AUTH-50: GET /auth/profile returns current user data
  - Prereqs: Logged in
  - Steps: 1) Call /auth/profile
  - Expected: Response contains userId, username, role, dzial, employeeCacheId, claims[], claimsVersion
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-AUTH-51: Profile data displayed correctly on Dashboard
  - Prereqs: Logged in, profile fetched
  - Steps: 1) Navigate to Dashboard 2) Check profile card
  - Expected: Username, role, dzial displayed from profile endpoint data
  - Priority: P2 | Status: NOT TESTED

---

### 2. Dashboard (DashboardActivity)

**Profile Card:**
- [ ] TC-DASH-01: Profile loaded from /api/employee/profile/{userId}
  - Prereqs: Logged in, userId in session
  - Steps: 1) Open Dashboard
  - Expected: Profile card populated from employee data
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-02: Full name displayed (name + fname, fallback to username)
  - Prereqs: Employee data available
  - Steps: 1) Check profile card name
  - Expected: Shows "fname name" or username if employee not linked
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-03: Position and contact info shown when available
  - Prereqs: Employee has workpost set
  - Steps: 1) Check profile card details
  - Expected: Position (workpost) displayed
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-DASH-04: Breadcrumb shows "Home > Panel pracownika"
  - Prereqs: Dashboard loaded
  - Steps: 1) Check top breadcrumb
  - Expected: Correct breadcrumb text
  - Priority: P3 | Status: NOT TESTED

**Tabs:**
- [ ] TC-DASH-10: "Zadania" tab active by default
  - Prereqs: Dashboard loaded
  - Steps: 1) Check default tab state
  - Expected: Zadania tab highlighted, its content visible
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-11: Tab switching shows/hides correct containers
  - Prereqs: Dashboard loaded
  - Steps: 1) Tap "Wnioski" 2) Tap "Zadania"
  - Expected: Correct content shown for each tab
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-12: Tab visual state changes (active/inactive)
  - Prereqs: Dashboard loaded
  - Steps: 1) Switch tabs
  - Expected: Active tab has distinct visual style
  - Priority: P2 | Status: NOT TESTED

**Tasks Tab:**
- [ ] TC-DASH-20: Tasks loaded via POST /api/tasks/list
  - Prereqs: Logged in, tasks exist
  - Steps: 1) Open Dashboard, Zadania tab
  - Expected: Task list populated with id, kontrahent, adres, status, dates
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-21: Search text filters tasks (resets page to 1)
  - Prereqs: Tasks loaded
  - Steps: 1) Type in search field
  - Expected: Tasks filtered by search text, page reset to 1
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-22: Pagination prev/next buttons work
  - Prereqs: More tasks than page size
  - Steps: 1) Tap next 2) Tap prev
  - Expected: Pages navigate correctly
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-23: Page info text "Pokazuje X-Y of Z"
  - Prereqs: Tasks loaded
  - Steps: 1) Check pagination text
  - Expected: Correct range and total shown
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-DASH-24: Loading progress bar shown during fetch
  - Prereqs: Slow network
  - Steps: 1) Trigger task load
  - Expected: Progress indicator visible during loading
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-DASH-25: Error Toast on API failure
  - Prereqs: API returns error
  - Steps: 1) Simulate server error
  - Expected: Toast with error message
  - Priority: P1 | Status: NOT TESTED

**Wnioski Tab:**
- [ ] TC-DASH-30: Wnioski loaded via POST /api/wnioski/list with userId
  - Prereqs: Logged in
  - Steps: 1) Switch to Wnioski tab
  - Expected: User's wnioski listed
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-31: Pagination works
  - Prereqs: Multiple wnioski
  - Steps: 1) Navigate pages
  - Expected: Correct page content
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-32: "Nowy wniosek" button opens NewRequestActivity
  - Prereqs: Wnioski tab active
  - Steps: 1) Tap "Nowy wniosek"
  - Expected: NewRequestActivity opens
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-33: Send wniosek action works (POST /api/wnioski/{id}/wyslij)
  - Prereqs: Draft wniosek exists
  - Steps: 1) Tap send on a draft wniosek
  - Expected: Status changes, list refreshes
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-34: Wnioski reloaded on resume (when returning from NewRequestActivity)
  - Prereqs: Created new wniosek
  - Steps: 1) Create wniosek 2) Return to Dashboard
  - Expected: New wniosek appears in list
  - Priority: P1 | Status: NOT TESTED

**Drawer:**
- [ ] TC-DASH-40: Menu button opens drawer
  - Prereqs: Dashboard loaded
  - Steps: 1) Tap hamburger menu
  - Expected: Drawer slides open
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-41: Drawer shows user name and role
  - Prereqs: Logged in
  - Steps: 1) Open drawer
  - Expected: Username and role displayed in drawer header
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-DASH-42: "Akceptacja wnioskow" navigates to ApprovalActivity
  - Prereqs: Drawer open
  - Steps: 1) Tap "Akceptacja wnioskow"
  - Expected: ApprovalActivity opens
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-43: Logout clears session and returns to login
  - Prereqs: Drawer open
  - Steps: 1) Tap "Wyloguj"
  - Expected: Session cleared, login screen shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-DASH-44: **MISSING**: Drawer links for Tasks, Calendar, Handlowcy, Oferta, Transport, Transport Ceny, Limity Kredytowe
  - Prereqs: Drawer open
  - Steps: 1) Check all drawer items
  - Expected: All screen links present and functional
  - Priority: P1 | Status: NOT TESTED

---

### 3. New Request (NewRequestActivity)

**Current State (PARTIAL):**
- [ ] TC-REQ-01: Back button finishes activity
  - Prereqs: NewRequestActivity open
  - Steps: 1) Tap back button
  - Expected: Activity closes, returns to previous screen
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-02: Request type spinner populated (hardcoded: Urlop, Delegacja, Praca zdalna)
  - Prereqs: Activity open
  - Steps: 1) Check type spinner
  - Expected: Shows available types (currently hardcoded)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-03: Leave type spinner populated (hardcoded)
  - Prereqs: Activity open
  - Steps: 1) Check leave type spinner
  - Expected: Leave types shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-04: Substitute spinner populated (hardcoded)
  - Prereqs: Activity open
  - Steps: 1) Check substitute spinner
  - Expected: Substitute options shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-05: Date pickers open and set date text
  - Prereqs: Activity open
  - Steps: 1) Tap date field 2) Select date
  - Expected: Date picker opens, selected date shown in field
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-06: Submit shows Toast and finishes
  - Prereqs: Activity open
  - Steps: 1) Fill form 2) Tap submit
  - Expected: Toast shown (currently no API call)
  - Priority: P1 | Status: NOT TESTED

**Full API Integration (NEW):**
- [ ] TC-REQ-22: Request types loaded from GET /api/wnioski/typy
  - Prereqs: Network available, logged in
  - Steps: 1) Open NewRequestActivity
  - Expected: Spinner populated from API with id+nazwa pairs (cached 24h on backend). Types include Urlop, Delegacja, Praca zdalna etc.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-23: Leave types loaded from GET /api/wnioski/rodzaje-urlopu only when type is "Urlop"
  - Prereqs: Type spinner loaded
  - Steps: 1) Select "Urlop" type 2) Check leave type spinner
  - Expected: Leave type spinner visible and populated (e.g. Wypoczynkowy, Na zadanie, Okolicznosciowy). Hidden for other types.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-24: Users/substitutes loaded from GET /api/wnioski/uzytkownicy
  - Prereqs: Network available
  - Steps: 1) Open activity 2) Check substitute spinner
  - Expected: Real users from API shown in substitute dropdown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-25: Remote work fields shown only for "Praca zdalna" type
  - Prereqs: Type spinner loaded
  - Steps: 1) Select "Praca zdalna" 2) Check for address fields
  - Expected: Fields for ulica, numer, miasto, kod pocztowy visible. BHP and RODO checkboxes visible. Hidden for other types.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-26: Date format YYYY-MM-DD with zero-padding
  - Prereqs: Date picker used
  - Steps: 1) Select date like January 5 2) Check stored value
  - Expected: "2026-01-05" not "2026-1-5"
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-27: Form submission via POST /api/wnioski
  - Prereqs: Form filled correctly
  - Steps: 1) Fill all required fields 2) Tap submit
  - Expected: POST /api/wnioski called with correct payload (typ, od, do, opis, zastepca, rodzajUrlopu, etc.). Success response: { id, status }
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-28: Validation — required fields (typ, dates, opis)
  - Prereqs: Activity open
  - Steps: 1) Leave required fields empty 2) Tap submit
  - Expected: Validation error shown. Required: typ, dataOd, dataDo, opis. Backend returns 400 with message if invalid.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-29: Error handling — network error
  - Prereqs: Airplane mode
  - Steps: 1) Fill form 2) Tap submit
  - Expected: Toast with network error message, form data preserved
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-30: Error handling — server 400 response
  - Prereqs: Invalid data sent
  - Steps: 1) Submit with conflicting dates
  - Expected: Toast with error message from server response
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-31: Error handling — server 500 response
  - Prereqs: Server error
  - Steps: 1) Trigger 500 (e.g., DB down)
  - Expected: Toast with generic error message
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-REQ-32: Success — shows confirmation and navigates back
  - Prereqs: Valid form submitted
  - Steps: 1) Submit valid form
  - Expected: Success Toast/message, finishes activity, Dashboard refreshes wnioski
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-33: ViewModel preserves form data on screen rotation
  - Prereqs: Form partially filled
  - Steps: 1) Fill some fields 2) Rotate device
  - Expected: All form field values preserved (requires ViewModel implementation)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-34: Freeze period warning for Urlop Wypoczynkowy
  - Prereqs: Freeze period exists for user's department
  - Steps: 1) Select "Urlop" > "Wypoczynkowy" 2) Select dates in freeze period
  - Expected: Warning displayed about freeze period conflict (GET /api/wnioski/zamrozenia/miesiac)
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-REQ-35: Business day calculation shown
  - Prereqs: Date range selected
  - Steps: 1) Select od and do dates
  - Expected: Number of business days calculated and displayed as caption
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-REQ-36: Description textarea (required field)
  - Prereqs: Activity open
  - Steps: 1) Check for description field 2) Try submit without it
  - Expected: Textarea present, validation error if empty
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-REQ-37: File upload via file picker
  - Prereqs: Activity open
  - Steps: 1) Tap attach file 2) Select file from device
  - Expected: File attached to form, visible in attachments list. Uploaded via multipart POST on submission.
  - Priority: P2 | Status: NOT TESTED

---

### 4. Approval (ApprovalActivity)

**Happy Path:**
- [ ] TC-APR-01: Approvals loaded via POST /api/wnioski/approvals
  - Prereqs: Logged in as Manager/HR, pending wnioski exist
  - Steps: 1) Open ApprovalActivity
  - Expected: List of wnioski awaiting approval shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-02: Table shows wniosek items with status badges
  - Prereqs: Approvals loaded
  - Steps: 1) Check list items
  - Expected: Each item shows employee name, type, dates, status badge with correct color
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-03: Approve button -> calls akceptacja-manager or akceptacja-hr based on role
  - Prereqs: Pending wniosek visible
  - Steps: 1) Tap approve on a wniosek
  - Expected: PUT /api/wnioski/{id}/akceptacja-manager (or akceptacja-hr) with approved: true
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-04: Reject button -> same endpoint with approved: false
  - Prereqs: Pending wniosek visible
  - Steps: 1) Tap reject on a wniosek
  - Expected: PUT endpoint called with approved: false, comment optional
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-05: List refreshes after approve/reject
  - Prereqs: Action taken
  - Steps: 1) Approve/reject 2) Check list
  - Expected: List reloaded, actioned item updated/removed
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-06: Search filters results
  - Prereqs: Approvals loaded
  - Steps: 1) Type in search field
  - Expected: Results filtered by employee name or type
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-07: Pagination works
  - Prereqs: Many pending wnioski
  - Steps: 1) Navigate pages
  - Expected: Correct page content
  - Priority: P1 | Status: NOT TESTED

**Edge Cases:**
- [ ] TC-APR-10: Empty list -> "Brak wnioskow" text shown
  - Prereqs: No pending wnioski
  - Steps: 1) Open ApprovalActivity
  - Expected: Empty state message shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-11: Back button finishes activity
  - Prereqs: ApprovalActivity open
  - Steps: 1) Tap back
  - Expected: Returns to Dashboard
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-APR-12: Error state shows Toast
  - Prereqs: API failure
  - Steps: 1) Trigger API error
  - Expected: Toast with error message
  - Priority: P1 | Status: NOT TESTED

**Missing vs Vue:**
- [ ] TC-APR-20: View/preview link (eye icon) -> navigate to EditRequestActivity?readonly=1
  - Prereqs: Wniosek in list
  - Steps: 1) Tap eye/view icon
  - Expected: EditRequestActivity opens in readonly mode
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-APR-21: "Synchronizuj pracownikow" button for Manager/HR roles
  - Prereqs: Logged in as Manager/HR
  - Steps: 1) Check for sync button
  - Expected: Button visible, calls POST /employee/sync
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-APR-22: Subtitle text differs by role (Manager vs HR)
  - Prereqs: Logged in with different roles
  - Steps: 1) Check subtitle text
  - Expected: Different description text for Manager vs HR
  - Priority: P3 | Status: NOT TESTED

---

### 5. Edit Request (EditRequestActivity)

**Loading:**
- [ ] TC-EDIT-01: Existing wniosek loaded from GET /api/wnioski/{id}
  - Prereqs: Valid wniosek ID passed via Intent
  - Steps: 1) Open EditRequestActivity with wniosek ID
  - Expected: All wniosek fields loaded and displayed (typ, od, do, opis, status, files, etc.)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-02: Loading indicator shown while fetching
  - Prereqs: Activity opening
  - Steps: 1) Observe during load
  - Expected: Progress indicator visible until data loaded
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-EDIT-03: 404 error handled (wniosek not found)
  - Prereqs: Invalid wniosek ID
  - Steps: 1) Open with non-existent ID
  - Expected: Error message shown, option to go back
  - Priority: P1 | Status: NOT TESTED

**Form Pre-fill:**
- [ ] TC-EDIT-04: All form fields pre-filled from wniosek data
  - Prereqs: Wniosek loaded
  - Steps: 1) Check all form fields
  - Expected: Type, dates, description, substitute, leave type etc. all populated
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-05: Status badge displayed correctly
  - Prereqs: Wniosek loaded
  - Steps: 1) Check status display
  - Expected: Correct status badge with appropriate color (Szkic, Wyslany, Zaakceptowany, etc.)
  - Priority: P1 | Status: NOT TESTED

**Editing:**
- [ ] TC-EDIT-06: Edit and save changes via PUT /api/wnioski/{id}
  - Prereqs: Wniosek in editable status (Szkic, Do poprawy)
  - Steps: 1) Modify description 2) Tap save
  - Expected: PUT request sent, success confirmation, data persisted
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-07: Readonly mode for non-editable statuses
  - Prereqs: Wniosek status is Zaakceptowany, Wyslany, or Odrzucony
  - Steps: 1) Open wniosek with final status
  - Expected: All fields disabled/readonly, no save button
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-08: Send to approval via POST /api/wnioski/{id}/wyslij
  - Prereqs: Wniosek in Szkic or Do poprawy status
  - Steps: 1) Tap "Wyslij do akceptacji"
  - Expected: Status changes to Wyslany, UI updates
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-09: Delete wniosek via DELETE /api/wnioski/{id}
  - Prereqs: Wniosek in Szkic status
  - Steps: 1) Tap delete 2) Confirm dialog
  - Expected: Wniosek deleted, navigates back to Dashboard
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-10: Correction comments visible for "Do poprawy" status
  - Prereqs: Wniosek returned for correction with comment
  - Steps: 1) Open wniosek with Do poprawy status
  - Expected: Manager/HR correction comment displayed prominently
  - Priority: P1 | Status: NOT TESTED

**Files:**
- [ ] TC-EDIT-11: Existing files listed
  - Prereqs: Wniosek has attachments
  - Steps: 1) Check files section
  - Expected: File names and upload dates shown
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-EDIT-12: Upload new file
  - Prereqs: Wniosek in editable status
  - Steps: 1) Tap attach 2) Select file
  - Expected: File uploaded via multipart POST, appears in list
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-EDIT-13: Delete file
  - Prereqs: File attached to wniosek
  - Steps: 1) Tap delete on file
  - Expected: File removed from list and server
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-EDIT-14: Download file
  - Prereqs: File attached
  - Steps: 1) Tap file name
  - Expected: File downloaded and opened/saved
  - Priority: P2 | Status: NOT TESTED

**Edge Cases:**
- [ ] TC-EDIT-15: Screen rotation preserves unsaved edits
  - Prereqs: Form modified but not saved
  - Steps: 1) Edit fields 2) Rotate device
  - Expected: All modifications preserved via ViewModel
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-EDIT-16: Concurrent edit detection (optimistic locking)
  - Prereqs: Same wniosek opened on two devices
  - Steps: 1) Edit on device A 2) Save on device B 3) Save on device A
  - Expected: Error message about conflict
  - Priority: P3 | Status: NOT TESTED

---

### 6. Tasks List (TasksListActivity)

**List Loading:**
- [ ] TC-TASK-01: Tasks loaded via GET /api/tasks (paginated)
  - Prereqs: Logged in, tasks exist
  - Steps: 1) Open TasksListActivity
  - Expected: Task list with id, typ, tytul, kontrahentNazwa, assignedToName, status, termin, isOverdue. Default pageSize=10.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-02: Loading skeleton shown during fetch
  - Prereqs: Activity opening
  - Steps: 1) Observe during load
  - Expected: Skeleton/shimmer visible
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TASK-03: Empty state shown when no tasks
  - Prereqs: No tasks visible for user
  - Steps: 1) Check list
  - Expected: "Brak zadan" message
  - Priority: P1 | Status: NOT TESTED

**Filtering:**
- [ ] TC-TASK-04: Search filters by tytul, kontrahent, id, or assignee username
  - Prereqs: Tasks loaded
  - Steps: 1) Type search text
  - Expected: List filtered, ILIKE search on backend (min 300ms debounce)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-05: Status filter dropdown (Nowe, W trakcie, Do wyjasnienia, Przeterminowane, Zakonczone, Anulowane)
  - Prereqs: Tasks loaded
  - Steps: 1) Select status filter
  - Expected: List filtered by selected status
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-06: Type filter (task types from GET /api/tasks/typy)
  - Prereqs: Tasks loaded, types fetched
  - Steps: 1) Select type filter
  - Expected: List filtered by typ field
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-07: Filters reset page to 1
  - Prereqs: On page 2+
  - Steps: 1) Apply filter
  - Expected: Page resets to 1
  - Priority: P2 | Status: NOT TESTED

**Pagination:**
- [ ] TC-TASK-08: Pagination prev/next buttons work
  - Prereqs: More tasks than page size
  - Steps: 1) Tap next 2) Tap prev
  - Expected: Correct page loaded
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-09: Page info shows "Strona X z Y"
  - Prereqs: Tasks loaded
  - Steps: 1) Check pagination info
  - Expected: Correct page number and total pages
  - Priority: P2 | Status: NOT TESTED

**Navigation:**
- [ ] TC-TASK-10: Tap task row opens TaskDetailActivity
  - Prereqs: Tasks in list
  - Steps: 1) Tap a task row
  - Expected: TaskDetailActivity opens with correct task ID
  - Priority: P1 | Status: NOT TESTED

**Create Task:**
- [ ] TC-TASK-11: Create task dialog/screen with required fields
  - Prereqs: TasksListActivity open
  - Steps: 1) Tap "Nowe zadanie" button 2) Fill: typ, tytul, opis, termin, kontrahentNazwa, assignedToIds
  - Expected: Form shown with all fields. AssignedToIds supports multi-select (bulk assign).
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-12: Submit create task via POST /api/tasks
  - Prereqs: Form filled
  - Steps: 1) Tap submit
  - Expected: Created response with templateId and instances[]. List refreshes. Each assignee gets their own task instance.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-13: Validation — tytul and assignedToIds required
  - Prereqs: Empty form
  - Steps: 1) Tap submit with empty fields
  - Expected: Validation error shown
  - Priority: P1 | Status: NOT TESTED

**Visibility:**
- [ ] TC-TASK-14: Regular user sees only assigned/created/observed tasks
  - Prereqs: Logged in as regular User
  - Steps: 1) Check task list
  - Expected: Only tasks where user is assignee, creator, or observer
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-15: Manager sees own + subordinate tasks
  - Prereqs: Logged in as Manager
  - Steps: 1) Check task list
  - Expected: Own tasks + tasks of managed employees visible
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TASK-16: HR/Admin sees all tasks
  - Prereqs: Logged in as HR or Admin
  - Steps: 1) Check task list
  - Expected: All tasks visible (no visibility filter)
  - Priority: P1 | Status: NOT TESTED

**Overdue Indicator:**
- [ ] TC-TASK-17: Overdue tasks marked with visual indicator
  - Prereqs: Task with past termin and active status
  - Steps: 1) Check task row
  - Expected: IsOverdue flag causes visual highlight (red/warning indicator)
  - Priority: P2 | Status: NOT TESTED

---

### 7. Task Detail (TaskDetailActivity)

**Detail Loading:**
- [ ] TC-TDET-01: Task detail loaded from GET /api/tasks/{id}
  - Prereqs: Valid task ID
  - Steps: 1) Open TaskDetailActivity
  - Expected: All detail fields shown: typ, tytul, opis, kontrahentNazwa, termin, status, assignedToName, createdByName, createdAt, startedAt, completedAt, totalInstances, completedInstances
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-02: 404 handled for non-existent task
  - Prereqs: Invalid task ID
  - Steps: 1) Open with bad ID
  - Expected: Error message, option to go back
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-03: 403 Forbid handled for no-access task
  - Prereqs: Task user cannot see
  - Steps: 1) Open task outside visibility
  - Expected: Access denied message
  - Priority: P1 | Status: NOT TESTED

**Status Transitions:**
- [ ] TC-TDET-04: Status change via PUT /api/tasks/{id}/status
  - Prereqs: Task in valid state for transition
  - Steps: 1) Tap status action button
  - Expected: Status updated, UI refreshes. Valid transitions enforced:
    - Nowe -> W trakcie, Anulowane
    - W trakcie -> Do wyjasnienia, Zakonczone, Anulowane
    - Do wyjasnienia -> W trakcie, Anulowane
    - Przeterminowane -> W trakcie, Zakonczone, Anulowane
    - Zakonczone -> (terminal)
    - Anulowane -> (terminal)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-05: Invalid transition rejected with error message
  - Prereqs: Attempt invalid transition
  - Steps: 1) Try to move Zakonczone to W trakcie
  - Expected: Error "Niedozwolona zmiana statusu" from backend (400)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-06: Terminal statuses (Zakonczone, Anulowane) hide action buttons
  - Prereqs: Task completed or cancelled
  - Steps: 1) Check available actions
  - Expected: No status change buttons visible
  - Priority: P1 | Status: NOT TESTED

**Comments Tab:**
- [ ] TC-TDET-10: Comments loaded from GET /api/tasks/{id}/comments
  - Prereqs: Task with comments
  - Steps: 1) Switch to comments tab
  - Expected: Comments listed chronologically with username, tresc, createdAt
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-11: Add comment via POST /api/tasks/{id}/comments
  - Prereqs: Comment tab active
  - Steps: 1) Type comment 2) Tap send
  - Expected: Comment added, list refreshes. Tresc required.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-12: Empty comment prevented
  - Prereqs: Comment input empty
  - Steps: 1) Tap send without text
  - Expected: Validation error, no API call
  - Priority: P2 | Status: NOT TESTED

**Files Tab:**
- [ ] TC-TDET-15: Files listed from GET /api/tasks/{id}/files
  - Prereqs: Task with files
  - Steps: 1) Switch to files tab
  - Expected: Files listed with nazwaPliku, uploadedBy, createdAt
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-16: Upload file via POST /api/tasks/{id}/files (multipart)
  - Prereqs: Files tab active
  - Steps: 1) Tap upload 2) Select file
  - Expected: File uploaded, list refreshes
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TDET-17: Download file via GET /api/tasks/{id}/files/{fileId}
  - Prereqs: File in list
  - Steps: 1) Tap file
  - Expected: File downloaded as application/octet-stream
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TDET-18: Delete file via DELETE /api/tasks/{id}/files/{fileId}
  - Prereqs: File in list, user has permission
  - Steps: 1) Tap delete on file 2) Confirm
  - Expected: File removed from list and server
  - Priority: P2 | Status: NOT TESTED

**Observers Tab:**
- [ ] TC-TDET-20: Observers listed from GET /api/tasks/{id}/observers
  - Prereqs: Task with observers
  - Steps: 1) Switch to observers tab
  - Expected: Observers listed with username, dzial, addedByName, createdAt
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TDET-21: Add observers via POST /api/tasks/{id}/observers
  - Prereqs: Observers tab active
  - Steps: 1) Search user 2) Select 3) Tap add
  - Expected: Observers added (supports multiple userIds). Duplicate ignored (ON CONFLICT DO NOTHING).
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TDET-22: Remove observer via DELETE /api/tasks/{id}/observers/{userId}
  - Prereqs: Observer in list, user has visibility
  - Steps: 1) Tap remove on observer
  - Expected: Observer removed from list
  - Priority: P2 | Status: NOT TESTED

**History Tab:**
- [ ] TC-TDET-25: History loaded from GET /api/tasks/{id}/historia
  - Prereqs: Task with history
  - Steps: 1) Switch to history tab
  - Expected: Timeline of actions: utworzono, zmiana_statusu, komentarz, dodano_plik, usunieto_plik, dodano_obserwatora. Each with username, old/new values, createdAt.
  - Priority: P1 | Status: NOT TESTED

**Notifications:**
- [ ] TC-TDET-30: Status change generates notification for visible users
  - Prereqs: Task with multiple visible users
  - Steps: 1) Change status
  - Expected: Other visible users receive notification (stored in task_notifications, pushed via SignalR)
  - Priority: P2 | Status: NOT TESTED

---

### 8. Calendar (CalendarActivity)

**Month View:**
- [ ] TC-CAL-01: Monthly calendar grid displayed (6 rows x 7 columns, Mon-Sun)
  - Prereqs: CalendarActivity open
  - Steps: 1) Check calendar grid
  - Expected: 42 cells, day headers Pon-Ndz, current month days numbered, prev/next month days grayed out
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-02: Current month and year shown in header (e.g. "Marzec 2026")
  - Prereqs: Activity open
  - Steps: 1) Check month label
  - Expected: Polish month name + year
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-03: Today's date highlighted with blue circle
  - Prereqs: Current month visible
  - Steps: 1) Find today's cell
  - Expected: Day number has blue circle background
  - Priority: P2 | Status: NOT TESTED

**Navigation:**
- [ ] TC-CAL-04: Previous month button navigates back
  - Prereqs: Calendar shown
  - Steps: 1) Tap left arrow
  - Expected: Previous month displayed, zamrozenia reloaded
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-05: Next month button navigates forward
  - Prereqs: Calendar shown
  - Steps: 1) Tap right arrow
  - Expected: Next month displayed, zamrozenia reloaded
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-06: "Dzis" button returns to current month
  - Prereqs: Navigated to different month
  - Steps: 1) Tap "Dzis"
  - Expected: Returns to current month
  - Priority: P2 | Status: NOT TESTED

**Freeze Periods:**
- [ ] TC-CAL-07: Freeze periods loaded from GET /api/wnioski/zamrozenia/miesiac?rok=&miesiac=
  - Prereqs: Freeze periods exist
  - Steps: 1) Open calendar for month with freezes
  - Expected: Freeze periods data loaded
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-08: Freeze period cells have striped red background
  - Prereqs: Freeze period in visible month
  - Steps: 1) Check cells within freeze date range
  - Expected: Cells have diagonal red stripe pattern
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-09: Freeze bar spans across days with label "Brak mozliwosci wybrania wolnego"
  - Prereqs: Freeze period displayed
  - Steps: 1) Check freeze bar
  - Expected: Bar spans correct number of cells, shows text + dates + department
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-CAL-10: Department filter dropdown filters displayed freezes
  - Prereqs: Multiple departments have freezes
  - Steps: 1) Select department from dropdown
  - Expected: Only freezes for selected department shown. Options: Handlowy, Logistyka, Serwis, IT, HR, Administracja
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-11: "Wszystkie dzialy" shows all freeze periods
  - Prereqs: Department filter active
  - Steps: 1) Select "Wszystkie dzialy"
  - Expected: All freezes visible regardless of department
  - Priority: P1 | Status: NOT TESTED

**HR CRUD (role-restricted):**
- [ ] TC-CAL-12: "Dodaj nowy" button visible only for HR role
  - Prereqs: Logged in as HR
  - Steps: 1) Check for add button
  - Expected: Button visible for HR, hidden for other roles
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-13: Create freeze period dialog — dzial, dataOd, dataDo, opis fields
  - Prereqs: HR user, tap "Dodaj nowy"
  - Steps: 1) Tap add 2) Fill form: dzial (required), dataOd (required), dataDo (required), opis (optional) 3) Tap save
  - Expected: POST /api/wnioski/zamrozenia called. Freeze appears on calendar.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-14: Edit freeze period — tap existing freeze bar
  - Prereqs: HR user, freeze visible
  - Steps: 1) Tap freeze bar
  - Expected: Edit dialog opens with pre-filled values. PUT /api/wnioski/zamrozenia/{id} on save.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-15: Delete freeze period
  - Prereqs: HR user, editing freeze
  - Steps: 1) Tap delete in edit dialog 2) Confirm
  - Expected: DELETE /api/wnioski/zamrozenia/{id} called, freeze removed from calendar
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-16: Validation — dzial and dates required for create/edit
  - Prereqs: Modal open
  - Steps: 1) Leave required fields empty 2) Tap save
  - Expected: Error "Wypelnij dzial i daty." shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-CAL-17: Non-HR user cannot edit/create freezes
  - Prereqs: Logged in as non-HR
  - Steps: 1) Tap freeze bar
  - Expected: No edit dialog opens. Add button not visible.
  - Priority: P1 | Status: NOT TESTED

**Error Handling:**
- [ ] TC-CAL-18: Network error during load shows error state
  - Prereqs: Network unavailable
  - Steps: 1) Open calendar
  - Expected: Error handling, no crash
  - Priority: P1 | Status: NOT TESTED

---

### 9. Handlowcy (HandlowcyActivity)

**Data Loading:**
- [ ] TC-HAND-01: Handlowcy loaded from GET /api/ax/handlowcy
  - Prereqs: Logged in, AX accessible
  - Steps: 1) Open HandlowcyActivity
  - Expected: Table with columns: HandlowiecId, Imie, Nazwisko, Stanowisko, Dzial, Status. Data cached 30min on backend.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-HAND-02: Loading indicator shown during fetch
  - Prereqs: Activity opening
  - Steps: 1) Observe during load
  - Expected: Loading indicator visible
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-HAND-03: AX unreachable — mock data fallback displayed
  - Prereqs: AX database offline
  - Steps: 1) Open HandlowcyActivity
  - Expected: Fallback mock data shown (6 sample handlowcy). No error crash.
  - Priority: P1 | Status: NOT TESTED

**Filtering:**
- [ ] TC-HAND-04: Search text filters by name/id
  - Prereqs: Data loaded
  - Steps: 1) Type in search field
  - Expected: Table filtered by Imie, Nazwisko, or HandlowiecId
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-HAND-05: Department filter (Dzial dropdown)
  - Prereqs: Data loaded
  - Steps: 1) Select department from dropdown
  - Expected: Table filtered by Dzial value (e.g. Handlowy, Logistyka, Serwis)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-HAND-06: Status indicator (active=1, inactive=0)
  - Prereqs: Data loaded with mixed statuses
  - Steps: 1) Check status column
  - Expected: Active (1) and inactive (0) shown with distinct visual style
  - Priority: P2 | Status: NOT TESTED

**Display:**
- [ ] TC-HAND-07: Full name displayed as "Imie Nazwisko"
  - Prereqs: Data loaded
  - Steps: 1) Check name column
  - Expected: First name + last name correctly formatted
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-HAND-08: Empty state if no handlowcy returned
  - Prereqs: Empty response
  - Steps: 1) Check table
  - Expected: Empty state message
  - Priority: P2 | Status: NOT TESTED

---

### 10. Transport (TransportActivity)

**Route Planning:**
- [ ] TC-TRANS-01: Map view displayed
  - Prereqs: TransportActivity open
  - Steps: 1) Check map component
  - Expected: Map rendered (Google Maps or similar)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TRANS-02: Route calculation via POST /api/transport/route
  - Prereqs: Start and end points entered
  - Steps: 1) Enter start location 2) Enter end location 3) Tap calculate
  - Expected: Route calculated with TomTom API. Payload includes startLat, startLng, endLat, endLng, optional truckType and waypoints.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TRANS-03: Truck type selection (weight, height, width, length, axleWeight, numberOfAxles)
  - Prereqs: Route form open
  - Steps: 1) Select/configure truck type
  - Expected: TruckType params sent with route request for accurate truck routing
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TRANS-04: Waypoints support (optional intermediate stops)
  - Prereqs: Route form
  - Steps: 1) Add waypoint 2) Calculate route
  - Expected: Waypoints array sent to API
  - Priority: P3 | Status: NOT TESTED

**Fleet Status:**
- [ ] TC-TRANS-05: Vehicles loaded from GET /api/transport/vehicles
  - Prereqs: Activity open, Webfleet configured
  - Steps: 1) Check vehicle list
  - Expected: Vehicle list from Webfleet (cached 15min on backend)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TRANS-06: Single vehicle detail via GET /api/transport/vehicles/{objectNo}
  - Prereqs: Vehicle in list
  - Steps: 1) Tap vehicle
  - Expected: Vehicle details shown
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TRANS-07: Vehicle not found returns 404
  - Prereqs: Invalid objectNo
  - Steps: 1) Access non-existent vehicle
  - Expected: "Pojazd nie znaleziony" error
  - Priority: P2 | Status: NOT TESTED

**Error Handling:**
- [ ] TC-TRANS-08: Route calculation error handled
  - Prereqs: TomTom API down or invalid coordinates
  - Steps: 1) Enter invalid coordinates 2) Calculate
  - Expected: Error "Blad podczas obliczania trasy" shown as Toast
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TRANS-09: Webfleet fetch error handled
  - Prereqs: Webfleet API unavailable
  - Steps: 1) Open vehicles
  - Expected: Error "Blad podczas pobierania pojazdow" shown
  - Priority: P1 | Status: NOT TESTED

---

### 11. Transport Ceny (TransportCenyListActivity)

**List Loading:**
- [ ] TC-TCENY-01: List loaded from GET /api/transport-ceny (paginated, default pageSize=20)
  - Prereqs: Logged in
  - Steps: 1) Open TransportCenyListActivity
  - Expected: Table with columns: ID, Kontrahent, Towar, Ilosc ton, Adres zaladunku, Adres odbioru, Koszt szacowany, Koszt zatwierdzony, Status, Data. Shows "X z Y wnioskow" subtitle.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-02: Loading skeleton shown during fetch
  - Prereqs: Activity opening
  - Steps: 1) Observe during load
  - Expected: Skeleton/shimmer visible in table area
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCENY-03: Empty state "Brak wnioskow" when no items
  - Prereqs: No transport ceny requests exist
  - Steps: 1) Check table
  - Expected: Empty row with message
  - Priority: P1 | Status: NOT TESTED

**Tabs:**
- [ ] TC-TCENY-04: "Moje wnioski" tab shows only user's own requests (tab=mine)
  - Prereqs: User has created requests
  - Steps: 1) Tap "Moje wnioski" tab
  - Expected: Only requests where user_id = current user. Query param tab=mine sent.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-05: "Wszystkie" tab visible only for Logistyka/Manager/Admin roles
  - Prereqs: Logged in as Logistyka
  - Steps: 1) Check tab visibility
  - Expected: "Wszystkie" tab shown for Logistyka/Manager/Admin; hidden for regular users
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-06: "Wszystkie" tab shows all requests (no user_id filter)
  - Prereqs: Logged in as Logistyka
  - Steps: 1) Tap "Wszystkie"
  - Expected: All transport price requests visible
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-07: Regular user sees only "Moje wnioski" tab
  - Prereqs: Logged in as regular User (non-logistyka)
  - Steps: 1) Check tabs
  - Expected: Only "Moje wnioski" tab visible, "Wszystkie" hidden
  - Priority: P1 | Status: NOT TESTED

**Filtering:**
- [ ] TC-TCENY-08: Status filter dropdown (PENDING, APPROVED, REJECTED, COMPLETED)
  - Prereqs: List loaded
  - Steps: 1) Select status from dropdown
  - Expected: List filtered by status. Labels: Oczekujacy, Zatwierdzony, Odrzucony, Zakonczony
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-09: Search input filters by kontrahent_nazwa, towar, adres_zaladunku, adres_odbioru
  - Prereqs: List loaded
  - Steps: 1) Type search text
  - Expected: List filtered (ILIKE on backend), 300ms debounce
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-10: Filter changes reset page to 1
  - Prereqs: On page 2+
  - Steps: 1) Apply any filter
  - Expected: Page resets to 1
  - Priority: P2 | Status: NOT TESTED

**Pagination:**
- [ ] TC-TCENY-11: Pagination with prev/next buttons
  - Prereqs: More than 20 items
  - Steps: 1) Tap Nastepna 2) Tap Poprzednia
  - Expected: Correct pages loaded. "Strona X z Y" shown.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-12: Prev disabled on page 1, Next disabled on last page
  - Prereqs: List loaded
  - Steps: 1) Check button states on first/last page
  - Expected: Buttons disabled appropriately
  - Priority: P2 | Status: NOT TESTED

**Status Badges:**
- [ ] TC-TCENY-13: Status badge colors match Vue design
  - Prereqs: List with mixed statuses
  - Steps: 1) Check badge colors
  - Expected: PENDING=yellow/amber, APPROVED=green, REJECTED=red, COMPLETED=gray
  - Priority: P2 | Status: NOT TESTED

**Navigation:**
- [ ] TC-TCENY-14: Tap row opens TransportCenyDetailActivity
  - Prereqs: Items in list
  - Steps: 1) Tap a row
  - Expected: Detail activity opens with correct request ID
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCENY-15: "Nowy wniosek" button opens TransportCenyNewActivity
  - Prereqs: List open
  - Steps: 1) Tap "Nowy wniosek"
  - Expected: New request form opens
  - Priority: P1 | Status: NOT TESTED

**Money Formatting:**
- [ ] TC-TCENY-16: Szacowany and zatwierdzony koszt formatted with PLN currency
  - Prereqs: Items with costs
  - Steps: 1) Check cost columns
  - Expected: Costs formatted as money (e.g. "1 234,56 PLN"). Zatwierdzony shows "-" if null.
  - Priority: P2 | Status: NOT TESTED

---

### 12. Transport Ceny Detail (TransportCenyDetailActivity)

**Detail Loading:**
- [ ] TC-TCED-01: Detail loaded from GET /api/transport-ceny/{id}
  - Prereqs: Valid request ID
  - Steps: 1) Open TransportCenyDetailActivity
  - Expected: Request data shown: id, username, kontrahent_nazwa, towar, ilosc_ton, sklad, ax_vend_contract_id, ax_cust_contract_id, adres_zaladunku, odbiorca, adres_odbioru, szacowany_koszt, zatwierdzony_koszt, status, komentarz_handlowiec, komentarz_logistyka, reviewed_by_username, reviewed_at. History array loaded.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-02: Loading indicator during fetch
  - Prereqs: Activity opening
  - Steps: 1) Observe
  - Expected: "Ladowanie..." text shown
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCED-03: 404 "Wniosek nie znaleziony" for non-existent ID
  - Prereqs: Invalid ID
  - Steps: 1) Open with bad ID
  - Expected: Error message shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-04: 403 Forbid for request not belonging to user (non-logistyka)
  - Prereqs: Regular user accessing another user's request
  - Steps: 1) Open request created by another user
  - Expected: Access denied error
  - Priority: P1 | Status: NOT TESTED

**Data Display:**
- [ ] TC-TCED-05: All info fields displayed in info grid
  - Prereqs: Detail loaded
  - Steps: 1) Check all info items
  - Expected: Kontrahent, Towar, Ilosc ton, Sklad, Kontrakt zakupu AX, Kontrakt sprzedazy AX, Adres zaladunku, Odbiorca, Adres odbioru — all shown with "-" for null values
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-06: Cost section shows szacowany and zatwierdzony (if exists) costs
  - Prereqs: Detail loaded
  - Steps: 1) Check cost boxes
  - Expected: Szacowany koszt always shown. Zatwierdzony koszt shown only if non-null (green box).
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-07: Komentarz handlowca shown when present
  - Prereqs: Request has handlowiec comment
  - Steps: 1) Check comment section
  - Expected: Comment box with label "Komentarz handlowca:" and text
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCED-08: Komentarz logistyki shown when present
  - Prereqs: Request reviewed with comment
  - Steps: 1) Check comment section
  - Expected: Blue comment box with label "Komentarz logistyki:" and text
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCED-09: Reviewed info shown (reviewer name + date)
  - Prereqs: Request reviewed
  - Steps: 1) Check bottom of detail
  - Expected: "Sprawdzony przez {name} dnia {date}" text
  - Priority: P2 | Status: NOT TESTED

**Review Panel (Logistyka only):**
- [ ] TC-TCED-10: Review panel visible only for Logistyka/Manager/Admin AND status is PENDING
  - Prereqs: Logged in as Logistyka, request PENDING
  - Steps: 1) Check for review panel
  - Expected: Panel "Decyzja logistyki" shown with koszt input, komentarz textarea, approve/reject buttons
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-11: Review panel hidden for non-logistics users
  - Prereqs: Logged in as regular User
  - Steps: 1) Check for review panel
  - Expected: Panel not visible even on PENDING requests
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-12: Review panel hidden for non-PENDING status
  - Prereqs: Logged in as Logistyka, request APPROVED
  - Steps: 1) Check for review panel
  - Expected: Panel not visible
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-13: Approve with zatwierdzony koszt via POST /api/transport-ceny/{id}/review
  - Prereqs: Review panel visible
  - Steps: 1) Enter koszt 2) Optional comment 3) Tap "Zatwierdz"
  - Expected: POST with { approved: true, zatwierdzonyKoszt: number, komentarz: string }. Status changes to APPROVED. Detail reloads. Success message "Wniosek zatwierdzony!"
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-14: Reject with comment via POST /api/transport-ceny/{id}/review
  - Prereqs: Review panel visible
  - Steps: 1) Add comment 2) Tap "Odrzuc"
  - Expected: POST with { approved: false, komentarz: string, zatwierdzonyKoszt: null }. Status changes to REJECTED. Success message "Wniosek odrzucony."
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-15: Review koszt pre-filled with szacowany_koszt_transportu
  - Prereqs: Review panel visible
  - Steps: 1) Check koszt input default value
  - Expected: Pre-filled with szacowany_koszt_transportu value
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCED-16: Review error shown on failure
  - Prereqs: Review API fails
  - Steps: 1) Submit review, API returns error
  - Expected: Error message shown in review panel
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-17: Review on non-PENDING request returns 400 "Wniosek nie jest w statusie PENDING"
  - Prereqs: Attempt review on already-reviewed request
  - Steps: 1) Submit review (race condition)
  - Expected: Error message from backend displayed
  - Priority: P2 | Status: NOT TESTED

**History:**
- [ ] TC-TCED-20: History timeline displayed from history array
  - Prereqs: Request has history entries
  - Steps: 1) Check history section
  - Expected: Timeline with entries: akcja (Utworzono, Zatwierdzono, Odrzucono), stary_status -> nowy_status, username, created_at, komentarz. Chronological order.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCED-21: Status labels in Polish (Oczekujacy, Zatwierdzony, Odrzucony, Zakonczony)
  - Prereqs: History with status entries
  - Steps: 1) Check status text
  - Expected: PENDING=Oczekujacy, APPROVED=Zatwierdzony, REJECTED=Odrzucony, COMPLETED=Zakonczony
  - Priority: P2 | Status: NOT TESTED

---

### 13. Transport Ceny New (TransportCenyNewActivity)

**Form Display:**
- [ ] TC-TCEN-01: Form displayed with all fields
  - Prereqs: Activity open
  - Steps: 1) Check form fields
  - Expected: Sections: Kontrakt AX (optional), Dane wniosku (kontrahent*, towar, ilosc ton, sklad=disabled/"Glowny", adres zaladunku, odbiorca, adres odbioru, szacowany koszt*), Komentarz
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-02: Sklad field disabled with default "Glowny"
  - Prereqs: Form open
  - Steps: 1) Check sklad field
  - Expected: Input disabled, shows "Glowny" (AX field in preparation note shown)
  - Priority: P2 | Status: NOT TESTED

**AX Contract Search:**
- [ ] TC-TCEN-03: AX contract search via GET /api/transport-ceny/ax-kontrakty?search=
  - Prereqs: Form open
  - Steps: 1) Type 2+ characters in contract search field
  - Expected: Dropdown with matching AX vend contracts after 400ms debounce. Shows ltVendContractId, vendName, itemId, qty.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-04: Search with <2 characters clears results
  - Prereqs: Previous search results shown
  - Steps: 1) Clear search to 1 character
  - Expected: Dropdown hidden, results cleared
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCEN-05: Select contract auto-fills form fields
  - Prereqs: AX contract results shown
  - Steps: 1) Tap a contract
  - Expected: Auto-fills: axVendContractId, axCustContractId, kontrahentNazwa (vendName), towar (itemId), iloscTon (qty), adresOdbioru (deliveryAddress), szacowanyKosztTransportu (estimatedTransportCost if exists)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-06: AX unavailable — graceful error "AX niedostepny"
  - Prereqs: AX database unreachable
  - Steps: 1) Search for contract
  - Expected: Response { error: "AX niedostepny", data: [] }. No crash, form still usable manually.
  - Priority: P1 | Status: NOT TESTED

**Validation:**
- [ ] TC-TCEN-07: Submit without kontrahent shows error "Kontrahent jest wymagany"
  - Prereqs: kontrahentNazwa empty
  - Steps: 1) Tap submit
  - Expected: Client-side validation error shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-08: Submit with szacowanyKosztTransportu <= 0 shows error
  - Prereqs: Cost is 0 or negative
  - Steps: 1) Tap submit
  - Expected: Error "Szacowany koszt transportu musi byc > 0"
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-09: Backend validation — KontrahentNazwa required (400)
  - Prereqs: Bypass client validation somehow
  - Steps: 1) Send empty kontrahentNazwa to API
  - Expected: 400 "KontrahentNazwa jest wymagana"
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCEN-10: Backend validation — SzacowanyKosztTransportu > 0 required (400)
  - Prereqs: Bypass client validation
  - Steps: 1) Send 0 cost to API
  - Expected: 400 "SzacowanyKosztTransportu musi byc > 0"
  - Priority: P2 | Status: NOT TESTED

**Submission:**
- [ ] TC-TCEN-11: Successful submit via POST /api/transport-ceny
  - Prereqs: Valid form
  - Steps: 1) Fill form 2) Tap "Wyslij do logistyki"
  - Expected: 201 Created with { id, status: "PENDING" }. Success message "Wniosek zostal wyslany do logistyki!". Auto-redirect to list after 1.5s.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-12: Loading state during submit (button disabled, text "Wysylanie...")
  - Prereqs: Submit in progress
  - Steps: 1) Tap submit 2) Observe button
  - Expected: Button disabled with loading text
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-TCEN-13: API error shown in error message
  - Prereqs: Backend returns error
  - Steps: 1) Submit, API fails
  - Expected: Error message shown, form data preserved
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-14: Cancel button navigates back to list
  - Prereqs: Form open
  - Steps: 1) Tap "Anuluj"
  - Expected: Navigates to TransportCenyListActivity without saving
  - Priority: P1 | Status: NOT TESTED

**Edge Cases:**
- [ ] TC-TCEN-15: Screen rotation preserves form data
  - Prereqs: Form partially filled
  - Steps: 1) Fill some fields 2) Rotate device
  - Expected: All field values preserved via ViewModel
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-TCEN-16: Network error during submit
  - Prereqs: Airplane mode
  - Steps: 1) Fill form 2) Submit
  - Expected: Error shown, form data preserved
  - Priority: P1 | Status: NOT TESTED

---

### 14. Limity Kredytowe List (LimityKredytoweListActivity)

**List Loading:**
- [ ] TC-LIMIT-01: List loaded from GET /api/limity-kredytowe (paginated, default pageSize=20)
  - Prereqs: Logged in
  - Steps: 1) Open LimityKredytoweListActivity
  - Expected: Table with: id, kontrahent_account_num, kontrahent_nazwa, obecny_limit, wnioskowany_limit, status, ax_sync, created_at, created_by (username)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LIMIT-02: Loading indicator during fetch
  - Prereqs: Activity opening
  - Steps: 1) Observe
  - Expected: Loading indicator visible
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-LIMIT-03: Empty state when no wniosek
  - Prereqs: No credit limit requests
  - Steps: 1) Check table
  - Expected: Empty state message
  - Priority: P1 | Status: NOT TESTED

**Tabs:**
- [ ] TC-LIMIT-04: "Moje" tab shows only user's own requests (tab=mine)
  - Prereqs: User has created requests
  - Steps: 1) Tap "Moje" tab
  - Expected: Only requests where user_id = current user
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LIMIT-05: "Wszystkie" tab visible for Windykacja/Admin users
  - Prereqs: Logged in as Windykacja or Admin
  - Steps: 1) Check tab visibility
  - Expected: "Wszystkie" tab shown. Regular users only see requests they created, manage, or are viewers of.
  - Priority: P1 | Status: NOT TESTED

**Filtering:**
- [ ] TC-LIMIT-06: Status filter (Szkic, Wyslany, Zaakceptowany, Odrzucony, etc.)
  - Prereqs: List loaded
  - Steps: 1) Select status
  - Expected: List filtered by status
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LIMIT-07: Search filters by kontrahent_account_num or kontrahent_nazwa (ILIKE)
  - Prereqs: List loaded
  - Steps: 1) Type search text
  - Expected: List filtered
  - Priority: P1 | Status: NOT TESTED

**Pagination:**
- [ ] TC-LIMIT-08: Pagination works (prev/next, page info)
  - Prereqs: More than 20 items
  - Steps: 1) Navigate pages
  - Expected: Correct pages loaded
  - Priority: P1 | Status: NOT TESTED

**Visibility/Access:**
- [ ] TC-LIMIT-09: Regular user sees only: own, managed employees', or viewer-granted requests
  - Prereqs: Logged in as regular user
  - Steps: 1) Check list
  - Expected: Only visible requests shown (via employee_manager or limity_kredytowe_viewers)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LIMIT-10: Windykacja user sees all requests
  - Prereqs: Logged in as Windykacja/Admin
  - Steps: 1) Check list
  - Expected: All credit limit requests visible
  - Priority: P1 | Status: NOT TESTED

**Navigation:**
- [ ] TC-LIMIT-11: Tap row opens LimitKredytowyDetailActivity
  - Prereqs: Items in list
  - Steps: 1) Tap a row
  - Expected: Detail activity opens with correct ID
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LIMIT-12: "Nowy wniosek" button opens LimitKredytowyActivity (create form)
  - Prereqs: List open
  - Steps: 1) Tap new button
  - Expected: Create form opens
  - Priority: P1 | Status: NOT TESTED

---

### 15. Limit Kredytowy Detail (LimitKredytowyDetailActivity)

**Detail Loading:**
- [ ] TC-LDET-01: Detail loaded from GET /api/limity-kredytowe/{id}
  - Prereqs: Valid ID
  - Steps: 1) Open detail
  - Expected: All fields shown: kontrahent_account_num, kontrahent_nazwa, obecny_limit, saldo, zamowione, pozostaly_kredyt, wartosc_zabezpieczen, naklady/przychody (poprzedni + biezacy), zadluzenie_przeterminowane, wnioskowany_limit, termin_zabezpieczen, opis_zabezpieczen, nowe_zabezpieczenia, dodatkowe_dochody, zobowiazania, uwagi, potwierdzone_przeterminowane, rozliczenie_plonami, status, ax_sync
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LDET-02: 404 "Wniosek nie znaleziony" for non-existent ID
  - Prereqs: Invalid ID
  - Steps: 1) Open with bad ID
  - Expected: Error message shown
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LDET-03: 403 for unauthorized access
  - Prereqs: User not creator/manager/windykacja/viewer
  - Steps: 1) Access restricted wniosek
  - Expected: Access denied
  - Priority: P1 | Status: NOT TESTED

**AX Sync:**
- [ ] TC-LDET-04: AX sync status indicator (ax_sync boolean)
  - Prereqs: Detail loaded
  - Steps: 1) Check AX sync indicator
  - Expected: Green indicator if ax_sync=true, warning if false (AX was unreachable during creation)
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-LDET-05: Manual sync via POST /api/limity-kredytowe/sync/{accountNum}
  - Prereqs: Detail loaded
  - Steps: 1) Tap sync button
  - Expected: Financial data refreshed from AX
  - Priority: P2 | Status: NOT TESTED

**Viewers:**
- [ ] TC-LDET-06: Viewers list from GET /api/limity-kredytowe/{id}/viewers
  - Prereqs: Wniosek has viewers
  - Steps: 1) Check viewers section
  - Expected: List of viewers with username, created_at
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-LDET-07: Add viewer via POST /api/limity-kredytowe/{id}/viewers
  - Prereqs: Creator/manager/windykacja
  - Steps: 1) Search user (min 2 chars, GET /limity-kredytowe/users/search) 2) Add viewer
  - Expected: Viewer added, list refreshes
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-LDET-08: Remove viewer via DELETE /api/limity-kredytowe/{id}/viewers/{userId}
  - Prereqs: Viewer exists, user has permission
  - Steps: 1) Tap remove
  - Expected: Viewer removed
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-LDET-09: Only creator/manager/windykacja can manage viewers (403 otherwise)
  - Prereqs: Regular user who is not creator/manager
  - Steps: 1) Try to add/remove viewer
  - Expected: 403 Forbidden
  - Priority: P1 | Status: NOT TESTED

---

### 16. Limit Kredytowy New (LimitKredytowyActivity)

**Kontrahent Search:**
- [ ] TC-LNEW-01: Kontrahent search via GET /api/kontrahenci?search=&nip=&adres=&nrAx=
  - Prereqs: Form open
  - Steps: 1) Type 2+ characters in search
  - Expected: Results from AX: id (ACCOUNTNUM), nazwa, obecnyLimit, nip, adres. Min 2 chars required.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LNEW-02: Select kontrahent auto-fills account number
  - Prereqs: Search results shown
  - Steps: 1) Tap kontrahent
  - Expected: kontrahentAccountNum set from selected item
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LNEW-03: Kontrahent finanse loaded from GET /api/kontrahenci/{accountNum}/finanse
  - Prereqs: Kontrahent selected
  - Steps: 1) After selection, check financial data
  - Expected: Financial snapshot loaded: obecnyLimit, saldo, zamowione, pozostalyKredyt, wartoscZabezpieczen, naklady, przychody, zadluzeniePrzeterminowane
  - Priority: P1 | Status: NOT TESTED

**Form:**
- [ ] TC-LNEW-04: Required fields: kontrahentAccountNum, wnioskowanyLimit > 0
  - Prereqs: Form open
  - Steps: 1) Submit with empty fields
  - Expected: Validation error. Backend returns 400 "kontrahentAccountNum i wnioskowanyLimit sa wymagane"
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LNEW-05: Optional fields: terminZabezpieczen, opisZabezpieczen, noweZabezpieczenia, dodatkoweDochody, zobowiazania, uwagi, potwierdzonePrzeterminowane, rozliczeniePlonami
  - Prereqs: Form open
  - Steps: 1) Fill optional fields 2) Submit
  - Expected: All optional fields sent and stored
  - Priority: P2 | Status: NOT TESTED

**Submission:**
- [ ] TC-LNEW-06: Submit via POST /api/limity-kredytowe
  - Prereqs: Valid form
  - Steps: 1) Fill form 2) Tap submit
  - Expected: 201 Created with { id, status: "Szkic", axSync: bool }. Navigate to detail or list.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LNEW-07: AX unreachable — wniosek created without snapshot (axSync=false)
  - Prereqs: AX database offline
  - Steps: 1) Select kontrahent 2) Submit
  - Expected: Wniosek created with axSync=false, kontrahent_nazwa empty. Warning shown.
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-LNEW-08: Kontrahent not found in AX — 404
  - Prereqs: AX reachable but kontrahent doesn't exist
  - Steps: 1) Enter invalid accountNum 2) Submit
  - Expected: 404 "Kontrahent nie znaleziony w AX"
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-LNEW-09: Server error during submit
  - Prereqs: Backend error
  - Steps: 1) Submit form
  - Expected: Error "Blad zapisu: {message}" shown
  - Priority: P1 | Status: NOT TESTED

---

### 17. Navigation (Drawer & Back)

- [ ] TC-NAV-01: Drawer contains links for all screens: Panel, Akceptacja, Zadania, Kalendarz, Handlowcy, Transport, Transport Ceny, Limity Kredytowe, Oferta
  - Prereqs: Logged in, drawer open
  - Steps: 1) Open drawer 2) Check all menu items
  - Expected: All links present and tappable
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-NAV-02: Each drawer link opens correct Activity
  - Prereqs: Drawer open
  - Steps: 1) Tap each link
  - Expected: Correct Activity opens for each link
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-NAV-03: Back button from each new screen returns to previous screen
  - Prereqs: Navigated to any screen
  - Steps: 1) Tap Android back button
  - Expected: Returns to previous screen correctly (Activity finish)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-NAV-04: Breadcrumb navigation (if implemented)
  - Prereqs: On a deep screen (e.g. TransportCenyDetail)
  - Steps: 1) Tap breadcrumb links
  - Expected: Navigate to parent screens (Pulpit > Transport Ceny > Detail)
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-NAV-05: Role-based drawer items — restricted items hidden for unauthorized roles
  - Prereqs: Logged in as regular user
  - Steps: 1) Open drawer
  - Expected: Admin-only or role-restricted items not shown (e.g. Akceptacja only for Manager/HR)
  - Priority: P1 | Status: NOT TESTED

---

### 18. Cross-Cutting Concerns

**Authentication:**
- [x] TC-XC-01: ✅ FIXED -- Bearer token now sent correctly, API Key removed
- [ ] TC-XC-02: 401 response triggers token refresh, then retry, fallback to login redirect
  - Prereqs: Token expired
  - Steps: 1) Make any API call with expired token
  - Expected: Interceptor calls POST /auth/refresh, retries with new token. If refresh also fails -> redirect to login.
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-03: Token persists across app restart (SharedPreferences)
  - Prereqs: Logged in
  - Steps: 1) Kill app 2) Restart
  - Expected: Token present, auto-login
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-04: Claims and claimsVersion stored from login response
  - Prereqs: Login successful
  - Steps: 1) Check stored claims
  - Expected: claims[] and claimsVersion from AuthResponse saved for permission checks
  - Priority: P2 | Status: NOT TESTED

**Navigation:**
- [ ] TC-XC-10: Drawer menu should list all screens
  - Prereqs: Drawer open
  - Steps: 1) Check all items
  - Expected: All 9+ screen links present
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-11: Android back button behavior correct on each screen
  - Prereqs: On any non-Dashboard screen
  - Steps: 1) Tap back
  - Expected: Returns to previous Activity
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-12: Activity results returned properly (e.g., NewRequest -> Dashboard refresh)
  - Prereqs: Created/edited item
  - Steps: 1) Return to parent screen
  - Expected: Parent refreshes data
  - Priority: P1 | Status: NOT TESTED

**Data Display Parity:**
- [ ] TC-XC-20: Status badge colors match Vue for ALL status types
  - Prereqs: Screens with status badges
  - Steps: 1) Check badge colors across all screens
  - Expected: Colors match Vue: Szkic=gray, Wyslany=blue, Zaakceptowany=green, Odrzucony=red, Do poprawy=orange, PENDING=amber, APPROVED=green, REJECTED=red, COMPLETED=gray, Nowe=blue, W trakcie=yellow, Zakonczone=green, Anulowane=gray, Do wyjasnienia=orange, Przeterminowane=red
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-21: Date formats consistent (DD.MM.YYYY for display)
  - Prereqs: Any screen with dates
  - Steps: 1) Check date display format
  - Expected: DD.MM.YYYY format consistently (matching Vue's TO_CHAR format)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-22: Polish language labels match Vue
  - Prereqs: Any screen
  - Steps: 1) Compare labels with Vue frontend
  - Expected: All labels in Polish, matching Vue text
  - Priority: P2 | Status: NOT TESTED

- [ ] TC-XC-23: Pagination text format matches ("Strona X z Y" or "Pokazuje X-Y z Z")
  - Prereqs: Paginated list
  - Steps: 1) Check pagination text
  - Expected: Consistent format across all lists
  - Priority: P2 | Status: NOT TESTED

**Error Handling:**
- [ ] TC-XC-30: Network offline -> Toast error on all screens
  - Prereqs: Airplane mode enabled on any screen
  - Steps: 1) Trigger API call
  - Expected: Toast with network error, no crash
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-31: Server error (500) -> Toast error on all screens
  - Prereqs: Backend error
  - Steps: 1) Trigger 500
  - Expected: Toast with error message
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-32: Empty states shown correctly on all list screens
  - Prereqs: Empty data for any list
  - Steps: 1) Check each list screen with no data
  - Expected: "Brak danych" or similar empty state message on: Tasks, Wnioski, Approvals, Transport Ceny, Limity, Handlowcy
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-33: Network error on new screens (Transport Ceny, Limity, Tasks, Calendar) — no crash
  - Prereqs: Network unavailable
  - Steps: 1) Open each new screen without network
  - Expected: Graceful error handling, no crash
  - Priority: P1 | Status: NOT TESTED

**Screen Rotation:**
- [ ] TC-XC-40: Screen rotation preserves ViewModel state on all screens
  - Prereqs: Data loaded on any screen
  - Steps: 1) Rotate device
  - Expected: List data, filters, pagination state preserved (requires ViewModel per Activity)
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-41: Form data preserved on rotation for ALL form screens
  - Prereqs: Form partially filled
  - Steps: 1) Fill form on: NewRequest, TransportCenyNew, LimitKredytowyNew, TaskCreate, CalendarModal 2) Rotate
  - Expected: All form field values preserved via ViewModel
  - Priority: P1 | Status: NOT TESTED

**Performance:**
- [ ] TC-XC-50: Large list (100+ items) loads without ANR
  - Prereqs: Many items in any list
  - Steps: 1) Open list with 100+ items
  - Expected: No "Application Not Responding" dialog, smooth scrolling
  - Priority: P1 | Status: NOT TESTED

- [ ] TC-XC-51: Debounced search does not fire excessive API calls
  - Prereqs: Search field on any list
  - Steps: 1) Type rapidly
  - Expected: Max 1 API call per 300ms of idle
  - Priority: P2 | Status: NOT TESTED

---

## Test Case Summary

| Section | Prefix | Range | Count |
|---------|--------|-------|-------|
| Authentication | TC-AUTH | 01-51 | 20 |
| Dashboard | TC-DASH | 01-44 | 18 |
| New Request | TC-REQ | 01-37 | 22 |
| Approval | TC-APR | 01-22 | 13 |
| Edit Request | TC-EDIT | 01-16 | 16 |
| Tasks List | TC-TASK | 01-17 | 17 |
| Task Detail | TC-TDET | 01-30 | 20 |
| Calendar | TC-CAL | 01-18 | 18 |
| Handlowcy | TC-HAND | 01-08 | 8 |
| Transport | TC-TRANS | 01-09 | 9 |
| Transport Ceny List | TC-TCENY | 01-16 | 16 |
| Transport Ceny Detail | TC-TCED | 01-21 | 21 |
| Transport Ceny New | TC-TCEN | 01-16 | 16 |
| Limity Kredytowe List | TC-LIMIT | 01-12 | 12 |
| Limit Kredytowy Detail | TC-LDET | 01-09 | 9 |
| Limit Kredytowy New | TC-LNEW | 01-09 | 9 |
| Navigation | TC-NAV | 01-05 | 5 |
| Cross-Cutting | TC-XC | 01-51 | 22 |
| **TOTAL** | | | **271** |

---

## Acceptance Criteria

For each screen to pass:
1. All data fields present in the Vue equivalent must be displayed
2. All user interactions must work (tap, scroll, form input)
3. Correct API endpoints called with correct HTTP methods and payloads
4. Loading, error, and empty states handled
5. Screen rotation does not lose data
6. Role-based visibility rules respected
7. Polish language labels match Vue frontend
8. **Bearer token authentication used (not API Key)** ✅ Implemented
9. Token refresh handles 401 transparently
10. Status badges use correct colors for ALL statuses (including PENDING, APPROVED, REJECTED, COMPLETED)

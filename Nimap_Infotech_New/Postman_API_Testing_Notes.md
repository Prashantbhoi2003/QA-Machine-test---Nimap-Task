# Postman API Testing - Setup & Explanation

## 1. Environment setup
Environment **"Nimap FieldForceConnect - Test Env"** with variables:

| Variable | Purpose |
|---|---|
| `base_url` | `https://test.fieldforceconnect.com` |
| `login_endpoint` | `/api/account/authenticate` - **confirmed** via DevTools Network tab |
| `get_customers_endpoint` | `/api/CRM/Leads` - **confirmed** (note: it's a POST, not a GET, despite being the "list" call) |
| `add_customer_endpoint` | `/api/customer` - **still a placeholder**, not yet confirmed from DevTools |
| `valid_username` / `valid_password` | Credentials from a real signed-up account |
| `currentUser_cookie` | Left blank initially - populated automatically by the Login request's Tests script (see below) |
| `customer_id` | Captured after Add Customer succeeds |

## 2. Auth mechanism (confirmed - not what we originally assumed)
This API is **not** Bearer-token based. What's actually happening:

- Login (`POST /api/account/authenticate`) returns a JSON body: `{ success, userId, CompanyDetail: {...}, ... }` on success, or `{ success: false, errormessage: "..." }` on failure.
- **The API returns HTTP 200 in both cases** - a wrong password does NOT get a 401/403. The only way to detect failure is the `success` field. This is worth flagging as an API design observation in the write-up.
- The server sends only one cookie via `Set-Cookie`: `DeviceId`. Postman's cookie jar handles this automatically.
- The `currentUser` cookie (which carries the session) is **not** sent by the server - the frontend JavaScript builds it itself from the login response JSON and stores it via `document.cookie`. Since Postman doesn't execute that JS, we replicate it:
  - The **Tests script** on `Login - Valid Credentials` takes the full login response, stringifies it, and sets it both as the `currentUser_cookie` environment variable and (where supported) directly in Postman's cookie jar.
  - Every authenticated request afterward (`Get Leads`, `Add Customer`) has a **Pre-request Script** that reads `currentUser_cookie` and attaches it as a `Cookie: currentUser=...` header.

## 3. Requests in the collection

**Auth → Login - Valid Credentials** (`POST {{base_url}}{{login_endpoint}}`)
- Tests: asserts `200`, asserts `success: true`, builds and stores the `currentUser` cookie, checks response time.
- Request body field names (currently `username`/`password`) are **not yet confirmed** - check the Payload tab of a real login request in DevTools and correct the keys if the app actually uses something like `EmailId`/`Password`.

**Auth → Login - Invalid Credentials** (same endpoint, wrong password)
- Tests: asserts `200` (not 401/403 - confirmed the API always returns 200), asserts `success: false`, asserts `errormessage` is present.

**Customer → Get Leads (Customer List)** (`POST {{base_url}}{{get_customers_endpoint}}`)
- Pre-request Script attaches the `currentUser` cookie.
- Body: **confirmed real payload** - a filter/pagination object (`From`/`To` as a page range, `DateType`, and a set of optional filter IDs for city/state/lead source/stage/type/status/tags/territory/user, all left blank for an unfiltered full list).
- Tests: asserts `200`, asserts a list/array shape, checks response time.

**Customer → Add Customer (Add Lead)** (`POST {{base_url}}{{add_customer_endpoint}}`)
- Pre-request Script attaches the `currentUser` cookie.
- Body: **confirmed real payload** - `LeadId: 0` for a new record, `LeadName`/`PersonName`/`MobileNo`/`Email`/`PersonLocation` are the fields actually filled from the form, everything else defaults to 0/empty/[].
- Tests: **confirmed real response** - `{ Status: 200, Message: 'Success', data: { Id, LeadName, ..., StatusId: 3 ('Cold'), LeadTypeId: 1 ('Retailer / Sub Dealer'), LeadStageId: 1 ('Enquiry') } }`, saves `data.Id` to `{{customer_id}}`.
- Note for the write-up: "customers" are actually called **Leads** on the backend - Add Customer, Get Leads, and the Leads list all operate on the same `Lead` entity, just presented as "Customers" in the UI.

## 4. How to run
1. Import both the collection and environment JSON files into Postman.
2. Select the environment (top-right dropdown).
3. Fill in `valid_username` / `valid_password` with a real signed-up account.
4. Run **Login - Valid Credentials** first (populates `currentUser_cookie`), then **Get Leads (Customer List)** or **Add Customer (Add Lead)** in any order.
5. Run **Login - Invalid Credentials** independently to demonstrate negative-case coverage.
6. Use Collection Runner to execute everything in sequence for a full pass/fail summary.

## 5. Why this structure
- Variables instead of hard-coded values → same collection works across environments.
- Cookie captured and replayed via scripts, mirroring exactly what the frontend does → demonstrates understanding of the app's actual (non-standard) auth flow, not just a textbook Bearer-token assumption.
- A negative test (invalid login) that specifically checks for the app's "200 OK but success: false" behavior → shows real API-level investigation, not boilerplate.

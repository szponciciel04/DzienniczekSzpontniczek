# PrometheusLoginHelper

`PrometheusLoginHelper` realizuje pełny flow logowania do eduvulcan.pl za pomocą loginu i hasła,
zwracając tokeny JWT gotowe do rejestracji urządzenia w Hebe CE.

## Użycie

`PrometheusLoginHelper` nie wymaga zewnętrznego `HttpClient` — tworzy go samodzielnie.

```kotlin
val helper = PrometheusLoginHelper()

val result: PrometheusLoginResult = helper.login(
    login = "jan.kowalski",
    password = "hasło",
    deviceModel = "Pixel 9"
)

// result.tenantTokens    — Map<String, String>: symbol tenanta → JWT
// result.mainAccessToken — główny token sesji z /api/ap
```

## Wynik — `PrometheusLoginResult`

| Pole | Typ | Opis |
|---|---|---|
| `tenantTokens` | `Map<String, String>` | Mapa `tenant → JWT`. Każdy wpis odpowiada jednej szkole/uczniowi powiązanemu z kontem |
| `mainAccessToken` | `String` | `AccessToken` z odpowiedzi `/api/ap` — pozwala autoryzować żądania do eduvulcan.pl bez kolejnego logowania |

---

## Kroki flow

### 1. Sprawdzenie captchy — `POST /Account/QueryUserInfo`

Przed pobraniem strony logowania helper odpytuje endpoint o to, czy dla danego loginu
wymagana jest captcha Proof-of-Work.

```
POST https://eduvulcan.pl/Account/QueryUserInfo
Content-Type: application/x-www-form-urlencoded

alias=<login>
```

Odpowiedź:

```json
{ "success": true, "data": { "ShowCaptcha": true } }
```

Jeśli zapytanie się nie powiedzie (błąd sieciowy itp.), helper zakłada `showCaptcha = false`
i kontynuuje.

---

### 2. Pobranie strony logowania — `GET /logowanie`

```
GET https://eduvulcan.pl/logowanie
```

Ze zwróconego HTML parsowane są (za pomocą Ksoup):

- **CSRF token** — z `input[name=__RequestVerificationToken][value]`
- **Parametry captchy** (tylko gdy `showCaptcha == true`) — z atrybutów `div.captcha-wrapper`:
  - `data-challenge`
  - `data-difficulty`
  - `data-rounds`

---

### 3. Obliczenie odpowiedzi captchy (jeśli wymagana)

Gdy `showCaptcha == true`, wywoływany jest `POWCaptchaResolver.computeCaptchaResponse(challenge, difficulty, rounds)`.

Algorytm Proof-of-Work:
1. Buduje bufor bajtów: `challenge` + nonce w ASCII
2. Dla każdej rundy szuka nonce (od 1 do 1 000 000 000) takiego, że pierwsze 4 bajty SHA-256 bufora
   interpretowane jako `uint32` big-endian są mniejsze niż `difficulty`
3. Znalezione nonce z poprzednich rund pozostają w buforze (wchodzą jako prefiks do kolejnych rund)
4. Zwraca nonce wszystkich rund połączone średnikiem: `nonce1;nonce2;...`

Gdy captcha nie jest wymagana, do formularza trafia pusty string.

---

### 4. Właściwe logowanie — `POST /logowanie`

```
POST https://eduvulcan.pl/logowanie
Content-Type: application/x-www-form-urlencoded
(followRedirects = false)

Alias=<login>&Password=<password>&captcha-response=<wynik PoW>&__RequestVerificationToken=<csrf>
```

Walidacja odpowiedzi:

| Warunek | Wyjątek |
|---|---|
| Body zawiera słowo „robot" lub „robak" | `IllegalStateException` — captcha odrzucona |
| Brak nagłówka `Location` w odpowiedzi | `IllegalStateException` — błędne dane logowania |

Żądanie jest wykonywane **bez automatycznych redirectów** — po poprawnym logowaniu serwer zwraca
`302` z nagłówkiem `Location`. Ciasteczka sesyjne są zarządzane automatycznie przez plugin
`HttpCookies` z `AcceptAllCookiesStorage` i nie wymagają ręcznego przekazywania.

---

### 5. Pobranie danych z `/api/ap` — `GET /api/ap`

```
GET https://eduvulcan.pl/api/ap
```

Ciasteczka z kroku 4 są dodawane automatycznie przez `AcceptAllCookiesStorage`.

Strona zawiera ukryty input `<input id="ap" type="hidden" value="...">`. Ksoup odczytuje
jego wartość (encje HTML są automatycznie odkodowywane), a następnie jest ona parsowana jako JSON:

```json
{
  "Success": true,
  "Tokens": ["<jwt-tenant-A>", "<jwt-tenant-B>"],
  "AccessToken": "<główny-token>",
  "IsConsentAccepted": true,
  "ErrorMessage": null
}
```

Walidacja:

| Warunek | Wyjątek |
|---|---|
| `Success == false` | `IllegalStateException` z treścią `ErrorMessage` |
| `Tokens` jest puste | `IllegalStateException` — brak uczniów na koncie |

---

### 6. Budowanie mapy tenantów

Każdy JWT z `Tokens[]` jest dekodowany funkcją `decodeJWT(jwt)`:

```kotlin
val payload: JwtPayload = decodeJWT(jwt)
// payload.tenant — np. "krakow"
```

Dekodowanie używa `Base64.UrlSafe` z opcjonalnym paddingiem i `kotlinx.serialization`.
Wynikiem jest mapa `tenant → jwt`, np.:

```
{
  "krakow"   → "eyJhbGci...",
  "warszawa" → "eyJhbGci..."
}
```

---

## Obsługa błędów

| Sytuacja | Zachowanie |
|---|---|
| Login lub hasło puste | `IllegalArgumentException` (przed jakimkolwiek żądaniem) |
| Błąd sieci przy `/Account/QueryUserInfo` | Ignorowany — captcha zakładana jako wyłączona |
| Captcha odrzucona przez serwer | `IllegalStateException` |
| Błędne dane logowania | `IllegalStateException` |
| `Success == false` w `/api/ap` | `IllegalStateException` |
| Pusta lista `Tokens` | `IllegalStateException` |

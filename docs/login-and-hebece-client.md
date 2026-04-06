# Logowanie i tworzenie klienta HebeCE

## Wymagania wstępne

Do logowania i rejestracji urządzenia potrzebujesz skonfigurowanego klienta Ktor (`HttpClient`).

```kotlin
val httpClient = HttpClient(/* engine – OkHttp lub Darwin */)
```

---

## 1. Logowanie loginem i hasłem

```kotlin
val helper = PrometheusLoginHelper(httpClient)

val result: PrometheusLoginResult = helper.login(
    login = "jan.kowalski",
    password = "hasło",
    deviceModel = "Pixel 9"
)

// result.tenantTokens  — Map<String, String>: symbol tenanta → JWT token dla tenanta
// result.mainAccessToken — główny accessToken z /api/ap
```

`PrometheusLoginResult` zawiera:

| Pole | Typ | Opis |
|---|---|---|
| `tenantTokens` | `Map<String, String>` | Symbol tenanta (np. `"krakow"`) → JWT token z `Tokens[]` |
| `mainAccessToken` | `String` | `AccessToken` z odpowiedzi `/api/ap` |

---

## 2. Tworzenie credential dla urządzenia

Przed rejestracją w Hebe musisz wygenerować nowe dane urządzenia (`RsaCredential`):

```kotlin
val credential = RsaCredential.createNew(
    deviceOs = "Android",
    deviceModel = "Pixel 9"
)
```

Wygenerowane `credential` zawiera parę kluczy RSA, certyfikat X.509 i losowy `deviceId`. Zapisz je — będą potrzebne do późniejszych żądań API.

---

## 3. Rejestracja w Hebe CE i tworzenie klienta

```kotlin
val api = SzpontHebeCeApi(credential, httpClient)

// Wybierz jednego tenanta z wyników logowania
val tenant = result.tenantTokens.keys.first()
val tokens = result.tenantTokens.values.toList()

// Zarejestruj urządzenie — zapisuje restUrl w credential
val restUrl: String = api.registerByJwt(tokens, tenant)
```

Po `registerByJwt` `credential.restUrl` jest automatycznie ustawione. Wszystkie kolejne wywołania `SzpontApi` będą używały tego adresu.

---

## 4. Pełny przykład (wielu tenantów)

Jeśli konto jest powiązane z wieloma szkołami (wieloma tenantami), zarejestruj osobny `credential` dla każdego tenanta:

```kotlin
val helper = PrometheusLoginHelper(httpClient)
val loginResult = helper.login("jan.kowalski", "hasło", "Pixel 9")

val clients = loginResult.tenantTokens.map { (tenant, token) ->
    val credential = RsaCredential.createNew(deviceOs = "Android", deviceModel = "Pixel 9")
    val api = SzpontHebeCeApi(credential, httpClient)
    api.registerByJwt(listOf(token), tenant)
    tenant to api
}.toMap()

// clients["krakow"]?.getAccounts()
```

---

## 5. Odczytanie danych tenanta z tokenu JWT

Jeśli potrzebujesz szczegółów (np. `uri`, `unitUId`) z tokenu JWT przed rejestracją:

```kotlin
val payload: JwtPayload = decodeJWT(token)
// payload.tenant   — symbol, np. "krakow"
// payload.uri      — bazowy URL, np. "https://uczen.eduvulcan.pl/krakow"
// payload.name     — imię i nazwisko ucznia
// payload.uid      — identyfikator użytkownika
// payload.unitUId  — identyfikator jednostki (szkoły)
```

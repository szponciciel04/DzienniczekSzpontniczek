# Analiza flow logowania eduvulcan.pl (do api/ap)

Data analizy: 2026-04-05
Zakres: flow dla platformy EduVulcan (Prometheus), od startu logowania do momentu odczytu i zdekodowania danych z endpointu /api/ap.

## 1. Punkt startowy logowania

Glowny punkt wejscia to:
- Metoda: login(data: LoginData)

Wejscie zawiera:
- login (alias uzytkownika)
- password
- deviceModel

Kod rzutuje LoginData na VulcanPrometheusLoginData i przerywa, gdy login lub haslo sa puste (IllegalStateException).

## 2. Sprawdzenie, czy captcha jest wymagana

Pierwszy request do eduvulcan.pl:
- POST https://eduvulcan.pl/Account/QueryUserInfo
- Body form-url-encoded:
  - alias=<login>

Metoda:
- PrometheusWebApi.getUserInfo(username)

Odpowiedz jest mapowana na ApiResponse<QueryUserInfoResponse>, gdzie istotne pole to:
- data.ShowCaptcha (showCaptcha)

To pole decyduje, czy klient musi policzyc odpowiedz CAPTCHA (PoW).

### 2.1. Dokladny JSON z /Account/QueryUserInfo

Mapowanie modelu (z kodu):
- ApiResponse<T>:
  - success: Boolean
  - data: T?
- QueryUserInfoResponse:
  - ShowCaptcha: Boolean
  - ExtraMessage: String?

Przykladowa odpowiedz (captcha wymagana):

```json
{
  "success": true,
  "data": {
    "ShowCaptcha": true,
    "ExtraMessage": null
  }
}
```

Przykladowa odpowiedz (captcha niewymagana):

```json
{
  "success": true,
  "data": {
    "ShowCaptcha": false,
    "ExtraMessage": ""
  }
}
```

Wazne: wrapper ma pole `success` male litery, a obiekt `data` ma klucze `ShowCaptcha` i `ExtraMessage` z wielkiej litery.

## 3. Pobranie strony logowania i tokenu CSRF

Kolejny request:
- GET https://eduvulcan.pl/logowanie

Metoda:
- PrometheusWebApi.getLoginPage()

HTML jest parsowany przez Ksoup, a z formularza wyciagany jest:
- __RequestVerificationToken (CSRF)

Jesli showCaptcha == true, z elementu div.captcha-wrapper pobierane sa atrybuty:
- data-challenge
- data-difficulty
- data-rounds

## 4. CAPTCHA Proof-of-Work (POWCaptchaResolver)

Implementacja:
- Metoda: computeCaptchaResponse(challenge, difficulty, rounds)

### 4.1. Parametry i walidacja
- rounds musi byc >= 0
- difficulty musi byc w zakresie 0..0xFFFF_FFFF

### 4.2. Material do haszowania
Algorytm buduje bufor bajtow ASCII:
1) Najpierw wpisuje challenge
2) Dla kazdej rundy dopisuje nonce jako liczbe dziesietna ASCII

Uwaga: nonce z poprzednich rund pozostaje w buforze. To znaczy, ze kolejna runda haszuje challenge + poprzednie_nonce + nowe_nonce.

### 4.3. Szukanie nonce
Dla kazdej rundy:
1) Iteruje nonce od 1 do 1_000_000_000
2) Liczy SHA-256 z aktualnej zawartosci bufora
3) Bierze pierwsze 4 bajty digestu jako liczbe 32-bit unsigned (big-endian)
4) Warunek sukcesu: value < difficulty

Po znalezieniu nonce:
- zapisuje nonce do tablicy wynikowej
- aktualizuje dlugosc bufora (czyli utrwala nonce jako czesc wejscia dla kolejnej rundy)

Gdy nonce nie zostanie znalezione do 1e9 prob, rzuca IllegalStateException.

### 4.4. Format odpowiedzi captcha
Wynik to lista nonce z wszystkich rund polaczona separatorem srednikowym:
- nonce1;nonce2;nonce3;...

Ten string trafia do pola formularza:
- captcha-response

## 5. Wlasciwe logowanie do /logowanie

Request:
- POST https://eduvulcan.pl/logowanie
- Body form-url-encoded:
  - Alias=<login>
  - Password=<password>
  - captcha-response=<wynik PoW lub pusty string>
  - __RequestVerificationToken=<csrf>

Metoda:
- PrometheusWebApi.loginToPage(...)

Walidacja odpowiedzi:
- Jesli body zawiera tekst o mechanizmie przeciw robotom i robakom -> CaptchaException
- Jesli brak naglowka location -> InvalidCredentialsException
- W przeciwnym razie metoda zwraca ciasteczka sesyjne (set-cookie)

## 6. Pobranie danych z api/ap

Po udanym loginie klient wykonuje:
- GET https://eduvulcan.pl/api/ap

Metody:
- PrometheusWebApi.getApiAp()
- PrometheusWebApi.processApiApWithConsent()
- ApiApHelper.decodeApiAp(apiApPage)

### 6.1. Jak dekodowane jest /api/ap
Parser:
- Wczytuje HTML
- Szuka elementu o selektorze #ap
- Odczytuje atrybut value
- Traktuje go jako JSON i mapuje na ApiApResponse

Struktura ApiApResponse zawiera m.in.:
- Success
- Tokens (lista tokenow JWT per tenant)
- AccessToken
- IsConsentAccepted
- CanAcceptConsent
- ErrorMessage
- dane alias/email/imie/nazwisko

Dokladny model ApiApResponse (1:1 z kodem):
- Success: Boolean
- Tokens: List<String>
- Alias: String
- Email: String?
- GivenName: String?
- Surname: String?
- IsConsentAccepted: Boolean
- CanAcceptConsent: Boolean
- AccessToken: String
- ErrorMessage: String?

Przykladowy JSON osadzony w `#ap[value]`:

```json
{
  "Success": true,
  "Tokens": [
    "eyJhbGciOi...tenantA...",
    "eyJhbGciOi...tenantB..."
  ],
  "Alias": "jan.kowalski",
  "Email": "jan.kowalski@example.com",
  "GivenName": "Jan",
  "Surname": "Kowalski",
  "IsConsentAccepted": true,
  "CanAcceptConsent": true,
  "AccessToken": "eyJhbGciOi...access...",
  "ErrorMessage": null
}
```

Przyklad przypadku bez uczniow:

```json
{
  "Success": true,
  "Tokens": [],
  "Alias": "jan.kowalski",
  "Email": "jan.kowalski@example.com",
  "GivenName": "Jan",
  "Surname": "Kowalski",
  "IsConsentAccepted": true,
  "CanAcceptConsent": true,
  "AccessToken": "eyJhbGciOi...access...",
  "ErrorMessage": null
}
```

Taki przypadek (`Tokens` puste + `IsConsentAccepted == true`) konczy sie `NoStudentsException`.

Przyklad przypadku bez zgody (consent):

```json
{
  "Success": true,
  "Tokens": ["eyJhbGciOi...tenantA..."],
  "Alias": "jan.kowalski",
  "Email": "jan.kowalski@example.com",
  "GivenName": "Jan",
  "Surname": "Kowalski",
  "IsConsentAccepted": false,
  "CanAcceptConsent": true,
  "AccessToken": "eyJhbGciOi...access...",
  "ErrorMessage": "Wymagane potwierdzenie zgody"
}
```

Dodatkowa walidacja:
- Jesli Tokens sa puste i IsConsentAccepted == true -> NoStudentsException

### 6.2. Obsluga zgody (consent)
Jesli IsConsentAccepted == false:
1) Przy autoAcceptConsent == false -> PrometheusConsentException
2) Przy autoAcceptConsent == true:
   - POST https://eduvulcan.pl/konto/zgody (Consent[0].Key=4, Consent[0].Value=true)
   - GET https://eduvulcan.pl/konto/zgody
   - ponowne GET /api/ap i ponowne dekodowanie
   - jesli nadal brak zgody -> PrometheusConsentException

## 7. Co trafia do credentials po /api/ap

W VulcanPrometheusLoginClient.login(...) po processApiApWithConsent() tworzony jest obiekt VulcanPrometheusLoginCredentials z polami:
- login
- password
- accessToken (z ApiApResponse.AccessToken)
- deviceModel

To konczy analizowany odcinek flow (do momentu wyciagniecia danych z /api/ap).

## 8. Dalszy krok (poza zakresem, ale wazny kontekst)

W odswiezaniu sesji (renewCredentials) dane z /api/ap sa dalej wykorzystywane:
- kazdy token z Tokens jest dekodowany (decodeJWT)
- z payloadu pobierany jest tenant
- dla tenantow wykonywane sa przejscia SSO i pobierane tokeny anti-forgery/appGuid dla web API ucznia i wiadomosci

To juz jest etap po samym wyciagnieciu danych z /api/ap.

### 8.1. Dokladny JSON payload tokenu JWT (Tokens z /api/ap)

Kazdy element `Tokens[]` to JWT. Kod bierze srodkowa czesc (payload), dekoduje Base64URL i mapuje na:

- name: String
- uid: String
- tenant: String
- unituid: String
- uri: String
- service: JsonElement
- caps: JsonElement
- nbf: Long
- exp: Long
- iat: Long

Przykladowy payload po dekodowaniu:

```json
{
  "name": "Jan Kowalski",
  "uid": "123456789",
  "tenant": "krakow",
  "unituid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "uri": "https://uczen.eduvulcan.pl/krakow",
  "service": {
    "name": "student"
  },
  "caps": {
    "messages": true,
    "grades": true
  },
  "nbf": 1710000000,
  "exp": 1710003600,
  "iat": 1710000000
}
```

Wazne: `service` i `caps` sa mapowane jako `JsonElement`, czyli kod nie wymusza ich stalego formatu w czasie deserializacji.

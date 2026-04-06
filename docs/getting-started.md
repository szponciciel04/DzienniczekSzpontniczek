# Getting started

## Generowanie credential

```kotlin
val credential = RsaCredential.createNew(
    deviceOs = "Android",
    deviceModel = "SM-A525F"
)
```

## Rejestracja credential

Po wygenerowaniu credential musisz je zarejestrować. Metoda rejestracji zależy od aplikacji.
**Po** udanej rejestracji zapisz credential — zawiera `restUrl` potrzebny do dalszych żądań.

### Dzienniczek VULCAN

Ta aplikacja używa rejestracji tokenem i PINem. Dane znajdziesz w zakładce *Dostęp mobilny* w module *Uczeń*
w webowej wersji e-dziennika.

> Użyj `SzpontHebeApi`, a nie `SzpontHebeCeApi`. To drugie jest przeznaczone dla aplikacji eduVULCAN.

```kotlin
val api = SzpontHebeApi(credential, httpClient)

api.registerByToken(
    securityToken = "<token>",
    pin = "<pin>",
    tenant = "<symbol>"
)
```

### eduVULCAN

Rejestracja w eduVULCAN wymaga JWT tokenów (osobny dla każdego ucznia).

Każdy token po zdekodowaniu wygląda następująco:

```json
{
  "name": "Jan Marek Kowalski (Fake123456)",
  "uid": "bdacb05d-f964-496d-9775-6f4cc26bf8e9",
  "tenant": "warszawa",
  "unituid": "3c000f77-bb3c-4514-8537-b9949b55c161",
  "uri": "http://uczen.eduvulcan.pl/warszawa/start?profil=bdacb05d-...",
  "service": "False",
  "caps": [],
  "nbf": 0,
  "exp": 0,
  "iat": 0
}
```

Dekodowanie tokenu:

```kotlin
val payload: JwtPayload = decodeJWT(token)
// payload.tenant  — symbol szkoły, np. "warszawa"
```

Rejestracja:

> Użyj `SzpontHebeCeApi`, a nie `SzpontHebeApi`. To drugie jest przeznaczone dla aplikacji Dzienniczek VULCAN.

```kotlin
val api = SzpontHebeCeApi(credential, httpClient)

api.registerByJwt(
    tokens = listOf("<jwt>"),
    tenant = "<tenant z jwt>"
)
```

### Jak aplikacja eduVULCAN uzyskuje tokeny?

Tokeny JWT są dostępne po zalogowaniu przez eduvulcan.pl. Zalogowany użytkownik może pobrać je
z endpointu `https://eduvulcan.pl/api/ap`. Strona zwraca HTML z ukrytym inputem `<input id="ap" type="hidden" value="...">`,
którego wartość to JSON:

```json
{
  "Tokens": ["<jwt>"],
  "Alias": "<username>",
  "Email": "<email>",
  "GivenName": "<imię>",
  "Surname": "<nazwisko>",
  "IsConsentAccepted": true,
  "CanAcceptConsent": true,
  "AccessToken": "<jwt>",
  "Success": true,
  "ErrorMessage": null
}
```

> Rejestracja nie powinna odbywać się gdy:
> 1. `Success` jest `false`
> 2. `IsConsentAccepted` jest `false`
> 3. `Tokens` jest pustą listą

Zamiast otwierać przeglądarkę, możesz zalogować się programowo używając `PrometheusLoginHelper`
— patrz [login-and-hebece-client.md](login-and-hebece-client.md).

## Pobranie listy kont

Po rejestracji możesz pobrać listę kont. Zawiera dane o uczniu, szkole, dzienniku i okresach.

```kotlin
val accounts: List<Account> = api.getAccounts()
```

## Serializacja i deserializacja credential

### Serializacja

```kotlin
// RsaCredential to data class — zserializuj go samodzielnie (np. przez kotlinx.serialization lub Room)
val type = credential.type
val restUrl = credential.restUrl
val certificate = credential.certificate
val privateKey = credential.privateKey
val fingerprint = credential.fingerprint
val deviceId = credential.deviceId
val deviceOs = credential.deviceOs
val deviceModel = credential.deviceModel
val notificationToken = credential.notificationToken
```

### Deserializacja

```kotlin
val credential = RsaCredential(
    type = type,
    restUrl = restUrl,
    certificate = certificate,
    privateKey = privateKey,
    fingerprint = fingerprint,
    deviceId = deviceId,
    deviceOs = deviceOs,
    deviceModel = deviceModel,
    notificationToken = notificationToken
)
```

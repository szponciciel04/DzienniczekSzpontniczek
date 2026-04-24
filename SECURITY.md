# Security Policy

## Wspierane wersje

Aktywne wsparcie bezpieczeństwa otrzymuje tylko najnowsza wersja projektu.

| Wersja | Wsparcie            |
| ------ | ------------------- |
| 0.1.x  | ✅ aktywne          |
| < 0.1  | ❌ brak wsparcia    |

## Zgłaszanie podatności

Jeśli odkryłeś/aś lukę bezpieczeństwa w Dzienniczku Szpontniczku, prosimy o **nieujawnianie jej publicznie** (np. przez Issues na GitHubie) do czasu jej naprawienia.

### Jak zgłosić?

Skorzystaj z jednej z poniższych metod:

- **GitHub Private Security Advisory** – otwórz zgłoszenie przez zakładkę [Security → Report a vulnerability](https://github.com/szponciciel04/DzienniczekSzpontniczek/security/advisories/new) w tym repozytorium *(zalecane)*
- **GitHub Issues** – tylko jeśli luka jest już publicznie znana i nie stanowi bezpośredniego zagrożenia

### Co podać w zgłoszeniu?

Aby jak najszybciej zrozumieć i odtworzyć problem, opisz:

1. Wersję aplikacji, której dotyczy luka
2. Platformę (Android / iOS)
3. Opis podatności i potencjalny wpływ na użytkownika
4. Kroki do odtworzenia (proof of concept)
5. Jeśli to możliwe – sugestię poprawki

### Czas odpowiedzi

Postaramy się odpowiedzieć na zgłoszenie w ciągu **7 dni roboczych** i na bieżąco informować o postępach.

## Zakres bezpieczeństwa

Projekt łączy się z oficjalnym API eduVULCAN oraz Dzienniczka VULCAN (protokół HebeCE). Szczególnie istotne obszary z punktu widzenia bezpieczeństwa to:

- **Uwierzytelnianie i rejestracja urządzenia** – tokeny dostępowe, certyfikaty klienta
- **Przechowywanie danych logowania** – dane wrażliwe przechowywane lokalnie na urządzeniu
- **Komunikacja sieciowa** – weryfikacja certyfikatów TLS, ochrona przed atakami MITM
- **Dane osobowe ucznia** – oceny, frekwencja, wiadomości, dane o nauczycielach

## Poza zakresem

Następujące kwestie **nie są** traktowane jako podatności w tym projekcie:

- Luki w samym API eduVULCAN / VULCAN (zgłoś je bezpośrednio do VULCAN'a)
- Problemy wynikające z używania nieoficjalnych modyfikacji aplikacji
- Ataki wymagające fizycznego dostępu do odblokowanego urządzenia

## Odpowiedzialne ujawnianie

Po potwierdzeniu i naprawieniu luki, autor zgłoszenia zostanie wymieniony w opisie wydania (chyba że woli pozostać anonimowy). Prosimy o odpowiedzialne ujawnianie – daj nam czas na wydanie poprawki przed publicznym opisem podatności.

---

*Polityka bezpieczeństwa projektu Dzienniczek Szpontniczek (Szpontium) – naszponcona z troską o Twoje dane 🔒*

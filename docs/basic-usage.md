# Basic usage

## Inicjalizacja API

Dla eduVULCAN:

```kotlin
val api = SzpontHebeCeApi(credential, httpClient)
```

Dla Dzienniczek VULCAN:

```kotlin
val api = SzpontHebeApi(credential, httpClient)
```

---

## Podstawowe funkcje

### Lista kont

```kotlin
val accounts: List<Account> = api.getAccounts()
```

### Szczęśliwy numerek

```kotlin
val lucky: LuckyNumber = api.getLuckyNumber(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    constituentUnitId = account.constituentUnit.id
)
```

### Oceny

```kotlin
val grades: List<Grade> = api.getGrades(
    restUrl = account.unit.restUrl,
    unitId = account.unit.id,
    pupilId = account.pupil.id,
    periodId = account.periods.last().id
)
```

### Średnie ocen

```kotlin
val averages: List<GradeAverage> = api.getGradesAverages(
    restUrl = account.unit.restUrl,
    unitId = account.unit.id,
    pupilId = account.pupil.id,
    periodId = account.periods.last().id
)
```

### Oceny końcowe / podsumowanie

```kotlin
val summary: List<GradeSummary> = api.getGradesSummary(
    restUrl = account.unit.restUrl,
    unitId = account.unit.id,
    pupilId = account.pupil.id,
    periodId = account.periods.last().id
)
```

### Sprawdziany i kartkówki

```kotlin
val exams: List<Exam> = api.getExams(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Zadania domowe

```kotlin
val homework: List<Homework> = api.getHomework(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Plan lekcji (z zastępstwami)

```kotlin
val schedule: List<Schedule> = api.getSchedule(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Lekcje zrealizowane

```kotlin
val completed: List<Lesson> = api.getCompletedLessons(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Lekcje zaplanowane

```kotlin
val planned: List<Lesson> = api.getPlannedLessons(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Frekwencja

```kotlin
val presence: List<PresenceExtra> = api.getPresenceExtra(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Statystyki frekwencji (miesięczne)

```kotlin
val monthStats: List<PresenceMonthStats> = api.getPresenceMonthStats(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    periodId = account.periods.last().id
)
```

### Statystyki frekwencji (per przedmiot)

```kotlin
val subjectStats: List<PresenceSubjectStats> = api.getPresenceSubjectStats(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    periodId = account.periods.last().id
)
```

### Uwagi

```kotlin
val notes: List<Note> = api.getNotes(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id
)
```

### Ogłoszenia

```kotlin
val announcements: List<Announcement> = api.getAnnouncements(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id
)
```

### Wiadomości odebrane

```kotlin
val messages: List<Message> = api.getReceivedMessages(
    restUrl = account.unit.restUrl,
    box = account.unit.restUrl, // klucz skrzynki
    pupilId = account.pupil.id
)
```

### Spotkania z rodzicami

```kotlin
val meetings: List<Meeting> = api.getMeetings(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1)
)
```

### Nauczyciele

```kotlin
val teachers: List<Teacher> = api.getTeachers(
    restUrl = account.unit.restUrl,
    periodId = account.periods.last().id,
    pupilId = account.pupil.id
)
```

### Informacje o szkole

```kotlin
val schoolInfo: List<SchoolInfo> = api.getSchoolInfo(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id
)
```

### Wycieczki

```kotlin
val trips: List<Trip> = api.getTrips(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Wakacje

```kotlin
val vacations: List<Vacation> = api.getVacations(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 30)
)
```

### Zdarzenia użytkownika

```kotlin
val events: List<UserEvent> = api.getUserEvents(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id
)
```

### Dodatkowe zmiany w planie

```kotlin
val scheduleExtra: List<ScheduleExtra> = api.getScheduleExtra(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Sloty czasowe

```kotlin
val timeslots: List<Timeslot> = api.getTimeslots()
```

### Dyżury

```kotlin
val duties: List<Duty> = api.getDuty(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id
)
```

### Menu stołówki

```kotlin
val menu: List<MealMenu> = api.getMealMenu(
    restUrl = account.unit.restUrl,
    pupilId = account.pupil.id,
    full = true,
    dateFrom = LocalDate(2024, 9, 1),
    dateTo = LocalDate(2024, 9, 7)
)
```

### Książka adresowa (wiadomości)

```kotlin
val addressbook: List<Address> = api.getAddressbook(
    restUrl = account.unit.restUrl,
    box = "<klucz skrzynki>"
)
```

---

## Powiadomienia push

### Ustawienie locale

```kotlin
api.setPushLocale("pl")
```

### Włączenie / wyłączenie wszystkich powiadomień

```kotlin
api.setAllPushSetting(turnOn = true)
```

### Konfiguracja konkretnego ustawienia push

```kotlin
val setting: PushSetting = api.setPushSetting(
    option = "GRADES",
    active = true
)
```

### Konfiguracja wielu ustawień naraz

```kotlin
val settings: List<PushSetting> = api.configurePush(
    options = mapOf("GRADES" to true, "MESSAGES" to false),
    locale = "pl"
)
```

---

## Operacje na wiadomościach

### Zmiana statusu wiadomości

```kotlin
api.changeMessageStatus(
    restUrl = account.unit.restUrl,
    boxKey = "<klucz skrzynki>",
    messageKey = "<klucz wiadomości>",
    status = 1
)
```

### Zmiana ważności wiadomości

```kotlin
api.changeMessageImportance(
    restUrl = account.unit.restUrl,
    boxKey = "<klucz skrzynki>",
    messageKey = "<klucz wiadomości>",
    importance = true
)
```

---

## Usuwanie credential

```kotlin
api.deleteCredential()
```

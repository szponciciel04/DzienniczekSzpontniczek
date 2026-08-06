# Dzienniczek CLI

`dzienniczek` exposes the mobile app's VULCAN/eduVULCAN features and its Librus client from scripts, agent tools, and an interactive terminal.

## Install

Java 17 or newer is required.

### macOS with Homebrew

```sh
brew install openjdk@17
brew install --build-from-source ./Formula/dzienniczek.rb
```

For a local checkout without a tap:

```sh
./scripts/install.sh
```

Add `~/.local/bin` to `PATH` if it is not already present.
The local launcher automatically uses Homebrew's `openjdk@17`; override it with `DZIENNICZEK_JAVA_HOME` when needed.

### Linux and Windows through WSL

```sh
sudo apt-get update
sudo apt-get install -y openjdk-17-jre
./scripts/install.sh
```

The Gradle distribution is also available at `cli/build/distributions/dzienniczek-0.2.0.tar.gz` after running `./gradlew :cli:distTar`.

## Login

```sh
# Dzienniczek VULCAN mobile-access token
dzienniczek login vulcan --token TOKEN --pin PIN --symbol SCHOOL

# eduVULCAN username/password, including proof-of-work captcha handling
dzienniczek login eduvulcan --username USER --password PASS

# eduVULCAN registration with JWTs acquired separately
dzienniczek login jwt --tenant SCHOOL --token JWT

# Librus Portal and a linked Synergia account
dzienniczek login librus --username EMAIL --password PASS
```

Use `--profile NAME` during login to keep multiple logins. Manage them with `profile list`, `profile use NAME`, and `profile remove NAME --yes`. VULCAN and Librus profiles may contain multiple pupils; use `account list` and `account use INDEX`.

Credentials are written to `${XDG_CONFIG_HOME:-~/.config}/dzienniczek/config.json` with mode `0600`. Override the location with `DZIENNICZEK_CONFIG` or `--config`. Use `--no-store-password` with eduVULCAN to omit the messaging password and supply `DZIENNICZEK_PASSWORD` when reading messages.

## Commands

Common commands:

```sh
dzienniczek dashboard
dzienniczek grades
dzienniczek grades averages
dzienniczek grades summary
dzienniczek schedule --from 2026-09-01 --to 2026-09-07
dzienniczek exams
dzienniczek homework
dzienniczek presence
dzienniczek presence months
dzienniczek presence subjects
dzienniczek notes
dzienniczek announcements
dzienniczek messages received
dzienniczek message --id MESSAGE_KEY
```

The CLI also exposes address books, completed/planned lessons, duties, kindergarten hours and teachers, lucky numbers, meal menus, meetings, detailed attendance, message folders and state changes, schedule changes, school information, teachers, timeslots, trips, user events, vacations, push settings, and remote credential deletion. Librus profiles additionally expose grade/event/note categories, subjects, users, classrooms, and auto-login tokens.

Every date range is inclusive and uses `--from YYYY-MM-DD --to YYYY-MM-DD`. The current Monday through Sunday is the default. Select data with `--account` and `--period`.

## Agent use

Non-interactive output defaults to JSON. For a stable explicit contract:

```sh
dzienniczek capabilities --json --compact
dzienniczek grades --json --compact
```

Secrets can be supplied without command-line exposure:

```sh
export DZIENNICZEK_USERNAME='...'
export DZIENNICZEK_PASSWORD='...'
export DZIENNICZEK_PROFILE='student'
dzienniczek dashboard --json --compact
```

Exit codes are stable: `0` success, `2` usage, `3` authentication, `4` network, `5` remote API, `6` local configuration, and `10` internal error. Errors are JSON on stderr in non-interactive mode. No prompt is attempted when no console is attached.

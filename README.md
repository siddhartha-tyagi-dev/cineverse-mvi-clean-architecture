# CineVerse

Android movie discovery application demonstrating:

- Kotlin
- Jetpack Compose
- MVI
- Clean Architecture
- Hilt
- Retrofit
- Coroutines
- Flow
- TMDB API

## Current Implementation

- Home screen
- Trending movies
- Popular movies
- Now Playing
- Top Rated

## Day 1 Architecture

The app is organized around Clean Architecture boundaries:

- `domain`: movie models, repository contract, and use cases
- `data`: TMDB Retrofit API, DTOs, mapper, and repository implementation
- `presentation`: Home MVI contract, ViewModel, and Compose UI
- `core`: shared TMDB image URL strategy
- `di`: Hilt modules for networking and repository binding

The Home flow is:

App launch -> Home screen -> TMDB API -> Repository -> UseCase -> MVI ViewModel -> StateFlow -> Compose UI

## TMDB Token Setup

Never commit a real TMDB token.

1. Create a TMDB API Read Access Token from your TMDB account settings.
2. Copy `local.properties.example` to `local.properties`, or edit your existing `local.properties`.
3. Add this line with your own token:

```properties
TMDB_ACCESS_TOKEN=your_tmdb_read_access_token_here
```

`local.properties` is already ignored by Git. The app reads this value into `BuildConfig.TMDB_ACCESS_TOKEN` at build time and sends it as a Bearer token through OkHttp.

If no token is provided, the project still compiles, but TMDB requests will return an authentication error at runtime.

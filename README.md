# Sample Android Automotive Wearable Monitoring

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/volvo-cars/automotive-media-sample/blob/main/LICENSE.md)

This is an example of how you can connect devices to the Volvo Cars Infotainment system. However, we at Volvo Cars are not medical device experts and do not intend to use this application in production. It's just happening to be a Glucose Monitor Application that we use as an example.

This project is following the clean architecture approach, by having an domain, data and
presentation layer.

Project structure

### Core Layer

This layer handles all common used utilities within the app

### DI Layer

This layer handles the creation of dependencies within the app, such as databases, apis, use-cases
etc...

### Domain Layer

This layer handles the business logic of the app. It also connects the data layer with the
presentation.

### Data Layer

This layer is responsible for providing the data required by the application.

### Presentation Layer

This layer executes the UI logic for the application.

### Service Layer

This layer handles three things for now;

1. A ServiceStarter that is a BroadcastReceiver that listens on intent BOOT_COMPLETE, and when that
   is received it starts the WorkManager and also the NotificationService.
2. A GlucoseFetchWorker that is a Periodical Worker and will fetch new glucose values every 15
   minutes (minimum time for periodic workers).
3. A NotificationService that is responsible to show a notification with the latest fetched glucose
   values. And will also show a notification if the glucose values are going outside the threshold
   values. Both of those things require that the user have allowed notification in the
   PreferenceScreen. If no user is signed in, then a Notification will show that the user needs to
   configure the app (sign in).

## MAD Scorecard

![MAD Scores summary](assets/mad_scorecard/summary.png "MAD scoreboard summary")

## Components Used

- [Work Manager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Hilt DI](https://developer.android.com/training/dependency-injection/hilt-android)
- [View Model](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Room DB](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Kotlin Flow](https://developer.android.com/kotlin/flow)
- [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines)

[How to integrate hilt with jetpack components](https://developer.android.com/training/dependency-injection/hilt-jetpack)

## License

Apache License 2.0

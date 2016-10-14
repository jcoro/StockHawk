# StockHawk
This App is Project 3 for the Udacity Android Developer Nanodegree. Stock Hawk tracks and graphs historical pricing for a stock portfolio.

![Movies App](http://www.coronite.net/assets/img/github/project3.jpg)

## Android Features / Libraries Implemented:

- [Google Cloud Messaging](https://developers.google.com/cloud-messaging/android/client)
- [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html)
- [Widgets](https://developer.android.com/design/patterns/widgets.html)
- [Intent Services](https://developer.android.com/reference/android/app/IntentService.html)
- [ResultReceiver](https://developer.android.com/reference/android/os/ResultReceiver.html)
- [William Chart](https://github.com/diogobernardino/WilliamChart) (for creating charts)
- [okhttp](http://square.github.io/okhttp/) (an HTTP client for downloading data)
- [schematic](https://github.com/SimonVT/schematic) (for generating content providers)

## Specifications
- `compileSdkVersion 23`
- `buildToolsVersion "23.0.2"`
- `minSdkVersion 15`
- `targetSdkVersion 23`

## Dependencies
```
dependencies {
    compile 'com.google.android.gms:play-services-gcm:8.4.0'
    compile 'com.squareup.okhttp:okhttp:2.5.0'
    apt 'net.simonvt.schematic:schematic-compiler:0.6.3'
    compile 'net.simonvt.schematic:schematic:0.6.3'
    compile 'com.melnykov:floatingactionbutton:1.2.0'
    compile 'com.diogobernardino:williamchart:2.2'
    compile 'com.android.support:design:23.2.1'
    compile 'com.android.support:appcompat-v7:23.2.1'
    compile('com.github.afollestad.material-dialogs:core:0.8.5.7@aar') {
        transitive = true
    }
    compile 'com.android.support:gridlayout-v7:23.2.1'
}
```


## Implementation
This App uses The [Yahoo! Finance API](https://developer.yahoo.com/yql/console) to obtain stock quotes for a portfolio. Note that historical stock data is fetched in `HistoricalDataIntentService.java` and current stock data is fetched periodically with the TaskService in `StockTaskService.java`.

This sample uses the Gradle build system. To build this project, use the "gradlew build" command or use "Import Project" in Android Studio.

If you have any questions I'd be happy to try and help. Please contact me at: john@coronite.net.

# License
This is a public domain work under [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/). Feel free to use it as you see fit.
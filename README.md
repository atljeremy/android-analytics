# Android Analytics

An extremely lightweight analytics library that allows an android application to wrap analytics providers and abstract tags and processing into small, managable, and specific use cases.

## Usage

### Adding to Android project

Start by adding the library to your project.

```
// to do
```

### Create Your Processor(s) (Optional)

Next, you'll create any `Analytics.Processor`'s that you may need in order to process the analytics event data before it's given to the `Analytics.Provider`(s). This could be to ensure all events are formatted the same way. Here's an example of just that, which might be called `SanitizingAnalyticsProcessor`.

```kotlin
class SanitizingAnalyticsProcessor: Analytics.Processor {
    override fun process(event: Analytics.Event): Analytics.Event {
        return event.apply {
            eventName = eventName
                    .replace(Regex("[^A-Za-z0-9 ]"), " ")
                    .replace(Regex("\\s{2,}"), " ")
                    .replace(" ", "_")
                    .toLowerCase()
        }
    }
}
```

### Create Your Provider(s) (Optional)

`Analytics.Provider`s are where analytics actually get tracked. You could think of an `Analytics
.Provider` as something like Firebase Analytics. Firebase Analytics "provides" a way to track analytics. In fact, this library provides a pre-built provider for Firebase Analytics, named, surprisingly, `FirebaseAnalyticsProvider`. If all you're using in your Android application is Firebase Analytics, you could choose to just use this pre-built provider. However, if you're not using Firebase Analytics, or have other analytics provides, here's how you'll create a custom `Analytics.Provider` of your own.

```kotlin
class FirebaseAnalyticsProvider @Inject constructor(
        private val firebase: FirebaseAnalytics
): Analytics.Provider {
    override fun track(event: Analytics.Event) {
        val bundle = Bundle().apply {
            event.data?.entries?.forEach {
                when (val value = it.value) {
                    is Int -> putInt(it.key, value)
                    is Long -> putLong(it.key, value)
                    is Double -> putDouble(it.key, value)
                    is Float -> putFloat(it.key, value)
                    is String -> putString(it.key, value)
                }
            }
        }
        firebase.logEvent(event.eventName, bundle)
    }

    override fun setGlobal(data: Map<String, String>) {
        // Choose how you'd like to track global attributes.
        // You might consider using user properties, something like:
        // data.entries.forEach {
        //     FirebaseAnalytics.getInstance(context).setUserProperty(it.key, it.value)
        // }
    }
}
```


### Create Your AnalyticsModule

Next, you'll create your `AnalyticsModule` which will be used to provide an `Analytics.Consumer`, as well as any `Analytics.Processor`s and/or `Analytics.Provider`s, to your application.

```kotlin
@Module
class AnalyticsModule(private val context: Context) {

    @Provides
    @AnalyticsConsumerQualifier
    fun analyticsConsumer(
            sanitizer: SanitizingAnalyticsProcessor,
            firebase: FirebaseAnalyticsProvider
    ): Analytics.Consumer {
        val consumer = CommonAnalyticsConsumer()
        // Register processors
        consumer.registerAllProcessors(listOf(
                sanitizer
        ))
        // This is where we can add/remove analytics services without changing any code throughout the app
        consumer.registerAllProviders(listOf(
                firebase
        ))
        return consumer
    }

    @Provides
    fun sanitizingAnalyticsProcessor(): SanitizingAnalyticsProcessor {
        return SanitizingAnalyticsProcessor()
    }

    @Provides
    fun firebaseAnalyticsProvider(): FirebaseAnalyticsProvider {
        return FirebaseAnalyticsProvider(FirebaseAnalytics.getInstance(context))
    }
}
```

### Create Your "Use Case" Analytics Module

// TO DO: add description

```kotlin
@Module(includes = [AnalyticsModule::class])
class SignInAnalyticsModule {

    @Provides
    @SignInScope
    fun signInAnalyticsConsumer(
            @AnalyticsConsumerQualifier consumerDelegate: Analytics.Consumer
    ): SignInAnalyticsConsumer {
        return SignInAnalyticsConsumer(consumerDelegate)
    }

}
```

### Use Your "Use Case" Analytics Module

// TO DO: add description

```kotlin
@SignInScope
@Subcomponent(
        modules = [SignInAnalyticsModule::class]
)
interface SignInComponent {
    @Subcomponent.Builder
    interface Builder {
        fun analyticsModule(module: AnalyticsModule): Builder
        fun build(): SignInComponent
    }

    fun inject(activity: SignInActivity)
}
```

### Create Your "Use Case" Analytics Events & Consumer

// TO DO: add description

```kotlin
private enum class SignInAnalyticsEvent(
        override var eventName: String,
        override var data: Map<String, Any>? = null
): Analytics.Event {
    FORGOT_PASSWORD("Forgot_Password"),
    SIGN_UP_SUCCESS("sign_up"),
    SIGN_UP_FAILED("failed_sign_up"),
    LOGIN_SUCCESS("login"),
    LOGIN_FAILED("failed_login");
}

class SignInAnalyticsConsumer @Inject constructor(
        analyticsConsumer: Analytics.Consumer
): Analytics.Consumer by analyticsConsumer {

    companion object {
        private const val USER_EMAIL_KEY = "user_email"
        private const val UNKNOWN = "unknown"
    }

    fun trackForgotPassword() {
        consume(SignInAnalyticsEvent.FORGOT_PASSWORD)
    }
    fun trackLoginSuccess(email: String?) {
        val e = email ?: UNKNOWN
        consume(SignInAnalyticsEvent.LOGIN_SUCCESS.include(mapOf(USER_EMAIL_KEY to e)))
    }
    fun trackLoginFailed(email: String?) {
        val e = email ?: UNKNOWN
        consume(SignInAnalyticsEvent.LOGIN_FAILED.include(mapOf(USER_EMAIL_KEY to e)))
    }
    fun trackSignUpSuccess(email: String?) {
        val e = email ?: UNKNOWN
        consume(SignInAnalyticsEvent.SIGN_UP_SUCCESS.include(mapOf(USER_EMAIL_KEY to e)))
    }
    fun trackSignUpFailed(email: String?) {
        val e = email ?: UNKNOWN
        consume(SignInAnalyticsEvent.SIGN_UP_FAILED.include(mapOf(USER_EMAIL_KEY to e)))
    }
}
```

### Use Your "Use Case" Analytics Component

// TO DO: add description

```kotlin
class SignInActivity : AppCompatActivity() {

    @Inject
    lateinit var analytics: SignInAnalyticsConsumer

    //...

    override fun onCreate(savedInstanceState: Bundle?) {
        //...
        D4DApplication.appComponent.signInComponent()
                .analyticsModule(AnalyticsModule(this))
                .build()
                .inject(this)
        //...
    }

    //...

    private fun onSuccessfulLogin(user: User) {
        if (null != user.apiKey) {
            analytics.trackLoginSuccess(user.email)
            //...
        } else {
            analytics.trackLoginFailed(user.email ?: signInEmail.text.toString())
            //...
        }
    }

    //...
}
```

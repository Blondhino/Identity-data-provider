![Header image](https://raw.githubusercontent.com/Blondhino/Identity-data-provider/master/identity_data_provider/src/main/res/drawable/idp.png)
# Identity Data Provider Android library

**Identtity Data provider** (**IDP**) library is created as **wrapper around** most popular authentication strategies such as **login by Google/Facebook/Apple**. It's purpose is to increase development speed and consolidate common strategies into one codebase. Library is open source and free to use !

<br>
<br>

# How add IDP into project
<br>

 ### 1. Add the following Gradle dependencies:

```groovy
repositories {  
  google()  
  mavenCentral()  
  maven { url 'https://jitpack.io' }  
}
```

```groovy
dependencies {  
implementation 'com.github.Blondhino:Identity-data-provider:$latest'
}
```
<br>

### 2. Initialize library

> IDP requires ComponentActivity so please make sure your hosting
> activity implements it !

```kotlin
class MainActivity : ComponentActivity() {
val identityDataProvider = IdentityDataProvider("backend base url")
/*...*/
  override fun onCreate(savedInstanceState: Bundle?) {
   /*...*/
     identityDataProvider.registerHostingActivity(this)
  }
}
```
<br>
<br>

# Login by Google using IDP


First of all you should [configure your Google API console project](https://developers.google.com/identity/sign-in/android/start-integrating#configure_a_project) and get **client id.**  After that open `strings.xml` file and add following resource with your data: 
  ```xml
<string name="google_client_id"> your google client id </string>
```

<br>

To perform login by Google simply call next line :

 ```kotlin
identityDataProvider.loginWithGoogle()
```

<br>

### Observe authentication state changes:

 ```kotlin
identityDataProvider.onAuthChangedListener={isAuthenticated ->
 /*your logic here*/
 }
```

<br>

### Get current authentication state: 

 ```kotlin
identityDataProvider.isUserAuthenticated()
```

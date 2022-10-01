![Header image](https://raw.githubusercontent.com/Blondhino/Identity-data-provider/master/identity_data_provider/src/main/res/drawable/idp.png)
# Identity Data Provider Android library

**Identtity Data provider** (**IDP**) library is created as **wrapper around** most popular authentication strategies such as **login by Google/Facebook/Apple**. It's purpose is to increase development speed and consolidate common strategies into one codebase. Library is open source and free to use !

# How add IDP into project

 #### 1. Add the following Gradle dependencies:

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

#### 2. Initialize library

> IDP requires ComponentActivity so please make sure your hosting
> activity implements it !

```kotlin
class MainActivity : ComponentActivity() {
val identityDataProvider = IdentityDataProvider("backend base url")
...
  override fun onCreate(savedInstanceState: Bundle?) {
   ...
     identityDataProvider.registerHostingActivity(this)
  }
}
```

# Login by Google using IDP



 
 
  

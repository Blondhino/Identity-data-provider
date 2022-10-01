![Header image](https://raw.githubusercontent.com/Blondhino/Identity-data-provider/master/identity_data_provider/src/main/res/drawable/idp.png)
# Identity Data Provider Android library

**Identtity Data provider** (**IDP**) library is created as **wrapper around** most popular authentication strategies such as **login by Google/Facebook/Apple**. It's purpose is to increase development speed and consolidate common strategies into one codebase. Library is open source and free to use !

How to set up Identity Data Provider (IDP) library

1.) open settings.gradle and insert:
    - maven { url 'https://jitpack.io' }
into repositories section

2.) open strings.xml in from app module and define this 3 strings:
    - <string name="facebook_app_id"> </string>
    - <string name="fb_login_protocol_scheme"> </string>
    - <string name="facebook_client_token"> </string>
    - <string name="google_client_id"> </string>

3.) open your manifest file and insert this inside application tags

<activity
      android:name="com.facebook.CustomTabActivity"
      android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="@string/fb_login_protocol_scheme" />
      </intent-filter>
    </activity>

    <meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
    <meta-data android:name="com.facebook.sdk.ClientToken" android:value="@string/facebook_client_token"/>

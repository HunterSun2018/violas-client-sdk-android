# violas-client-sdk-android-demo

## usage
    1. copy violas-client-sdk-android.jar to [your app]/libs
    2. add violas-client-sdk-android.jar as library for your app
    3. add the following dependencies in build.gradle

        implementation 'com.google.code.gson:gson:2.8.6'
        implementation 'org.bouncycastle:bcprov-jdk15to18:1.68'
        implementation 'org.bitcoinj:bitcoinj-core:0.15.10'
        implementation 'design.contract:libbech32:1.0.0'
        implementation 'org.apache.httpcomponents:httpclient:4.3.5'    //4.5.10
        implementation 'org.apache.commons:commons-lang3:3.11'
        implementation 'com.google.protobuf:protobuf-java:3.13.0'
        implementation 'com.google.protobuf:protobuf-java-util:3.13.0'

        implementation files('libs/sdk.jar')

    4. add the following code to tag android in build.gradle
        android {
            ...
            packagingOptions {
                        exclude 'META-INF/DEPENDENCIES'
                    }
        }

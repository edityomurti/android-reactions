object Config {
    const val minSdk = 19
    const val targetSdk = 28
}

object Publish {
    const val group = "com.github.pgreze"
    const val artifactId = "android-reactions"
    const val version = "1.1"
    const val url = "https://github.com/pgreze/android-reactions"
}

object Versions {
    const val kotlinVersion = "1.3.41"
    const val supportLibraryVersion = "27.1.1"
}

object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlinVersion}"
    const val support = "com.android.support:support-compat:${Versions.supportLibraryVersion}"
    const val appcompat = "com.android.support:appcompat-v7:${Versions.supportLibraryVersion}"
    const val lottie = "com.airbnb.android:lottie:3.1.0"
    const val material = "com.google.android.material:material:1.2.0-alpha01"
    const val recyclical = "com.afollestad:recyclical:1.1.1"
}
plugins { id("com.android.application") }
android {
    namespace = "com.calibg.waililevel"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.calibg.waililevel"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes { release { isMinifyEnabled = false } }
}
dependencies { testImplementation("junit:junit:4.13.2") }

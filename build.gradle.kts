plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.it.attendance"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.it.attendance"
        minSdk = 27
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources.excludes += "META-INF/DEPENDENCIES"
        resources.excludes += "META-INF/NOTICE*"
        resources.excludes += "META-INF/LICENSE*"
        resources.excludes += "META-INF/INDEX.LIST"

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore:24.10.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.applandeo:material-calendar-view:1.9.0")
    implementation ("io.github.pilgr:paperdb:2.7.2")
    implementation ("com.github.pro100svitlo:creditCardNfcReader:1.0.3")
    implementation ("com.android.support:multidex:1.0.3")
    implementation ("com.saadahmedev.popup-dialog:popup-dialog:1.0.5")


}


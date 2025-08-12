import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.testImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    kotlin("kapt")
}

android {
    namespace = "com.instadownloader.instasave.igsave.ins"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.instadownloader.instasave.igsave.ins"
        minSdk = 23
        targetSdk = 34
        versionCode =139
        versionName ="1.39"
        multiDexEnabled = true

        dataBinding {
            enable = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding =true
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-crashlytics")

    implementation ("com.aos.libs:admob:1.2.0")
    implementation ("com.aos.libs:billing:1.0.0")
    implementation ("com.aos.libs:base:1.1.0")


    implementation ("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation ("androidx.core:core-ktx:1.5.0")
    implementation ("androidx.appcompat:appcompat:1.3.0")
    implementation ("com.google.android.material:material:1.3.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")

    implementation ("androidx.lifecycle:lifecycle-process:2.6.0")

    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    //retrofit2
    implementation ("com.squareup.retrofit2:retrofit:2.5.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.5.0")

    //glide
    implementation ("com.github.bumptech.glide:glide:4.11.0")
            annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")

    //download lib
    implementation ("com.liulishuo.filedownloader:library:1.7.6")

    implementation ("androidx.multidex:multidex:2.0.1")

    implementation ("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation ("io.reactivex.rxjava3:rxkotlin:3.0.0")

    implementation ("com.jakewharton.rxbinding4:rxbinding:4.0.0")

    implementation ("androidx.room:room-runtime:2.4.2")
            ksp ("androidx.room:room-compiler:2.4.2")
    implementation( "androidx.room:room-ktx:2.4.2")

    implementation ("org.greenrobot:eventbus:3.3.1")


    implementation ("org.jsoup:jsoup:1.14.3")
    implementation ("com.google.code.gson:gson:2.8.9")


}
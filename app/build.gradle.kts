plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ams"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ams"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets")
            }
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BoM for managing Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")  // Add this line

    implementation ("com.google.firebase:firebase-auth:22.1.0")
    implementation ("com.google.firebase:firebase-firestore:24.6.0") // Update as needed
    implementation ("com.google.android.gms:play-services-tasks:18.0.2") // Update as needed
    implementation ("androidx.core:core:1.10.0")
    // ML Kit Face Detection
    implementation ("com.google.mlkit:face-detection:16.1.7")
    // Biometric library
    implementation ("androidx.biometric:biometric:1.2.0-alpha04")

    implementation ("com.google.android.material:material:1.13.0-alpha05")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.google.android.gms:play-services-maps:18.0.2") // Check for the latest version
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.camera:camera-core:1.1.0-alpha11")
    implementation ("androidx.camera:camera-camera2:1.1.0-alpha11")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-alpha11")
    implementation ("androidx.camera:camera-view:1.0.0-alpha31")

    implementation ("androidx.camera:camera-core:1.2.0")
    implementation ("androidx.camera:camera-camera2:1.2.0")
    implementation ("androidx.camera:camera-lifecycle:1.2.0")
    implementation ("androidx.camera:camera-view:1.2.0") // Update this to 1.2.0
    implementation ("androidx.camera:camera-mlkit-vision:1.5.0-alpha01") // Update this to 1.2.0

    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.2.0")

    implementation ("androidx.core:core:1.7.0")
    implementation ("androidx.core:core-ktx:1.7.0")

// https://mvnrepository.com/artifact/com.shuhart.stepview/stepview
    implementation ("com.github.acefalobi:android-stepper:0.3.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.apache.poi:poi:5.2.3")
 //   # https://mvnrepository.com/artifact/androidx.work/work-runtime-ktx
    implementation ("androidx.work:work-runtime-ktx:2.8.1")



    implementation ("org.tensorflow:tensorflow-lite:2.16.1")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")


// https://mvnrepository.com/artifact/com.android.support/appcompat-v7
    implementation ("com.android.support:appcompat-v7:28.0.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0") // Glide library

}

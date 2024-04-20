plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.image_to_text"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.image.to.text.ocrscanner.textconverter.extract.text.translateapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 10
        versionName = "2.2.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admob_open_id", " ca-app-pub-7055337155394452/2249735067")
            resValue("string", "admob_app_id", "ca-app-pub-4219822921938965~6037175836")
            resValue("string", "admob_banner_id", "ca-app-pub-7055337155394452/2249735067")
            resValue("string", "admob_native_id", "ca-app-pub-7055337155394452/3262799379")
            resValue("string", "admob_inter_id", "ca-app-pub-7055337155394452/3471919069")
            resValue ("string", "fb_inter_splash_ids", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
            resValue ("string", "fb_inter_main_ids", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
            resValue ("string", "fb_native_splash_ids", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
            resValue ("string", "fb_native_main_ids", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
            resValue ("string", "fb_banner_main_ids", "IMG_16_9_APP_INSTALL#437465911980806_437547768639287")
        }
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admob_open_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "admob_app_id", "ca-app-pub-4219822921938965~6037175836")
            resValue("string", "admob_banner_id", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "admob_native_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_inter_id", "ca-app-pub-3940256099942544/1033173712")
            resValue ("string", "fb_inter_splash_ids", "IMG_16_9_APP_INSTALL#437465911980806_437547905305940")
            resValue ("string", "fb_inter_main_ids", "IMG_16_9_APP_INSTALL#437465911980806_437547905305940")
            resValue ("string", "fb_native_splash_ids", "IMG_16_9_APP_INSTALL#437465911980806_437548041972593")
            resValue ("string", "fb_native_main_ids", "IMG_16_9_APP_INSTALL#437465911980806_437548041972593")
            resValue ("string", "fb_banner_main_ids", "IMG_16_9_APP_INSTALL#437465911980806_437547768639287")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        prefab = true
        viewBinding = true
        dataBinding=true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    packagingOptions {
        excludes += "META-INF/INDEX.LIST"
        excludes += "META-INF/DEPENDENCIES"
    }


}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation("com.google.android.ads:mediation-test-suite:3.0.0")
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-camera2:1.1.0") // Updated version
    implementation("com.google.mlkit:language-id-common:16.1.0")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    implementation("androidx.activity:activity:1.8.0")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("com.google.android.gms:play-services-mlkit-language-id:17.0.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Remove the following line since Firebase ML Vision is already included in Firebase BOM
    // implementation("com.google.firebase:firebase-ml-vision:24.1.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // Update Google Cloud Translate dependency version
    implementation("com.google.cloud:google-cloud-translate:2.2.0")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    // Firebase dependencies
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")

    // Other dependencies
    implementation("com.airbnb.android:lottie:4.0.0")
    implementation("com.google.android.play:app-update:2.0.0")
    implementation("androidx.annotation:annotation:1.0.0")
    implementation("io.insert-koin:koin-android:3.3.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.work:work-runtime-ktx:2.8.0")
    implementation("com.facebook.android:audience-network-sdk:6.11.0")
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:18.0.2")
    implementation("com.android.billingclient:billing:6.1.0") {
        exclude(group = "com.google.android.gms", module = "play-services-vision")
        exclude(group = "com.google.android.gms", module = "play-services-vision-common")
    }
    implementation("com.google.firebase:firebase-ml-vision:24.1.0") {
        exclude(group = "com.google.android.gms", module = "play-services-vision")
        exclude(group = "com.google.android.gms", module = "play-services-vision-common")
    }

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Image cropping library
    implementation("com.github.yalantis:ucrop:2.2.7")

    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics:18.6.2")
}



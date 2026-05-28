plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.appvenenos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appvenenos"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Mantengo tu bloque intacto como pediste
    aaptOptions {
        noCompress("tflite")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        jniLibs {
            // Esto permite que las librerías de IA funcionen en dispositivos nuevos
            useLegacyPackaging = true
        }
    }
}

// --- ESTE BLOQUE ES VITAL: Pégalo justo debajo del bloque android ---
// Es lo que engaña a las librerías que piden el SDK 36
configurations.all {
    resolutionStrategy {
        force("androidx.core:core-ktx:1.12.0")
        force("androidx.appcompat:appcompat:1.6.1")
        force("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
        // Si usas navegación, esto evita que pida el SDK 36
        force("androidx.navigation:navigation-fragment-ktx:2.7.7")
        force("androidx.navigation:navigation-ui-ktx:2.7.7")
        force("androidx.activity:activity:1.8.0")
        force("androidx.activity:activity-ktx:1.8.0")
    }
}
dependencies {
    // Estas son las que daban error. Las ponemos fijas para el SDK 34:
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.0") // Esta es la clave
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Pruebas
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Retrofit: Para hablar con Django
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide: Para que se vean las fotos de los animales
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // CameraX (Para ver a través de la lente)
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // TensorFlow Lite (El cerebro de la IA)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Librerías de TensorFlow Lite para que funcione la IA
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}
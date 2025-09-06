# 🌐 NetworkObserver

[![Maven Central](https://img.shields.io/maven-central/v/io.github.tarifchakder.networkobserver/networkobserver)](https://central.sonatype.com/artifact/io.github.tarifchakder.networkobserver/networkobserver) 
[![License](https://img.shields.io/github/license/tarifchakder/NetworkObserver)](LICENSE) 
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org) 
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.8.2-blue)](https://github.com/JetBrains/compose-multiplatform) 
![badge-android](https://img.shields.io/badge/Platform-Android-6EDB8D.svg?style=flat) 
![badge-ios](https://img.shields.io/badge/Platform-iOS-CDCDCD.svg?style=flat) 
![badge-js](https://img.shields.io/badge/Platform-JS%2FWASM-FDD835.svg?style=flat)

---

## ✨ Overview

**NetworkObserver** is a lightweight Kotlin Multiplatform library built for **Compose Multiplatform**. It provides a simple, unified API to monitor network connectivity across **Android**, **iOS**, and **Web/Wasm**, with **real-time updates** and **platform-specific insights**.

---

## 🖥️ Demo

|              Android              |            IOS             |   
|:---------------------------------:|:--------------------------:|
| ![Android](screenshot/mobile.png) | ![IOS](screenshot/ios.png) |

---

## 📦 Features

- ⚡ **Cross-platform**: Android, iOS, Web/Wasm
- 📡 **Real-time network updates**
- 🌐 **Network type detection**: WiFi, Cellular, Ethernet, Unknown
- 🎨 **Composable-ready**: integrate easily into your UI

---

## 🚀 Installation

Add NetworkObserver to your **multiplatform** project by depending on it from `commonMain`.

### Gradle (Kotlin DSL)

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.tarifchakder.networkobserver:networkobserver:<latest-version>")
            }
        }
    }
}
```
### Version Catalog

```toml
[versions]
networkObserver = "1.0.0" # use latest version

[libraries]
network-observer = { module = "io.github.tarifchakder.networkobserver:networkobserver", version.ref = "networkObserver" }
```

## Usage
```kotlin
MaterialTheme {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {

        val status by networkObserverAsState()
        Text("Status: $status")

        val networkType by networkTypeAsState()
        Text("Network type: $networkType")
    }
}
```

## 🤝 Contributing
Issues and PRs are welcome!
If you’d like to add features or fix bugs, please open an issue first so we can discuss scope and approach.
## License
This project is distributed under the MIT License.
[LICENSE](LICENSE) 




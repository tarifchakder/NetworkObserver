# 🌐 NetworkObserver

[![Maven Central](https://img.shields.io/maven-central/v/io.github.tarifchakder.networkobserver/networkobserver)](https://central.sonatype.com/artifact/io.github.tarifchakder.networkobserver/networkobserver) [![License](https://img.shields.io/github/license/tarifchakder/NetworkObserver)](LICENSE) [![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org) [![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.8.2-blue)](https://github.com/JetBrains/compose-multiplatform) ![badge-android](https://img.shields.io/badge/Platform-Android-6EDB8D.svg?style=flat) ![badge-ios](https://img.shields.io/badge/Platform-iOS-CDCDCD.svg?style=flat) ![badge-desktop](https://img.shields.io/badge/Platform-Desktop-DB413D.svg?style=flat) ![badge-js](https://img.shields.io/badge/Platform-JS%2FWASM-FDD835.svg?style=flat)

---

## ✨ Overview

**KToast** is a lightweight toast/notice library built for **Compose Multiplatform**. It gives you a simple, unified API to show toasts across **Android**, **iOS**, **Desktop (JVM)**, and **Web/Wasm**—with sensible defaults and smooth transitions.

---

## 🖥️ Demo

|              Android              |              Desktop               |            Web             |            IOS             |
|:---------------------------------:|:----------------------------------:|:--------------------------:|:--------------------------:|
| ![Android](screenshot/mobile.gif) | ![Desktop](screenshot/desktop.gif) | ![Web](screenshot/web.gif) | ![IOS](screenshot/ios.gif) |

---

## 📦 Features

- ⚡ **Cross-platform**: Android, iOS, Desktop (JVM), Web/Wasm
- 🎛️ **Simple API**: one `ToastState` you can pass around
- 🎨 **Composable** host: place it anywhere in your layout
- 🌀 **Transitions**: slide/fade presets via `ToastDefaults`
- 🧱 **Works with your theme**: drop into any Material setup

---

## 🚀 Installation

Add KToast to your **multiplatform** project by depending on it from `commonMain`.

### Gradle (Kotlin DSL)

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.tarifchakder.ktoast:ktoast:1.0.0")
            }
        }
    }
}
```
### Version Catalog

```toml
[versions]
toast = "1.0.0"

[libraries]
k-toast = { module = "io.github.tarifchakder.ktoast:ktoast", version.ref = "toast" }
```

## Usage

```Kotlin
// Create toast state
val toast = ToastState()
```
```kotlin
MaterialTheme {
  Box {
    ToastHost(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      alignment = Alignment.BottomCenter,
      hostState = toast,
      transitionSpec = { ToastDefaults.SLIDE }
    )
  }
}
```

```Kotlin
// Now call it anywhere
toast1.showToast("Hi this is a common toast")
```
## 🤝 Contributing
Issues and PRs are welcome!
If you’d like to add features or fix bugs, please open an issue first so we can discuss scope and approach.
## License
This project is distributed under the MIT License.
[LICENSE](LICENSE) 




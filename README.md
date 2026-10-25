# Jelajah Sungai Pulai

![Jelajah Sungai Pulai feature graphic](https://raw.githubusercontent.com/khrlanamm/khrlanamm/main/assets/projects/jelajah-sungai-pulai.webp)

Village profile app for Nagari Sungai Pulai, West Sumatra. Built during KKN-PPM UGM 2025, tested by 20 residents over 14 days, and handed over to the village.

[Google Play](https://play.google.com/store/apps/details?id=com.khrlanamm.jelajahsungaipulai)

## Features

- **Nagari profile**: village information and the list of village officials (perangkat nagari).
- **Kampung**: the three kampung (Tanah Nago, Silaut III, Mekar Sari Mulyo), each with its own demographics.
- **Demografi**: population, heads of family, gender, age groups, education, occupation, marital status, religion, and counts per kampung or RT.
- **Penduduk (admin only)**: search, add, edit, and delete resident records.
- Google sign-in with Credential Manager.

## Data and access

Firestore holds the resident records in `penduduk`, including NIK and family card numbers. The security rules in [`firestore.rules`](firestore.rules) keep that collection for admins only:

- An admin is a user whose email has a document `admins/{email}` (document ID is the email in lower case). Add new admins in the Firebase Console the same way.
- The Demografi screen reads `statistik/semua` and `statistik/{kampung}`, which hold counts only. The admin app rewrites them every time it loads the resident list, including after each add, edit, or delete (`data/StatistikPublisher.kt`).
- Signed-in users can read the statistics and the reference lists (`agama`, `kampung`, `pekerjaan`). Everything else is denied.

The Firebase config (`app/google-services.json`) is committed. It is not a secret: access is controlled by the rules above.

## Stack

Kotlin, XML layouts, Navigation component, ViewModel and LiveData, Firebase Authentication and Firestore, Credential Manager, Glide.

## Build

```sh
./gradlew :app:testDebugUnitTest   # unit tests
./gradlew :app:assembleDebug
```

Release bundles are signed in Android Studio (Build, Generate Signed App Bundle) with the upload key, which is kept outside the repo.

Deploy the security rules with the Firebase CLI:

```sh
firebase deploy --only firestore:rules
```

## License

All rights reserved. The source code is public for reference only. See [LICENSE](LICENSE).

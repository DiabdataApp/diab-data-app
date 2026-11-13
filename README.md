<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->

![GitHub contributors](https://img.shields.io/github/contributors/DiabdataApp/diab-data-android?color=blue&label=CONTRIBUTORS)
![GitHub Release](https://img.shields.io/github/v/release/DiabdataApp/diab-data-android?color=teal&label=VERSION)
![CI Tests](https://img.shields.io/github/actions/workflow/status/DiabdataApp/diab-data-android/ci-test.yml?label=CI%20Tests)
![GitHub top language](https://img.shields.io/github/languages/top/DiabdataApp/diab-data-android)

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/DiabdataApp/diab-data-android">
    <img src="resources/Logo/new_logo.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Diab Data</h3>
  <p align="center">
  <a href="https://github.com/DiabdataApp/diab-data-android/releases">Releases</a> • <a href="https://app.diabdata.fr/">Website</a>
  </p>
  <p align="center">
    An android app that lets you keep track of all diabetes related information that you may need to communicate on a regular basis.
  </p>
  <br/>
</div>

<br/>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>

- [About The Project](#about-the-project)
- [Features](#features)
- [Screenshots](#screenshots)
    - [Built With](#built-with)
- [Installation](#installation)
    - [Prerequisites](#prerequisites)
    - [Clone the repo](#clone-the-repo)
- [Project's architecture](#projects-architecture)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [Contact](#contact)

</details>

<!-- ABOUT THE PROJECT -->

## About The Project

<!-- ![image](public/imgs/product_screenshot.png) -->

This project was born from my habit of tracking my diabetes related information on a google sheets
document which was a bit
annoying on the long run. This app aims to let users add important dates and add their medical
results to the app in order to
have everything in the same place and easier to find and update.

## Features

<table>
  <thead>
    <tr>
      <th scope="col">Feature</th>
      <th scope="col">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th scope="row">Important dates</th>
      <td>Save important dates (First pump installation, etc...)</td>
    </tr>
    <tr>
      <th scope="row">Medical appointments</th>
      <td>Add your appointments and set up reminders</td>
    </tr>
    <tr>
      <th scope="row">Weight tracking</th>
      <td>Keep weights records and display graphs</td>
    </tr>
    <tr>
      <th scope="row">Hba1c tracking</th>
      <td>Keep hba1c records and display graphs</td>
    </tr>
    <tr>
      <th scope="row">Medication/Medical devices tracking</th>
      <td>Save your treatments and equipment and get alerted when they expire</td>
    </tr>
    <tr>
      <th scope="row">DataMatrix scanner</th>
      <td>Scan and add treatments and devices to your records by scanning their data matrix</td>
    </tr>
    <tr>
      <th scope="row">Widget</th>
      <td>Add the glance widget to your homescreen to keep an eye on relevant information in real time</td>
    </tr>
    <tr>
      <th scope="row">Wear Os</th>
      <td>Use complications to keep track of expiring devices, treatments or upcoming appointments</td>
    </tr>
    <tr>
      <th scope="row">Backup</th>
      <td>Export/import all of your data</td>
    </tr>
    <tr>
      <th scope="row">Cast data mode</th>
      <td>Update your data via the webapp companion or send a link to get restricted access to your latest information to your practitioner</td>
    </tr>
  </tbody>
</table>

## Screenshots

<details>
  <summary>Screenshots</summary>
  <table>
    <tr>
      <td><img src="resources/screenshots/filled_homescreen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/fab_menu_add_light.png" width="200"/></td>
      <td><img src="resources/screenshots/data_matrix_scan_light.png" width="200"/></td>
      <td><img src="resources/screenshots/graphs_screen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/database_view_screen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/filled_devicescreen_light.png" width="200"/></td>
    </tr>
    <tr>
      <td><img src="resources/screenshots/settings_screen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/user_profile_screen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/empty_devicescreen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/empty_homescreen_light.png" width="200"/></td>
      <td><img src="resources/screenshots/cast_data_mode_screen_light.png" width="200"/></td>
    </tr>
  </table>
</details>

### Built With

This project is built using the following frameworks/libraries:

<div>

[![Static Badge](https://img.shields.io/badge/Kotlin-fuchsia?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Static Badge](https://img.shields.io/badge/Jetpack%20Compose-002b41?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose/)
[![Static Badge](https://img.shields.io/badge/Room-green)](https://developer.android.com/jetpack/androidx/releases/room)

</div>

<div>

[![Static Badge](https://img.shields.io/badge/Android%20Studio-3DDC84?logo=androidstudio&logoColor=white)](https://developer.android.com/studio/)
[![Static Badge](https://img.shields.io/badge/Google%20material%20font-4285F4?logo=googlefonts&logoColor=white)](https://fonts.google.com/icons?icon.size=24&icon.color=%235f6368&icon.query=date&icon.set=Material+Symbols&icon.style=Outlined)
[![Static Badge](https://img.shields.io/badge/Vico-4285F4)](https://github.com/patrykandpatrick/vico)

</div>

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- INSTALLATION -->

## Installation

### Prerequisites

- Android Studio Ladybug (2024.3) or newer
- JDK 17+
- Android SDK with API 35 (compile) / API 26 (min)

### Clone the repo

Clone repository

```bash
git clone https://github.com/DiabdataApp/diab-data-android.git
```

#### Setting up your variables

Next, rename the local.properties.sample file to local.properties and fill in the required fields:

```
sdk.dir=/path/to/your/sdk
RELAY_SERVER_URL=your.relay.url
RELEASE_STORE_FILE=KEYSTORE-PATH.jks
RELEASE_STORE_PASSWORD=KEYSTORE-PASSWORD
RELEASE_KEY_ALIAS=diabdata
RELEASE_KEY_PASSWORD=KEY-PASSWORD
```

You can then open the project in Android Studio.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Project's architecture

This project follows a feature-based architecture:

```
├── app/
├── shared/ # String resources, drawables and code shared between modules
├── wear/ # Wear Os Module
├── core/
│ ├── database/ # Room DB, DAOs, migrations
│ ├── model/ # Database model entities
│ ├── ui/ # Core UI components used across the app
│ ├── utils/
│ └── notifications/ # Notification management
├── feature/
│ ├── appointments/ # Appointments management
│ ├── devices/ # Medical devices management
│ ├── casting/ # Data casting mode
│ ├── hba1c/ # Hba1c records management
│ ├── weight/ # Weight records management
│ ├── settings/ # Settings
│ └── dataMatrixScanner/ # Code scanner
└── widget/ # Glance widget
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contributing

This project follows a **feature branch** workflow with three main branches:

`feature branch` → PR → `dev` (beta pre-release)
`dev` → PR → `main` (production release)

### Branch naming convention

- First letter of your first name, capitalized (e.g., `F`)
- Your last name, in uppercase (e.g., `COSSU`)
- A dash `-`
- A short, capitalized identifier for the feature (spaces replaced by underscores)

**Example:** Florian Cossu working on updating the database → `FCOSSU-Database_migration`

### Workflow

1. Create your Feature Branch from `dev`:

  ```bash
  git checkout dev
  git pull origin dev
  git checkout -b FCOSSU-Database_migration
  ```

2. Commit your changes:

  ```bash
  git commit -m 'Add some new feature'
  ```

3. Push your branch and open a Pull Request → `dev`:

  ```bash
  git push origin FCOSSU-Database_migration
  ```

4. Once the PR is reviewed and merged into `dev`:
    - CI tests run automatically
    - A beta pre-release is created with debug APKs
5. When `dev` is ready for production, a PR is opened `dev → main`:

- A production release is created with signed & minified APKs
- The content of the release notes will be retrieved from the PR description

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Visual identity

The app heavily relies on
Google's [Material Symbols & Icons](https://fonts.google.com/icons?icon.size=24&icon.color=%231f1f1f&icon.set=Material+Symbols&icon.style=Rounded&selected=Material+Symbols+Rounded:home:FILL@1;wght@400;GRAD@0;opsz@24).
The settings used to retrieve the SVG files are:

| variant  | weight | grade | optical size | style                            |
|:--------:|:------:|:-----:|:------------:|:---------------------------------|
| Outlined |  400   |   0   |      24      | Material Symbols (new) - Rounded |
|  Filled  |  400   |   0   |      24      | Material Symbols (new) - Rounded |

Material Symbols are © Google and distributed under
the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0). Some icons in
`shared/src/main/res/drawable/` are hand-modified versions of them. The license requires modified files
to say so (section 4(b)), so any icon you change must start with a comment like:

```xml
<!-- Modified from Material Symbols "medication" (Google, Apache 2.0) -->
```

The app font is [Google Sans Flex](https://fonts.google.com/specimen/Google+Sans+Flex), © Google, under
the [SIL Open Font License 1.1](licenses/google_sans_flex_OFL.txt).

The in-app licenses screen is generated by [AboutLibraries](https://github.com/mikepenz/AboutLibraries).
Assets that are not Gradle dependencies (like the font) are declared manually in `app/config/`. If the OFL
text changes, regenerate its definition from the project root:

```bash
jq -Rs '{hash: "OFL-1.1", name: "SIL Open Font License 1.1", url: "https://openfontlicense.org", content: .}' \
  licenses/google_sans_flex_OFL.txt > app/config/licenses/OFL-1.1.json
```

<!-- ROADMAP -->

## Roadmap

See the [open issues](https://github.com/DiabdataApp/diab-data-android/issues) for a full list of
proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->

## Contact

Florian
Cossu - [Linkedin](https://www.linkedin.com/in/florian-cossu/) - [Github](https://github.com/Florian-cossu)

Project
Link: [https://github.com/DiabdataApp/diab-data-android](https://github.com/DiabdataApp/diab-data-android)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
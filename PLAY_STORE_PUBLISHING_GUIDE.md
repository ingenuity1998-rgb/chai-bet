# Google Play Store Publishing Guide for Chai Bet

This guide walks you step-by-step through compiling this Android project, creating a signed bundle (.aab), and passing Google Play review smoothly.

---

### Step 1: Open in Android Studio
1. Unzip `chaibet_android_project.zip`.
2. Open Android Studio -> Click **Open** -> Select the `chaibet_android` folder.
3. Allow Gradle to sync dependencies automatically.

---

### Step 2: Generate Signed App Bundle (.aab)
1. In Android Studio, go to menu: **Build -> Generate Signed Bundle / APK...**
2. Choose **Android App Bundle (.aab)** -> Click **Next**.
3. Create a new Keystore file (save it as `chaibet_release.keystore` and note your password).
4. Select `release` build variant and click **Finish**.
5. Your compiled `.aab` file will be generated in `app/release/app-release.aab`.

---

### Step 3: Google Play Console Setup
1. Log in to [Google Play Console](https://play.google.com/console).
2. Click **Create App**:
   - App Name: `Chai Bet - Friendly Focus Stakes`
   - Default Language: `English (India)`
   - App or Game: `App`
   - Free or Paid: `Free`
3. Fill out the **App Content** declarations:
   - **Target Audience:** Select 18+ (ensures no restrictions regarding micro-stakes).
   - **Data Safety:** Declare that no personal data is collected or shared with third parties.

---

### Step 4: Accessibility Service Declaration Form (Critical)
Google requires a specific form for AccessibilityService apps. Fill it out as follows:
- **Question:** What is the core feature that uses the Accessibility Service?
  - **Answer:** *"Chai Bet uses the AccessibilityService solely to detect when user-configured social media applications (such as Instagram or YouTube) are launched into the foreground during active focus hours, in order to display the user's voluntary focus commitment overlay."*
- **Question:** Does the app collect, store, or share any personal or sensitive user data using Accessibility?
  - **Answer:** *"No. The app does not inspect, read, store, or transmit screen contents, keystrokes, messages, or user credentials."*

---

### Step 5: Upload to Internal Testing
1. In Play Console, go to **Testing -> Internal testing**.
2. Upload `app-release.aab`.
3. Add your own Gmail address as a tester.
4. Download the app directly from your Play Store tester link onto your Android phone!

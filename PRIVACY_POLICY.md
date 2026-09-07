# Privacy Policy for NeuroPath

**Effective Date:** September 7, 2026  
**Last Updated:** September 7, 2026  
**Publisher:** FourgeAI LABS  
**Contact Email:** ericcollins9858@gmail.com  

---

## 1. Introduction & Overview

**NeuroPath** ("we", "our", or "us"), developed by **FourgeAI LABS**, is an adaptive, sensory-friendly educational platform designed for neurodiverse and neurotypical learners. Our mission is to provide an accessible, supportive, and empowering learning environment while upholding the highest standards of data privacy, user security, and children's safety.

This Privacy Policy explains how information is handled when you use the NeuroPath Android application (the "App").

---

## 2. Children's Privacy (COPPA & GDPR-K Compliance)

NeuroPath is designed for children, students, families, and educators. Protecting children's privacy is our highest priority:

* **No Personal Data Collection:** We do **not** require children or parents to provide real names, email addresses, phone numbers, or any personally identifiable information (PII) to use the App.
* **On-Device Data Storage:** All learner profile settings (such as chosen avatar, grade level, learning interests, sensory preferences, and progress statistics) are saved **locally on your device** using local databases (`Room` / `SharedPreferences`).
* **Parental Controls:** Profile setup, curriculum standards configuration, and parent settings are safeguarded behind parent verification controls.

---

## 3. Data Collection and Usage

### A. Information Stored Locally on Your Device
The following data is generated during App usage and remains stored strictly on your local Android device:
* **Learner Profiles:** Display name/nickname, grade level, interests, avatar selection, and neurotype support preferences (e.g., ADHD, Dyslexia font toggle, High-Contrast mode).
* **Educational Progress:** Lesson completion history, quiz scores, reward badges, and daily learning goal stats.
* **App Settings:** Theme selections, text-to-speech (TTS) speed preferences, and local model settings.

### B. Optional Location Data for Curriculum Alignment
* **Purpose:** The App provides an optional location auto-detect feature to map relevant state and regional educational standards (e.g., state curriculum frameworks, school district standards).
* **Handling:** Location permissions (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`) are requested only when a parent/user chooses to auto-detect their regional educational standards. Coordinates are processed locally or via standard system geocoding solely to identify the country, state/province, and school district. 
* **No Location Tracking:** We do **not** track location in the background, store location history, or share location data with any third party.

### C. AI Tutoring & Interaction Data
* **Cloud AI (Gemini API):** When using cloud-powered AI tutoring features, user prompts and lesson contexts are sent securely to Google's Gemini API endpoints strictly to generate educational responses. These prompts contain no user-identifying metadata.
* **On-Device Local AI (Gemma Models):** When local offline AI mode is enabled, model packages (such as Gemma weights from Hugging Face) are downloaded to device storage, and all AI processing occurs **100% locally on your device** without transmitting data over the internet.

---

## 4. Device Permissions

The App may request the following standard Android permissions:

| Permission | Purpose | Optional/Required |
| :--- | :--- | :--- |
| `INTERNET` | Used for cloud AI tutoring queries, fetching public educational standards, and downloading optional local model packages. | Required |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Used solely to auto-detect regional state curriculum standards upon parent request. | Optional |
| `POST_NOTIFICATIONS` | Used to deliver daily learning reminders and goal milestones. | Optional |
| `RECORD_AUDIO` | Used for voice search input during interactive learning sessions. | Optional |

You can grant or revoke any optional permission at any time through your Android device's **Settings -> Apps -> NeuroPath -> Permissions**.

---

## 5. Third-Party Services

NeuroPath interacts with the following trusted third-party service providers solely to enable core App functionality:

* **Google Cloud & Gemini API:** Used to deliver real-time educational answers and socratic tutoring. Usage is subject to [Google's Privacy Policy](https://policies.google.com/privacy).
* **Google Maps Geocoding Services:** Used for reverse geocoding to resolve state and district educational frameworks. Subject to [Google Privacy Policy](https://policies.google.com/privacy).
* **Hugging Face:** Used as an optional download repository for local open-weights Gemma models. Subject to [Hugging Face Privacy Policy](https://huggingface.co/privacy).

We do **not** sell, rent, trade, or share any user data with advertisers, data brokers, or marketing networks. The App contains **no advertisements** and **no third-party tracking software**.

---

## 6. Data Security and Retention

* **Data Control:** Because your data is stored locally on your device, you have complete control over it at all times.
* **Data Deletion:** You can delete all App data instantly by using the **"Clear App Data"** option in the Parent Dashboard, or by navigating to **Android Settings -> Apps -> NeuroPath -> Storage -> Clear Data / Clear Storage**, or by uninstalling the Application.

---

## 7. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect app updates or regulatory requirements. Any updates will be published within the project repository and reflected in the "Last Updated" date at the top of this document.

---

## 8. Contact Us

If you have any questions, concerns, or requests regarding this Privacy Policy or data protection in NeuroPath, please contact us:

* **Publisher:** FourgeAI LABS
* **Developer Email:** ericcollins9858@gmail.com
* **Project Repository:** [https://github.com/fourgeailabs/neuropath](https://github.com/fourgeailabs/neuropath)

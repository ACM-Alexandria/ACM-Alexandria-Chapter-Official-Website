# Google Sheets & Drive API Integration Setup Guide

This guide explains how to configure Google Cloud Platform (GCP) and obtain the OAuth 2.0 credentials required for the ACM Alexandria Website backend to export event and club registrations to Google Sheets.

## Step 1: Google Cloud Console Configuration

1. **Access the Console:**
   Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create new project (e.g. `acm-website`)
3. **Select Your Project:**
   Ensure you select your target project (e.g., `acm-website`) from the project dropdown at the top header of the console.
4. **Enable APIs:**
   Navigate to the **APIs & Services > Library** page and enable the following APIs:

   * **Google Sheets API** (Direct Link: [Google Sheets API](https://console.cloud.google.com/apis/library/sheets.googleapis.com))
   * **Google Drive API** (Direct Link: [Google Drive API](https://console.cloud.google.com/apis/library/drive.googleapis.com))
5. **Configure OAuth Consent Screen:**
   Go to **APIs & Services > OAuth consent screen**:

   * **User Type:** Select **External**.
   * **App Information:** Fill in the basic app registration details (e.g., App Name: `ACM Alexandria Website`, User support email).
   * **Scopes:** Add the following scopes:
     * `https://www.googleapis.com/auth/spreadsheets`
     * `https://www.googleapis.com/auth/drive`
   * **Test Users:** Add your Google email address (e.g., `omarzydan610@gmail.com`). *This is critical; otherwise, Google will block your login attempts with a 403 error.*
   * **Publishing Status (Highly Recommended):** Under **OAuth consent screen**, click the **Publish App** button. This will move your app out of the "Testing" state.
     > **Note:** You do not need to submit the app for Google verification. Keeping it "Unverified" is perfectly fine for personal/chapter use, but publishing it removes Google's default **7-day expiration limit** on your refresh token.
     >
6. **Create OAuth Credentials:**

   * Go to **APIs & Services > Credentials**.
   * Click **Create Credentials** at the top and select **OAuth client ID**.
   * **Application Type:** Select **Web application**.
   * **Name:** `ACM Sheets Integrator`
   * **Authorized Redirect URIs:** Add exactly:
     `https://developers.google.com/oauthplayground`
   * Click **Create**.
   * Copy the generated **Client ID** and **Client Secret**.

---

## Step 2: Generate the Refresh Token (via Google OAuth Playground)

Since your backend needs to run headless (without showing a login pop-up to administrators every time), we use a persistent **Refresh Token** to fetch fresh access tokens automatically.

1. Go to the [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/).
2. Click the **Gear Icon (OAuth 2.0 Configuration)** in the top right corner.
3. Check the box **"Use your own OAuth credentials"**.
4. Paste your **OAuth Client ID** and **OAuth Client Secret** (from Step 1).
5. In Step 1 (on the left column under "Select & authorize APIs"), enter these two scopes in the input box:
   * `https://www.googleapis.com/auth/spreadsheets`
   * `https://www.googleapis.com/auth/drive`
6. Click the blue **Authorize APIs** button.
7. Log in using your test-user Google account and approve the permissions.
   > **Note:** If Google shows a warning saying *"Google hasn't verified this app"*, click **Advanced** at the bottom and click **Go to ACM Website (unsafe)** to proceed.
   >
8. Once redirected back to the Playground, click the blue **Exchange authorization code for tokens** button in Step 2.
9. mark on auto refresh before expire
10. regenrate token
11. Copy the **Refresh Token** shown in the JSON response payload.

---

## Step 3: Configure the Spring Boot Backend

Open your `backend/acmwebsite/src/main/resources/application.properties` file and configure the keys under the Google Sheets section:

```properties
# Google Sheets Integration Settings
google.sheets.client-id=YOUR_CLIENT_ID.apps.googleusercontent.com
google.sheets.client-secret=YOUR_CLIENT_SECRET
google.sheets.refresh-token=YOUR_REFRESH_TOKEN

# (Optional) Destination Folders
# Paste specific Google Drive Folder IDs here to save the generated spreadsheets in them.
# If left empty, sheets will be created in the root of your Google Drive.
google.sheets.events-folder-id=
google.sheets.clubs-folder-id=
```

---

## Step 4: Finding Google Drive Folder IDs (Optional)

If you want Event and Club sheets to be created inside specific Google Drive folders:

1. Open the target folder in your Google Drive using a web browser.
2. Examine the address bar URL. It will look like:
   `https://drive.google.com/drive/folders/1A2B3C4D5E6F7G8H9I0J`
3. Copy the letters and numbers at the end of the URL (e.g., `1A2B3C4D5E6F7G8H9I0J`).
4. Paste this ID into the corresponding properties key in `application.properties` (listed above).

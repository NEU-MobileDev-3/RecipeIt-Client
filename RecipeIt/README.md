# RecipeIt! - Snap, Cook, Enjoy

**AI-Powered Ingredient Recognition & Recipe Generator**

---

## 📱 Overview
RecipeIt! is a mobile-first AI-powered Android application that helps users turn available ingredients into smart, personalized recipes in seconds. Users simply snap or upload an image of their ingredients, and the app uses image recognition and generative AI to suggest recipe ideas that match user preferences.

---

## 🚀 Features
- Chat-based interface for recipe suggestions
- Upload or capture ingredient images
- AI-powered ingredient recognition
- Recipe generation with markdown formatting
- User authentication and profile management
- Sliders and toggles for calorie and dietary preferences (vegan, gluten-free, dairy-free)
- Firebase integration for storage and security

---

## 🛠️ Tech Stack
**Frontend (Android):**
- Java (Android Studio)
- Material UI components
- Glide (image loading)

**Backend:**
- Node.js + Express.js
- Google Vision API for ingredient detection
- Gemini API for recipe generation
- Firebase Admin SDK

**Database:**
- Firebase Authentication & Firestore

---

## 🔧 Setup Instructions
### 🔹 Prerequisites
- Android Studio installed
- Java Development Kit (JDK)
- Git

### 🔹 Cloning the Project
```bash
git clone https://github.com/NEU-MobileDev-3/RecipeIt-Client.git
cd RecipeIt-Client/RecipeIt
```

### 🔹 Running the App (Frontend)
1. Open `RecipeIt` folder in Android Studio.
2. Let Gradle sync completely.
3. Connect an Android emulator or real device.
4. Run the app (`Shift + F10` or green Run button).

---

## 🧪 Testing Instructions
### 🔹 Test Account
- **Email:** recipeuser1@gmail.com
- **Password:** recipe123

### 🔹 Figma with various app screens to follow during test
- https://www.figma.com/design/cX2ToV3KFwbnjjVSg4kNGf/Final-Submission-Flow?node-id=0-1&p=f&t=oDN7HIAOu7oXLiS8-0

### 🔹 Steps to Test
1. Launch the app to access the splash screen.
2. Log in using the test account or create a new one.
3. Tap the 📷 camera icon to capture/upload an image of ingredients.
4. Wait for recognition and chat-based recipe suggestion.
5. Explore toggles and sliders to modify suggestions.

➡️ **Note:** Ensure internet connection is enabled during testing.

---

## 📂 Project Structure (Frontend)
```
RecipeIt-Client/
└── RecipeIt/             ← Android app folder
    ├── app/              ← Main application module
    ├── build.gradle.kts
    ├── README.md         ← You're here!
    └── ...
```

---

## 👥 Team 
- **Chang Liu**
- **Fnu Aisha**
- **Linrui Luo** 
- **Senay Tilahun** 
- **Yadhukrishnan Pankajakshan** 

---

## 📜 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 🔗 Useful Links
- Frontend Repo: https://github.com/NEU-MobileDev-3/RecipeIt-Client
- Backend Repo: https://github.com/NEU-MobileDev-3/RecipeIt-Server

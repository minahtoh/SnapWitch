# SnapWitch

SnapWitch is an Android app that allows users to schedule automatic toggling of device settings like **Bluetooth, Wi-Fi, Airplane Mode, and Mobile Data via settings redirect** at specific times. It also includes a **reminder feature** that notifies users when it's time to turn off certain settings.

## 🚀 Features
- **Schedule Settings Control** – Automatically turn off Wi-Fi, Bluetooth, DND, and more at scheduled times.
- **Reminder Alerts** – Get notified before a setting change occurs.
- **Repeating Schedules** – Set actions to repeat on specific days.
- **User-Friendly Interface** – Simple and intuitive UI with an orange-accented theme.

## 📱 Screenshots
![Screenshot_20250218-065114](https://github.com/user-attachments/assets/a1321ac0-14aa-4010-827a-32a81c85a907)
![Screenshot_20250218-065123](https://github.com/user-attachments/assets/90f5924e-a833-40a9-ae89-8cdd62074558)
![Screenshot_20250218-065158](https://github.com/user-attachments/assets/e6806eec-6433-4482-90f4-2eeea61732db)
![Screenshot_20250218-065243](https://github.com/user-attachments/assets/6ef9e0d9-3f34-46e4-a8c5-2e15be1d0027)

## 🛠️ Tech Stack
- **Jetpack Compose** – Modern Android UI framework.
- **AlarmManager** – Used for scheduling tasks.
- **DataStore** – Persistent storage for user preferences.
- **BroadcastReceiver** – Handles scheduled actions.

## 📥 Installation
1. Clone the repository:
   ```sh
    git clone https://github.com/minahtoh/SnapWitch.git
   ```
2. Open in **Android Studio**.
3. Run the app on an **emulator or physical device**.

## 🔧 Usage
1. Set up a schedule by selecting a **time** and **action** (e.g., turn off Wi-Fi at 10 PM).
2. Enable **repeating** for specific days if needed.
3. Get notified before the action triggers.
4. Manage or delete scheduled tasks from the settings.

## 📌 Known Issues
- Some settings (like **Airplane Mode & Mobile Data**) require **manual confirmation** due to Android restrictions.
- Ensure the app has **battery optimizations disabled** for proper alarm execution.

## 🎯 Roadmap
- [ ] Add support for **custom user actions**.
- [ ] Improve UI/UX with **animations**.

## 🤝 Contributing
Pull requests are welcome! If you’d like to contribute, please fork the repo and submit a PR.


---

📢 **Have a feature request or issue?** Open an **[issue](https://github.com/minahtoh/SnapWitch/issues)** on GitHub!


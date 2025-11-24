# 🍔 QuickBite  
A fast, simple, and modern food-ordering Android application built using **Java and Kotlin**, **XML**, and **Android Studio**.  
QuickBite focuses on delivering a smooth, clean, and efficient user experience for browsing menus, ordering food, and managing user accounts.

---

## 🚀 Features

### ✅ **User Authentication**
- Login & Signup system  
- Secure user session handling  
- Google authentication (optional)

### 🍽️ **Menu Browsing**
- Clean UI for listing food items  
- Category-wise menu display  
- Item details with description & price  

### 🛒 **Cart & Orders**
- Add items to cart  
- Update item quantity  
- Place orders easily  
- View previous order history  

### 🎨 **Modern UI/UX**
- Smooth animations  
- Minimal, clutter-free layout  
- Fast navigation  

---

## 🛠️ Tech Stack

| Component | Used |
|----------|------|
| Language | Java and Kotlin |
| UI | XML |
| App Framework | Android SDK |
| Database | Firebase |
| Tools | Android Studio |

---

## 🧠 How QuickBite Works – Detailed App Flow (Student Mode + Canteen Member Mode)

QuickBite is a two-mode Android application designed for college canteens.  
It allows **students** to order food easily and **canteen staff** to manage orders and edit menus in real time.

---

## 🔄 1. Mode Selection (Student / Canteen Member)

When the app starts, the user is asked to choose a mode:

- **Student Mode**
- **Canteen Member Mode**

Each mode unlocks a different workflow and UI layout.

---

## 🎓 2. Student Mode – Order Placement System

### 2.1 **Menu Display (No Images)**
Students can view the full menu with:
- Item name  
- Price  
- Availability status  

The menu is stored in:
- Firebase (if cloud-based)  
**or**
- Local database (if offline)

The Student UI is simple and clean since no images are used.

### 2.2 **Selecting Items**
Instead of a cart system, the student directly:
- Selects the item  
- Chooses quantity (optional)  
- Places the order instantly  

### 2.3 **Placing an Order**
When the student taps **“Order”**:
1. The order is saved to the Firebase database under **Orders**.
2. The order contains:
   - Item name  
   - Quantity  
   - Student name / ID  
   - Timestamp  
   - Order status (default: *Pending*)  

### 2.4 **Order Status Tracking**
Students can view their order status:
- Pending  
- Accepted  
- Completed  

---

## 🧑‍🍳 3. Canteen Member Mode – Admin Panel

This mode is only for the canteen staff.

### 3.1 **Menu Management**
Canteen members can:
- Add new items  
- Edit item names  
- Edit prices  
- Mark items as available/unavailable  
- Delete items  

All updates are instantly stored in the database and visible to students.

### 3.2 **View Orders**
Canteen staff can see all incoming student orders in real time:
- Pending orders  
- Accepted orders  
- Completed orders  

Orders are displayed in a simple list format.

### 3.3 **Accepting Orders**
Each order has an **“Accept”** button.

When clicked:
- The order status changes from **Pending → Accepted**  
- Student sees the update immediately  
- Helps manage rush hours efficiently  

### 3.4 **Marking Orders Completed**
After preparing food, the canteen member taps:
- **“Complete Order"**

Order status updates to **Completed**.

---

## 🛠️ 4. Backend Logic

The entire app runs on:
- **Firebase Realtime Database** or **Firestore**
- No images → lightweight and fast  
- Two collections/tables (recommended):

1. `menu_items`  
2. `orders`  

This ensures smooth data syncing between students and canteen staff.

---

## 🎨 5. UI/UX Pattern

The app follows a minimal structure:
- No images → faster performance  
- Text-based menu  
- Simple buttons for actions  
- Clear separation of Student and Canteen Member features  
- Lightweight screens with RecyclerViews  

---

## ⚡ 6. Why QuickBite Works Efficiently

- No image loading → super fast  
- Real-time database → instant sync  
- Divided roles → clean workflow  
- No cart → less confusion, faster ordering  
- Direct update system → students always see real-time status  

---

## 🔚 Summary

**QuickBite = Order Management + Menu Control in One App.**

- Students → Order food quickly  
- Canteen Members → Manage menu, view orders, accept orders  

A simple, fast, college-friendly solution.



# Shoppe - Modern E-Commerce Android App

A complete, production-ready e-commerce mobile application built with modern Android development practices. Shoppe showcases a full-featured shopping experience with seamless user authentication, product browsing, cart management, and secure payment processing.

📌 Watch the short demo here:
👉 https://youtube.com/shorts/SrThHb7D8Gk

### 🎯 Core Features
- **Onboarding System** - Interactive introduction to the app
- **User Authentication** - Email/password login with Firebase integration
- **Product Catalog** - Browse products by brands/categories
- **Product Details** - Detailed product views with images and variants
- **Shopping Cart** - Real-time cart management with quantity controls
- **Favorites/Wishlist** - Save products for later purchase
- **Order Management** - Complete order history and tracking
- **Secure Payments** - Stripe integration for secure checkout
- **Address Management** - Add/edit/delete shipping addresses
- **Map Integration** - Location picker for addresses using Google Maps
- **Multi-language Support** - Arabic/English language switching
- **Guest Checkout** - Allow shopping without account creation

### 🎨 UI/UX Features
- **Material Design 3** - Modern, clean interface following Material Design guidelines
- **Smooth Animations** - Shared element transitions and micro-interactions
- **Custom Bottom Navigation** - Unique FAB-style cart button with badge
- **Responsive Design** - Optimized for various screen sizes
- **Dark/Light Theme** - Automatic theme switching support
- **Lottie Animations** - Engaging loading and onboarding animations

### 🔧 Technical Features
- **MVVM Architecture** - Clean separation of concerns
- **Hilt Dependency Injection** - Modern DI framework
- **Jetpack Compose** - Declarative UI toolkit
- **Navigation Component** - Type-safe navigation
- **Coroutines & Flow** - Reactive programming
- **Retrofit & OkHttp** - Robust networking
- **Room Database** - Local data persistence (if implemented)
- **Shared Preferences** - User session management
- **Firebase Integration** - Authentication and analytics

## 📱 Screens

1. **Onboarding Screen** - App introduction with swipeable pages
2. **Start Screen** - Welcome screen with login/signup options
3. **Login/Signup Screens** - User authentication with Google Sign-In
4. **Home Screen** - Featured products and categories
5. **Category Screen** - Browse products by brand/category
6. **Product Details Screen** - Individual product information
7. **Products Screen** - Product listings by brand
8. **Cart Screen** - Shopping cart management
9. **Favorite Screen** - Wishlist management
10. **Profile Screen** - User profile and settings
11. **Address Management** - Add/edit shipping addresses
12. **Map Picker** - Select location on map
13. **Order Screens** - Order history and details
14. **Payment Screen** - Secure checkout with Stripe
15. **Review Screen** - Product reviews and ratings

## 🏗️ Architecture

### Project Structure
```
app/src/main/java/com/lee/shoppe/
├── data/
│   ├── model/           # Data models and DTOs
│   ├── network/         # Network layer (Retrofit, API)
│   └── repository/      # Repository pattern implementation
├── ui/
│   ├── components/      # Reusable UI components
│   ├── navigation/      # Navigation setup and routes
│   ├── screens/         # Individual screen implementations
│   ├── theme/           # App theming and colors
│   ├── utils/           # UI utilities and helpers
│   └── viewmodel/       # ViewModels for state management
├── util/                # Utility classes
└── utils/               # Additional utilities
```

### Architecture Patterns
- **MVVM (Model-View-ViewModel)** - Clean architecture with separation of concerns
- **Repository Pattern** - Abstract data sources
- **Dependency Injection** - Hilt for DI management
- **Single Source of Truth** - ViewModels manage app state

### Key Components

#### Data Layer
- **NetworkService** - Retrofit API interface for Shopify integration
- **NetworkManager** - Network state management
- **SharedPreferenceManager** - Local storage and caching
- **Models** - Kotlin data classes for API responses

#### UI Layer
- **Compose UI** - Declarative UI components
- **ViewModels** - State management and business logic
- **Navigation** - Type-safe navigation with arguments
- **Theme** - Material Design 3 theming

#### Business Logic
- **Repository** - Data repository implementations
- **Use Cases** - Business logic separation (if implemented)
- **State Management** - StateFlow and Compose state

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Android SDK** - Target API 35, Min API 24
- **Gradle with Kotlin DSL** - Build system

### UI & Navigation
- **Material Design 3** - Design system
- **Compose Navigation** - Navigation component
- **Compose Animation** - UI animations
- **Lottie** - Animation library
- **Coil** - Image loading

### Architecture & DI
- **Hilt** - Dependency injection
- **MVVM** - Architecture pattern
- **Coroutines** - Asynchronous programming
- **StateFlow** - Reactive streams

### Networking & Data
- **Retrofit** - HTTP client
- **OkHttp** - HTTP client with interceptors
- **Gson** - JSON parsing
- **Shared Preferences** - Simple storage

### Authentication & Payment
- **Firebase Auth** - Authentication service
- **Stripe** - Payment processing
- **Google Sign-In** - Social authentication

### Maps & Location
- **Google Maps SDK** - Map integration
- **Places API** - Location services
- **Maps Compose** - Compose map components

### Testing
- **JUnit** - Unit testing
- **Espresso** - UI testing
- **Compose Testing** - UI component testing

## 📋 Requirements

### Prerequisites
- **Android Studio** - Latest stable version
- **Kotlin** - 2.0.0 or higher
- **Android SDK** - API 35 (target), API 24 (minimum)
- **Git** - For version control

### API Keys Required
1. **Shopify API** - For product and customer management
2. **Stripe API** - For payment processing
3. **Google Maps API** - For location services
4. **Firebase** - For authentication and analytics

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/Shoppe.git
cd Shoppe
```

### 2. Configure API Keys
Create `local.properties` file in project root and add:
```properties
# API Keys - DO NOT COMMIT TO GIT
SHOPIFY_API_KEY=your_shopify_api_key
SHOPIFY_PASSWORD=your_shopify_password
STRIPE_API_KEY=your_stripe_api_key

# SDK Path
sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```
Or add to `gradle.properties` (also protected by .gitignore)

### 3. Configure Google Maps
Open `app/src/main/AndroidManifest.xml` and update:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="your_google_maps_api_key" />
```

### 4. Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Download `google-services.json` and place it in `app/` directory
3. Enable Firebase Authentication in your project

### 5. Build and Run
```bash
# Build the project
./gradlew build

# Install on device/emulator
./gradlew installDebug
```

## 🔧 Configuration

### Shopify Configuration
1. Create a Shopify store
2. Generate API credentials in Shopify Admin
3. Configure products and collections
4. Set up webhook for order management

### Stripe Configuration
1. Create a Stripe account
2. Get API keys from Stripe Dashboard
3. Configure webhook endpoints
4. Set up payment methods

### Google Maps Configuration
1. Enable Maps SDK and Places API in Google Cloud Console
2. Generate API key with proper restrictions
3. Enable billing for the project

## 📱 App Flow

### User Journey
1. **Onboarding** → First-time user introduction
2. **Authentication** → Login/Signup or guest access
3. **Home** → Browse featured products
4. **Shopping** → Add products to cart
5. **Checkout** → Address selection and payment
6. **Order Completion** → Success confirmation

### Navigation Flow
```
Onboarding → Start → Login/Signup → Home
    ↓              ↓                ↓
    └──────────────┴────────────────┴──→ Cart → Checkout → Payment → Success
```

## 🎯 Key Implementations

### Shared Element Transitions
- Smooth animations between product lists and details
- Custom transition animations with FastOutSlowInEasing
- Proper back stack management

### Cart Management
- Real-time cart updates with badge notifications
- Draft order integration with Shopify
- Guest cart preservation

### Payment Integration
- Stripe WebView integration
- Secure checkout session creation
- Multiple payment method support

### Address Management
- Google Maps integration for location selection
- CRUD operations for user addresses
- Default address selection

## 🔒 Security Considerations

- API keys stored in local.properties (protected by .gitignore)
- HTTPS for all network communications
- Input validation and sanitization
- Secure payment processing with Stripe
- User authentication with Firebase

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### UI Tests
```bash
./gradlew connectedDebugAndroidTest
```

## 📈 Performance Optimizations

- **Image Loading** - Coil with caching and memory optimization
- **Lazy Loading** - LazyColumn for large lists
- **State Management** - Efficient recomposition
- **Network Caching** - Response caching with OkHttp
- **Memory Management** - Proper cleanup in ViewModels

## 🐛 Known Issues

- Payment flow may need URL configuration updates
- Some hardcoded example URLs in payment constants
- Network error handling could be improved

## 🔄 Future Enhancements

- **Push Notifications** - Order status updates
- **Product Search** - Advanced search functionality
- **Reviews & Ratings** - User-generated content
- **Wishlist Sharing** - Social features
- **Order Tracking** - Real-time tracking
- **Multi-currency Support** - International markets
- **AR Product Preview** - Augmented reality features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For support and inquiries:
- Create an issue in the GitHub repository
- Email: eng.ahmedkhaled.work@gmail.com

## 🙏 Acknowledgments

- **Shopify** - For the e-commerce platform API
- **Stripe** - For payment processing
- **Google** - For Maps and Firebase services
- **Jetpack Compose Team** - For the amazing UI toolkit
- **Android Community** - For continuous inspiration and support

---

**Built with ❤️ using modern Android development practices**

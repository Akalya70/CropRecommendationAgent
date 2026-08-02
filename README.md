# 🌱 Crop Recommendation Agent

An AI-powered web application that helps farmers identify the most suitable crop to cultivate based on soil and environmental conditions. The system provides intelligent crop recommendations using Google Gemini AI and a user-friendly interface.

## 🚀 Features

- 🔐 User Registration and Login
- 🌾 Crop Recommendation based on agricultural inputs
- 🤖 AI-powered recommendations using Google Gemini API
- 🌿 Soil and land analysis
- 🍃 Leaf analysis support
- 👨‍💼 Admin dashboard for managing users and recommendations
- 📱 Responsive and user-friendly interface

## 🛠️ Tech Stack

### Frontend
- HTML5
- CSS3
- JavaScript

### Backend
- Java
- Spring Boot
- Spring Data JPA

### Database
- MySQL
### AI Integration
- Google Gemini API

### Tools
- Maven
- Git
- GitHub
- IntelliJ IDEA
- Postman

## 📂 Project Structure

```
CropRecommendationAgent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
│   │   └── static/
├── pom.xml
├── README.md
└── .gitignore
```

## ⚙️ Installation

1. Clone the repository

```bash
git clone https://github.com/Akalya70/CropRecommendationAgent.git
```

2. Open the project in IntelliJ IDEA.

3. Configure MySQL database.

4. Update `application.properties`.

```properties
spring.datasource.url=YOUR_DATABASE_URL
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

gemini.api.key=YOUR_API_KEY
```

5. Run the project.

```bash
mvn spring-boot:run
```

6. Open your browser.

```
http://localhost:8080
```

## 📸 Screenshots

Add screenshots of:

- Home Page
- Login Page
- User Dashboard
- Crop Recommendation Page
- Admin Dashboard
- Leaf Analysis Page

## 🎯 Future Enhancements

- Weather API Integration
- Disease Detection using AI
- Multi-language Support
- Mobile Application
- Fertilizer Recommendation
- Market Price Prediction


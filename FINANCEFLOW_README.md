# FinanceFlow Backend API

**Personal Finance & Investment Tracker - REST API**

A secure, full-featured REST API for managing personal finances, tracking transactions, and analyzing spending patterns.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Getting Started](#getting-started)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Authentication Flow](#authentication-flow)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)

---

## 🎯 Overview

FinanceFlow Backend is a Spring Boot REST API that provides comprehensive personal finance management capabilities. It supports user authentication, account management, transaction tracking, and spending analytics with role-based access control and JWT security.

---

## 🛠 Technology Stack

### Core Technologies
- **Java 17** - Programming language
- **Spring Boot 3.3.5** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database ORM
- **PostgreSQL 16** - Relational database
- **JWT (JSON Web Tokens)** - Stateless authentication
- **Hibernate** - JPA implementation
- **Maven** - Dependency management

### Supporting Libraries
- **Lombok** - Reduce boilerplate code
- **BCrypt** - Password hashing
- **JJWT 0.12.6** - JWT token generation/validation
- **Jackson** - JSON serialization
- **Bean Validation** - Request validation

---

## ✨ Features

### Authentication & Security
- ✅ User registration with email verification
- ✅ JWT-based authentication (access tokens)
- ✅ Secure password hashing with BCrypt
- ✅ Email verification tokens with expiration
- ✅ Role-based access control (USER, ADMIN)
- ✅ Protected endpoints with JWT filter

### Account Management
- ✅ Multiple account types (Checking, Savings, Credit Card, Investment)
- ✅ Real-time balance tracking
- ✅ Multi-currency support
- ✅ Account CRUD operations
- ✅ User-specific account isolation

### Transaction Tracking
- ✅ Income and expense recording
- ✅ Category-based organization
- ✅ Automatic balance updates
- ✅ Date range filtering
- ✅ Transaction history
- ✅ Spending summaries

### Category System
- ✅ 14 pre-seeded system categories
- ✅ Custom user categories
- ✅ Income/Expense categorization
- ✅ Category icons and colors

### Analytics
- ✅ Total income/expense calculations
- ✅ Net balance tracking
- ✅ Spending by category breakdowns
- ✅ Transaction count statistics

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 16+
- Your favorite IDE (IntelliJ IDEA recommended)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/financeflow-backend.git
   cd financeflow-backend
   ```

2. **Create PostgreSQL database**
   ```sql
   CREATE DATABASE financeflow_db;
   ```

3. **Configure application properties**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/financeflow_db
   spring.datasource.username=your_postgres_username
   spring.datasource.password=your_postgres_password
   
   # JWT Configuration
   jwt.secret=your-super-secret-key-here-at-least-256-bits
   jwt.expiration=86400000
   ```

4. **Generate a secure JWT secret**
   ```bash
   openssl rand -base64 64
   ```
   Copy the output and paste it into `jwt.secret` in `application.properties`

5. **Build the project**
   ```bash
   mvn clean install
   ```

6. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The API will start on `http://localhost:8080`

### First Run

On first startup, the application will automatically:
- Create all database tables (via Hibernate DDL auto)
- Seed 14 system categories (via DataLoader)

---

## 🗄 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    country VARCHAR(100),
    timezone VARCHAR(50),
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    token_expires_at TIMESTAMP,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Accounts Table
```sql
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Categories Table
```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(10),
    color VARCHAR(7),
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Relationships
- User → Accounts (One-to-Many)
- User → Categories (One-to-Many, custom only)
- User → Transactions (One-to-Many)
- Account → Transactions (One-to-Many)
- Category → Transactions (One-to-Many)

---

## 📡 API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login user | No |
| GET | `/api/auth/verify?token=xyz` | Verify email | No |
| POST | `/api/auth/resend-verification` | Resend verification email | No |

**Register Request:**
```json
{
  "email": "john@example.com",
  "username": "johndoe",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "country": "USA",
  "timezone": "America/New_York"
}
```

**Login Request:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john@example.com",
  "username": "johndoe",
  "fullName": "John Doe"
}
```

---

### User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/me` | Get current user profile | Yes |
| PUT | `/api/users/me` | Update user profile | Yes |
| DELETE | `/api/users/me` | Delete user account | Yes |

---

### Account Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/accounts` | Get all user accounts | Yes |
| POST | `/api/accounts` | Create new account | Yes |
| GET | `/api/accounts/{id}` | Get account by ID | Yes |
| PUT | `/api/accounts/{id}` | Update account | Yes |
| DELETE | `/api/accounts/{id}` | Delete account | Yes |
| GET | `/api/accounts/{id}/balance` | Get account balance | Yes |

**Create Account Request:**
```json
{
  "name": "Chase Checking",
  "type": "CHECKING",
  "balance": 5000.00,
  "currency": "USD",
  "description": "Primary checking account"
}
```

**Account Types:** `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `INVESTMENT`

---

### Category Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/categories` | Get all categories | Yes |
| GET | `/api/categories?type=EXPENSE` | Filter by type | Yes |
| GET | `/api/categories/{id}` | Get category by ID | Yes |
| POST | `/api/categories` | Create custom category | Yes |
| PUT | `/api/categories/{id}` | Update custom category | Yes |
| DELETE | `/api/categories/{id}` | Delete custom category | Yes |

**Create Category Request:**
```json
{
  "name": "Crypto Investing",
  "type": "EXPENSE",
  "icon": "₿",
  "color": "#F7931A"
}
```

**Pre-seeded Categories:**
- **Expenses:** Groceries, Dining Out, Transportation, Entertainment, Shopping, Bills & Utilities, Healthcare, Education, Travel, Other Expenses
- **Income:** Salary, Freelance, Investments, Other Income

---

### Transaction Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/transactions` | Get all transactions | Yes |
| GET | `/api/transactions?startDate=2024-01-01&endDate=2024-12-31` | Filter by date range | Yes |
| GET | `/api/transactions/{id}` | Get transaction by ID | Yes |
| POST | `/api/transactions` | Create transaction | Yes |
| PUT | `/api/transactions/{id}` | Update transaction | Yes |
| DELETE | `/api/transactions/{id}` | Delete transaction | Yes |
| GET | `/api/transactions/summary` | Get income/expense summary | Yes |
| GET | `/api/transactions/by-category` | Get spending by category | Yes |

**Create Transaction Request:**
```json
{
  "accountId": 1,
  "categoryId": 1,
  "amount": 50.00,
  "type": "DEBIT",
  "description": "Grocery shopping at Whole Foods",
  "transactionDate": "2024-12-15"
}
```

**Transaction Types:**
- `DEBIT` - Expense (money out)
- `CREDIT` - Income (money in)

**Transaction Summary Response:**
```json
{
  "totalIncome": 5000.00,
  "totalExpenses": 2500.00,
  "netBalance": 2500.00,
  "transactionCount": 45
}
```

**Spending by Category Response:**
```json
[
  {
    "categoryId": 1,
    "categoryName": "Groceries",
    "categoryIcon": "🛒",
    "totalAmount": 450.00,
    "transactionCount": 12
  },
  ...
]
```

---

## 🔐 Authentication Flow

### 1. Registration Flow
```
User → POST /api/auth/register
  ↓
Backend: Create user (emailVerified = false)
  ↓
Backend: Generate verification token (expires in 24h)
  ↓
Backend: Save user to database
  ↓
Response: "Check your email to verify account"
```

### 2. Email Verification Flow
```
User clicks link: GET /api/auth/verify?token=xyz
  ↓
Backend: Find user by token
  ↓
Backend: Check if token expired
  ↓
Backend: Set emailVerified = true
  ↓
Backend: Clear token (one-time use)
  ↓
Response: "Email verified! You can now login"
```

### 3. Login Flow
```
User → POST /api/auth/login
  ↓
Spring Security: Authenticate credentials
  ↓
Spring Security: Check if emailVerified = true
  ↓
Backend: Generate JWT token
  ↓
Response: { token, user info }
```

### 4. Authenticated Request Flow
```
Client → Request with header: Authorization: Bearer {token}
  ↓
JwtAuthenticationFilter: Extract token
  ↓
JwtAuthenticationFilter: Validate token (signature + expiration)
  ↓
JwtAuthenticationFilter: Load user from database
  ↓
JwtAuthenticationFilter: Set user in SecurityContext
  ↓
Controller: Access user via @AuthenticationPrincipal
```

---

## 📁 Project Structure

```
src/main/java/com/tiltedhat/financeflow_backend/
├── config/
│   ├── SecurityConfig.java           # Spring Security configuration
│   └── DataLoader.java               # Seeds system categories
├── controller/
│   ├── AuthController.java           # Authentication endpoints
│   ├── AccountController.java        # Account CRUD endpoints
│   ├── CategoryController.java       # Category CRUD endpoints
│   └── TransactionController.java    # Transaction CRUD endpoints
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CreateAccountRequest.java
│   │   ├── CreateCategoryRequest.java
│   │   └── CreateTransactionRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── AccountResponse.java
│       ├── CategoryResponse.java
│       ├── TransactionResponse.java
│       └── MessageResponse.java
├── entity/
│   ├── User.java                     # User entity (implements UserDetails)
│   ├── Account.java                  # Account entity
│   ├── Category.java                 # Category entity
│   ├── Transaction.java              # Transaction entity
│   ├── Role.java                     # User role enum
│   ├── AccountType.java              # Account type enum
│   ├── CategoryType.java             # Category type enum
│   └── TransactionType.java          # Transaction type enum
├── exception/
│   └── GlobalExceptionHandler.java   # Centralized error handling
├── mapper/
│   ├── UserMapper.java               # DTO ↔ Entity conversions
│   ├── AccountMapper.java
│   ├── CategoryMapper.java
│   └── TransactionMapper.java
├── repository/
│   ├── UserRepository.java           # User data access
│   ├── AccountRepository.java        # Account data access
│   ├── CategoryRepository.java       # Category data access
│   └── TransactionRepository.java    # Transaction data access
├── security/
│   ├── JwtUtil.java                  # JWT generation/validation
│   ├── JwtAuthenticationFilter.java  # JWT request filter
│   └── CustomUserDetailsService.java # Load users for Spring Security
└── service/
    ├── AuthService.java              # Authentication business logic
    ├── AccountService.java           # Account business logic
    ├── CategoryService.java          # Category business logic
    └── TransactionService.java       # Transaction business logic
```

---

## 🧪 Testing

### Manual Testing with Postman/cURL

1. **Register a user:**
   ```bash
   POST http://localhost:8080/api/auth/register
   Content-Type: application/json
   
   {
     "email": "test@example.com",
     "username": "testuser",
     "password": "password123",
     "firstName": "Test",
     "lastName": "User"
   }
   ```

2. **Verify email** (get token from database):
   ```bash
   GET http://localhost:8080/api/auth/verify?token=YOUR_TOKEN
   ```

3. **Login:**
   ```bash
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json
   
   {
     "email": "test@example.com",
     "password": "password123"
   }
   ```

4. **Use JWT token for authenticated requests:**
   ```bash
   GET http://localhost:8080/api/accounts
   Authorization: Bearer YOUR_JWT_TOKEN
   ```

### Unit Testing (Future)

```bash
mvn test
```

---

## 🔒 Security Features

### Implemented
- ✅ BCrypt password hashing (cost factor 10)
- ✅ JWT token-based authentication
- ✅ Stateless sessions (no cookies)
- ✅ Email verification required for login
- ✅ Token expiration (24 hours default)
- ✅ User data isolation (users can only access their own data)
- ✅ Protected endpoints (JWT required)
- ✅ Input validation on all requests
- ✅ SQL injection prevention (JPA/Hibernate)

### Security Best Practices
- All passwords are hashed (never stored in plain text)
- JWT tokens are signed with HS256
- Database queries use parameterized queries
- User input is validated before processing
- CORS can be configured as needed
- Rate limiting can be added (future)

---

## 🚧 Future Enhancements

### Planned Features
- [ ] Budget tracking module
- [ ] Investment portfolio tracking
- [ ] Recurring transactions
- [ ] Email service integration (SendGrid/AWS SES)
- [ ] Refresh token support
- [ ] Password reset functionality
- [ ] Two-factor authentication (2FA)
- [ ] Account sharing/permissions
- [ ] Export data (CSV, PDF)
- [ ] Databricks analytics integration
- [ ] Scheduled reports
- [ ] Bill reminders
- [ ] Financial goals tracking
- [ ] Mobile app support

### Technical Improvements
- [ ] Pagination for large datasets
- [ ] Advanced filtering and sorting
- [ ] API rate limiting
- [ ] Comprehensive unit tests
- [ ] Integration tests
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Docker containerization
- [ ] CI/CD pipeline
- [ ] Logging and monitoring
- [ ] Performance optimization

---

## 📝 Notes

### Email Verification
Currently, email verification tokens are generated but emails are not sent (email service not implemented). For development:
- Manually set `emailVerified = true` in database, OR
- Get verification token from database and call `/api/auth/verify?token=xyz`

### Database Migrations
Currently using Hibernate DDL auto (`spring.jpa.hibernate.ddl-auto=update`). For production, consider:
- Flyway or Liquibase for version-controlled migrations
- Setting DDL auto to `validate` or `none`

### JWT Secret
The JWT secret in `application.properties` should be:
- At least 256 bits (32 characters)
- Randomly generated
- Kept secure (use environment variables in production)
- Never committed to version control

---

## 🤝 Contributing

This is a portfolio project, but suggestions and improvements are welcome!

---

## 📄 License

This project is for educational and portfolio purposes.

---

## 👤 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your Profile](https://linkedin.com/in/yourprofile)

---

## 🙏 Acknowledgments

- Spring Boot community
- JWT.io for JWT debugging
- PostgreSQL team
- All open-source contributors

---

**Built with ☕ and Java**

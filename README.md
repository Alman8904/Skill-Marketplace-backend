# Skill Marketplace — Backend API

A **reputation-based freelancing platform** REST API built with Spring Boot. Consumers hire providers for specific skills, payments are held in escrow until work is approved, and every user builds a public trust profile automatically from their order history.

**Live API Docs:** https://skill-marketplace.onrender.com/swagger-ui/index.html

**Live Frontend Link:** https://skill-marketplace-frontend.onrender.com

**Frontend Repo:** https://github.com/Alman8904/Skill-marketplace-frontend.git

> First request may take ~30 seconds to wake up — hosted on free tier.

---

## How It Works

1. **Register** as a `CONSUMER`, `PROVIDER`, or `ADMIN` (admin is auto-created — not user-selectable)
2. **Providers** list skills from the admin-managed catalog, setting hourly rate, experience, and service mode (`REMOTE`/`LOCAL`)
3. **Consumers** search providers by skill name with optional filters (rate range, service mode, experience)
4. **Consumer places an order** → specifies estimated hours → `agreedPrice` is auto-calculated as `rate × hours`
5. **Consumer authorizes payment** → funds are deducted from wallet and held in escrow
6. **Provider accepts** the order (only possible after payment is authorized) and sets a deadline
7. **Provider starts work** → status moves to `IN_PROGRESS`
8. **Provider delivers** → submits delivery notes and a URL
9. **Consumer approves** → funds released to provider wallet atomically → order is `COMPLETED`
10. **Trust scores** are computed automatically from order history — no manual ratings needed

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| Security | Spring Security + JWT (JJWT 0.13) |
| Database | PostgreSQL |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI / Swagger UI 2.5 |
| Build | Maven (Maven Wrapper included) |
| Containerization | Docker (multi-stage build) |
| CI/CD | GitHub Actions + Render |
| Utilities | Lombok |
| Testing | JUnit 5, Mockito |
| Scheduling | Spring `@Scheduled` |

---

## Key Features

### Escrow Payment System
Money moves in three steps only — authorize → capture → (or) refund. Funds are locked between authorization and delivery approval, protecting both parties.

### Abuse Protections
- `deliveryUrl` hidden from consumer until order is `COMPLETED` — prevents taking work without paying
- Refund blocked once provider has accepted — consumer cannot pull funds mid-job
- Provider acceptance blocked until payment is authorized
- Delivery URL validated with `@Pattern` — must start with `http://` or `https://`, cannot be blank

### Automated Expiry Scheduler
`OrderExpiryScheduler` runs every hour and handles two stuck-money scenarios:
- **Provider missed deadline** — order is `ACCEPTED` or `IN_PROGRESS` and deadline has passed → auto-refund consumer, cancel order
- **Consumer ghosted after delivery** — order has been `DELIVERED` for more than 3 days → auto-capture payment to provider, complete order

### Trust & Reputation System
Scores calculated automatically from order history — no star ratings needed.

| Badge | Criteria |
|---|---|
| `NEW` | No order history yet |
| `TRUSTED` | Completion rate ≥ 80% and zero refunds |
| `NEUTRAL` | Completion rate ≥ 50% |
| `RISKY` | Completion rate < 50% or significant refunds |

---

## Architecture Overview

```
src/main/java/com/Skill/Marketplace/SM/
│
├── Controllers/          ← REST layer
│   ├── AuthController         /auth/**           (public)
│   ├── UserController         /public/user/**
│   ├── CategoryController     /admin/category/**
│   ├── SkillController        /admin/skills/**   (paginated)
│   ├── UserSkillController    /user-skills/**
│   ├── OrderController        /orders/**
│   ├── PaymentController      /payment/**
│   ├── TrustController        /trust/**
│   └── HealthController       /
│
├── Services/             ← Business logic
│   ├── AuthService            JWT login
│   ├── UserService            Profile CRUD
│   ├── CategoryService        Category management
│   ├── SkillService           Skill management
│   ├── UserSkillService       Provider listings + filtered search
│   ├── OrderService           Full order lifecycle
│   ├── MockPaymentService     Wallet + escrow simulation
│   ├── OrderExpiryScheduler   Hourly auto-refund + auto-capture
│   └── TrustService           Reputation score calculation
│
├── Entities/             ← JPA entities + enums
│   ├── UserModel              id, username, firstName, lastName, password, userType, walletBalance
│   ├── Category               categoryId, categoryName
│   ├── Skill                  id, skillName, category
│   ├── UserSkill              provider listing with rate, experience, isActive, serviceMode
│   ├── Order                  full order with payment + delivery tracking
│   ├── OrderStatus            PENDING, ACCEPTED, IN_PROGRESS, DELIVERED, COMPLETED, CANCELLED
│   ├── PaymentStatus          PENDING, AUTHORIZED, CAPTURED, REFUNDED, FAILED
│   ├── ServiceMode            REMOTE, LOCAL
│   └── UserType               CONSUMER, PROVIDER, ADMIN
│
├── DTO/                  ← Request/Response objects (per domain)
├── Repo/                 ← Spring Data JPA repositories
├── Exception/            ← Custom exceptions + GlobalExceptionHandler
└── Security/
    ├── SecurityConfig         Stateless JWT filter chain
    ├── CorsConfig             CORS allowlist
    ├── JWTAuthFilter          Extracts + validates JWT per request
    ├── JWTUtil                Token generation and parsing
    ├── DataInitializer        Auto-creates admin on first startup
    └── OpenApiConfig          Swagger JWT auth button
```

---

## Database Schema

Flyway runs all migrations automatically on startup.

```
category (1) ──────────────────────────────> skill (N)
                                               │
user_model (1) ─────────────────────────────> user_skill (N)

user_model ──────────> orders <──────────── user_model
  (as consumer, 1:N)            (as provider, 1:N)

orders (N) ──────────────────────────────── skill (1)
```

**Tables:**

| Table | Key Columns |
|---|---|
| `user_model` | id, username, password, user_type, wallet_balance |
| `category` | category_id, category_name |
| `skill` | id, skill_name, category_id |
| `user_skill` | user_skill_id, user_id, skill_id, rate, experience, is_active, service_mode, description |
| `orders` | order_id, consumer_id, provider_id, skill_id, agreed_price, estimated_hours, deadline, status, mock_payment_id, mock_payment_status, delivery_url, delivery_notes, + timestamps |

**Migrations:**

| Migration | What It Does |
|---|---|
| `V1__initial_schema.sql` | All core tables, constraints, and indexes |
| `V2__add_wallet.sql` | Adds `wallet_balance` to users, `estimated_hours` + `deadline` to orders |
| `V3__add_delivery_fields.sql` | Adds delivery tracking + mock payment fields to orders |
| `V4__seed_test_data.sql` | Seeds categories, skills, and test users for development |

---

## API Reference

All protected endpoints require: `Authorization: Bearer <your_jwt_token>`

Full interactive docs: https://skill-marketplace.onrender.com/swagger-ui/index.html

---

### Auth — `/auth` *(public)*

| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/create` | Register a new user |
| `POST` | `/auth/login` | Login — returns JWT as plain string |

**Register body:**
```json
{
  "username": "john_doe",
  "password": "securePassword",
  "firstName": "John",
  "lastName": "Doe",
  "userType": "CONSUMER"
}
```

---

### User — `/public/user`

| Method | Path | Description |
|---|---|---|
| `GET` | `/public/user/profile` | Get your own profile |
| `PUT` | `/public/user/update` | Update firstName, lastName, etc. |
| `DELETE` | `/public/user/delete` | Delete your account |

---

### Categories — `/admin/category`

| Method | Path | Role | Description |
|---|---|---|---|
| `GET` | `/admin/category` | Any | List all categories |
| `GET` | `/admin/category/{id}` | Any | Get category by ID |
| `POST` | `/admin/category` | `ADMIN` | Create a category |
| `PUT` | `/admin/category/{id}` | `ADMIN` | Update a category |
| `DELETE` | `/admin/category/{id}` | `ADMIN` | Delete a category |

---

### Skills — `/admin/skills`

Skills support pagination: `?page=0&size=10&sort=skillName`

| Method | Path | Role | Description |
|---|---|---|---|
| `GET` | `/admin/skills` | Any | List all skills (paginated) |
| `GET` | `/admin/skills/{id}` | Any | Get skill by ID |
| `POST` | `/admin/skills` | `ADMIN` | Create a skill |
| `PUT` | `/admin/skills/{id}` | `ADMIN` | Update a skill |
| `DELETE` | `/admin/skills/{id}` | `ADMIN` | Delete a skill |

---

### Provider Listings — `/user-skills`

| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/user-skills/assign` | `PROVIDER` | List one or more skills (batch) |
| `GET` | `/user-skills/all-userSkills` | `PROVIDER` | View your active skill listings |
| `PUT` | `/user-skills/update/{userSkillId}` | `PROVIDER` | Update rate, experience, description, serviceMode |
| `DELETE` | `/user-skills/deactivate/{userSkillId}` | `PROVIDER` | Soft-deactivate a listing |
| `GET` | `/user-skills/search` | `CONSUMER` or `PROVIDER` | Search and filter providers |

**Search query parameters:**

| Param | Type | Required | Description |
|---|---|---|---|
| `skill` | string | ✅ | Skill name (partial match) |
| `minRate` | double | ❌ | Minimum hourly rate in ₹ |
| `maxRate` | double | ❌ | Maximum hourly rate in ₹ |
| `serviceMode` | `REMOTE` / `LOCAL` | ❌ | Filter by service mode |
| `minExperience` | int | ❌ | Minimum years of experience |
| `page`, `size`, `sort` | Pageable | ❌ | Default: size=10, sort=rate |

---

### Orders — `/orders`

| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/orders/place` | `CONSUMER` / `PROVIDER` | Place an order |
| `POST` | `/orders/accept?orderId=&deadline=` | `PROVIDER` | Accept order + set deadline (ISO datetime) |
| `POST` | `/orders/cancel?orderId=` | `CONSUMER` | Cancel PENDING order (auto-refunds if payment was authorized) |
| `POST` | `/orders/start-work?orderId=` | `PROVIDER` | Move to IN_PROGRESS |
| `POST` | `/orders/deliver-work` | `PROVIDER` | Submit delivery notes + URL |
| `POST` | `/orders/approve-delivery?orderId=` | `CONSUMER` | Approve delivery → releases payment atomically |
| `GET` | `/orders/my-orders` | `CONSUMER` | Orders you placed (deliveryUrl hidden until COMPLETED) |
| `GET` | `/orders/received-orders` | `PROVIDER` | Orders assigned to you |

---

### Payments — `/payment`

| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/payment/add-funds` | Any | Add funds to your wallet (max ₹1,00,000 per transaction) |
| `GET` | `/payment/wallet-balance` | Any | Check your wallet balance |
| `POST` | `/payment/authorize` | `CONSUMER` | Authorize payment for an order (holds funds in escrow) |
| `POST` | `/payment/refund?orderId=` | `CONSUMER` | Refund — only allowed while order is still PENDING |

**Authorize body:**
```json
{
  "orderId": 5,
  "amount": 150.00
}
```

---

### Trust — `/trust`

| Method | Path | Description |
|---|---|---|
| `GET` | `/trust/provider/{username}` | Public trust score for a provider |
| `GET` | `/trust/consumer/{username}` | Public trust score for a consumer |
| `GET` | `/trust/me` | Your full private trust breakdown |

---

### Health — `/`

```json
{
  "app": "Skill Marketplace API",
  "status": "UP",
  "timestamp": "2026-02-19T10:00:00",
  "docs": "/swagger-ui.html"
}
```

---

## Payment Flow

```
Consumer adds funds to wallet
         │
         ▼
Consumer authorizes payment for order
  ├── Funds deducted from consumer wallet
  ├── Held in escrow
  └── Payment ID generated: PAY_XXXXXXXX
         │
         ▼
Provider accepts order (blocked until payment = AUTHORIZED)
         │
         ▼
Provider delivers → Consumer approves
  ├── Funds added to provider wallet (atomically in same @Transactional)
  └── mockPaymentStatus = CAPTURED

  ─── OR ───

Consumer cancels (while PENDING only)
  ├── Funds returned to consumer wallet
  └── mockPaymentStatus = REFUNDED
```

---

## Order Lifecycle

```
[Consumer places order]
         │
         ▼
      PENDING ──► Consumer can cancel here (auto-refunds if paid)
         │
[Consumer authorizes payment]
         │
[Provider accepts + sets deadline]
         │
         ▼
      ACCEPTED ──► Auto-cancelled if provider misses deadline (consumer refunded)
         │
[Provider starts work]
         │
         ▼
    IN_PROGRESS ──► Auto-cancelled if deadline passes (consumer refunded)
         │
[Provider delivers (notes + URL)]
         │
         ▼
     DELIVERED ──► Auto-completed after 3 days if consumer doesn't approve (provider paid)
         │
[Consumer approves delivery]
         │
         ▼
     COMPLETED ──► Provider wallet credited
```

---

## Security & Roles

Authentication is stateless JWT. Login returns a plain JWT string. Send it as `Authorization: Bearer <token>` on all protected endpoints.

| Role | Permissions |
|---|---|
| `CONSUMER` | Place orders, authorize/refund payments (PENDING only), cancel pending orders, approve deliveries, search providers, view trust scores |
| `PROVIDER` | List skills, accept/start/deliver orders, view received orders |
| `ADMIN` | Create/update/delete categories and skills. Auto-created on startup via `DataInitializer`. |

**Exception handling** is centralized in `GlobalExceptionHandler`:

| Exception | Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `BadRequestException` | 400 |
| `ForbiddenException` | 403 |
| `ConflictException` | 409 |
| `UnauthorizedException` | 401 |
| `MethodArgumentNotValidException` | 400 (with field-level error map) |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use `./mvnw`)
- PostgreSQL 14+

### 1. Create the database

```sql
CREATE DATABASE skill_marketplace;
```

### 2. Set environment variables

```bash
export DB_URL=jdbc:postgresql://localhost:5432/skill_marketplace
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_64_char_random_secret_here
export JWT_EXPIRATION=3600000
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=yourAdminPassword123
```

### 3. Run

```bash
./mvnw spring-boot:run
```

Flyway auto-runs all migrations. The admin user is created automatically on first boot.

| URL | Description |
|---|---|
| `http://localhost:8080` | API root / health check |
| `http://localhost:8080/swagger-ui.html` | Interactive API docs |

---

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_URL` | ✅ | — | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | ✅ | — | Database username |
| `DB_PASSWORD` | ✅ | — | Database password |
| `JWT_SECRET` | ✅ | — | HMAC secret key for JWT signing (use 64+ chars) |
| `JWT_EXPIRATION` | ❌ | `3600000` | Token TTL in milliseconds (default: 1 hour) |
| `ADMIN_USERNAME` | ✅ | — | Admin account username (auto-created on startup) |
| `ADMIN_PASSWORD` | ✅ | — | Admin account password |

Generate a strong JWT secret:
```bash
openssl rand -base64 64
```

> ⚠️ Never hardcode or commit real secrets to version control.

---

## Running with Docker

```bash
# Build
docker build -t skill-marketplace-api .

# Run
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/skill_marketplace \
  -e DB_USERNAME=your_user \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=admin123 \
  skill-marketplace-api
```

---

## Running Tests

```bash
./mvnw test
```

- `AuthServiceTest` — login flow with mocked JWT and UserDetailsService
- `UserServiceTest` — user CRUD operations

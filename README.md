# Ticket Scan Backend

A Spring Boot REST API backend for managing event ticket bookings, scanning, and verification. Built for "The Notebook Concert" ticket management system with support for PDF ticket generation, QR code verification, and Google Sheets integration.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Running the Application](#running-the-application)
- [Building & Deployment](#building--deployment)
- [Architecture](#architecture)
- [Dependencies](#dependencies)

## Features

✅ **Ticket Management**
- Create and manage event bookings
- Generate PDF tickets with QR codes
- Scan and verify tickets at the event
- Approve/reject ticket submissions
- Real-time ticket status tracking

✅ **Security**
- JWT token-based authentication for admin operations
- Password-protected admin login with BCrypt encryption
- Bearer token validation for premium bookings

✅ **Integration**
- Google Sheets API integration for data storage and retrieval
- Cloudinary integration for image hosting

✅ **Performance**
- Caffeine-based caching for optimized performance
- Cache invalidation on ticket updates
- Batch processing capabilities

✅ **Additional Features**
- QR code generation using ZXing library
- PDF ticket generation with iText
- Comprehensive error logging
- Health check endpoints via Spring Actuator

## Tech Stack

### Backend Framework
- **Spring Boot 4.0.6** - REST API framework
- **Spring Web** - REST controller support
- **Spring Security** - Cryptographic utilities
- **Spring Cache** - In-memory caching

### Data & APIs
- **Google Sheets API** - Data storage and synchronization
- **Cloudinary** - Image hosting and management

### Libraries & Utilities
- **Lombok** - Reduce boilerplate code
- **JWT (jjwt)** - Token-based authentication
- **ZXing** - QR code generation
- **iText PDF** - PDF document generation
- **Caffeine** - High-performance caching

### Build & Runtime
- **Maven** - Dependency management and build
- **Java 21** - Runtime environment
- **Spring Boot DevTools** - Development convenience

### Testing
- **JUnit** - Unit testing framework

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **Google Sheets API credentials** (JSON key file)
- **Cloudinary account** (for image hosting)
- **Git** (optional, for version control)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd demo
```

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Set Up Environment Variables

Create a `.env` file or configure your system environment variables:

```bash
# Google Sheets Configuration
export GOOGLE_CREDENTIALS=base64_encoded_credentials

# Cloudinary Configuration
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret

# Booking Configuration
export RESERVATION_EXPIRY_MINUTES=10
export TICKET_MAX_LIMIT=80
```

**Environment Variables Reference:**
- `GOOGLE_CREDENTIALS` - Base64 encoded Google Sheets API credentials
- `CLOUDINARY_CLOUD_NAME` - Cloudinary cloud name for image uploads
- `CLOUDINARY_API_KEY` - Cloudinary API key
- `CLOUDINARY_API_SECRET` - Cloudinary API secret
- `RESERVATION_EXPIRY_MINUTES` - Duration (in minutes) before a reservation expires (default: 10)
- `TICKET_MAX_LIMIT` - Maximum number of tickets allowed per booking (default: 80)

## Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Admin Authentication (BCrypt hashed password)
admin.password=$2a$12$CcQSWQEu9WnKpoQzJCHXpuk6uxRMCHt3ey4FqidGGtk1NjVgc2hs2

# Booking & Reservation Configuration
reservation.expiry.minutes=${RESERVATION_EXPIRY_MINUTES:10}
ticket.max.limit=${TICKET_MAX_LIMIT:80}

# Logging Configuration
logging.level.root=INFO
logging.level.com.example.demo=DEBUG

# Actuator (Health checks)
management.endpoint.health.show-details=always

# Cloudinary Configuration
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}

# Application Name
spring.application.name=demo
```

### Security

⚠️ **Warning**: The `application.properties` file contains sensitive credentials. 

**Best Practices:**
- Use environment variables for production deployments
- Never commit credentials to version control
- Use a `.gitignore` file to exclude sensitive files
- Store secrets in a secure vault (AWS Secrets Manager, HashiCorp Vault, etc.)

## API Endpoints

### Admin Authentication

#### Login
```http
POST /api/admin/login
Content-Type: application/json

{
  "password": "your-password"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY4NzI3NzY0MCwiZXhwIjoxNjg3Mjc4MjQwfQ.xxx"
}
```

### Ticket Management

#### Scan a Ticket
```http
POST /api/scan-ticket
Content-Type: application/json

{
  "uuid": "ticket-uuid-123"
}

Response:
{
  "status": "VALID|INVALID|ALREADY_SCANNED",
  "message": "Ticket scanned successfully",
  "ticketId": "ticket-uuid-123"
}
```

#### Get All Pending Tickets
```http
GET /api/pending-tickets

Response:
[
  {
    "uuid": "ticket-123",
    "eventName": "The Notebook Concert",
    "email": "user@example.com",
    "status": "PENDING",
    "createdAt": "2024-05-09T10:30:00"
  }
]
```

#### Approve a Ticket
```http
POST /api/approve-ticket
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "uuid": "ticket-uuid-123"
}

Response:
"SUCCESS"
```

#### Reject a Ticket
```http
POST /api/reject-ticket
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "uuid": "ticket-uuid-123"
}

Response:
"REJECTED"
```

#### Get All Tickets
```http
GET /api/all-tickets

Response:
[
  {
    "uuid": "ticket-123",
    "eventName": "The Notebook Concert",
    "attendeeName": "John Doe",
    "email": "john@example.com",
    "status": "APPROVED",
    "createdAt": "2024-05-09T10:30:00"
  }
]
```

#### Approve a Booking
```http
POST /api/approve-booking
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "bookingId": "BOOK-123456"
}

Response:
"BOOKING_APPROVED"
```

#### Reject a Booking
```http
POST /api/reject-booking
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "bookingId": "BOOK-123456"
}

Response:
"BOOKING_REJECTED"
```

### Reservation Management

#### Create a Booking Reservation
```http
POST /api/bookings/reserve
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "ticketCount": 2,
  "totalAmount": 150.00,
  "paymentType": "PAID|FREE"
}

Response:
{
  "reservationId": "RES-123456",
  "status": "RESERVED",
  "totalAmount": 150.00,
  "createdAt": "2024-05-09T10:30:00"
}
```

#### Update a Booking Reservation
```http
PUT /api/bookings/reserve/{reservationId}
Content-Type: application/json

{
  "name": "John Doe Updated",
  "email": "john.updated@example.com",
  "phone": "+1234567890",
  "ticketCount": 2,
  "totalAmount": 150.00,
  "paymentType": "PAID|FREE"
}

Response:
{
  "reservationId": "RES-123456",
  "status": "UPDATED",
  "totalAmount": 150.00,
  "updatedAt": "2024-05-09T11:30:00"
}
```

#### Confirm a Booking Reservation
```http
POST /api/bookings/confirm/{reservationId}
Content-Type: application/json
Authorization: Bearer <jwt-token> (required for FREE payment type)

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "utr": "transaction-id-123",
  "ticketCount": 2,
  "totalAmount": 150.00,
  "paymentType": "PAID|FREE"
}

Response:
{
  "bookingId": "BOOK-123456",
  "status": "CONFIRMED",
  "totalAmount": 150.00,
  "confirmedAt": "2024-05-09T10:30:00"
}
```

#### Download Ticket PDF
```http
GET /api/bookings/download/{bookingId}

Response: PDF file (application/pdf)
Filename: The_Notebook_Concert_Ticket_{bookingId}.pdf
```

### Health Check

#### Application Health
```http
GET /actuator/health

Response:
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

## Running the Application

### Development Mode

```bash
# Using Maven
mvn spring-boot:run

# Or compile and run the JAR
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Production Mode

```bash
# Build the application
mvn clean package

# Run with production properties (if available)
java -jar target/demo-0.0.1-SNAPSHOT.jar \
  --server.port=8080 \
  --spring.profiles.active=production
```

### Using Docker

Build the Docker image:
```bash
docker build -t ticket-scan-backend .
```

Run the container:
```bash
docker run -d \
  -p 8080:8080 \
  -e GOOGLE_CREDENTIALS=base64_encoded_credentials \
  -e CLOUDINARY_CLOUD_NAME=your_cloud_name \
  -e CLOUDINARY_API_KEY=your_api_key \
  -e CLOUDINARY_API_SECRET=your_api_secret \
  --name ticket-backend \
  ticket-scan-backend
```

## Building & Deployment

### Building the Project

```bash
# Clean and build
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# Build with specific Java version
mvn clean package -Djava.version=21
```

### Output Artifacts

- `target/demo-0.0.1-SNAPSHOT.jar` - Executable JAR
- `target/demo-0.0.1-SNAPSHOT.jar.original` - Original JAR (dependencies)

### Docker Deployment

See the included `Dockerfile` for containerization. The image includes:
- Java 21 runtime
- Spring Boot application
- All required dependencies

## Architecture

### Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Main Spring Boot application entry point
├── config/                        # Configuration classes
│   ├── CacheConfig.java          # Caffeine cache configuration
│   ├── CloudinaryConfig.java     # Cloudinary API setup
│   ├── GoogleSheetsConfig.java   # Google Sheets API setup
│   └── WebConfig.java            # CORS and web configuration
├── controller/                    # REST API endpoints
│   ├── AdminController.java      # Admin authentication
│   ├── BookingController.java    # Booking operations
│   └── TicketController.java     # Ticket management
├── model/                         # Data models (DTOs)
│   ├── BookingRequest.java
│   ├── TicketRequest.java
│   ├── TicketResponse.java
│   ├── TicketDTO.java
│   ├── NewTicket.java
│   └── TicketRequest.java
├── service/                       # Business logic
│   ├── BookingService.java       # Booking operations
│   ├── TicketService.java        # Ticket management
│   ├── TicketCacheService.java   # Cache management
│   ├── EmailService.java         # Email notifications
│   ├── PdfService.java           # PDF generation
│   ├── QRService.java            # QR code generation
│   ├── CloudinaryService.java    # Image hosting
│   └── GoogleSheetService.java   # Google Sheets integration
└── utilities/                     # Helper utilities
    ├── JwtUtil.java              # JWT token operations
    └── ...
```

### Data Flow

```
Client Request
     ↓
  Controller
     ↓
  Service (Business Logic)
     ↓
  Cache/Persistence Layer
     ├── Caffeine Cache
     ├── Google Sheets
     └── Cloudinary (Images)
     ↓
  Response
```

### Key Services

- **TicketService** - Core ticket scanning and verification logic
- **BookingService** - Handles ticket booking creation and management
- **PdfService** - Generates PDF tickets with QR codes
- **GoogleSheetService** - Syncs data with Google Sheets
- **EmailService** - Sends notifications and confirmations
- **CloudinaryService** - Manages event images and logos

## Dependencies

### Core Spring Boot Dependencies
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-cache` - Caching support
- `spring-boot-starter-actuator` - Health monitoring
- `spring-boot-devtools` - Development tools
- ⚠️ `spring-boot-starter-mail` - Email support (currently disabled in pom.xml)

### Google Integration
- `google-api-services-sheets:v4-rev20230815-2.0.0` - Google Sheets API
- `google-auth-library-oauth2-http:1.19.0` - Authentication
- `google-http-client-gson:1.43.3` - HTTP client

### Authentication & Security
- `jjwt-api:0.11.5` - JWT token generation/validation
- `jjwt-impl:0.11.5` - JWT implementation
- `jjwt-jackson:0.11.5` - Jackson integration
- `spring-security-crypto` - Password encryption (BCrypt)

### Utilities
- `lombok` - Boilerplate reduction
- `caffeine` - High-performance caching
- `com.google.zxing:core:3.5.4` - QR code generation
- `com.itextpdf:itextpdf:5.5.13.3` - PDF generation
- `com.cloudinary:cloudinary-http44:1.39.0` - Cloudinary integration

For the complete list, see `pom.xml`.

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DemoApplicationTests

# Run with coverage
mvn test jacoco:report
```

### Building Locally

```bash
# Full build with tests
mvn clean install

# Quick build (skip tests)
mvn clean install -DskipTests

# Compile only
mvn compile
```

### IDE Setup

This project is compatible with:
- **IntelliJ IDEA** (Recommended)
- **Eclipse IDE**
- **Visual Studio Code** (with Spring Boot extension)
- **NetBeans**

## Troubleshooting

### Common Issues

**Issue: Google Sheets API Authorization Error**
- Ensure `credentials.json` is in `src/main/resources/`
- Verify the service account has edit access to the target Google Sheet
- Check that API is enabled in Google Cloud Console

**Issue: Cloudinary Upload Errors**
- Verify `CLOUDINARY_*` environment variables are set
- Check API key and secret are correct
- Ensure public_id is unique

**Issue: Email Sending Fails**
- ⚠️ Email notifications are currently disabled in `pom.xml` (spring-boot-starter-mail is commented out)
- To enable: Uncomment the mail dependency in `pom.xml`
- Then verify Gmail app password is correct (not regular password)
- Enable "Less secure apps" or use OAuth2
- Check Gmail SMTP settings in `application.properties`

**Issue: JWT Token Validation Fails**
- Ensure token is prefixed with "Bearer " in Authorization header
- Check token expiration time
- Verify secret key matches between token generation and validation

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is part of "The Notebook Concert" ticketing system.

## Support

For issues, questions, or feature requests, please open an issue in the repository or contact the development team.

---

**Last Updated:** May 17, 2026  
**Maintained by:** Development Team  
**Java Version:** 21  
**Spring Boot Version:** 4.0.6


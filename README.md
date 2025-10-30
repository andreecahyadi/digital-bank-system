# Digital Banking

A microservices-based digital banking application built with Spring Boot, SQL Server, Docker

- **User Management**: Register users, search, and manage accounts
- **Wallet Operations**: Create wallets, top-up balance, check balance
- **Transactions**: Transfer money between users with PIN validation
- **Analytics**: Transaction summaries, top receivers, daily volumes

### 3. Build Services
```bash
# Build User Service
cd user-service
mvn clean package -DskipTests
cd ..

# Build Wallet Service
cd wallet-service
mvn clean package -DskipTests
cd ..

# Build Transaction Service
cd transaction-service
mvn clean package -DskipTests
cd ..
```

### 4. Run with Docker Compose
```bash
docker-compose up --build
```

### 5. Access Services

- **User Service API**: http://localhost:8081/swagger-ui.html
- **Wallet Service API**: http://localhost:8082/swagger-ui.html
- **Transaction Service API**: http://localhost:8083/swagger-ui.html

## Docker Commands
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild and start
docker-compose up --build

# View running containers
docker-compose ps
```

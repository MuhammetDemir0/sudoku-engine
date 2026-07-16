# Sudoku Engine

A full-stack Sudoku generation, solving and algorithm visualization platform built with **Java 25**, **Spring Boot 3.x**, **PostgreSQL**, and **vanilla JavaScript**.

## 📋 Project Overview

Sudoku Engine is a comprehensive platform for:
- **Generating** random Sudoku puzzles with variable difficulty levels
- **Solving** Sudoku puzzles using advanced algorithms
- **Visualizing** the solving process step-by-step
- **Managing** puzzle history and solutions

## 🛠️ Tech Stack

### Backend
- **Language:** Java 25
- **Framework:** Spring Boot 3.3.x
- **Build Tool:** Maven
- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate
- **Testing:** JUnit 5, TestNG, Mockito

### Frontend
- **Language:** Vanilla JavaScript (ES6+)
- **Styling:** CSS3 with responsive design
- **No Build Tool:** Plain HTML/CSS/JS for simplicity (can add build tools later)

### DevOps & Tools
- **Containerization:** Docker & Docker Compose
- **Version Control:** Git
- **IDE:** IntelliJ IDEA / VS Code recommended

## 📁 Project Structure

```
sudoku-engine/
├── pom.xml                          # Maven configuration
├── docker-compose.yml               # PostgreSQL + pgAdmin setup
├── .gitignore                       # Git ignore rules
├── .editorconfig                    # Code style consistency
├── README.md                        # This file
│
├── src/main/java/com/sudokuengine/
│   ├── SudokuEngineApplication.java # Spring Boot entry point
│   ├── controller/                  # REST API endpoints
│   ├── service/                     # Business logic
│   ├── repository/                  # Database access (JPA)
│   ├── model/                       # JPA Entities
│   ├── dto/                         # Data Transfer Objects
│   ├── util/                        # Utility classes
│   └── algorithm/                   # Sudoku algorithms
│
├── src/main/resources/
│   ├── application.properties       # Spring Boot configuration
│   └── db/
│       └── schema.sql               # PostgreSQL DDL scripts
│
├── src/test/java/com/sudokuengine/
│   └── (Unit & Integration tests)
│
└── frontend/
    ├── index.html                   # Main HTML page
    ├── css/
    │   └── style.css                # Styling
    └── js/
        └── app.js                   # Frontend logic
```

## 🚀 Getting Started

### Prerequisites
- **Java 25** or later
- **Maven 3.8+**
- **Docker & Docker Compose** (for PostgreSQL)
- **Git**

### Quick Start

#### 1. Start PostgreSQL
```bash
docker-compose up -d
```

This starts:
- **PostgreSQL** on `localhost:5432`
- **pgAdmin** on `localhost:5050`

Default credentials:
- PostgreSQL: `sudoku_user` / `sudoku_password`
- pgAdmin: `admin@sudokuengine.local` / `admin`

#### 2. Build the Project
```bash
mvn clean compile
```

#### 3. Run the Application
```bash
mvn spring-boot:run
```

Application starts on: **http://localhost:8080**

#### 4. Open Frontend
```
Open browser: http://localhost:8080/
Frontend served from: ./frontend/index.html
```

## 📝 Configuration

### Database Connection
Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sudoku_db
spring.datasource.username=sudoku_user
spring.datasource.password=sudoku_password
```

### Logging Level
```properties
logging.level.com.sudokuengine=DEBUG  # Change to INFO for production
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=YourTestClass
```

## 📦 Deployment

### Build JAR
```bash
mvn clean package
```

Output: `target/sudoku-engine-1.0.0.jar`

### Run JAR
```bash
java -jar target/sudoku-engine-1.0.0.jar
```

## 🔧 Development Tips

### IDE Setup
**IntelliJ IDEA:**
- Open project folder → "Open"
- Maven will auto-download dependencies
- Mark `src/main/java` as Sources Root
- Mark `src/test/java` as Test Sources Root

**VS Code:**
- Install: "Extension Pack for Java" (Microsoft)
- Install: "Spring Boot Extension Pack" (Pivotal)

### Code Formatting
The project uses `.editorconfig` for consistent code style across editors.

### Database Management
Access pgAdmin:
```
http://localhost:5050
Email: admin@sudokuengine.local
Password: admin
```

Add PostgreSQL server:
- Host: `postgresql` (Docker service name)
- Port: `5432`
- Username: `sudoku_user`
- Password: `sudoku_password`

## 📚 API Endpoints (Coming Soon)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/puzzles/generate` | Generate new puzzle |
| POST | `/api/puzzles/solve` | Solve a puzzle |
| GET | `/api/puzzles/{id}` | Get puzzle details |
| GET | `/api/solutions` | List solutions |

## 🎯 Development Roadmap

- [x] Project setup & Maven configuration
- [x] Spring Boot + PostgreSQL integration
- [x] Frontend scaffold
- [ ] Sudoku generator algorithm
- [ ] Sudoku solver algorithm
- [ ] REST API endpoints
- [ ] Frontend puzzle grid interaction
- [ ] Algorithm visualization
- [ ] User authentication
- [ ] Puzzle history & stats
- [ ] Docker image for production

## 🐛 Troubleshooting

### PostgreSQL Connection Error
```
Check if Docker container is running:
docker ps

Restart containers:
docker-compose restart
```

### Maven Compilation Error
```bash
mvn clean
mvn compile -X  # Verbose mode for debugging
```

### Port Already in Use
```bash
# Change Spring Boot port in application.properties
server.port=8081
```

## 📄 License

MIT License - See LICENSE file for details

## 👤 Author

**Muhammet Demir**
- GitHub: [@MuhammetDemir0](https://github.com/MuhammetDemir0)
- Project: [sudoku-engine](https://github.com/MuhammetDemir0/sudoku-engine)

---

**Last Updated:** 2024-07-16 | **Status:** 🔧 Setup Phase Complete

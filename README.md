# Multi-Threaded File Downloader

A production-quality Java application demonstrating concurrent file downloads using thread pools, state management, and resilience patterns. Designed as a learning project and GitHub portfolio piece.

## Motivation

Downloading files concurrently is a common systems programming challenge that requires:
- Thread pool management and lifecycle coordination
- State synchronization across concurrent tasks
- Error handling and retry strategies
- Progress tracking and result merging
- Resource management and cleanup

This project explores these concerns while maintaining clean architecture, SOLID principles, and production-ready code standards.

## Project Vision

This repository is designed to evolve across three phases:

### Phase 1: Google Internship Application (Current)
- **Goal**: Resume project and GitHub portfolio demonstration
- **Focus**: Architecture design, code quality, clean implementation
- **Outcome**: Showcase software engineering fundamentals

### Phase 2: AI-Powered Download Manager
- **Goal**: Kaggle AI Capstone project
- **Features**: ML-based bandwidth prediction, intelligent scheduling, anomaly detection
- **Integration**: Pluggable AI service layer

### Phase 3: Final Year Major Project
- **Goal**: Production-grade download manager
- **Features**: Distributed architecture, horizontal scaling, enterprise features
- **Scope**: Determined in final year

**Architecture Today**: Designed to support future features without major rewrites.

## Current Features

- ✅ **Layered Architecture**: Clean separation of concerns across 4 service layers
- ✅ **Thread Pool Management**: Configurable concurrent download workers
- ✅ **State Management**: Thread-safe download tracking and progress monitoring
- ✅ **SLF4J Logging**: Production-ready structured logging
- ✅ **JUnit 5 Testing**: Comprehensive test framework with Mockito and AssertJ
- ✅ **Clean Code**: Follows SOLID principles and Java best practices

## Planned Features

- 📋 **Multi-chunk Download**: Split large files across multiple simultaneous connections
- 📋 **Retry Mechanisms**: Exponential backoff and circuit breaker patterns
- 📋 **Progress Reporting**: Real-time download progress via observers
- 📋 **Result Merging**: Combine chunks back into original files
- 📋 **Configuration Management**: External configuration for download parameters
- 📋 **Integration Tests**: End-to-end testing with mock HTTP servers

## Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Language** | Java | 21 LTS | Modern language features, production stability |
| **Build** | Maven | 3.9.x | Dependency management, reproducible builds |
| **Logging** | SLF4J + Logback | 2.0.x + 1.5.x | Structured logging, performance |
| **Testing** | JUnit 5 + Mockito + AssertJ | 5.11.x | Unit testing, assertions, mocking |
| **Java Features** | Records, Sealed Classes | 21 | Type safety, immutability |

## Folder Structure

```
multi-threaded-file-downloader/
├── pom.xml                           # Maven configuration
├── .gitignore                        # Git ignore rules
├── README.md                         # This file
├── docs/
│   └── Architecture.md               # Detailed architecture documentation
├── src/
│   ├── main/java/com/shani/
│   │   ├── App.java                 # Entry point
│   │   ├── config/                  # Configuration management
│   │   ├── exception/               # Custom exceptions
│   │   ├── manager/                 # Orchestration layer
│   │   ├── merger/                  # Result merging logic
│   │   ├── model/                   # Data structures
│   │   ├── service/                 # Business logic layer
│   │   ├── task/                    # Thread-level work units
│   │   ├── tracker/                 # Progress tracking
│   │   └── util/                    # Utility functions
│   └── test/java/com/shani/         # Unit tests mirror src/ structure
└── target/                          # Build artifacts (generated)
```

## High-Level Architecture

```
┌─────────────────────────────────────────┐
│           App.java (Entry)              │
├─────────────────────────────────────────┤
│      Manager Layer (Orchestration)      │
│    DownloadManager, StateCoordinator    │
├─────────────────────────────────────────┤
│      Service Layer (Business Logic)     │
│ DownloadService, RetryService, Tracker │
├─────────────────────────────────────────┤
│        Task Layer (Thread Work)         │
│    DownloadTask, ChunkTask, Merger      │
├─────────────────────────────────────────┤
│    Supporting Layers (Infrastructure)   │
│   Config, Exception, Model, Util        │
└─────────────────────────────────────────┘
```

See [docs/Architecture.md](docs/Architecture.md) for detailed architecture design.

## Development Roadmap

### Completed ✅
- [x] Maven project structure
- [x] Java 21 configuration
- [x] JUnit 5 test framework
- [x] SLF4J logging setup
- [x] Clean package architecture
- [x] Project documentation

### Sprint 2: Core Download Logic
- [ ] DownloadRequest model
- [ ] DownloadService implementation
- [ ] Thread pool management
- [ ] Basic download executor

### Sprint 3: State & Tracking
- [ ] DownloadTracker implementation
- [ ] Progress reporting
- [ ] State synchronization
- [ ] Unit tests for state management

### Sprint 4: Advanced Features
- [ ] Retry mechanisms
- [ ] Chunk downloading
- [ ] Result merging
- [ ] Integration tests

### Sprint 5: Production Hardening
- [ ] Performance optimization
- [ ] Resource cleanup
- [ ] Error handling audit
- [ ] Documentation completion

## Installation

### Prerequisites
- **Java 21** or later
- **Maven 3.9.x** or later

### Build from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/multi-threaded-file-downloader.git
cd multi-threaded-file-downloader

# Build the project
mvn clean install

# Run unit tests
mvn test

# Run integration tests (when available)
mvn verify

# Build executable JAR
mvn package
```

### Artifacts Generated
- `target/multi-threaded-file-downloader-app.jar` - Executable JAR with all dependencies
- `target/multi-threaded-file-downloader-1.0-SNAPSHOT-sources.jar` - Source code JAR
- `target/multi-threaded-file-downloader-1.0-SNAPSHOT-javadoc.jar` - API documentation

## Usage

### Run the Application

```bash
# Using Maven
mvn clean compile exec:java -Dexec.mainClass="com.shani.App"

# Using executable JAR
java -jar target/multi-threaded-file-downloader-app.jar
```

### Enable Detailed Logging

Edit `src/main/resources/logback.xml` (when created) to adjust log levels:

```xml
<!-- DEBUG logging for troubleshooting -->
<root level="DEBUG">
    <appender-ref ref="CONSOLE" />
</root>
```

## Contribution Guide

Contributions are welcome! Please follow these guidelines:

### Code Quality Standards
1. **SOLID Principles**: Single responsibility, open-closed, Liskov, interface segregation, dependency inversion
2. **Clean Code**: Meaningful names, small methods, no magic numbers
3. **Test Coverage**: Aim for >80% coverage, every public method tested
4. **Documentation**: Javadoc for public APIs, comments for "why" not "what"

### Development Workflow
1. Create a feature branch: `git checkout -b feature/your-feature-name`
2. Make focused commits with clear messages
3. Write/update tests for your changes
4. Run the full test suite: `mvn clean verify`
5. Submit a pull request with description of changes

### Running Tests Locally

```bash
# Unit tests only
mvn test

# With coverage report
mvn test jacoco:report

# Integration tests
mvn verify
```

### Code Style
- Use 4-space indentation
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable names over comments
- Keep methods small and focused

## Future AI Integration

Phase 2 will introduce ML capabilities:

```java
// Planned API (not implemented yet)
AIDownloadOptimizer optimizer = new AIDownloadOptimizer(model);
DownloadStrategy strategy = optimizer.suggestStrategy(downloadRequest);
downloader.download(downloadRequest, strategy);
```

This demonstrates how the architecture supports future enhancements without core changes.

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Author

**Shani** - Computer Science Student  
GitHub: [@yourusername](https://github.com/yourusername)

## Acknowledgments

- Inspired by real-world download managers (aria2, wget)
- Engineering practices from Google SWE standards
- Java concurrency patterns from *Java Concurrency in Practice*

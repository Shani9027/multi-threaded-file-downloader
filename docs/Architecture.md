# Architecture Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Layer Responsibilities](#layer-responsibilities)
3. [Package Structure](#package-structure)
4. [Data Flow: Download Request Lifecycle](#data-flow-download-request-lifecycle)
5. [Thread Lifecycle & Concurrency Model](#thread-lifecycle--concurrency-model)
6. [Design Decisions](#design-decisions)
7. [SOLID Principles Applied](#solid-principles-applied)
8. [Future Extension Strategy](#future-extension-strategy)

---

## Architecture Overview

The Multi-Threaded File Downloader uses a **Layered Architecture** with four service tiers plus supporting infrastructure:

```
┌─────────────────────────────────────────────────────────────────┐
│                   Application Entry Point                        │
│                      (App.java)                                  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│          LAYER 1: MANAGER (Orchestration)                        │
│                                                                   │
│  Responsibilities:                                               │
│  • Coordinate download lifecycle                                │
│  • Manage thread pool and worker threads                         │
│  • Handle client requests                                        │
│                                                                   │
│  Key Classes: DownloadManager, DownloadCoordinator              │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│          LAYER 2: SERVICE (Business Logic)                       │
│                                                                   │
│  Responsibilities:                                               │
│  • Download strategy and execution logic                         │
│  • Retry mechanisms and error handling                           │
│  • Progress tracking and state management                        │
│  • Result merging coordination                                   │
│                                                                   │
│  Key Classes: DownloadService, RetryService, DownloadTracker   │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│          LAYER 3: TASK (Thread-Level Work)                       │
│                                                                   │
│  Responsibilities:                                               │
│  • Individual file/chunk download execution                      │
│  • HTTP connections and data transfer                            │
│  • Task result reporting                                         │
│  • Resource cleanup (connections, buffers)                       │
│                                                                   │
│  Key Classes: DownloadTask, ChunkDownloadTask, ResultMerger     │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│  LAYER 4: INFRASTRUCTURE (Supporting Services)                   │
│                                                                   │
│  Responsibilities:                                               │
│  • Data modeling (DownloadRequest, FileMetadata)                │
│  • Configuration management                                      │
│  • Custom exceptions and error codes                             │
│  • Utility functions (URL validation, file ops)                 │
│                                                                   │
│  Packages: model, config, exception, util                       │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Principles

- **Layered Boundaries**: Each layer only depends on layers below it
- **No Circular Dependencies**: Information flows downward only
- **Dependency Inversion**: High-level modules depend on abstractions, not concrete implementations
- **Testability**: Each layer can be tested independently with mocks
- **Extensibility**: New features added without modifying existing layers

---

## Layer Responsibilities

### Layer 1: Manager (Orchestration & Coordination)

**Purpose**: Orchestrates the entire download workflow and manages thread lifecycle.

**Responsibility**:
- Receive download requests from application
- Create and configure thread pool executors
- Delegate work to service layer
- Monitor overall progress
- Handle graceful shutdown

**Key Classes**:

| Class | Responsibility |
|-------|-----------------|
| `DownloadManager` | Main orchestrator; coordinates all download operations |
| `DownloadCoordinator` | Coordinates communication between services |

**Interfaces**:

```java
// (Pseudo-code, actual implementation in Phase 2)
public interface DownloadManager {
    DownloadResult download(DownloadRequest request);
    void cancel(String downloadId);
    void shutdown();
}
```

**Not in Scope (Phase 1)**:
- Actual HTTP operations
- Thread pool implementation details
- Retry logic

---

### Layer 2: Service (Business Logic)

**Purpose**: Implements business rules, error handling, and state management.

**Responsibility**:
- Implement download strategy (parallel vs sequential)
- Define and implement retry mechanisms
- Track download progress in real-time
- Manage download state transitions
- Coordinate chunk merging

**Key Classes**:

| Class | Responsibility |
|-------|-----------------|
| `DownloadService` | Core download logic, strategy selection |
| `RetryService` | Retry strategies, circuit breaker, exponential backoff |
| `DownloadTracker` | Thread-safe progress tracking, statistics |
| `MergeService` | Coordinate chunk merging and validation |

**Example Patterns** (to be implemented):

```java
// Phase 2 implementation
public class DownloadService {
    
    // Retry with exponential backoff
    public void downloadWithRetry(DownloadRequest request, int maxRetries) {
        // Implementation with backoff logic
    }
    
    // Track progress thread-safely
    public void trackProgress(String downloadId, long bytesDownloaded) {
        tracker.updateProgress(downloadId, bytesDownloaded);
    }
}
```

**Not in Scope (Phase 1)**:
- HTTP client implementation
- Actual file I/O
- Network communication

---

### Layer 3: Task (Thread-Level Execution)

**Purpose**: Executes individual work units within thread pool threads.

**Responsibility**:
- Execute single file/chunk download
- Handle HTTP connections
- Perform actual data transfer
- Report results back to service layer
- Clean up resources (connections, memory)

**Key Classes**:

| Class | Responsibility |
|-------|-----------------|
| `DownloadTask` (implements Runnable) | Executes single file download |
| `ChunkDownloadTask` | Downloads single chunk of a large file |
| `ResultMerger` | Merges completed chunks into final file |
| `TaskResult` | Encapsulates task execution result |

**Concurrency Guarantees**:
- Each task runs in its own thread
- Tasks are thread-isolated (minimal shared state)
- Results aggregated through thread-safe queues

**Not in Scope (Phase 1)**:
- Actual I/O operations
- Network implementations
- Thread pool management

---

### Layer 4: Infrastructure (Supporting Services)

#### 4a. Model Package (`model/`)

**Purpose**: Define data structures and value objects.

**Planned Classes**:
- `DownloadRequest` - Input parameters for download
- `FileMetadata` - File information (size, type, hash)
- `DownloadProgress` - Current progress snapshot
- `DownloadStatistics` - Aggregate metrics

**Design Pattern**: Immutable Value Objects using Java Records (Java 21)

```java
// Example structure
public record DownloadRequest(
    String fileId,
    String url,
    String destination,
    int maxThreads
) {}
```

---

#### 4b. Config Package (`config/`)

**Purpose**: Manage configuration and constants.

**Planned Classes**:
- `DownloadConfig` - Configuration parameters
- `ThreadPoolConfig` - Thread pool settings
- `RetryConfig` - Retry behavior configuration

**Design Rationale**:
- Externalize configuration from code
- Enable runtime parameter adjustment
- Support different environments (dev, test, prod)

---

#### 4c. Exception Package (`exception/`)

**Purpose**: Define custom exception hierarchy.

**Planned Classes**:
- `DownloadException` - Base exception
- `NetworkException` - Network-related failures
- `FileException` - File I/O errors
- `ConfigurationException` - Configuration validation errors

**Exception Hierarchy**:

```
Exception
└── RuntimeException (for unchecked exceptions)
    └── DownloadException
        ├── NetworkException
        ├── FileException
        └── ConfigurationException
```

**Rationale**: 
- Meaningful error types for different failure scenarios
- Enables specific error handling by calling code
- Easier debugging with domain-specific exceptions

---

#### 4d. Util Package (`util/`)

**Purpose**: Provide utility functions and helpers.

**Planned Classes**:
- `UrlValidator` - URL validation and parsing
- `FileUtils` - File system operations
- `ThreadUtils` - Thread naming, monitoring utilities
- `ByteUtils` - Byte array operations

---

#### 4e. Tracker Package (`tracker/`)

**Purpose**: Track download progress and statistics.

**Responsibility**:
- Maintain thread-safe progress state
- Calculate download speed
- Estimate time remaining
- Collect aggregate statistics

---

#### 4f. Merger Package (`merger/`)

**Purpose**: Coordinate result merging after parallel downloads.

**Responsibility**:
- Validate chunk integrity
- Merge chunks in correct order
- Calculate final checksums
- Clean up temporary files

---

## Package Structure

```
com.shani
│
├── app
│   └── App.java                    # Entry point
│
├── manager                         # Layer 1: Orchestration
│   ├── DownloadManager.java        # Main orchestrator
│   └── DownloadCoordinator.java    # Coordination logic
│
├── service                         # Layer 2: Business Logic
│   ├── DownloadService.java        # Core download logic
│   ├── RetryService.java           # Retry strategies
│   └── DownloadTracker.java        # Progress tracking
│
├── task                            # Layer 3: Thread Work
│   ├── DownloadTask.java           # Runnable task
│   ├── ChunkDownloadTask.java      # Chunk download
│   ├── ResultMerger.java           # Result merging
│   └── TaskResult.java             # Task outcome
│
├── model                           # Layer 4: Data Models
│   ├── DownloadRequest.java        # Input request
│   ├── FileMetadata.java           # File information
│   ├── DownloadProgress.java       # Progress snapshot
│   └── DownloadStatistics.java     # Aggregate stats
│
├── tracker                         # Layer 4: Progress Tracking
│   ├── ProgressTracker.java        # Progress interface
│   └── DefaultProgressTracker.java # Concrete implementation
│
├── merger                          # Layer 4: Merging
│   ├── Merger.java                 # Merger interface
│   └── ChunkMerger.java            # Chunk merger impl
│
├── config                          # Layer 4: Configuration
│   ├── DownloadConfig.java         # Download settings
│   ├── ThreadPoolConfig.java       # Thread pool settings
│   └── RetryConfig.java            # Retry settings
│
├── exception                       # Layer 4: Error Handling
│   ├── DownloadException.java      # Base exception
│   ├── NetworkException.java       # Network errors
│   ├── FileException.java          # File I/O errors
│   └── ConfigurationException.java # Config errors
│
└── util                            # Layer 4: Utilities
    ├── UrlValidator.java           # URL validation
    ├── FileUtils.java              # File utilities
    ├── ThreadUtils.java            # Thread utilities
    └── ByteUtils.java              # Byte operations
```

---

## Data Flow: Download Request Lifecycle

### Typical Download Flow (Happy Path)

```
1. User initiates download
   ↓
2. App.java creates DownloadRequest
   ↓
3. DownloadManager receives request
   ↓ [Layer 1 → Layer 2]
4. DownloadService validates and processes request
   ↓ [Layer 2 → Layer 2]
5. DownloadTracker initializes progress tracking
   ↓ [Layer 2 → Layer 2]
6. DownloadManager submits tasks to thread pool
   ↓ [Layer 1 → Layer 3]
7. Worker threads execute DownloadTask
   ↓ [Layer 3: HTTP download operations]
8. Task completes and reports result
   ↓ [Layer 3 → Layer 2]
9. DownloadTracker updates progress
   ↓ [Layer 2]
10. All chunks downloaded
    ↓
11. ResultMerger merges chunks
    ↓
12. Final result returned to user
```

### State Transitions During Download

```
CREATED
  ↓ (submit to thread pool)
QUEUED
  ↓ (worker picks up task)
IN_PROGRESS
  ├─ (error occurs)
  ├─→ RETRY → IN_PROGRESS
  │   ├─ (max retries exceeded)
  │   └─→ FAILED
  │
  └─ (success)
     ├─ (merge required)
     ├─→ MERGING
     │   ├─ (merge error)
     │   └─→ MERGE_FAILED
     │
     │   └─ (merge success)
     └─→ COMPLETED

COMPLETED (Success)
FAILED (Error)
CANCELLED (User cancelled)
```

---

## Thread Lifecycle & Concurrency Model

### Thread Pool Architecture

```
┌─────────────────────────────────────────┐
│     DownloadManager Thread Pool         │
│  (Fixed-size or adaptive size)          │
│                                         │
│  ┌────┐  ┌────┐  ┌────┐  ┌────┐       │
│  │WT1 │  │WT2 │  │WT3 │  │WT N│  ... │
│  └┬───┘  └┬───┘  └┬───┘  └┬───┘       │
│   │       │       │       │            │
└───┼───────┼───────┼───────┼────────────┘
    │       │       │       │
    ↓       ↓       ↓       ↓
  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
  │Task1│ │Task2│ │Task3│ │TaskN│
  └─────┘ └─────┘ └─────┘ └─────┘
    │       │       │       │
    └───────┼───────┼───────┘
            ↓
    ┌──────────────────┐
    │  Work Queue      │
    │  (BlockingQueue) │
    └──────────────────┘
```

### Concurrency Guarantees

**Thread-Safe Operations**:
- DownloadTracker: Uses `ConcurrentHashMap` or `AtomicReference`
- Work Queue: Uses `BlockingQueue` (inherently thread-safe)
- Result aggregation: Uses thread-safe collections

**Shared State Minimization**:
- Each task operates on independent file chunks
- Minimal synchronization points
- Lock-free algorithms where possible

**Example: Progress Tracking**

```
Thread A (DownloadTask 1)     Thread B (DownloadTask 2)
       │                             │
       └──→ Update Progress ←────────┘
             (Thread-Safe)
             
DownloadTracker maintains:
- AtomicLong bytesDownloaded
- ConcurrentHashMap<String, TaskProgress>
```

### Graceful Shutdown

```
1. User initiates shutdown
   ↓
2. DownloadManager.shutdown() called
   ↓
3. Stop accepting new tasks
   ↓
4. Wait for in-flight tasks to complete
   ↓
5. Force terminate if timeout exceeded
   ↓
6. Release all resources
   ↓
7. Return aggregated results
```

---

## Design Decisions

### Decision 1: Layered Architecture

**What**: Four-layer design (Manager → Service → Task → Infrastructure)

**Why**:
- ✅ **Testability**: Each layer tested independently
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Extensibility**: Add features without touching core layers
- ✅ **Scalability**: Can scale individual layers independently

**Alternatives Considered**:
- ❌ Flat structure: Too coupled, hard to maintain
- ❌ Microservices: Overkill for single application
- ❌ Hexagonal: Adds complexity without clear benefit for this project

**Decision Rationale**: Layered architecture is proven, widely understood, and provides the clarity needed for a learning project.

---

### Decision 2: Thread Pool Model

**What**: Fixed-size or adaptive thread pool for concurrent downloads

**Why**:
- ✅ **Resource Control**: Prevent thread explosion
- ✅ **Performance**: Reuse threads, reduce creation overhead
- ✅ **Scalability**: Handle 1000s of downloads without 1000s of threads
- ✅ **Simple**: Standard Java `ExecutorService`

**Alternatives Considered**:
- ❌ One thread per download: Resource intensive at scale
- ❌ Async/Reactive: More complex, not needed for MVP
- ❌ Virtual threads (Java 21): Not using yet, but will explore in Phase 2

**Decision Rationale**: Thread pools are industry standard, provide good performance/resource tradeoff.

---

### Decision 3: Immutable Data Models

**What**: Use Java 21 Records for data objects

```java
public record DownloadRequest(String url, String destination) {}
```

**Why**:
- ✅ **Thread-safe**: Immutable objects safe to share across threads
- ✅ **Concise**: Reduces boilerplate code
- ✅ **Modern**: Shows Java 21 expertise
- ✅ **Semantics**: Clearly expresses intent (data, not logic)

**Alternatives Considered**:
- ❌ Mutable POJOs: Risk of data corruption in concurrent code
- ❌ Traditional classes: Too much boilerplate
- ❌ Lombok: Adds dependency, less explicit

**Decision Rationale**: Records are ideal for immutable data transfer objects, especially in concurrent contexts.

---

### Decision 4: SLF4J + Logback

**What**: Facade pattern for logging

**Why**:
- ✅ **Abstraction**: Swap log implementation without code changes
- ✅ **Performance**: Logback is fast, optimized for high throughput
- ✅ **Structured**: Supports structured logging (MDC, markers)
- ✅ **Industry Standard**: Used in 80%+ of Java applications

**Alternatives Considered**:
- ❌ System.out.println: Not suitable for production
- ❌ java.util.logging: Older, less flexible
- ❌ Log4j2: Excellent but SLF4J is more flexible

**Decision Rationale**: SLF4J is the industry standard; flexibility + performance.

---

### Decision 5: Package-by-Layer

**What**: Organize packages by architectural layer (not by feature)

```
manager/     ← Orchestration
service/     ← Business logic
task/        ← Thread work
model/       ← Data structures
config/      ← Configuration
exception/   ← Error handling
util/        ← Utilities
```

**Why**:
- ✅ **Clear Dependencies**: Dependencies flow downward only
- ✅ **Testability**: Mock entire layers easily
- ✅ **Scalability**: Easy to add new layers later
- ✅ **Team Communication**: Clear what each layer does

**Alternatives Considered**:
- ❌ Package-by-feature: Hard with single feature
- ❌ Domain-driven: Overkill for current scope

**Decision Rationale**: Layer-based packaging makes dependencies explicit and testability high.

---

## SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)

**Principle**: Each class has exactly one reason to change.

**Application**:

| Class | Single Responsibility |
|-------|------------------------|
| `DownloadManager` | Orchestrate downloads |
| `DownloadService` | Implement download logic |
| `DownloadTracker` | Track progress |
| `RetryService` | Handle retry strategy |
| `ResultMerger` | Merge results |

**Benefit**: Easy to modify individual behaviors without side effects.

---

### 2. Open/Closed Principle (OCP)

**Principle**: Open for extension, closed for modification.

**Application**: Service interfaces allow multiple implementations

```java
public interface DownloadStrategy {
    void download(DownloadRequest request);
}

// Can add implementations without modifying DownloadService
public class ParallelDownloadStrategy implements DownloadStrategy { ... }
public class SequentialDownloadStrategy implements DownloadStrategy { ... }
```

**Benefit**: Add new features without changing existing code, reduce regression risk.

---

### 3. Liskov Substitution Principle (LSP)

**Principle**: Subtypes must be substitutable for their base types.

**Application**: All tasks implement Runnable contract

```java
public class DownloadTask implements Runnable { ... }
public class ChunkDownloadTask implements Runnable { ... }
// Both can be submitted to ExecutorService identically
executor.submit(downloadTask);
executor.submit(chunkTask);
```

**Benefit**: Polymorphism enables flexible task scheduling.

---

### 4. Interface Segregation Principle (ISP)

**Principle**: Clients should not depend on interfaces they don't use.

**Application**: Small, focused interfaces

```java
// NOT: One huge DownloadService interface
// YES: Small focused interfaces
public interface ProgressTracker { ... }
public interface Merger { ... }
public interface RetryHandler { ... }
```

**Benefit**: Loose coupling, easier testing with focused mocks.

---

### 5. Dependency Inversion Principle (DIP)

**Principle**: High-level modules should depend on abstractions, not concrete implementations.

**Application**: Manager depends on Service interface, not concrete class

```java
public class DownloadManager {
    private final DownloadService service; // Abstract dependency
    
    public DownloadManager(DownloadService service) {
        this.service = service; // Injected, not created
    }
}
```

**Benefit**: Easy to test with mock services, swap implementations at runtime.

---

## Future Extension Strategy

### Extensibility Architecture

The current architecture supports these extensions without major rewrites:

```
Current (Phase 1)         →    Phase 2                  →    Phase 3
─────────────────              ────────                      ──────
Sync Downloads                 Multi-chunk Downloads        Distributed
Basic Retry                     AI-Powered Scheduling       Horizontal Scaling
Single Thread Pool             Advanced Metrics             Enterprise Features
                               ML Model Integration         ...
```

### Extension Point 1: Download Strategies

**How to Add**: Implement new `DownloadStrategy`

```java
// Phase 2: AI-based strategy
public class AIDownloadStrategy implements DownloadStrategy {
    // Leverages ML model for optimal scheduling
}
```

**Impact**: DownloadManager unchanged, pure extension.

---

### Extension Point 2: Retry Mechanisms

**How to Add**: Implement new `RetryPolicy`

```java
// Phase 2: Circuit breaker pattern
public class CircuitBreakerRetry implements RetryPolicy {
    // Advanced retry with failure tracking
}
```

**Impact**: Service layer unchanged, new policy registered at startup.

---

### Extension Point 3: Storage Backends

**How to Add**: Implement `FileStore` interface

```java
// Phase 2: Cloud storage
public class S3FileStore implements FileStore {
    // Downloads directly to AWS S3
}
```

**Impact**: Task layer abstracted from storage details.

---

### Extension Point 4: Monitoring & Observability

**How to Add**: Add observers to DownloadTracker

```java
// Phase 2: Prometheus metrics
tracker.addObserver(new PrometheusObserver());
tracker.addObserver(new LoggingObserver());
```

**Impact**: Non-invasive, multiple observers supported.

---

### Extension Point 5: ML Integration

**How to Add**: Inject AI service into Service layer

```java
// Phase 2: ML-powered decisions
public class MLDownloadService extends DownloadService {
    private final AIOptimizer optimizer;
    
    @Override
    public DownloadStrategy selectStrategy(DownloadRequest request) {
        // Use ML to select optimal strategy
        return optimizer.suggestStrategy(request);
    }
}
```

**Impact**: Service layer extended, not modified. Manager unchanged.

---

## Testing Strategy

### Unit Testing

**Scope**: Individual classes in isolation

```
Manager Tests
├── Test DownloadManager
└── Test DownloadCoordinator

Service Tests
├── Test DownloadService
├── Test RetryService
└── Test DownloadTracker

Task Tests
├── Test DownloadTask
└── Test ChunkDownloadTask
```

**Tools**: JUnit 5, Mockito, AssertJ

---

### Integration Testing

**Scope**: Multiple components working together

```
Manager + Service Tests
├── DownloadManager → DownloadService
├── Retry Logic → State Tracking
└── Error Scenarios

Service + Task Tests
├── DownloadService → DownloadTask
├── Progress Updates
└── Result Collection
```

---

### Concurrency Testing

**Scope**: Thread safety and race conditions

```
Concurrent Operations
├── Multiple threads updating progress
├── Simultaneous task completion
└── Shutdown during in-flight downloads
```

**Tools**: JUnit 5 Parameterized Tests, Thread testing utilities

---

## Summary

This architecture provides:

✅ **Clarity**: Clear layer separation and responsibilities  
✅ **Testability**: Each component independently testable  
✅ **Maintainability**: Changes isolated to single layers  
✅ **Scalability**: Designed for growth and new features  
✅ **Learning**: Demonstrates modern Java patterns  
✅ **Portfolio**: Production-quality code and design  

The foundation is solid for Phase 2 AI integration and Phase 3 enterprise features.


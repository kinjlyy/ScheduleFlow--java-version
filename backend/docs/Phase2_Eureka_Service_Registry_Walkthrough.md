# Phase 2: Eureka Service Discovery Registry — Implementation Walkthrough

## 1. Project Structure

The ScheduleFlow codebase now contains two independent Spring Boot services under `scheduleflow/`:

```
scheduleflow/
├── scheduleflow-eureka-server/              ◄── NEW: Standalone Service Registry (Phase 2)
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/scheduleflow/eureka/
│           │       └── EurekaServerApplication.java
│           └── resources/
│               └── application.properties
│
└── backend/                                 ◄── Timetable Service (Phase 1 + Phase 2 Eureka Client)
    ├── pom.xml                                  (Updated with Eureka Client BOM & dependency)
    └── src/
        └── main/
            ├── java/
            │   └── com/scheduleflow/
            │       ├── ScheduleFlowApplication.java  (Annotated with @EnableDiscoveryClient)
            │       └── ... (Service, Scheduler, Controllers, Entities)
            └── resources/
                └── application.properties       (Configured with spring.application.name=TIMETABLE-SERVICE)
```

---

## 2. Dependencies Added

### Eureka Server (`scheduleflow-eureka-server/pom.xml`)
- `org.springframework.boot:spring-boot-starter-web` — Provides embedded Tomcat HTTP server for the dashboard and REST API.
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-server` — Core Netflix Eureka Server registry engine and web UI dashboard.
- `org.springframework.boot:spring-boot-starter-actuator` — Production observability endpoints (`/actuator/health`, `/actuator/info`).
- `org.springframework.cloud:spring-cloud-dependencies` (BOM version `2023.0.0`) — Ensures version compatibility across Spring Cloud components for Spring Boot 3.2.0.

### Timetable Service (`backend/pom.xml`)
- `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client` — Eureka Client library enabling heartbeat pinging, registry fetching, and service registration.
- `org.springframework.cloud:spring-cloud-dependencies` (BOM version `2023.0.0`) — Dependency management import.

---

## 3. Configuration Files

### Eureka Server (`scheduleflow-eureka-server/src/main/resources/application.properties`)
```properties
spring.application.name=scheduleflow-eureka-server
server.port=${PORT:8761}

# Standalone configuration — disable self-registration and registry fetching
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=${EUREKA_URI:http://localhost:8761/eureka}

# Preservation and eviction tuning
eureka.server.enable-self-preservation=${EUREKA_SELF_PRESERVATION:true}
eureka.server.eviction-interval-timer-in-ms=60000

# Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Timetable Service (`backend/src/main/resources/application.properties`)
```properties
spring.application.name=TIMETABLE-SERVICE
server.port=${PORT:8080}

# Eureka Client Registration
eureka.client.service-url.defaultZone=${EUREKA_URI:http://localhost:8761/eureka}
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}:${server.port}
```

---

## 4. Annotation Breakdown

### `@EnableEurekaServer` (on `EurekaServerApplication.java`)
- Activates auto-configuration for Eureka Server.
- Initializes the in-memory service registry (`PeerAwareInstanceRegistry`).
- Mounts Eureka REST endpoints (`/eureka/apps`, `/eureka/instances`) and the HTML dashboard UI at port 8761.

### `@EnableDiscoveryClient` (on `ScheduleFlowApplication.java`)
- Enables Spring Cloud discovery client capabilities.
- Registers `TIMETABLE-SERVICE` with the registry specified in `eureka.client.service-url.defaultZone`.
- Initiates background heartbeat thread to send pings every 30 seconds.

---

## 5. How Eureka Works Internally

1. **Service Registration:** On startup, `TIMETABLE-SERVICE` sends a POST request (`/eureka/apps/TIMETABLE-SERVICE`) to Eureka Server containing instance metadata (IP, port, health check URL, instance ID).
2. **In-Memory Registry:** Eureka Server stores the instance metadata in a concurrent hash map indexed by app name (`TIMETABLE-SERVICE`).
3. **Heartbeat & Leases:** `TIMETABLE-SERVICE` sends periodic heartbeat pings (PUT request every 30s) to renew its lease.
4. **Eviction:** If Eureka Server does not receive a heartbeat within 90 seconds (default lease expiration), it marks the instance as DOWN or evicts it from the registry.
5. **Self-Preservation Mode:** If network issues cause a sudden drop in heartbeats (>15% threshold), Eureka Server enters self-preservation mode to prevent mass-eviction of healthy services due to temporary network partition.

---

## 6. How Timetable Service Registers Itself

1. When `ScheduleFlowApplication` starts up, the `@EnableDiscoveryClient` annotation triggers `EurekaClientAutoConfiguration`.
2. The client reads `spring.application.name=TIMETABLE-SERVICE` and `server.port=8080`.
3. It constructs an `InstanceInfo` metadata payload:
   - AppName: `TIMETABLE-SERVICE`
   - HostName / IP: `prefer-ip-address = true`
   - Port: `8080`
   - HealthCheckUrl: `http://<IP>:8080/actuator/health`
   - StatusPageUrl: `http://<IP>:8080/actuator/info`
4. The client issues a HTTP POST to `http://localhost:8761/eureka/apps/TIMETABLE-SERVICE`.
5. Eureka Server accepts the registration and displays `TIMETABLE-SERVICE` under "Instances currently registered with Eureka".

---

## 7. Eureka Dashboard Explanation

Navigating to `http://localhost:8761` presents:

- **System Status:** Environment, uptime, current memory usage, and self-preservation status.
- **DS Replicas:** Distributed Eureka server nodes (none in standalone mode).
- **Instances Currently Registered with Eureka:**
  - **Application:** `TIMETABLE-SERVICE`
  - **AMIs:** `n/a`
  - **Availability Zones:** `1`
  - **Status:** `UP (1) - TIMETABLE-SERVICE:8080`
- **General Info:** Total capacity, registered instance count, lease renewal percentage.

---

## 8. Verification Steps

To verify Phase 2 setup locally:

1. **Build Projects:**
   ```bash
   # Build Eureka Server
   cd scheduleflow/scheduleflow-eureka-server
   mvn clean package -DskipTests

   # Build Timetable Service
   cd ../backend
   mvn clean package -DskipTests
   ```

2. **Start Eureka Server:**
   ```bash
   cd scheduleflow/scheduleflow-eureka-server
   mvn spring-boot:run
   ```
   *Expected Output:* Server starts on port 8761. Eureka dashboard accessible at `http://localhost:8761`.

3. **Start Timetable Service:**
   ```bash
   cd scheduleflow/backend
   mvn spring-boot:run
   ```
   *Expected Output:* Service starts on port 8080 and logs:
   `DiscoveryClient_TIMETABLE-SERVICE/TIMETABLE-SERVICE:8080 - registration status: 204`

4. **Verify Registry:**
   Open `http://localhost:8761` in browser. Verify `TIMETABLE-SERVICE` appears with status `UP`.

---

## 9. Future Integration with API Gateway (Phase 3)

In Phase 3, `scheduleflow-api-gateway` will be introduced. It will also register as a Eureka Client and use service discovery routing (`lb://` protocol):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: timetable-service-route
          uri: lb://TIMETABLE-SERVICE
          predicates:
            - Path=/api/timetables/**, /api/generate/**, /api/lectures/**
```

The Gateway queries Eureka for `TIMETABLE-SERVICE` instances and dynamically load balances requests across available instances without hardcoding IP addresses or ports.

---

## 10. Why Phase 2 Was Necessary Before API Gateway

1. **Decoupled Service Locations:** Without Service Discovery, the Gateway would need hardcoded hostnames and ports (`http://localhost:8080`).
2. **Dynamic Scaling:** As microservices scale to multiple instances, Eureka provides real-time instance tracking and load balancing targets (`lb://SERVICE-NAME`).
3. **Foundation for Inter-Service Feign Clients (Phase 6):** OpenFeign clients use Eureka to look up target services by logical name (`@FeignClient(name = "RESOURCE-SERVICE")`). Service Discovery must exist before Feign can operate.

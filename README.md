# Wiki Connect Core MediaWiki Client

A lightweight Java library for interacting with the MediaWiki Action API.

## Project Information

- **Group ID:** `org.qrdlife.wikiconnect`
- **Artifact ID:** `core-mediawiki-client`
- **License:** [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)
- **Repository:** [GitHub](https://github.com/wiki-connect/core-mediawiki-client)
- **Java Requirement:** 11 or higher

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.qrdlife.wikiconnect</groupId>
    <artifactId>core-mediawiki-client</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Usage

### Basic Setup
```java
ActionApi api = new ActionApi("https://en.wikipedia.org/w/api.php")
        .setUserAgent("MyWikiBot/1.0")
        .build();
```

### Authentication with Memory Cleanup (char[])
To prevent passwords from lingering in heap memory:
```java
char[] password = new char[]{'s', 'e', 'c', 'r', 'e', 't'};
UserAndPassword auth = new UserAndPassword("username", password, api);

if (auth.login()) {
    System.out.println("Login successful!");
}
// password array is automatically zeroed out internally after login attempt.
```

### JSON-Based Session Persistence (Secure Cookie Storage)
Vulnerability-free session persistence using safe JSON serialization:
```java
File cookieFile = new File("session_cookies.json");
ActionApi api = new ActionApi("https://en.wikipedia.org/w/api.php")
        .setFileCookie(cookieFile) // Persists session cookies as JSON
        .build();
```

### Custom Connection & Socket Timeouts
Configure timeouts to prevent requests from blocking indefinitely:
```java
ActionApi api = new ActionApi("https://en.wikipedia.org/w/api.php")
        .setTimeout(5000, 15000) // Connect timeout: 5s, Socket timeout: 15s
        .build();
```

### Transient Error Retry
Enable automatic exponential retry for GET requests on transient network or 5xx server issues:
```java
ActionApi api = new ActionApi("https://en.wikipedia.org/w/api.php")
        .setMaxRetries(3) // Will retry transient GET failures up to 3 times
        .build();
```

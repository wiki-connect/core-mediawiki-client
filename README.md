# Core MediaWiki Client

A lightweight Java client for interacting with **MediaWiki APIs**.  
This library provides simple and efficient access to MediaWiki endpoints for building bots, tools, or integrations.

---

## 📦 Project Information

- **Group ID:** `org.qrdlife.wikiconnect`
- **Artifact ID:** `core-mediawiki-client`
- **License:** [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)

Repository: [GitHub](https://github.com/wiki-connect/core-mediawiki-client)

---

## 🔧 Requirements

- **Java:** 11 or higher
- **Maven:** 3.6+

Dependencies (managed via Maven):
- [Apache HttpClient 5](https://hc.apache.org/httpcomponents-client-5.4.x/) – HTTP communication
- [org.json](https://github.com/stleary/JSON-java) – JSON parsing
- [JUnit 5](https://junit.org/junit5/) – Unit testing (testing)
- [Mockito](https://site.mockito.org/) – Mocking framework (testing)

---

## 🚀 Installation

Add the dependency to your **Maven project**:

```xml
<dependency>
  <groupId>org.qrdlife.wikiconnect</groupId>
  <artifactId>core-mediawiki-client</artifactId>
  <version>{version from a release}</version>
</dependency>

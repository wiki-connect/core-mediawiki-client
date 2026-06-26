# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2026-06-26

### Added
- **Security Policy**: Added `SECURITY.md` defining the supported versions and reporting process.
- **SSRF Mitigation**: Implemented strict scheme validation in `ActionApi` (supporting `http` and `https` only).
- **Transient Failures Mitigation**: Added connect and socket timeouts (default 30 seconds) and support for fluent timeout customization.
- **Vulnerability Scans in CI**: Integrated OWASP Dependency-Check scanner to analyze maven dependencies on each build.
- **Dependabot Hardening**: Added `github-actions` updates ecosystem to Dependabot configuration.
- **Exponential Backoff Retry**: Added automatic request retry mechanism for GET requests on transient network or 5xx server issues.

### Changed
- **Dangerous Serialization Replaced**: Swapped Java Object serialization (`ObjectInputStream`/`ObjectOutputStream`) with secure, text-based JSON serialization in `FileCookieJar` to prevent deserialization RCE.
- **Heap Memory Cleanup**: Converted password handling from `String` to `char[]` in `UserAndPassword` and implemented immediate memory zeroing upon login attempt.
- **Fluent API Builder**: `ActionApi.build()` now returns `ActionApi` to support method chaining.
- **Exception Handling**: Standardized logger usage in place of `e.printStackTrace()` to prevent sensitive path exposure.
- **Token Signature**: Removed misleading checked `JSONException` from `Token.get()` signature.
- **Code Style**: Renamed fields to match standard camelCase Java naming conventions.

## [1.2.2] - 2025-10-12

### Added
- Initial implementation of the MediaWiki API Client features.

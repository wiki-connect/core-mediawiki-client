# Security Policy

## Supported Versions

Only the latest major/minor releases are supported with security updates.

| Version | Supported |
| ------- | --------- |
| 1.3.x   | ✅ Yes     |
| < 1.3.0 | ❌ No      |

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please do not report it publicly. Instead, report it privately to the maintainers:

- **Email**: security@wiki-connect.org (placeholder)
- **Response time**: You can expect a response within 48 hours.

Please include:
- A detailed description of the vulnerability.
- Steps to reproduce (proof of concept).
- The potential impact.

## Recent Security Hardening (v1.3.0)

As of version 1.3.0, several critical security improvements were introduced:
- **CWE-502**: Standard Java serialization (via `ObjectInputStream`/`ObjectOutputStream`) has been completely replaced with safe, text-based JSON serialization in `FileCookieJar` to prevent deserialization execution gadget chains.
- **CWE-312**: Sensitive credentials (passwords, API tokens) are zeroed out from heap memory immediately after authentication requests.
- **CWE-918 / CWE-400**: Endpoint validation has been added to restrict schemes to HTTPS/HTTP, and client connection/socket timeouts are now enforced.

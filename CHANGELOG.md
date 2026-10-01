# Changelog

All notable documentation and project changes for StockSmart.

## [Unreleased]

### Changed — Canonical spec restored (2026-10-01)

- Restored **PROJECT.md** as the B.Tech college-level specification (replaced leftover enterprise/hexagonal/ledger guidelines).
- Restored **CLAUDE.md** to match the simplified layered architecture.

### Changed — College-level scope (2026-09-30)

- Added **PROJECT.md** as the canonical B.Tech project specification.
- Added **README.md**, **TODO.md**, and **CHANGELOG.md**.
- Simplified **CLAUDE.md** and updated `docs/*` to describe a **monolithic layered** architecture instead of hexagonal/enterprise patterns.
- Replaced double-entry ledger, GS1/EPCIS, Flyway-as-required, MapStruct-as-required, RFC 7807, optimistic locking, and offline scanner buffering with college-appropriate designs in documentation.
- Retained Mermaid diagrams where useful; updated labels and flows to match simplified inventory and security models.

### Added

- Initial repository documentation (`documentation added` commit): architecture, database, API, security, flows, and diagrams under `docs/`.

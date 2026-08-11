# Changelog

All notable changes are recorded here. The project follows semantic versioning after its first tagged release.

## [Unreleased]

### Added

- Hash-only evidence manifest and research provenance documentation.
- Public malware-handling, contribution, security, support, and maintenance policies.
- Bundled redacted fixtures with backend sanitization tests.
- Read-only CI for public-safety, backend, and frontend checks.

### Changed

- Replaced private-path log import with idempotent bundled-sample import.
- Defanged historical infrastructure across API, SQL, UI, and documentation.
- Reframed offensive reproduction material as defensive analysis exercises.
- Replaced hard-coded database credentials and broad CORS defaults with environment-driven local settings.

### Security

- Removed victim identifiers and workstation paths from the current public tree.
- Made it explicit that rule misses are not safety verdicts.
- Added automated rejection of dangerous artifacts and sensitive literals.

### Known residual risk

- Earlier public commits still contain removed text. History cleanup requires a separate, coordinated rewrite and is not part of this change.

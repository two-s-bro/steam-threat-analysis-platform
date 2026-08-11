# Roadmap

This roadmap describes intended maintenance; it is not a promise of dates or adoption.

## Next

- Merge the public-safety hardening change after review and CI.
- Add parser tests for malformed and multilingual timeline records.
- Document false-positive expectations for every bundled detection rule.
- Publish a first signed, source-only release with generated checksums.

## Later

- Add a database-free demo profile for easier contributor onboarding.
- Export the redacted attack timeline as STIX 2.1 without victim identifiers.
- Add schema validation for hash manifests and IOC fixtures.
- Establish a documented cadence for dependency review and issue triage.

## Not planned

- Hosting or distributing malware samples or recoverable source.
- Dynamic detonation, C2 probing, credential extraction, or phishing UI reproduction.
- Treating the educational text matcher as an antivirus or full YARA implementation.

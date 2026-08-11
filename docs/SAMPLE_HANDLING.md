# Malware sample handling policy

## Public-repository rule

This is a sample-free repository. The following must never be committed, attached, linked, generated in CI, or requested from contributors:

- executable or library binaries;
- Python bytecode or recoverable source from a malicious package;
- payloads, archives, disk images, memory dumps, packet captures, or raw logs;
- credentials, session material, personal identifiers, or workstation paths;
- content that restores a defanged IOC or enables phishing reproduction.

Renaming a file, adding `.SAFE_DISABLED`, encrypting it, or placing it in an archive does not make it acceptable for publication.

## What may be published

- SHA-256 hashes and byte sizes;
- defanged domains and paths that do not identify a victim;
- minimal synthetic or redacted text fixtures;
- defensive detections, false-positive notes, incident-response checks, and high-level behavior descriptions.

## Maintainer review checklist

1. Run `python scripts/verify_public_safety.py`.
2. Inspect the tracked-file list and diff for binary or encoded payload content.
3. Verify that every identifier is synthetic or explicitly redacted.
4. Verify that external infrastructure is non-clickable.
5. Reject the change if provenance or authorization is unclear.

The private evidence corpus is not part of the project workspace and must stay isolated under the owner's incident-evidence controls.

#!/usr/bin/env python3
"""Fail when the public tree contains unsafe artifacts or audited sensitive values."""

from __future__ import annotations

import hashlib
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SKIP_DIRS = {".git", "node_modules", "target", "dist", "__pycache__", ".idea", ".vscode"}
FORBIDDEN_SUFFIXES = {
    ".exe", ".dll", ".pyd", ".pyc", ".bin", ".rar", ".zip", ".7z", ".tar", ".gz"
}
MAX_PUBLIC_FILE_BYTES = 2 * 1024 * 1024

SENSITIVE_FINGERPRINTS = {
    "05ff7d29f9e40bbab9d1ca3f6caf1cb01ddee46589a48e3ad97f76f5e3cd74cd",
    "cc7d1328aaa5a815afe2c659b71c5c85cccc27499b8d8d1e898170bce4aa5573",
    "d793f4304bcfb0c23e47b8904c586299e47a7d186b53bed1c375189fa061cf60",
    "88350076803428bd8cc7e9c02ffac8a2b81250ed7b95e9135913cb89ea82d94b",
    "9c0050f209a2c3705050c0dfd987bf3e2efc405b9bfc37d8934133e11e0febb7",
    "eab950216d32ea2efc237eaadb46ddc3ad747aa78ef0462f3335007a8440cb05",
    "9e9663121a0595884fd120108e2389a1ab66a2ad8413d92b9c4060dfd54878f3",
    "cc867b7fe6e910c9e1487ca0ebde537ad19c2ba63049f5b34241b29051d5f90d",
}
TOKEN = re.compile(r"(?iu)[\w+.-]{4,128}")
STEAM_ID_64 = re.compile(r"\b7656\d{13}\b")
PRIVATE_HOME = re.compile(r"(?i)[a-z]:[\\/]{1,2}(?:users|用户)[\\/]{1,2}[^\\/\s]+")
WINDOWS_ABSOLUTE_PATH = re.compile(r"(?i)(?<![a-z0-9_])[a-z]:[\\/]{1,2}")
ACCOUNT_IDENTIFIER = re.compile(
    r"(?i)(?:account(?:_hash)?|账户(?:名)?(?:哈希)?|[?&]u)\s*[:=]\s*(?!\[REDACTED_)[0-9a-z]{8,20}"
)
AVATAR_IDENTIFIER = re.compile(
    r"(?i)(?:avatar(?:_hash)?|头像(?:哈希)?)\s*[:=]\s*(?!\[REDACTED_)[0-9a-f]{40}"
)
HARDCODED_PASSWORD = re.compile(r"(?im)^\s*password\s*:\s*(?!\$\{)\S+")
PRIVATE_KEY = re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----")


def contains_sensitive_fingerprint(text: str, fingerprints=SENSITIVE_FINGERPRINTS) -> bool:
    for token in TOKEN.findall(text):
        normalized = token.casefold().strip(".-+")
        if not normalized:
            continue
        candidates = [normalized]
        labels = normalized.split(".")
        candidates.extend(
            ".".join(labels[index:])
            for index in range(1, len(labels) - 1)
        )
        for candidate in candidates:
            fingerprint = hashlib.sha256(candidate.encode("utf-8")).hexdigest()
            if fingerprint in fingerprints:
                return True
    return False


def iter_public_files():
    for path in ROOT.rglob("*"):
        if not path.is_file():
            continue
        relative = path.relative_to(ROOT)
        if any(part in SKIP_DIRS for part in relative.parts):
            continue
        yield path, relative


def main() -> int:
    failures: list[str] = []
    checked = 0

    for path, relative in iter_public_files():
        checked += 1
        relative_text = relative.as_posix()
        lower_name = path.name.lower()

        if lower_name.endswith(".safe_disabled") or path.suffix.lower() in FORBIDDEN_SUFFIXES:
            failures.append(f"{relative_text}: forbidden artifact type")
            continue

        data = path.read_bytes()
        if len(data) > MAX_PUBLIC_FILE_BYTES:
            failures.append(f"{relative_text}: exceeds {MAX_PUBLIC_FILE_BYTES} bytes")
        if b"\x00" in data:
            failures.append(f"{relative_text}: binary/NUL content is not allowed")
            continue

        try:
            content = data.decode("utf-8")
        except UnicodeDecodeError:
            failures.append(f"{relative_text}: content is not UTF-8 text")
            continue

        searchable = relative_text + "\n" + content
        if contains_sensitive_fingerprint(searchable):
            failures.append(f"{relative_text}: contains an audited sensitive fingerprint")
        if STEAM_ID_64.search(searchable):
            failures.append(f"{relative_text}: contains an unredacted SteamID64-like value")
        if PRIVATE_HOME.search(searchable):
            failures.append(f"{relative_text}: contains an absolute user-home path")
        if WINDOWS_ABSOLUTE_PATH.search(searchable):
            failures.append(f"{relative_text}: contains a Windows drive-absolute path")
        if ACCOUNT_IDENTIFIER.search(searchable):
            failures.append(f"{relative_text}: contains an unredacted account identifier")
        if AVATAR_IDENTIFIER.search(searchable):
            failures.append(f"{relative_text}: contains an unredacted avatar identifier")
        if HARDCODED_PASSWORD.search(content):
            failures.append(f"{relative_text}: contains a hard-coded YAML password")
        if PRIVATE_KEY.search(content):
            failures.append(f"{relative_text}: contains private-key material")

    if failures:
        print("Public-safety verification failed:", file=sys.stderr)
        for failure in sorted(set(failures)):
            print(f"- {failure}", file=sys.stderr)
        return 1

    print(f"Public-safety verification passed: {checked} files checked.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

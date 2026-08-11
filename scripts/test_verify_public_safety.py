import hashlib
import unittest

from scripts.verify_public_safety import (
    PRIVATE_HOME,
    WINDOWS_ABSOLUTE_PATH,
    contains_sensitive_fingerprint,
)


class PublicPathPatternTest(unittest.TestCase):

    def test_detects_user_home_and_event_absolute_paths(self):
        private_homes = [
            "C:" + "\\Users\\analyst\\evidence.txt",
            "z:" + "\\\\USERS\\\\analyst\\\\evidence.txt",
            "D:" + "/users/analyst/evidence.txt",
            "E:" + "\\用户\\analyst\\evidence.txt",
        ]
        event_paths = [
            "C:" + "\\Steam\\client.log",
            "e:" + "\\\\steam\\\\client.log",
            "F:" + "/Games/Steam/client.log",
        ]

        for candidate in private_homes:
            with self.subTest(candidate=candidate):
                self.assertIsNotNone(PRIVATE_HOME.search(candidate))
                self.assertIsNotNone(WINDOWS_ABSOLUTE_PATH.search(candidate))
        for candidate in event_paths:
            with self.subTest(candidate=candidate):
                self.assertIsNotNone(WINDOWS_ABSOLUTE_PATH.search(candidate))

    def test_allows_redacted_and_environment_relative_paths(self):
        candidates = [
            "%USERPROFILE%\\AppData\\Local\\fixture.txt",
            "%STEAM_ROOT%\\steamui\\fixture.js",
            "%SystemRoot%\\System32\\fixture.dll",
            "[PRIVATE_HOME]\\fixture.txt",
            "relative/path/fixture.txt",
        ]

        for candidate in candidates:
            with self.subTest(candidate=candidate):
                self.assertIsNone(PRIVATE_HOME.search(candidate))
                self.assertIsNone(WINDOWS_ABSOLUTE_PATH.search(candidate))

    def test_irreversible_fingerprint_matching_uses_only_synthetic_data(self):
        synthetic = "syntheticaccount42"
        fingerprint = hashlib.sha256(synthetic.encode("utf-8")).hexdigest()

        self.assertTrue(contains_sensitive_fingerprint(
            "account: " + synthetic,
            {fingerprint},
        ))
        self.assertFalse(contains_sensitive_fingerprint(
            "account: [REDACTED_ACCOUNT]",
            {fingerprint},
        ))

    def test_detects_audited_domain_inside_subdomains(self):
        synthetic_domain = "synthetic.example"
        fingerprint = hashlib.sha256(synthetic_domain.encode("utf-8")).hexdigest()

        for candidate in (
            "cdn." + synthetic_domain,
            "25.edge." + synthetic_domain,
            "https://cdn." + synthetic_domain + ".",
        ):
            with self.subTest(candidate=candidate):
                self.assertTrue(contains_sensitive_fingerprint(
                    candidate,
                    {fingerprint},
                ))

        self.assertFalse(contains_sensitive_fingerprint(
            "synthetic.invalid",
            {fingerprint},
        ))

    def test_detects_audited_unicode_path_component(self):
        synthetic_component = "\u5408\u6210\u9694\u79bb\u76ee\u5f55600"
        fingerprint = hashlib.sha256(
            synthetic_component.casefold().encode("utf-8")
        ).hexdigest()

        self.assertTrue(contains_sensitive_fingerprint(
            "fixtures/" + synthetic_component + "/sample.txt",
            {fingerprint},
        ))
        self.assertFalse(contains_sensitive_fingerprint(
            "fixtures/public-samples/sample.txt",
            {fingerprint},
        ))


if __name__ == "__main__":
    unittest.main()

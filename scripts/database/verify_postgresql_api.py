"""Smoke test seeded authentication and key reads against a PostgreSQL backend."""

import json
import os
from urllib.request import Request, urlopen


BASE = os.environ.get("SHIYU_VERIFY_BASE_URL", "http://127.0.0.1:19001")


def request(path: str, token: str | None = None, body: dict | None = None) -> dict:
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode() if body is not None else None
    with urlopen(Request(BASE + path, data=data, headers=headers), timeout=10) as response:
        return json.load(response)


def main() -> None:
    login = request(
        "/api/iam/auth/login", body={"username": "admin", "password": "123456"}
    )
    if login.get("code") != 200 or not login.get("data", {}).get("accessToken"):
        raise AssertionError(f"PostgreSQL seeded login failed: {login.get('code')}")
    token = login["data"]["accessToken"]
    for path in (
        "/api/education/exam/available-summary",
        "/api/agent/intents/options",
        "/api/model/platforms/options",
        "/api/model/model-configurations/options",
        "/api/iam/dicts?pageNum=1&pageSize=10",
    ):
        result = request(path, token=token)
        if result.get("code") != 200:
            raise AssertionError(f"PostgreSQL API failed: {path}: {result.get('code')}")
    print("PostgreSQL seeded login and five business reads passed.")


if __name__ == "__main__":
    main()

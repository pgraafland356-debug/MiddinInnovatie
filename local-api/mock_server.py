#!/usr/bin/env python3
"""
Minimal local API for Middin Innovatie debug builds.
Endpoints match the Android app (AuthRepository). Chat in the app is local (Room + product knowledge); this server still exposes /chat/messages for testing RemoteChatRepository if wired again.

Listen on 0.0.0.0:8080 so a physical phone can use http://<PC_LAN_IP>:8080
(Emulator: http://10.0.2.2:8080)
"""
from __future__ import annotations

import json
import threading
import time
import uuid
from http.server import BaseHTTPRequestHandler, HTTPServer
from typing import Any
from urllib.parse import urlparse

HOST = "0.0.0.0"
PORT = 8080
DEV_TOKEN = "local-mock-api-token"

_messages: list[dict[str, Any]] = []
_lock = threading.Lock()


def _json_body(handler: BaseHTTPRequestHandler) -> dict[str, Any]:
    length = int(handler.headers.get("Content-Length", "0") or "0")
    if length <= 0:
        return {}
    raw = handler.rfile.read(length)
    try:
        return json.loads(raw.decode("utf-8"))
    except json.JSONDecodeError:
        return {}


def _send_json(handler: BaseHTTPRequestHandler, status: int, obj: Any) -> None:
    data = json.dumps(obj).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Content-Length", str(len(data)))
    handler.end_headers()
    handler.wfile.write(data)


def _bearer(handler: BaseHTTPRequestHandler) -> str | None:
    auth = handler.headers.get("Authorization", "")
    if auth.lower().startswith("bearer "):
        return auth[7:].strip()
    return None


def _path_kind(raw_path: str) -> str | None:
    """Match /auth/login and /api/v1/auth/login (any prefix before final segment)."""
    path = urlparse(raw_path).path
    if path.rstrip("/").endswith("/auth/login"):
        return "login"
    if path.rstrip("/").endswith("/chat/messages"):
        return "chat"
    return None


class Handler(BaseHTTPRequestHandler):
    def log_message(self, format: str, *args: Any) -> None:
        print("[%s] %s - %s" % (self.log_date_time_string(), self.address_string(), format % args))

    def do_POST(self) -> None:
        kind = _path_kind(self.path)

        if kind == "login":
            body = _json_body(self)
            user = body.get("username") or body.get("email") or ""
            pwd = body.get("password") or ""
            if not user.strip() or not pwd:
                _send_json(self, 400, {"error": "username/email and password required"})
                return
            _send_json(self, 200, {"token": DEV_TOKEN})
            return

        if kind == "chat":
            token = _bearer(self)
            if token != DEV_TOKEN:
                _send_json(self, 401, {"error": "unauthorized"})
                return
            body = _json_body(self)
            text = (body.get("text") or "").strip()
            if not text:
                _send_json(self, 400, {"error": "empty text"})
                return
            entry = {
                "id": str(uuid.uuid4()),
                "text": text,
                "authorName": "You",
                "createdAt": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            }
            with _lock:
                _messages.append(entry)
            _send_json(self, 200, {"ok": True})
            return

        self.send_error(404, "Not Found")

    def do_GET(self) -> None:
        kind = _path_kind(self.path)

        if kind == "chat":
            token = _bearer(self)
            if token != DEV_TOKEN:
                _send_json(self, 401, {"error": "unauthorized"})
                return
            with _lock:
                snapshot = list(_messages)
            _send_json(self, 200, snapshot)
            return

        path_only = urlparse(self.path).path.rstrip("/") or "/"
        if path_only in ("/", "/health"):
            _send_json(
                self,
                200,
                {
                    "ok": True,
                    "service": "middin-mock-api",
                    "endpoints": ["POST /auth/login", "GET /chat/messages", "POST /chat/messages"],
                },
            )
            return

        self.send_error(404, "Not Found")


def main() -> None:
    server = HTTPServer((HOST, PORT), Handler)
    print("Middin mock API listening on http://127.0.0.1:%s" % PORT)
    print("  Emulator: http://10.0.2.2:%s" % PORT)
    print("  Phone on Wi-Fi: http://<this-PC-LAN-IP>:%s" % PORT)
    print("  Login accepts any non-empty username + password.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopped.")
        server.shutdown()


if __name__ == "__main__":
    main()

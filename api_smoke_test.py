#!/usr/bin/env python3
"""Smoke-test скрипт для REST API lab1.

Запуск:  python3 api_smoke_test.py [BASE_URL]  (по умолчанию http://localhost:8080)

Использует только стандартную библиотеку (urllib) — внешних зависимостей нет.
Выход: 0 если все проверки прошли, 1 если есть хотя бы один FAIL.
"""

import json
import random
import string
import sys
import urllib.error
import urllib.request

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

# --- Счётчики результатов -------------------------------------------------
passed = 0
failed = 0


def check(name, ok, detail=""):
    """Печатает результат одной проверки и ведёт статистику."""
    global passed, failed
    status = "PASS" if ok else "FAIL"
    if ok:
        passed += 1
    else:
        failed += 1
    line = f"[{status}] {name}"
    if detail:
        line += f"  -> {detail}"
    print(line)


# --- Низкоуровневые помощники ---------------------------------------------
def request(method, path, body=None, token=None):
    """Выполняет HTTP-запрос. Возвращает (status, json_or_None)."""
    url = BASE_URL + path
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode()
            return resp.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        parsed = None
        if raw:
            try:
                parsed = json.loads(raw)
            except json.JSONDecodeError:
                parsed = raw
        return e.code, parsed
    except urllib.error.URLError as e:
        print(f"  !! Не удалось подключиться к {url}: {e.reason}")
        print("     Проверьте, что сервер запущен и порт корректен.")
        sys.exit(2)


def random_username(prefix="user"):
    return prefix + "".join(random.choices(string.ascii_lowercase + string.digits, k=8))


# --- Проверки --------------------------------------------------------------
def test_register_ok():
    """Успешная регистрация -> 201 + UserResponse(id, username)."""
    user = random_username()
    code, body = request("POST", "/auth/register", {"username": user, "password": "SecretPass123"})
    ok = code == 201 and body and body.get("username") == user and isinstance(body.get("id"), int)
    check("register: валидные данные -> 201 + тело", ok, str(body))
    return user


def test_register_duplicate(username):
    """Повторная регистрация того же username -> 409 conflict."""
    code, body = request("POST", "/auth/register", {"username": username, "password": "SecretPass123"})
    check("register: дубликат username -> 409", code == 409 and body.get("error") == "conflict",
          f"status={code}, body={body}")


def test_register_validation():
    """Некорректные данные -> 400 bad_request."""
    cases = [
        ({"username": "ab", "password": "SecretPass123"}, "короткий username"),
        ({"username": "ok_username", "password": "short"}, "короткий пароль"),
        ({"username": "bad user!", "password": "SecretPass123"}, "недопустимые символы"),
    ]
    for payload, label in cases:
        code, body = request("POST", "/auth/register", payload)
        check(f"register: {label} -> 400", code == 400 and body.get("error") == "bad_request",
              f"status={code}, body={body}")


def test_login_ok():
    """Логин -> 200 + AuthResponse(token, tokenType='Bearer', expiresIn)."""
    user = random_username()
    request("POST", "/auth/register", {"username": user, "password": "SecretPass123"})
    code, body = request("POST", "/auth/login", {"username": user, "password": "SecretPass123"})
    ok = code == 200 and body and body.get("tokenType") == "Bearer" and body.get("token") and body.get("expiresIn")
    check("login: верные креды -> 200 + токен", ok, str(body))
    return body["token"] if ok else None


def test_login_wrong_password():
    """Неверный пароль -> 401 unauthorized."""
    user = random_username()
    request("POST", "/auth/register", {"username": user, "password": "SecretPass123"})
    code, body = request("POST", "/auth/login", {"username": user, "password": "WrongPass123"})
    check("login: неверный пароль -> 401", code == 401 and body.get("error") == "unauthorized",
          f"status={code}, body={body}")


def test_login_missing_fields():
    """Пустые креды -> 400 bad_request."""
    code, body = request("POST", "/auth/login", {"username": "", "password": ""})
    check("login: пустые поля -> 400", code == 400, f"status={code}, body={body}")


def test_data_without_token():
    """GET /api/data без токена -> 401."""
    code, body = request("GET", "/api/data")
    check("data: без токена -> 401", code == 401, f"status={code}, body={body}")


def test_data_with_token(token):
    """GET /api/data с токеном -> 200 + список."""
    if not token:
        check("data: с токеном -> 200 (пропущено, нет токена)", False, "token отсутствует")
        return
    code, body = request("GET", "/api/data", token=token)
    check("data: с токеном -> 200 + список", code == 200 and isinstance(body, list),
          f"status={code}, count={len(body) if isinstance(body, list) else 'N/A'}")


def main():
    print(f"Тестирую API на {BASE_URL}\n")
    user = test_register_ok()
    test_register_duplicate(user)
    test_register_validation()
    token = test_login_ok()
    test_login_wrong_password()
    test_login_missing_fields()
    test_data_without_token()
    test_data_with_token(token)

    print(f"\nИтого: {passed} passed, {failed} failed")
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
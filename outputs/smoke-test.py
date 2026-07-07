#!/usr/bin/env python3
"""
智教通（Smart Edu Scheduler）后端 API 冒烟测试脚本
==================================================
仅依赖 requests 库，按顺序执行 12 个核心接口验收用例。
所有测试失败不中断，继续执行后续用例，最后输出通过率汇总。
"""

import json
import os
import sys
import time
from typing import Any, Dict, Optional, Tuple

import requests


# ============================================================
# 配置
# ============================================================
BASE_URL = os.environ.get("SMART_EDU_BASE_URL", "http://localhost:8085")
HEALTH_BASE_URL = os.environ.get("SMART_EDU_HEALTH_BASE_URL", BASE_URL)
TIMEOUT = 10  # 单次请求超时秒数

# 种子账号（来自 STARTUP-GUIDE.md）
ACCOUNTS = {
    "admin":     {"username": "admin",     "password": "password123"},
    "student01": {"username": "student01", "password": "password123"},
    "teacher01": {"username": "teacher01", "password": "password123"},
    "academic01":{"username": "academic01","password": "password123"},
    "qbadmin01": {"username": "qbadmin01", "password": "password123"},
}

# 全局 token 缓存（测试间共享）
TOKENS: Dict[str, str] = {}

# 测试结果汇总
RESULTS: list[Dict[str, Any]] = []


# ============================================================
# 工具函数
# ============================================================
def log_result(test_id: int, name: str, passed: bool, detail: str = "") -> None:
    """记录单条测试结果并打印到标准输出。"""
    status = "[PASS]" if passed else "[FAIL]"
    msg = f"Test {test_id:02d} {status} {name}"
    if detail:
        msg += f" — {detail}"
    print(msg)
    RESULTS.append({"id": test_id, "name": name, "passed": passed, "detail": detail})


def _login_raw(account_key: str) -> Tuple[Optional[str], Optional[str]]:
    """
    执行登录，返回 (accessToken, error_message)。
    不记录测试结果（供内部复用）。
    """
    creds = ACCOUNTS[account_key]
    url = f"{BASE_URL}/api/v1/auth/login"
    try:
        resp = requests.post(
            url,
            json={"username": creds["username"], "password": creds["password"]},
            timeout=TIMEOUT,
            headers={"Content-Type": "application/json"},
        )
    except requests.RequestException as e:
        return None, f"请求异常: {e}"

    if resp.status_code != 200:
        return None, f"HTTP {resp.status_code}: {resp.text[:120]}"

    try:
        body = resp.json()
    except ValueError:
        return None, f"非 JSON 响应: {resp.text[:120]}"

    if body.get("code") != 200:
        return None, f"业务码 {body.get('code')}: {body.get('message')}"

    token = body.get("data", {}).get("accessToken", "")
    if not token:
        return None, "响应中无 accessToken"

    TOKENS[account_key] = token
    return token, None


def do_login(account_key: str, test_id: int, test_name: str) -> Optional[str]:
    """
    执行登录并记录测试结果，返回 accessToken。
    """
    token, err = _login_raw(account_key)
    creds = ACCOUNTS[account_key]
    if err:
        log_result(test_id, test_name, False, err)
        return None
    log_result(test_id, test_name, True,
               f"用户={creds['username']}, token={token[:20]}...")
    return token


def auth_get(account_key: str, path: str, test_id: int, test_name: str,
             params: Optional[Dict] = None) -> Optional[requests.Response]:
    """
    带 Bearer token 的 GET 请求，自动校验 HTTP 200 与业务码 200。
    """
    token = TOKENS.get(account_key, "")
    if not token:
        log_result(test_id, test_name, False, f"缺少 {account_key} token，请确认登录测试先通过")
        return None

    url = f"{BASE_URL}{path}"
    headers = {"Authorization": f"Bearer {token}"}
    try:
        resp = requests.get(url, headers=headers, params=params, timeout=TIMEOUT)
    except requests.RequestException as e:
        log_result(test_id, test_name, False, f"请求异常: {e}")
        return None

    if resp.status_code != 200:
        log_result(test_id, test_name, False,
                   f"HTTP {resp.status_code}: {resp.text[:120]}")
        return None

    try:
        body = resp.json()
    except ValueError:
        log_result(test_id, test_name, False, f"非 JSON 响应: {resp.text[:120]}")
        return None

    if body.get("code") != 200:
        log_result(test_id, test_name, False,
                   f"业务码 {body.get('code')}: {body.get('message')}")
        return None

    # 简短描述响应 data 概况
    data = body.get("data")
    desc = describe_data(data)
    log_result(test_id, test_name, True, desc)
    return resp


def describe_data(data: Any) -> str:
    """生成对响应 data 的简短描述。"""
    if data is None:
        return "data=null"
    if isinstance(data, list):
        return f"列表, 长度={len(data)}"
    if isinstance(data, dict):
        keys = list(data.keys())
        return f"对象, 字段={keys[:6]}"
    return f"类型={type(data).__name__}"


# ============================================================
# 测试用例
# ============================================================

def test_01_health_check() -> None:
    """健康检查"""
    paths = ("/api/v1/actuator/health", "/actuator/health")
    failures = []
    for path in paths:
        url = f"{HEALTH_BASE_URL}{path}"
        try:
            resp = requests.get(url, timeout=TIMEOUT)
        except requests.RequestException as e:
            failures.append(f"{path}=请求异常:{e}")
            continue

        if resp.status_code != 200:
            failures.append(f"{path}=HTTP {resp.status_code}")
            continue

        try:
            body = resp.json()
        except ValueError:
            failures.append(f"{path}=非 JSON 响应")
            continue

        if body.get("status") == "UP":
            log_result(1, "健康检查", True, f"经由 {path}, status=UP")
            return
        failures.append(f"{path}=status {body.get('status')}")

    log_result(1, "健康检查", False, "; ".join(failures))


def test_02_admin_login() -> None:
    """管理员登录"""
    do_login("admin", 2, "管理员登录")


def test_03_get_current_user() -> None:
    """获取当前用户信息"""
    auth_get("admin", "/api/v1/auth/me", 3, "获取当前用户")


def test_04_course_list() -> None:
    """课程列表"""
    auth_get("admin", "/api/v1/courses/offerings", 4, "课程列表")


def test_05_student_login() -> None:
    """学生登录"""
    do_login("student01", 5, "学生登录")


def test_06_student_schedule() -> None:
    """学生课表"""
    auth_get("student01", "/api/v1/schedules/my", 6, "学生课表")


def test_07_student_enrollments() -> None:
    """学生选课列表"""
    auth_get("student01", "/api/v1/enrollments/my", 7, "学生选课列表")


def test_08_exam_list() -> None:
    """考试列表 (使用 admin token)"""
    auth_get("admin", "/api/v1/exams", 8, "考试列表")


def test_09_admin_roles() -> None:
    """角色管理 (Admin)"""
    auth_get("admin", "/api/v1/admin/roles", 9, "角色管理")


def test_10_academic_stats() -> None:
    """教务统计 (academic01 先登录)"""
    if "academic01" not in TOKENS:
        token, err = _login_raw("academic01")
        if err:
            log_result(10, "教务统计", False, f"教务登录失败: {err}")
            return
    auth_get("academic01", "/api/v1/academic/enrollments/stats", 10, "教务统计")


def test_11_teacher_scores() -> None:
    """教师查看成绩 (teacher01 先登录)"""
    if "teacher01" not in TOKENS:
        token, err = _login_raw("teacher01")
        if err:
            log_result(11, "教师成绩", False, f"教师登录失败: {err}")
            return
    auth_get("teacher01", "/api/v1/scores/offering/1", 11, "教师成绩")


def test_12_question_audit() -> None:
    """题库审核列表 (qbadmin01 先登录)"""
    if "qbadmin01" not in TOKENS:
        token, err = _login_raw("qbadmin01")
        if err:
            log_result(12, "题库审核列表", False, f"题库管理员登录失败: {err}")
            return
    auth_get("qbadmin01", "/api/v1/questions/audit/list", 12, "题库审核列表",
             params={"page": 1, "size": 5})


# ============================================================
# 主流程
# ============================================================
def print_summary() -> None:
    """打印通过率汇总。"""
    total = len(RESULTS)
    passed = sum(1 for r in RESULTS if r["passed"])
    failed = total - passed
    rate = (passed / total * 100) if total > 0 else 0.0

    print()
    print("=" * 56)
    print("  智教通 API 冒烟测试报告")
    print("=" * 56)
    print(f"  服务地址 : {BASE_URL}")
    print(f"  总用例数 : {total}")
    print(f"  通    过 : {passed}")
    print(f"  失    败 : {failed}")
    print(f"  通过率   : {rate:.1f}%")
    print("=" * 56)

    if failed:
        print()
        print("失败用例明细:")
        for r in RESULTS:
            if not r["passed"]:
                print(f"  - Test {r['id']:02d} {r['name']}: {r['detail']}")

    print()
    if rate == 100.0:
        print("[OK] 全部通过!")
    elif rate >= 80.0:
        print("[WARN] 大部分通过，请检查失败用例。")
    else:
        print("[FAIL] 通过率较低，建议确认服务是否正常启动。")


def main() -> None:
    print(f"智教通 API 冒烟测试开始 — {BASE_URL}")
    print(f"时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print()

    # 按顺序执行全部 12 个测试
    tests = [
        test_01_health_check,
        test_02_admin_login,
        test_03_get_current_user,
        test_04_course_list,
        test_05_student_login,
        test_06_student_schedule,
        test_07_student_enrollments,
        test_08_exam_list,
        test_09_admin_roles,
        test_10_academic_stats,
        test_11_teacher_scores,
        test_12_question_audit,
    ]

    for test_fn in tests:
        try:
            test_fn()
        except Exception as exc:
            # 防御性兜底：即使测试函数内部抛出未预期异常也不中断
            name = test_fn.__name__
            log_result(0, name, False, f"未预期异常: {exc}")

    print_summary()
    sys.exit(0 if all(r["passed"] for r in RESULTS) else 1)


if __name__ == "__main__":
    main()

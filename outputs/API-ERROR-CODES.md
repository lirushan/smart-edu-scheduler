# 智教通 API 错误码表

| 错误码 | 枚举 | 含义 | HTTP |
|:---:|------|------|:---:|
| 200 | — | 成功 | 200 |
| 10001 | PARAM_INVALID | 参数校验失败 | 400 |
| 10002 | EXAM_NOT_STARTED | 考试未开始 | 400 |
| 10003 | EXAM_ENDED | 考试已结束 | 400 |
| 10004 | EXAM_NOT_FOUND | 考试不存在 | 404 |
| 10005 | ALREADY_SUBMITTED | 已交卷 | 409 |
| 10006 | ROUND_NOT_ACTIVE | 选课轮次未激活 | 400 |
| 10007 | ENROLLMENT_LIMIT_EXCEEDED | 选课门数超限 | 400 |
| 10008 | CREDIT_LIMIT_EXCEEDED | 学分超限 | 400 |
| 10009 | COURSE_FULL | 课程已满 | 400 |
| 10010 | CONFLICT | 数据冲突/重复 | 409 |
| 10011 | UNAUTHORIZED | 未授权 | 401 |
| 10012 | FORBIDDEN | 角色权限不足 | 403 |
| 10013 | NOT_FOUND | 资源不存在 | 404 |
| 10014 | BAD_REQUEST | 请求错误 | 400 |
| 99999 | INTERNAL_ERROR | 服务器内部错误 | 500 |

## 前端错误处理流程

```
axios interceptor
  ├─ response.data.code === 200 → 返回 data.data
  ├─ response.data.code !== 200 → ElMessage.error(message)
  │                                → Promise.reject
  └─ 网络异常:
       ├─ 401 → 清除 token → 跳转登录页
       ├─ 429 → "请求过于频繁"
       └─ 其他 → ElMessage.error("网络异常")
```

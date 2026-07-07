# 智教通（Smart Edu Scheduler）本地开发启动指南

## 环境要求

| 工具 | 版本 |
|------|------|
| Docker Desktop | 26+ |
| JDK | 21 |
| Node.js | 22 |
| Maven | 3.9+ |
| Git | 2.40+ |

## 快速启动（Docker 全栈）

```bash
# 1. 启动所有服务（首次需构建 3-5 分钟）
docker compose up -d

# 2. 查看服务状态
docker ps --filter "name=smart-edu"
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:8085 | Vue 3 + Element Plus 紫色毛玻璃 |
| 后端 API | http://localhost:8086 | Spring Boot 3.4.3 |
| API 文档 | http://localhost:8086/doc.html | Knife4j |
| MySQL | localhost:3308 | 数据库 |
| Redis | localhost:6379 | 缓存/分布式锁 |
| RabbitMQ | http://localhost:15672 | 消息队列管理（guest/guest） |

## 种子账号

| 账号 | 角色 | 密码 |
|------|------|------|
| admin | 管理员 | password123 |
| student01 | 学生（张明远） | password123 |
| teacher01 | 教师（赵教授） | password123 |
| academic01 | 教务人员 | password123 |
| qbadmin01 | 题库管理员 | password123 |

## 本地前后端开发（热重载）

```bash
# 先确保 Docker 中间件已启动
docker compose up -d mysql redis rabbitmq

# 启动后端（本地 IDE/JAR）
cd smart-edu-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8090

# 启动前端（Vite dev server）
cd smart-edu-frontend
npm run dev  # 默认端口 8085，代理 /api → localhost:8090
```

## 健康检查

```bash
# 后端
curl http://localhost:8086/actuator/health

# 前端
curl -o /dev/null -w "%{http_code}" http://localhost:8085/
```

## 停止服务

```bash
docker compose down          # 停止所有容器
docker compose down -v       # 停止并删除数据卷（会清空数据库！）
```

## Docker 镜像重建

```bash
# 修改代码后重新构建
docker compose build backend    # 仅后端（10-15s）
docker compose build frontend   # 仅前端（15-20s）
docker compose up -d backend frontend  # 重启新镜像
```

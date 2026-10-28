# TCP 端口检测服务

一个基于 Spring Boot 的 HTTP 服务，用于检测单个主机的一段 TCP 端口，或批量检测多组主机与端口。项目提供 Swagger UI，可直接在浏览器中查看和调试接口。

## 环境要求

- JDK 17
- Maven 3.9+

先确认 Maven 使用的也是 JDK 17：

```bash
java -version
mvn -version
```

两条命令的 Java 版本都应为 `17`。首次构建时 Maven 会下载依赖，需要能够访问 Maven 仓库。

## 启动项目

克隆代码并进入项目目录：

```bash
git clone <仓库地址>
cd demo
```

先运行测试：

```bash
mvn clean test
```

看到 `BUILD SUCCESS` 后启动服务：

```bash
mvn spring-boot:run
```

日志中出现以下内容表示启动成功：

```text
Tomcat started on port 8080 (http)
Started DemoApplication
```

服务默认监听 `8080` 端口。浏览器打开以下地址可以进一步确认：

- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

按 `Ctrl+C` 停止服务。

## 接口验证

### 检测单个主机的端口范围

启动服务后，可用服务自身监听的 `8080` 端口进行验证。

macOS / Linux：

```bash
curl "http://localhost:8080/tcp/single?path=localhost&startPort=8080&endPort=8080&timeout=1000"
```

Windows PowerShell：

```powershell
Invoke-RestMethod "http://localhost:8080/tcp/single?path=localhost&startPort=8080&endPort=8080&timeout=1000" | ConvertTo-Json -Depth 10
```

预期结果中 `code` 为 `1`，开放端口列表包含 `8080`：

```json
{
  "data": {
    "path": "localhost",
    "portRange": "8080-8080",
    "passablePortList": [8080],
    "passablePortCount": 1
  },
  "message": "成功",
  "code": 1
}
```

查询参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `path` | `localhost` | IP 或域名，也可传入带 `http://`、`https://` 和路径的地址 |
| `startPort` | `0` | 起始端口，包含该端口 |
| `endPort` | `1024` | 结束端口，包含该端口 |
| `timeout` | `1000` | 每次 TCP 连接的超时时间，单位为毫秒 |

### 批量检测主机与端口

macOS / Linux：

```bash
curl -X POST "http://localhost:8080/tcp/multi" \
  -H "Content-Type: application/json" \
  -d '{"socketList":[{"path":"localhost","port":8080}],"timeout":1000}'
```

Windows PowerShell：

```powershell
$body = @{
  socketList = @(
    @{ path = "localhost"; port = 8080 }
  )
  timeout = 1000
} | ConvertTo-Json -Depth 5

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/tcp/multi" `
  -ContentType "application/json" `
  -Body $body | ConvertTo-Json -Depth 10
```

预期结果中 `8080` 的 `passable` 为 `true`：

```json
{
  "data": [
    { "path": "localhost", "port": 8080, "passable": true }
  ],
  "message": "成功",
  "code": 1
}
```

## 打包运行

```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

打包过程会自动执行测试。启动后的验证方式与上文相同。

## 常见问题

### `8080` 端口已被占用

临时改用其他端口启动，例如 `9090`：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

PowerShell 中如果参数被错误解析，可以加引号：

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=9090"
```

随后将验证命令和 Swagger 地址中的 `8080` 替换为 `9090`。

### 编译时提示 Lombok 生成的方法不存在

确认 `mvn -version` 显示 Maven 正在使用 JDK 17，然后重新执行：

```bash
mvn clean test
```

IDE 中运行时还需开启注解处理（Annotation Processing），但使用上述 Maven 命令不依赖 IDE 设置。

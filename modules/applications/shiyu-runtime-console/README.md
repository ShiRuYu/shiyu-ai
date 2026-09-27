# ShiYu 本地运行控制台

控制台静态资源由独立的 Vue 3 / TypeScript 工程构建到当前模块资源中。Spring Boot 仍只有 `ShiyuBootstrapApplication` 一个入口；IDEA、直接运行 Jar 和 Windows 启动器使用同一份页面及管理 API。

## 运行与授权

- 开发 / IDEA：使用现有启动类，访问 `http://127.0.0.1:9000/console/`。启动输出中的 `SHIYU_CONSOLE_URL=...` 是一次性本机授权链接；浏览器兑换后会立即从地址栏清除授权凭证。保存启动项后由 IDEA 用户手动重启。
- 直接运行 Jar：以 `--spring.profiles.active=windows` 加载桌面运行配置，使用系统属性 `-Dapp.home=...` 或环境变量 `APP_HOME` 指定持久化目录。默认端口为 9000。
- 一般生产 profile 默认关闭控制台；可通过 `SHIYU_CONSOLE_ENABLED=true` 显式开启。页面和管理 API 仅接受 loopback 来源，不作为远程管理入口。

一次性授权链接只在本机控制台启动输出中显示，不要复制到工单、诊断包或共享日志。管理 Cookie 为进程内存会话；业务 Token 仍须由接口工作台的业务登录获得，并继续经过原有业务授权。

## Windows x64 app-image

构建机需要 Windows x64、JDK 21（含 `jpackage`）、Maven、Node.js 和 pnpm。前端资源应先在 `console-ui` 运行 `pnpm install --frozen-lockfile`、`pnpm test`、`pnpm build`；构建命令会将页面资源写入 `src/main/resources/META-INF/resources/console/`。

从仓库根目录执行：

```powershell
$env:SHIYU_JDK21_HOME = 'C:\Program Files\Java\jdk-21'
.\scripts\package-runtime-console-windows.ps1 -OutputDirectory dist\windows-x64
.\scripts\verify-runtime-console-app-image.ps1 -AppImageDirectory dist\windows-x64\ShiYu
```

结果是 `dist/windows-x64/ShiYu/` 自包含发行目录，内有 `ShiYu.exe`、Java 运行时和 Jar。它不是可脱离资源运行的单文件 EXE。把整个 `ShiYu` 目录解压到一个路径（允许中文和空格），再双击 `ShiYu.exe`。默认数据目录为 `%LOCALAPPDATA%\ShiYu\runtime`；启动器控制界面可改下一次使用的 `APP_HOME`，改目录不会迁移、复制、覆盖或删除旧数据。

`verify-runtime-console-app-image.ps1` 会在临时数据目录中用随包 Java 启动后端，检查控制台页面及资源可读、管理 API 未授权时拒绝访问，并在退出后清理本次临时数据。打包脚本保留 Java 原生启动器命令，保证托盘程序能启动其后端子进程。

启动器会持有后端子进程及其 Job Object，显示启动日志，并提供浏览器控制台、启动、受控停止 / 重启和配置恢复入口。控制台不就绪、端口占用、数据目录锁冲突或启动器托管的后端异常退出时，启动器显示错误并保留日志；异常退出不会自动无限重启。普通托管重启先发出内网授权关闭请求，超时后只终止自身 Job Object 中的进程树。

## 数据与恢复说明

- 默认发行 profile 使用 H2、本地文件、JVector。外部 LLM 账户 / 模型、PostgreSQL、Redis、S3、Kafka 等外部设施不由发行包创建或迁移。
- 重启生效的控制台配置保存在 `APP_HOME/data/runtime-console/config/` 的版本化快照；密钥仅由 Windows 当前用户 DPAPI 加密。跨 Windows 用户或 DPAPI 解密失败时会明确启动失败，不回退明文。
- 配置保存不会改写已经运行的连接池或 provider。`恢复上次成功版本` 只创建一个恢复快照，不回滚业务数据库内容。
- 控制台管理会话不能调用业务 API；接口工作台限当前实例 `/api/**`。Token 与密码仅驻留当前页面内存，页面重载后应重新登录。
- IDEA 模式下网页不会停止或重启 IDEA 进程；使用 IDEA 按钮或重新运行启动类。

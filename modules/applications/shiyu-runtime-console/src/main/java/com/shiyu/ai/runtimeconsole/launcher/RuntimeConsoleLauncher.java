package com.shiyu.ai.runtimeconsole.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/** jpackage 启动入口；Spring Boot 应用仍是唯一的业务应用。 */
public final class RuntimeConsoleLauncher {

    private static final String TITLE = "ShiYu 本地运行控制台";
    private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int LOG_LIMIT = 5 * 1024 * 1024;

    private final Path installRoot;
    private final Path supportDirectory;
    private final Path settingsFile;
    private final Path launcherLog;
    private final FileChannel lockChannel;
    private final FileLock instanceLock;
    private final Properties settings = new Properties();
    private final Deque<String> recentLines = new ArrayDeque<>();
    private final AtomicReference<String> requestedAction = new AtomicReference<>("");
    private final AtomicBoolean closed = new AtomicBoolean();
    private final SecureRandom secureRandom = new SecureRandom();
    private final String controlToken = randomToken();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LauncherCommandServer commandServer;
    private volatile Process backendProcess;
    private volatile WindowsJobObject jobObject;
    private Path appHome;
    private volatile URI consoleBase;
    private JFrame window;
    private JLabel statusLabel;
    private JTextField appHomeField;
    private JTextArea logArea;
    private JButton stopButton;
    private JButton restartButton;
    private TrayIcon trayIcon;

    private RuntimeConsoleLauncher() throws Exception {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            throw new IllegalStateException("ShiYu Windows 启动器只能在 Windows 上运行");
        }
        installRoot = discoverInstallRoot();
        supportDirectory = localApplicationData().resolve("ShiYu");
        Files.createDirectories(supportDirectory);
        settingsFile = supportDirectory.resolve("launcher.properties");
        launcherLog = supportDirectory.resolve("launcher.log");
        lockChannel = FileChannel.open(supportDirectory.resolve("launcher.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock acquired;
        try {
            acquired = lockChannel.tryLock();
        } catch (OverlappingFileLockException exception) {
            acquired = null;
        }
        if (acquired == null) {
            lockChannel.close();
            throw new AlreadyRunningException();
        }
        instanceLock = acquired;
        loadSettings();
        String configuredHome = settings.getProperty("app.home", "").trim();
        appHome = configuredHome.isEmpty() ? supportDirectory.resolve("runtime") : Path.of(configuredHome);
        appHome = appHome.toAbsolutePath().normalize();
        commandServer = new LauncherCommandServer(controlToken, this::handleLauncherCommand);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            RuntimeConsoleLauncher launcher = new RuntimeConsoleLauncher();
            Runtime.getRuntime().addShutdownHook(new Thread(launcher::close, "shiyu-launcher-close"));
            SwingUtilities.invokeLater(launcher::showWindow);
        } catch (AlreadyRunningException exception) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "ShiYu 启动器已在运行，请从系统托盘打开现有窗口。", TITLE, JOptionPane.INFORMATION_MESSAGE));
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "启动器初始化失败：" + safeMessage(exception), TITLE, JOptionPane.ERROR_MESSAGE));
        }
    }

    private void showWindow() {
        window = new JFrame(TITLE);
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        window.setMinimumSize(new Dimension(760, 540));
        window.setSize(860, 640);
        window.setLocationRelativeTo(null);
        window.setIconImage(createTrayImage());

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        root.setBackground(new Color(242, 246, 244));
        JPanel heading = new JPanel(new BorderLayout(8, 8));
        heading.setOpaque(false);
        JLabel title = new JLabel("ShiYu 本地运行控制台");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        statusLabel = new JLabel("正在准备启动器…", SwingConstants.RIGHT);
        statusLabel.setForeground(new Color(39, 104, 88));
        heading.add(title, BorderLayout.WEST);
        heading.add(statusLabel, BorderLayout.EAST);
        root.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 10));
        center.setOpaque(false);
        JPanel homePanel = new JPanel(new BorderLayout(8, 0));
        homePanel.setOpaque(false);
        JLabel homeLabel = new JLabel("APP_HOME（下次启动使用；不迁移或删除旧目录）");
        appHomeField = new JTextField(appHome.toString());
        JButton browse = new JButton("选择目录…");
        browse.addActionListener(event -> chooseAppHome());
        JButton saveHome = new JButton("保存目录");
        saveHome.addActionListener(event -> saveAppHome());
        JPanel pathRow = new JPanel(new BorderLayout(8, 0));
        pathRow.setOpaque(false);
        pathRow.add(appHomeField, BorderLayout.CENTER);
        JPanel pathButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pathButtons.setOpaque(false);
        pathButtons.add(browse);
        pathButtons.add(saveHome);
        pathRow.add(pathButtons, BorderLayout.EAST);
        homePanel.add(homeLabel, BorderLayout.NORTH);
        homePanel.add(pathRow, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton open = new JButton("打开控制台");
        open.addActionListener(event -> openConsole(""));
        JButton start = new JButton("启动");
        start.addActionListener(event -> startBackend());
        stopButton = new JButton("停止");
        stopButton.addActionListener(event -> requestGracefulShutdown("stop"));
        restartButton = new JButton("重启");
        restartButton.addActionListener(event -> requestGracefulShutdown("restart"));
        JButton recover = new JButton("恢复上次成功配置…");
        recover.addActionListener(event -> restoreConfiguration());
        JButton viewLogs = new JButton("查看启动日志");
        viewLogs.addActionListener(event -> showWindow());
        actions.add(open);
        actions.add(start);
        actions.add(stopButton);
        actions.add(restartButton);
        actions.add(recover);
        actions.add(viewLogs);
        center.add(homePanel, BorderLayout.NORTH);
        center.add(actions, BorderLayout.CENTER);

        logArea = new JTextArea(18, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(25, 34, 33));
        logArea.setForeground(new Color(211, 226, 220));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("启动与故障日志（授权 URL 不写入日志）"));
        center.add(logScroll, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
        window.setContentPane(root);
        window.setVisible(true);
        installTrayIcon();
        appendLog("启动器已就绪；默认运行目录：" + appHome);
        startBackend();
        refreshButtons();
    }

    private void installTrayIcon() {
        if (!SystemTray.isSupported()) return;
        PopupMenu menu = new PopupMenu();
        MenuItem openWindow = new MenuItem("打开启动器");
        openWindow.addActionListener(event -> showWindow());
        MenuItem openConsole = new MenuItem("打开浏览器控制台");
        openConsole.addActionListener(event -> openConsole(""));
        MenuItem start = new MenuItem("启动 ShiYu");
        start.addActionListener(event -> startBackend());
        MenuItem stop = new MenuItem("停止 ShiYu");
        stop.addActionListener(event -> requestGracefulShutdown("stop"));
        MenuItem restart = new MenuItem("重启 ShiYu");
        restart.addActionListener(event -> requestGracefulShutdown("restart"));
        MenuItem recover = new MenuItem("恢复上次成功配置");
        recover.addActionListener(event -> restoreConfiguration());
        MenuItem logs = new MenuItem("查看启动日志");
        logs.addActionListener(event -> showWindow());
        MenuItem exit = new MenuItem("退出启动器");
        exit.addActionListener(event -> {
            if (backendProcess != null && backendProcess.isAlive()) {
                int choice = JOptionPane.showConfirmDialog(window, "退出启动器会停止它托管的 ShiYu 后端，是否继续？",
                        TITLE, JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
                Process running = backendProcess;
                requestGracefulShutdown("stop");
                CompletableFuture.runAsync(() -> {
                    try { running.waitFor(20, TimeUnit.SECONDS); }
                    catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
                    close();
                    System.exit(0);
                });
                return;
            }
            close();
            System.exit(0);
        });
        for (MenuItem item : List.of(openWindow, openConsole, start, stop, restart, recover, logs, exit)) menu.add(item);
        trayIcon = new TrayIcon(createTrayImage(), TITLE, menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(event -> showWindow());
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception exception) {
            appendLog("系统托盘不可用：" + safeMessage(exception));
        }
    }

    private void chooseAppHome() {
        JFileChooser chooser = new JFileChooser(appHome.toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择下一次启动的 APP_HOME");
        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            appHomeField.setText(chooser.getSelectedFile().toPath().toAbsolutePath().normalize().toString());
        }
    }

    private void saveAppHome() {
        try {
            Path selected = Path.of(appHomeField.getText().trim()).toAbsolutePath().normalize();
            if (selected.equals(supportDirectory) || selected.startsWith(supportDirectory)) {
                throw new IllegalArgumentException("APP_HOME 不能位于启动器自身状态目录内");
            }
            appHome = selected;
            settings.setProperty("app.home", appHome.toString());
            Path temporary = settingsFile.resolveSibling(settingsFile.getFileName() + ".tmp");
            try (var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                settings.store(writer, "ShiYu launcher settings; APP_HOME changes do not migrate existing data");
            }
            moveAtomically(temporary, settingsFile);
            appendLog("已保存 APP_HOME；只在下次启动后使用，不会迁移或改动原目录：" + appHome);
        } catch (Exception exception) {
            showError("无法保存 APP_HOME：" + safeMessage(exception));
        }
    }

    private synchronized void startBackend() {
        if (backendProcess != null && backendProcess.isAlive()) {
            setStatus("ShiYu 正在运行（PID " + backendProcess.pid() + "）");
            return;
        }
        setStatus("正在启动 ShiYu…");
        requestedAction.set("");
        consoleBase = null;
        refreshButtons();
        try {
            Path backendJar = findBackendJar();
            Path javaw = findJavaw();
            WindowsJobObject newJob = WindowsJobObject.create();
            List<String> command = new ArrayList<>();
            command.add(javaw.toString());
            command.add("-Dapp.home=" + appHome);
            command.add("-Dshiyu.console.launcher-managed=true");
            command.add("-jar");
            command.add(backendJar.toString());
            command.add("--spring.profiles.active=windows");
            ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
            processBuilder.directory(installRoot.toFile());
            processBuilder.environment().put("APP_HOME", appHome.toString());
            processBuilder.environment().put("SHIYU_LAUNCHER_CONTROL_PORT", Integer.toString(commandServer.port()));
            processBuilder.environment().put("SHIYU_LAUNCHER_CONTROL_TOKEN", controlToken);
            processBuilder.environment().put("SHIYU_CONSOLE_ENABLED", "true");
            Process process = processBuilder.start();
            try {
                newJob.assign(process);
            } catch (RuntimeException failure) {
                process.destroyForcibly();
                newJob.close();
                throw failure;
            }
            backendProcess = process;
            jobObject = newJob;
            appendLog("后端进程已启动，PID=" + process.pid());
            Thread output = new Thread(() -> readBackendOutput(process), "shiyu-backend-output-" + process.pid());
            output.setDaemon(true);
            output.start();
            Thread monitor = new Thread(() -> monitorBackend(process, newJob), "shiyu-backend-monitor-" + process.pid());
            monitor.setDaemon(true);
            monitor.start();
        } catch (Exception exception) {
            setStatus("启动失败");
            appendLog("启动失败：" + safeMessage(exception));
            showError("无法启动 ShiYu：" + safeMessage(exception) + "\n请查看下方启动日志。");
        } finally {
            refreshButtons();
        }
    }

    private void readBackendOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SHIYU_CONSOLE_URL=")) {
                    String value = line.substring("SHIYU_CONSOLE_URL=".length()).trim();
                    acceptConsoleUrl(value);
                    continue;
                }
                appendLog(line);
            }
        } catch (IOException exception) {
            appendLog("读取后端输出失败：" + safeMessage(exception));
        }
    }

    private void acceptConsoleUrl(String value) {
        try {
            URI candidate = URI.create(value);
            if (!"http".equalsIgnoreCase(candidate.getScheme())
                    || !"127.0.0.1".equals(candidate.getHost())
                    || !"/console/".equals(candidate.getPath())
                    || candidate.getFragment() == null
                    || !candidate.getFragment().startsWith("grant=")) {
                appendLog("后端已就绪，但控制台授权地址未通过本机地址校验。");
                return;
            }
            consoleBase = new URI(candidate.getScheme(), null, candidate.getHost(), candidate.getPort(),
                    "/console/", null, null);
            setStatus("ShiYu 已就绪（PID " + (backendProcess == null ? "—" : backendProcess.pid()) + "）");
            appendLog("后端已就绪；浏览器授权凭证已交给系统浏览器，未写入启动日志。");
            if (!browse(candidate)) appendLog("无法自动打开浏览器；请从启动器选择“打开浏览器控制台”。");
            refreshButtons();
        } catch (Exception exception) {
            appendLog("处理控制台启动链接失败：" + safeMessage(exception));
        }
    }

    private void monitorBackend(Process process, WindowsJobObject ownedJob) {
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return;
        } finally {
            ownedJob.close();
        }
        if (jobObject == ownedJob) jobObject = null;
        if (backendProcess == process) backendProcess = null;
        consoleBase = null;
        String action = requestedAction.getAndSet("");
        if ("restart".equals(action)) {
            appendLog("旧后端已退出（代码 " + exitCode + "），开始一次受控重启。");
            setStatus("正在重启…");
            CompletableFuture.delayedExecutor(700, TimeUnit.MILLISECONDS).execute(this::startBackend);
        } else if ("stop".equals(action) || exitCode == 0) {
            appendLog("后端已停止（代码 " + exitCode + "）。");
            setStatus("ShiYu 已停止");
            refreshButtons();
        } else {
            appendLog("后端异常退出（代码 " + exitCode + "）。未自动循环重启；请查看日志后手动启动。");
            setStatus("后端异常退出；等待用户处理");
            refreshButtons();
            showError("ShiYu 启动失败或异常退出（代码 " + exitCode + "）。没有自动循环重启，请查看启动日志。");
        }
    }

    private void handleLauncherCommand(String action) {
        appendLog("收到浏览器控制台的受控" + ("restart".equals(action) ? "重启" : "停止") + "请求。");
        requestGracefulShutdown(action);
    }

    private void requestGracefulShutdown(String action) {
        Process child = backendProcess;
        if (child == null || !child.isAlive()) {
            if ("restart".equals(action)) startBackend();
            else setStatus("ShiYu 已停止");
            return;
        }
        if (!requestedAction.compareAndSet("", action)) {
            appendLog("已有生命周期操作正在处理，请稍候。");
            return;
        }
        setStatus("正在" + ("restart".equals(action) ? "重启" : "停止") + "…");
        refreshButtons();
        URI base = consoleBase;
        Thread shutdown = new Thread(() -> {
            boolean graceful = false;
            if (base != null) {
                try {
                    HttpRequest request = HttpRequest.newBuilder(base.resolve("api/lifecycle/internal-shutdown"))
                            .timeout(Duration.ofSeconds(3))
                            .header("X-ShiYu-Launcher-Control", controlToken)
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                    graceful = response.statusCode() == 202;
                } catch (Exception exception) {
                    appendLog("后端优雅关闭接口不可用：" + safeMessage(exception));
                }
            }
            if (!graceful && child.isAlive()) {
                appendLog("无法完成优雅关闭，正在请求停止当前托管子进程（PID " + child.pid() + "）。");
                child.destroy();
            }
            try {
                if (!child.waitFor(18, TimeUnit.SECONDS) && child.isAlive()) {
                    appendLog("优雅关闭超时，仅终止当前 Job Object 中的后端进程树。");
                    if (jobObject != null) jobObject.close();
                    if (child.isAlive()) child.destroyForcibly();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }, "shiyu-graceful-shutdown");
        shutdown.setDaemon(true);
        shutdown.start();
    }

    private void openConsole(String tab) {
        URI base = consoleBase;
        if (base == null) {
            showError("后端尚未就绪，暂时无法生成新的授权链接。");
            return;
        }
        Thread opener = new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(base.resolve("api/lifecycle/internal-link"))
                        .timeout(Duration.ofSeconds(3))
                        .header("X-ShiYu-Launcher-Control", controlToken)
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode());
                JsonNode node = objectMapper.readTree(response.body()).get("url");
                if (node == null || !node.isTextual()) throw new IOException("missing authorized URL");
                URI authorized = URI.create(node.asText());
                if (!sameConsoleOrigin(base, authorized) || authorized.getFragment() == null
                        || !authorized.getFragment().startsWith("grant=")) {
                    throw new IOException("backend returned a non-local console URL");
                }
                URI opened = new URI(authorized.getScheme(), null, authorized.getHost(), authorized.getPort(),
                        authorized.getPath(), tab.isBlank() ? null : "tab=" + tab, authorized.getFragment());
                if (!browse(opened)) throw new IOException("系统未能启动默认浏览器");
            } catch (Exception exception) {
                showError("无法打开控制台：" + safeMessage(exception));
            }
        }, "shiyu-open-console");
        opener.setDaemon(true);
        opener.start();
    }

    private void restoreConfiguration() {
        Process child = backendProcess;
        if (child != null && child.isAlive()) {
            if (consoleBase == null) {
                showError("后端仍在启动或关闭中。请等待进程退出后再执行离线配置恢复。");
            } else {
                openConsole("config");
            }
            return;
        }
        try {
            OfflineConfigRecovery.RestoreOutcome outcome = new OfflineConfigRecovery(objectMapper).restoreLastApplied(appHome);
            if (outcome.activeRevision() == outcome.restoredFromRevision()) {
                appendLog("当前配置版本已是上次成功版本 v" + outcome.activeRevision() + "，无需恢复。");
                JOptionPane.showMessageDialog(window, "当前活动配置已是上次成功版本 v" + outcome.activeRevision() + "。",
                        TITLE, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            appendLog("已离线恢复配置 v" + outcome.restoredFromRevision() + "，新活动版本 v" + outcome.activeRevision()
                    + "；密文原样保留，未解密或迁移数据。");
            int choice = JOptionPane.showConfirmDialog(window,
                    "已恢复上次成功配置 v" + outcome.restoredFromRevision() + "。现在启动 ShiYu 吗？",
                    TITLE, JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) startBackend();
        } catch (IOException exception) {
            showError("离线恢复失败：" + safeMessage(exception));
        }
    }

    private void appendLog(String message) {
        String safe = message == null ? "" : message;
        if (safe.startsWith("SHIYU_CONSOLE_URL=")) safe = "控制台授权链接已生成（已隐藏，不写入日志）。";
        final String line = LOG_TIME.format(LocalDateTime.now()) + "  " + safe;
        synchronized (recentLines) {
            recentLines.addLast(line);
            while (recentLines.size() > 800) recentLines.removeFirst();
        }
        try {
            if (Files.exists(launcherLog) && Files.size(launcherLog) > LOG_LIMIT) {
                moveAtomically(launcherLog, launcherLog.resolveSibling("launcher.log.1"));
            }
            try (BufferedWriter writer = Files.newBufferedWriter(launcherLog, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ignored) {
            // 诊断磁盘空间不足时，启动器内存视图仍保持可用。
        }
        SwingUtilities.invokeLater(() -> {
            if (logArea == null) return;
            synchronized (recentLines) {
                logArea.setText(String.join("\n", recentLines));
            }
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void refreshButtons() {
        SwingUtilities.invokeLater(() -> {
            boolean running = backendProcess != null && backendProcess.isAlive();
            boolean ready = running && consoleBase != null;
            if (stopButton != null) stopButton.setEnabled(ready && requestedAction.get().isEmpty());
            if (restartButton != null) restartButton.setEnabled(ready && requestedAction.get().isEmpty());
        });
    }

    private void setStatus(String value) {
        SwingUtilities.invokeLater(() -> {
            if (statusLabel != null) statusLabel.setText(value);
            if (trayIcon != null) trayIcon.setToolTip(value);
        });
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            if (window != null && !window.isVisible()) window.setVisible(true);
            JOptionPane.showMessageDialog(window, message, TITLE, JOptionPane.ERROR_MESSAGE);
        });
    }

    private Path findBackendJar() throws IOException {
        Path applicationDirectory = installRoot.resolve("app");
        try (var files = Files.list(applicationDirectory)) {
            return files.filter(path -> path.getFileName().toString().matches("shiyu-ai-bootstrap-[0-9][A-Za-z0-9.-]*\\.jar"))
                    .findFirst().orElseThrow(() -> new IOException("发行目录缺少 shiyu-ai-bootstrap 后端 Jar：" + applicationDirectory));
        }
    }

    private Path findJavaw() throws IOException {
        Path packaged = installRoot.resolve("runtime").resolve("bin").resolve("javaw.exe");
        Path development = Path.of(System.getProperty("java.home"), "bin", "javaw.exe");
        Path javaw = Files.isRegularFile(packaged) ? packaged : development;
        if (!Files.isRegularFile(javaw)) throw new IOException("未找到 Java 21 javaw.exe：" + javaw);
        return javaw;
    }

    private Path discoverInstallRoot() throws Exception {
        Path location = Path.of(RuntimeConsoleLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path applicationDirectory = Files.isDirectory(location) ? location : location.getParent();
        return applicationDirectory != null && "app".equalsIgnoreCase(String.valueOf(applicationDirectory.getFileName()))
                ? applicationDirectory.getParent()
                : applicationDirectory;
    }

    private Path localApplicationData() {
        String local = System.getenv("LOCALAPPDATA");
        if (local != null && !local.isBlank()) return Path.of(local);
        String profile = System.getenv("USERPROFILE");
        if (profile != null && !profile.isBlank()) return Path.of(profile, "AppData", "Local");
        return Path.of(System.getProperty("user.home"), "AppData", "Local");
    }

    private void loadSettings() throws IOException {
        if (Files.isRegularFile(settingsFile)) {
            try (var reader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
                settings.load(reader);
            }
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean browse(URI uri) {
        try {
            if (!java.awt.Desktop.isDesktopSupported()) return false;
            java.awt.Desktop.getDesktop().browse(uri);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean sameConsoleOrigin(URI expected, URI candidate) {
        return "http".equalsIgnoreCase(candidate.getScheme())
                && "127.0.0.1".equals(candidate.getHost())
                && expected.getPort() == candidate.getPort()
                && "/console/".equals(candidate.getPath());
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static Image createTrayImage() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(29, 88, 74));
        graphics.fillRoundRect(0, 0, 32, 32, 9, 9);
        graphics.setColor(new Color(207, 250, 235));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        graphics.drawString("詩", 5, 24);
        graphics.dispose();
        return image;
    }

    private static String safeMessage(Throwable failure) {
        String message = failure.getMessage();
        return message == null || message.isBlank() ? failure.getClass().getSimpleName() : message;
    }

    private void close() {
        if (!closed.compareAndSet(false, true)) return;
        try {
            if (backendProcess != null && backendProcess.isAlive()) {
                appendLog("启动器退出时关闭其托管 Job Object 进程树。");
                if (jobObject != null) jobObject.close();
                backendProcess.destroyForcibly();
            }
        } catch (RuntimeException ignored) {
            // 作为最后一道保护，Windows 会在进程清理时关闭 Job 句柄。
        }
        if (commandServer != null) commandServer.close();
        if (trayIcon != null && SystemTray.isSupported()) SystemTray.getSystemTray().remove(trayIcon);
        try {
            instanceLock.release();
            lockChannel.close();
        } catch (IOException ignored) {
            // 启动器意外退出时由操作系统释放锁。
        }
    }

    /** 表示运行时控制台检测到已有实例正在运行。 */
    private static final class AlreadyRunningException extends Exception {
        @java.io.Serial private static final long serialVersionUID = 1L;
    }
}

package dev.duskbyte.gui;

import dev.duskbyte.DuskByte;
import dev.duskbyte.managers.AuthManager;
import dev.duskbyte.managers.cloud.CloudApiClient;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

/**
 * 启动时的登录/注册窗口(Java 默认 Swing 界面)。
 *
 * 流程:游戏初始化时先弹出此窗口并阻塞主线程,
 * 登录或注册成功后才继续加载游戏(Mojang 加载画面 → MC 标题界面)。
 * 如果本地已有有效 token(自动登录成功),窗口不会弹出,直接进游戏。
 * 未登录直接关闭窗口 = 退出游戏。
 */
public final class AuthDialog {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AuthDialog.class);

    private static final Color COLOR_ERROR = new Color(200, 40, 40);
    private static final Color COLOR_OK = new Color(30, 140, 60);
    private static final Color COLOR_BUSY = new Color(160, 110, 0);

    private AuthDialog() {
    }

    /**
     * 阻塞当前线程直到登录成功。
     *
     * @return true = 已认证,可以继续加载游戏
     */
    public static boolean promptBlocking() {
        // 双保险:即使调用方忘了设置,也在 AWT 初始化前把 headless 关掉
        System.setProperty("java.awt.headless", "false");

        boolean authed = DuskByte.INSTANCE != null && DuskByte.INSTANCE.authenticated;
        boolean headless = java.awt.GraphicsEnvironment.isHeadless();
        LOGGER.info("[AuthDialog] promptBlocking: auto-login authenticated={}, headless={}", authed, headless);
        if (authed) {
            return true; // 自动登录成功,不需要弹窗
        }
        if (headless) {
            // AWT 已经以 headless 模式完成初始化,Swing 窗口弹不出来:
            // 放行游戏加载,由 MinecraftClientMixin 兜底的游戏内 AuthScreen 完成登录
            LOGGER.warn("[AuthDialog] AWT still headless, cannot show Swing dialog; "
                    + "falling back to in-game auth screen");
            return true;
        }

        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("[AuthDialog] showing login dialog");
                show(latch);
                LOGGER.info("[AuthDialog] login dialog closed");
            } catch (Throwable t) {
                // EDT 上的任何异常都会导致窗口不出现且主线程永久等待,必须兜底放行
                LOGGER.error("[AuthDialog] failed to show login dialog", t);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean ok = DuskByte.INSTANCE != null && DuskByte.INSTANCE.authenticated;
        LOGGER.info("[AuthDialog] promptBlocking finished, authenticated={}", ok);
        return ok;
    }

    private static void show(CountDownLatch latch) {
        JDialog dialog = new JDialog((Frame) null, "DuskByte", false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        final boolean[] registerMode = {false};

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(2, 4, 8, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("DuskByte 登录", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f).deriveFont(java.awt.Font.BOLD));
        panel.add(titleLabel, c);

        // 用户名
        c.gridy++;
        c.gridwidth = 1;
        c.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel("用户名:"), c);
        c.gridx = 1;
        JTextField userField = new JTextField(18);
        panel.add(userField, c);

        // 邮箱(仅注册模式显示)
        c.gridx = 0;
        c.gridy++;
        JLabel emailLabel = new JLabel("邮箱:");
        panel.add(emailLabel, c);
        c.gridx = 1;
        JTextField emailField = new JTextField(18);
        panel.add(emailField, c);

        // 密码
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("密码:"), c);
        c.gridx = 1;
        JPasswordField passField = new JPasswordField(18);
        panel.add(passField, c);

        // 状态行
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(statusLabel, c);

        // 按钮
        c.gridy++;
        JPanel buttons = new JPanel(new BorderLayout(8, 0));
        JButton toggleButton = new JButton("没有账号？去注册");
        JButton submitButton = new JButton("登 录");
        buttons.add(toggleButton, BorderLayout.WEST);
        buttons.add(submitButton, BorderLayout.EAST);
        panel.add(buttons, c);

        // 初始为登录模式,隐藏邮箱
        emailLabel.setVisible(false);
        emailField.setVisible(false);

        Runnable finish = () -> {
            dialog.dispose();
            latch.countDown();
        };

        Runnable onSubmit = () -> {
            boolean register = registerMode[0];
            String username = userField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty() || (register && email.isEmpty())) {
                statusLabel.setForeground(COLOR_ERROR);
                statusLabel.setText(register ? "请填写所有字段" : "请填写用户名和密码");
                return;
            }

            submitButton.setEnabled(false);
            toggleButton.setEnabled(false);
            statusLabel.setForeground(COLOR_BUSY);
            statusLabel.setText(register ? "注册中..." : "登录中...");

            new Thread(() -> {
                String error;
                try {
                    AuthManager auth = AuthManager.getInstance();
                    if (auth == null) {
                        error = "内部错误: 客户端未初始化";
                    } else {
                        CloudApiClient.CloudResponse response = register
                                ? auth.register(username, email, password)
                                : auth.login(username, password);

                        if (auth.isAuthenticated()) {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setForeground(COLOR_OK);
                                statusLabel.setText("成功，正在启动游戏...");
                                // 短暂停留让用户看到成功提示,再放行游戏加载
                                javax.swing.Timer timer = new javax.swing.Timer(400, ev -> finish.run());
                                timer.setRepeats(false);
                                timer.start();
                            });
                            return;
                        }
                        error = describeError(response);
                    }
                } catch (Exception e) {
                    error = "网络错误: " + e.getMessage();
                }

                final String msg = error;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(COLOR_ERROR);
                    statusLabel.setText(msg);
                    submitButton.setEnabled(true);
                    toggleButton.setEnabled(true);
                });
            }, "DuskByte-Auth").start();
        };

        toggleButton.addActionListener(e -> {
            boolean reg = !registerMode[0];
            registerMode[0] = reg;
            emailLabel.setVisible(reg);
            emailField.setVisible(reg);
            submitButton.setText(reg ? "注 册" : "登 录");
            titleLabel.setText(reg ? "DuskByte 注册" : "DuskByte 登录");
            toggleButton.setText(reg ? "已有账号？去登录" : "没有账号？去注册");
            statusLabel.setText(" ");
            panel.revalidate();
        });

        submitButton.addActionListener(e -> onSubmit.run());
        // 密码框回车 = 提交
        passField.addActionListener(e -> onSubmit.run());
        dialog.getRootPane().setDefaultButton(submitButton);

        // 关闭窗口:已登录则继续启动;未登录视为放弃 → 退出游戏
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DuskByte.INSTANCE != null && DuskByte.INSTANCE.authenticated) {
                    finish.run();
                } else {
                    System.exit(0);
                }
            }
        });

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true); // 防止被其它窗口(如游戏窗口)遮挡
        dialog.setVisible(true);
        dialog.toFront();
        userField.requestFocusInWindow();
    }

    private static String describeError(CloudApiClient.CloudResponse response) {
        // 服务端 500 时 error 是笼统的 "Internal Server Error",
        // 真正的原因在 message 里(如 "no such table: users"),优先显示它
        String error = response.getMessage();
        if (error == null || error.isEmpty()) error = response.getError();
        if (error == null || error.isEmpty()) error = "HTTP " + response.statusCode;
        return error;
    }
}

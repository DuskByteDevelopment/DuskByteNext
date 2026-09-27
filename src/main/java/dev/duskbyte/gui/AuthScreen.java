package dev.duskbyte.gui;

import dev.duskbyte.DuskByte;
import dev.duskbyte.managers.AuthManager;
import dev.duskbyte.managers.cloud.CloudApiClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.title.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * 启动时强制显示的登录/注册界面(由 MinecraftClientMixin 在未登录时弹出)。
 *
 * 使用原版 Minecraft 默认 Screen 背景与原版控件(不再自绘黑色背景);
 * 登录或注册成功后切换到原版标题界面,资源加载期间游戏自身的
 * Mojang 加载画面会照常显示,加载完成后即为 MC 标题界面。
 */
public final class AuthScreen extends Screen {
    private static final int COLOR_OK = 0x55FF55;
    private static final int COLOR_ERROR = 0xFF5555;
    private static final int COLOR_BUSY = 0xFFFF55;

    private TextFieldWidget usernameField;
    private TextFieldWidget emailField;
    private TextFieldWidget passwordField;
    private ButtonWidget loginButton;
    private ButtonWidget registerButton;
    private ButtonWidget toggleModeButton;

    private boolean isRegisterMode = false;
    private volatile String statusMessage = "";
    private volatile int statusColor = 0xFFFFFF;
    private volatile boolean loading = false;

    public AuthScreen() {
        super(Text.of("DuskByte Login"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int fieldWidth = 200;
        int fieldHeight = 20;

        // 用户名
        usernameField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, centerY - 60, fieldWidth, fieldHeight, Text.of("Username"));
        usernameField.setPlaceholder(Text.of("用户名 (3-20字符)"));
        usernameField.setMaxLength(20);
        this.addDrawableChild(usernameField);

        // 邮箱(仅注册模式显示)
        emailField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, centerY - 35, fieldWidth, fieldHeight, Text.of("Email"));
        emailField.setPlaceholder(Text.of("邮箱"));
        emailField.setMaxLength(100);
        emailField.setVisible(false);
        emailField.active = false;
        this.addDrawableChild(emailField);

        // 密码
        passwordField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, centerY - 10, fieldWidth, fieldHeight, Text.of("Password"));
        passwordField.setPlaceholder(Text.of("密码 (至少6位)"));
        passwordField.setMaxLength(128);
        this.addDrawableChild(passwordField);

        // 切换 登录/注册 模式
        toggleModeButton = ButtonWidget.builder(
                Text.of("没有账号？去注册"),
                button -> toggleMode()
        ).dimensions(centerX - fieldWidth / 2, centerY + 20, fieldWidth, 20).build();
        this.addDrawableChild(toggleModeButton);

        // 登录按钮
        loginButton = ButtonWidget.builder(
                Text.of("登 录"),
                button -> doLogin()
        ).dimensions(centerX - fieldWidth / 2, centerY + 50, fieldWidth / 2 - 2, 20).build();
        this.addDrawableChild(loginButton);

        // 注册按钮
        registerButton = ButtonWidget.builder(
                Text.of("注 册"),
                button -> doRegister()
        ).dimensions(centerX + 2, centerY + 50, fieldWidth / 2 - 2, 20).build();
        registerButton.visible = false;
        registerButton.active = false;
        this.addDrawableChild(registerButton);
    }

    private void toggleMode() {
        if (loading) return;

        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            toggleModeButton.setMessage(Text.of("已有账号？去登录"));
            loginButton.visible = false;
            loginButton.active = false;
            registerButton.visible = true;
            registerButton.active = true;
            emailField.setVisible(true);
            emailField.active = true;
        } else {
            toggleModeButton.setMessage(Text.of("没有账号？去注册"));
            loginButton.visible = true;
            loginButton.active = true;
            registerButton.visible = false;
            registerButton.active = false;
            emailField.setVisible(false);
            emailField.active = false;
        }
        statusMessage = "";
    }

    private void doLogin() {
        if (loading) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("请填写用户名和密码", COLOR_ERROR);
            return;
        }

        loading = true;
        setStatus("登录中...", COLOR_BUSY);

        // 网络请求放后台线程,所有 UI 状态更新回到客户端线程执行
        new Thread(() -> {
            String error;
            try {
                CloudApiClient.CloudResponse response = AuthManager.getInstance().login(username, password);

                if (AuthManager.getInstance().isAuthenticated()) {
                    // 登录成功:进入原版标题界面
                    client.execute(() -> {
                        loading = false;
                        client.setScreen(new TitleScreen());
                    });
                    return;
                }
                error = describeError(response);
            } catch (Exception e) {
                error = "网络错误: " + e.getMessage();
            }

            final String fail = error;
            client.execute(() -> {
                loading = false;
                setStatus("登录失败: " + fail, COLOR_ERROR);
            });
        }, "DuskByte-Auth-Login").start();
    }

    private void doRegister() {
        if (loading) return;

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            setStatus("请填写所有字段", COLOR_ERROR);
            return;
        }

        loading = true;
        setStatus("注册中...", COLOR_BUSY);

        new Thread(() -> {
            String error;
            try {
                CloudApiClient.CloudResponse response = AuthManager.getInstance().register(username, email, password);

                if (AuthManager.getInstance().isAuthenticated()) {
                    // 注册成功:进入原版标题界面
                    client.execute(() -> {
                        loading = false;
                        client.setScreen(new TitleScreen());
                    });
                    return;
                }
                error = describeError(response);
            } catch (Exception e) {
                error = "网络错误: " + e.getMessage();
            }

            final String fail = error;
            client.execute(() -> {
                loading = false;
                setStatus("注册失败: " + fail, COLOR_ERROR);
            });
        }, "DuskByte-Auth-Register").start();
    }

    private static String describeError(CloudApiClient.CloudResponse response) {
        String error = response.getError();
        if (error == null || error.isEmpty()) error = response.getMessage();
        if (error == null || error.isEmpty()) error = "HTTP " + response.statusCode;
        return error;
    }

    private void setStatus(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 原版默认 Screen 背景(不再自绘黑底)
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 标题与版本(原版风格文字)
        context.drawCenteredTextWithShadow(this.textRenderer, "DuskByte", centerX, centerY - 100, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "v" + DuskByte.INSTANCE.getVersion().trim(), centerX, centerY - 85, 0x808080);

        // 状态信息
        if (!statusMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, statusMessage, centerX, centerY + 80, statusColor);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // 登录前不允许 Esc 退出(未登录时下一 tick 也会强制重新弹出)
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

package dev.duskbyte.gui;

import dev.duskbyte.DuskByte;
import dev.duskbyte.managers.AuthManager;
import dev.duskbyte.managers.cloud.CloudApiClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.awt.*;

public final class AuthScreen extends Screen {
    private TextFieldWidget usernameField;
    private TextFieldWidget emailField;
    private TextFieldWidget passwordField;
    private ButtonWidget loginButton;
    private ButtonWidget registerButton;
    private ButtonWidget toggleModeButton;

    private boolean isRegisterMode = false;
    private String statusMessage = "";
    private int statusColor = Color.WHITE.getRGB();
    private boolean loading = false;

    public AuthScreen() {
        super(Text.of("DuskByte Auth"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int fieldWidth = 200;
        int fieldHeight = 20;

        // Username
        usernameField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, centerY - 60, fieldWidth, fieldHeight, Text.of("Username"));
        usernameField.setPlaceholder(Text.of("用户名 (3-20字符)"));
        usernameField.setMaxLength(20);
        this.addDrawableChild(usernameField);

        // Email (only visible in register mode)
        emailField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, centerY - 35, fieldWidth, fieldHeight, Text.of("Email"));
        emailField.setPlaceholder(Text.of("邮箱"));
        emailField.setMaxLength(100);
        emailField.setVisible(false);
        emailField.active = false;
        this.addDrawableChild(emailField);

        // Password
        passwordField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, centerY - 10, fieldWidth, fieldHeight, Text.of("Password"));
        passwordField.setPlaceholder(Text.of("密码 (至少6位)"));
        passwordField.setMaxLength(128);
        passwordField.setSecret(true);
        this.addDrawableChild(passwordField);

        // Toggle mode button
        toggleModeButton = ButtonWidget.builder(
                Text.of("没有账号？去注册"),
                button -> toggleMode()
        ).dimensions(centerX - fieldWidth / 2, centerY + 20, fieldWidth, 20).build();
        this.addDrawableChild(toggleModeButton);

        // Login button
        loginButton = ButtonWidget.builder(
                Text.of("登 录"),
                button -> doLogin()
        ).dimensions(centerX - fieldWidth / 2, centerY + 50, fieldWidth / 2 - 2, 20).build();
        this.addDrawableChild(loginButton);

        // Register button
        registerButton = ButtonWidget.builder(
                Text.of("注 册"),
                button -> doRegister()
        ).dimensions(centerX + 2, centerY + 50, fieldWidth / 2 - 2, 20).build();
        registerButton.visible = false;
        registerButton.active = false;
        this.addDrawableChild(registerButton);
    }

    private void toggleMode() {
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
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("请填写用户名和密码", Color.RED.getRGB());
            return;
        }

        loading = true;
        setStatus("登录中...", Color.YELLOW.getRGB());

        // 在新线程里做网络请求，避免卡UI
        new Thread(() -> {
            try {
                CloudApiClient.CloudResponse response = AuthManager.getInstance().login(username, password);

                if (response.success) {
                    // 登录成功，关闭这个界面
                    client.execute(() -> {
                        this.close();
                    });
                } else {
                    String error = response.getError();
                    setStatus("登录失败: " + (error != null ? error : "未知错误"), Color.RED.getRGB());
                }
            } catch (Exception e) {
                setStatus("网络错误: " + e.getMessage(), Color.RED.getRGB());
            }
            loading = false;
        }).start();
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            setStatus("请填写所有字段", Color.RED.getRGB());
            return;
        }

        loading = true;
        setStatus("注册中...", Color.YELLOW.getRGB());

        new Thread(() -> {
            try {
                CloudApiClient.CloudResponse response = AuthManager.getInstance().register(username, email, password);

                if (response.success) {
                    client.execute(() -> {
                        this.close();
                    });
                } else {
                    String error = response.getError();
                    setStatus("注册失败: " + (error != null ? error : "未知错误"), Color.RED.getRGB());
                }
            } catch (Exception e) {
                setStatus("网络错误: " + e.getMessage(), Color.RED.getRGB());
            }
            loading = false;
        }).start();
    }

    private void setStatus(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 背景
        context.fill(0, 0, this.width, this.height, new Color(20, 20, 30, 255).getRGB());

        int centerX = this.width / 2;

        // 标题
        String title = "DuskByte";
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawCenteredTextWithShadow(this.textRenderer, title, centerX, this.height / 2 - 100, new Color(255, 80, 80, 255).getRGB());

        // 版本
        String version = "v" + DuskByte.INSTANCE.getVersion().trim();
        context.drawCenteredTextWithShadow(this.textRenderer, version, centerX, this.height / 2 - 85, Color.GRAY.getRGB());

        // 状态信息
        if (!statusMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, statusMessage, centerX, this.height / 2 + 80, statusColor);
        }

        // Loading 遮罩
        if (loading) {
            context.fill(0, 0, this.width, this.height, new Color(0, 0, 0, 150).getRGB());
            String loadingText = "请稍候...";
            context.drawCenteredTextWithShadow(this.textRenderer, loadingText, centerX, this.height / 2, Color.WHITE.getRGB());
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
    }
}

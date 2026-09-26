
package dev.duskbyte.setting;

public class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, 100);
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name, defaultValue);
        this.maxLength = maxLength;
    }

    public int getMaxLength() { return maxLength; }

    public void append(char c) {
        if (value.length() < maxLength) value = value + c;
    }

    public void backspace() {
        if (!value.isEmpty()) value = value.substring(0, value.length() - 1);
    }

    public void clear() { value = ""; }
}

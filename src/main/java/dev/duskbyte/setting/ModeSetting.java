
package dev.duskbyte.setting;

import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, List<String> modes, int defaultIndex) {
        super(name, modes.get(defaultIndex));
        this.modes = modes;
        this.index = defaultIndex;
    }

    public void cycle() {
        index = (index + 1) % modes.size();
        value = modes.get(index);
    }

    public List<String> getModes() { return modes; }
    public int getIndex() { return index; }
}

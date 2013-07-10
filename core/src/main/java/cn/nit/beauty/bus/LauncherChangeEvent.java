package cn.nit.beauty.bus;

import cn.nit.beauty.model.Category;

/**
 * Created by gengke on 13-7-10.
 */
public class LauncherChangeEvent {
    private Category launcher;

    public LauncherChangeEvent(Category launcher) {
        this.launcher = launcher;
    }

    public Category getLauncher() {
        return launcher;
    }
}

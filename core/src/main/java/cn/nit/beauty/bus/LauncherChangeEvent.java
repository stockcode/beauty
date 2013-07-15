package cn.nit.beauty.bus;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.database.Category;

/**
 * Created by gengke on 13-7-10.
 */
public class LauncherChangeEvent {
    private List<Category> launchers = new ArrayList<Category>();

    public LauncherChangeEvent() {
    }

    public List<Category> getLaunchers() {
        return launchers;
    }

    public void add(Category launcher) {
        launchers.add(launcher);
    }
}

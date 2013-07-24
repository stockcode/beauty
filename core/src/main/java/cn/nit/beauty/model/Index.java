package cn.nit.beauty.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nit.beauty.database.Category;

/**
 * Created by Administrator on 13-7-24.
 */

public class Index {
    private List<Category> categories;
    public Map<String, List<String>> roots =new HashMap<String, List<String>>();

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Map<String, List<String>> getRoots() {
        return roots;
    }

    public void setRoots(Map<String, List<String>> roots) {
        this.roots = roots;
    }

    @Override
    public String toString() {
        return "Index{" +
                "categories=" + categories +
                ", roots=" + roots +
                '}';
    }
}

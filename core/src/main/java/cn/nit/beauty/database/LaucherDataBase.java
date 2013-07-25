package cn.nit.beauty.database;

import java.util.ArrayList;
import java.util.List;

import cn.nit.beauty.database.CategoryDao.Properties;
import cn.nit.beauty.database.DaoMaster.DevOpenHelper;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.Query;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class LaucherDataBase {
	private static final String DB_NAME = "beauty.db";
	private static final int DB_VERSION = 1;
	private Context sContext = null;

	private DevOpenHelper helper;
	private SQLiteDatabase db;


    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private CategoryDao categoryDao;

	public LaucherDataBase(Context context) {
		sContext = context;

		helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        db = helper.getWritableDatabase();                
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        categoryDao = daoSession.getCategoryDao();
	}
	
	public void upgrade() {
		helper.onUpgrade(db, 1, 2);
	}

	// 是否存在ID为的微博
//	public boolean isItemExit() {
//		Boolean is = false;
//		Cursor cursor = sdb.query(TB_ITEMS, null, null, null, null, null, null);
//		is = cursor.moveToFirst();
//		cursor.close();
//		return is;
//	}

	public void updateChoice(Category category) {
			categoryDao.update(category);

	}

	public void insertItems(List<Category> categories) {
		for (Category category : categories) {
            Query<Category> query = categoryDao.queryBuilder().where(Properties.CATEGORY.eq(category.getCATEGORY()), Properties.TITLE.eq(category.getTITLE())).build();
            if (query.unique() == null)
			categoryDao.insert(category);
		}

	}

	public List<Category> getItems(String from) {
		Query<Category> query = categoryDao.queryBuilder().where(Properties.CATEGORY.eq(from)).build();
		return query.list();
	}
	public int getItemsUrl(String title) {

		Query<Category> query = categoryDao.queryBuilder().where(Properties.TITLE.eq(title)).build();
		Category category = query.unique();
		if (category != null) {
			return category.getICON();
		}
		
		return -1;
	}
	public List<Category> getItems() {
		return categoryDao.loadAll();		
	}

    public List<Category> getRootCategories() {
        Query<Category> query = categoryDao.queryBuilder().where(Properties.CATEGORY.eq("ROOT")).build();
        return query.list();
    }

	// 判断JOkes表中是否有某条数据
	public boolean hasItems() {
		return categoryDao.count() != 0;
	}

	// 删除当天数据
	public void deleteItems() {
		categoryDao.deleteAll();

	}
	
	public boolean hasLauncher() {		
		return getLauncher().size() != 0;
	}
	
	public void deleteLauncher() {
		DeleteQuery<Category> query = categoryDao.queryBuilder().where(Properties.CHOICE.eq(true)).buildDelete();
		query.executeDeleteWithoutDetachingEntities();

	}

	public List<Category> getLauncher() {
		Query<Category> query = categoryDao.queryBuilder().where(Properties.CHOICE.eq(true)).build();
		return query.list();
	}

	public void insertLauncher(List<Category> launchers) {
		for (Category launcher : launchers) {
            if (launcher.getTITLE() == null || launcher.getTITLE().equals("none")) continue;
			categoryDao.insert(launcher);
		}

	}

    public List<String> getFavoriteList() {
        List<String> list = new ArrayList<String>();

        Query<Category> query = categoryDao.queryBuilder().where(Properties.CATEGORY.eq("favorite")).build();

        for(Category category : query.list()) {
            list.add(category.getURL());
        }
        return  list;
    }

    public void updateFavorite(String objectKey) {
        Query<Category> query = categoryDao.queryBuilder().where(Properties.URL.eq(objectKey)).build();
        Category category = query.unique();
        if (category == null) {
            category = new Category();
            category.setCATEGORY("favorite");
            category.setURL(objectKey);
            String[] strs = objectKey.split("/");
            category.setTITLE(strs[strs.length - 1]);
            categoryDao.insert(category);
        }
    }
}

package james.com.simplezhihudaily.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import james.com.simplezhihudaily.Model.NewsInfo;


public class ZhihuDailyDB {
    //db name
    public static final String DB_NAME = "ZhihuDaily";
    //db version
    public static final int VERSION = 1;
    public static ZhihuDailyDB zhihuDailyDB;
    private SQLiteDatabase db;

    /**
     * use the design pattern -- Singleton
     * @param context
     */
    private ZhihuDailyDB(Context context){
        ZhihuDailyDBhelper dbHelper = new ZhihuDailyDBhelper(context, DB_NAME, null,VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * get the instance of ZhihuDailyDB
     * @param context context
     * @return db instance
     */
    public synchronized static ZhihuDailyDB getInstance(Context context){
        if (zhihuDailyDB == null){
            zhihuDailyDB = new ZhihuDailyDB(context);
        }
        return zhihuDailyDB;
    }
    //preserve instances to db
    public void saveBaseNews(NewsInfo newsInfo){
        if (newsInfo != null){
            ContentValues values = new ContentValues();
            values.put("title", newsInfo.getTitle());
            values.put("date", newsInfo.getDate());
            values.put("img",newsInfo.getUrls());
            values.put("id",newsInfo.getId());
            values.put("content",newsInfo.getContent());
            db.insert("ZhihuNews" , null, values);
        }
    }

    /**
     * if user open an article, then insert the content of article
     * @param id    the id of the article
     * @param content   content of the article
     */
    public void insertContent(String id,String content){
        ContentValues values = new ContentValues();
        values.put("content",content);
        db.update(ZhihuDailyDBhelper.TABLE_NAME,values,"id = ?",new String[] {id});
    }
    /**
     * query the id and if it has content,return ture
     * @param id    id of article
     * @return  true or false
     */
    public boolean hasContent(String id){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"id = ?",new String[]{id},null,null,null);
        if (cursor!= null && cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex("content")) != null){
                    cursor.close();
                    return true;
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return false;
        /*
        cursor.moveToFirst();
        if (cursor.getString(cursor.getColumnIndex("content")) == null){
            cursor.close();
            return false;
        }else {
            cursor.close();
            return true;
        }
        */
    }

    /**
     * to see how many news is already in the db
     * @param date  date
     * @param length    the latest number of news
     * @return  should insert how many news
     */
    public int isInserted(String date,int length){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"date = ?",new String[]{date},null,null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        Log.d("how many in the db",String.valueOf(count));
        return length - count;//直接返回差距个数 最少为0 最多不限
    }

    /**
     * get article from db through id
     * @param id    id of the article
     * @return
     */
    public String getArticle(String id){
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"id = ?",new String[]{id},null,null,null,null);
        if (cursor.getCount() == 0){
            cursor.close();
            return null;
        }else {
            Log.d("getArticle",String.valueOf(cursor.getColumnIndex("content")));
            cursor.moveToFirst();
            String htmlString = cursor.getString(cursor.getColumnIndex("content"));
            cursor.close();
            return htmlString;
        }
    }
    /**
     * read the news of a certain day
     * @param date which day do you want to read ?
    */
    public List<NewsInfo> loadNewsInfo(String  date){
        List<NewsInfo> list = new ArrayList<>();
        Cursor cursor = db.query(ZhihuDailyDBhelper.TABLE_NAME,null,"date = ?",new String[] {date,null,null,null,null},null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                NewsInfo newsInfo = new NewsInfo();
                newsInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
                newsInfo.setDate(date);
                newsInfo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                newsInfo.setUrls(cursor.getString(cursor.getColumnIndex("img")));
                newsInfo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                list.add(newsInfo);
            }while (cursor.moveToNext());
        }
        if (null != cursor){
            cursor.close();
        }
        return list;
    }
}
package cn.ucai.git.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import cn.ucai.git.I;
import cn.ucai.git.bean.User;

/**
 * Created by Administrator on 2016/5/19.
 */
public class UserDao extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "user";

    public UserDao(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "user.db", factory, 1);
    }

    public UserDao(Context context) {
        super(context,"user.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +"("+
                I.User.USER_ID +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                I.User.USER_NAME +" TEXT NOT NULL," +
                I.User.PASSWORD + " TEXT NOT NULL," +
                I.User.NICK + " TEXT NOT NULL," +
                I.User.UN_READ_MSG_COUNT + " INTEGER DEFAULT 0" +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(I.User.USER_ID,user.getMUserId());
        values.put(I.User.USER_NAME,user.getMUserName());
        values.put(I.User.NICK,user.getMUserNick());
        values.put(I.User.PASSWORD,user.getMUserPassword());
        values.put(I.User.UN_READ_MSG_COUNT,user.getMUserUnreadMsgCount());
        SQLiteDatabase db = getWritableDatabase();
        long insert = db.insert(TABLE_NAME, null, values);
        return insert > 0;
    }

    public boolean updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(I.User.USER_ID,user.getMUserId());
        values.put(I.User.NICK,user.getMUserNick());
        values.put(I.User.PASSWORD,user.getMUserPassword());
        values.put(I.User.UN_READ_MSG_COUNT,user.getMUserUnreadMsgCount());
        SQLiteDatabase db = getWritableDatabase();
        long update = db.update(TABLE_NAME, values,I.User.USER_NAME+"=?",new String[]{user.getMUserName()});
        return update > 0;
    }

    public User findUser(String username) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "select * from " + TABLE_NAME + " where " + I.User.USER_NAME + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{username});
        if (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(I.User.USER_ID));
            String nick = cursor.getString(cursor.getColumnIndex(I.User.NICK));
            String password = cursor.getString(cursor.getColumnIndex(I.User.PASSWORD));
            int unmessage = cursor.getInt(cursor.getColumnIndex(I.User.UN_READ_MSG_COUNT));
            return new User(id, username, password, nick, unmessage);
        }
        cursor.close();
        return null;
    }
}

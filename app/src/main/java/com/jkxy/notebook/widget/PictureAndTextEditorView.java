package com.jkxy.notebook.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.jkxy.notebook.util.BmobUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Think on 2016/10/20.
 * <p>
 * 图文混排编辑器
 */

public class PictureAndTextEditorView extends EditText {

    private final String TAG = "PATEditorView";
    private Context mContext;
    private List<String> mContentList;

    public static final String mBitmapTag = "☆";
    private String mNewLineTag = "\n";

    public PictureAndTextEditorView(Context context) {
        super(context);
        init(context);
    }

    public PictureAndTextEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PictureAndTextEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mContentList = getmContentList();
        insertData();
    }

    /**
     * 设置数据
     */
    private void insertData() {
        if (mContentList.size() > 0) {
            for (String str : mContentList) {
                if (str.indexOf(mBitmapTag) != -1) {//判断是否是图片地址
                    String path = str.replace(mBitmapTag, "");//还原地址字符串
                    Bitmap bitmap = getSmallBitmap(path, 480, 800);
                    //插入图片
                    insertBitmap(path, bitmap);
                } else {
                    //插入文字
                    SpannableString ss = new SpannableString(str);
                    append(ss);
                }
            }
        }
    }

    /**
     * 插入图片
     *
     * @param bitmap
     * @param path
     * @return
     */
    public SpannableString insertBitmap(String path, Bitmap bitmap) {

        Editable edit_text = getEditableText();//获取EditText的文字
        int index = getSelectionStart(); // 获取光标所在位置,第几行

        //插入换行符，使图片单独占一行
//        SpannableString newLine = new SpannableString("\n");
//        edit_text.insert(index, newLine);//插入图片前换行
        // 创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
        path = mBitmapTag + path + mBitmapTag;
        SpannableString spannableString = new SpannableString(path);
        // 根据Bitmap对象创建ImageSpan对象
        ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
        // 用ImageSpan对象替换你指定的字符串
        spannableString.setSpan(imageSpan, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 将选择的图片追加到EditText中光标所在位置
        if (index < 0 || index >= edit_text.length()) {
            edit_text.append(spannableString);
        } else {
            edit_text.insert(index, spannableString);
        }
//        edit_text.insert(index, newLine);//插入图片后换行
        return spannableString;
    }


    /**
     * 插入图片
     *
     * @param path
     */
    public void insertBitmap(String path) {
        Bitmap bitmap = getSmallBitmap(path, 480, 800);
        insertBitmap(path, bitmap);
    }

    /**
     * 用集合的形式获取控件里的内容
     *
     * @return
     */
    public List<String> getmContentList() {
        if (mContentList == null) {
            mContentList = new ArrayList<String>();
        }
//        String content = getText().toString().replaceAll(mNewLineTag, "");
        String content = getText().toString();
        Logger.d("getmContentList content : " + content);
        if (content.length() > 0 && content.contains(mNewLineTag)) {
            String[] split = content.split(mNewLineTag);
            mContentList.clear();
            for (String str : split) {
                mContentList.add(str);
            }
        }
        //剔除bitmap tag，再添加到集合里
        if (content.length() > 0 && content.contains(mBitmapTag)) {
            String[] split = content.split("☆");
//            mContentList.clear();
            for (String str : split) {
                mContentList.add(str);
            }
        } else {
            mContentList.add(content);
        }

        return mContentList;
    }

    /**
     * 设置显示的内容集合
     *
     * @param contentList
     */
    public void setmContentList(List<String> contentList) {
        if (mContentList == null) {
            mContentList = new ArrayList<>();
        }
        this.mContentList.clear();
        this.mContentList.addAll(contentList);
        insertData();
    }

    float oldY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldY = event.getY();
                requestFocus();
                break;
            case MotionEvent.ACTION_MOVE:
                float newY = event.getY();
                //移动距离
                if (Math.abs(oldY - newY) > 20) {
                    //放弃焦点
                    clearFocus();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public Bitmap getSmallBitmap(String fileFullPath, int reqWidth, int reqHeight) {
        String[] filenames = fileFullPath.split("/");
        String filename = filenames[filenames.length - 1];
        File file = new File(fileFullPath.replaceAll(filename, ""), filename);
//        File filePath = new File(fileFullPath.replaceAll(filename,""));
        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
//        File filePath = new File(Environment.getExternalStorageDirectory() + "/Download/" + filename);
        /*if (!filePath.exists()) {
            filePath.mkdir();
            //☆/storage/sdcard/Download/4248ce44579c4e2cc5c9a6cc9f5c1539.jpg☆

            Log.e(TAG, "filename: " + filename);
            //文件不存在->从云端下载
            String url = BmobUtils.obtaiFileUrlByLocalPath(fileFullPath);

            BmobUtils.downloadFile(filename, url, filePath);

        }*/


        try {

            Logger.d(TAG, "filePath.getAbsolutePath(): " + filePath.getAbsolutePath());
            boolean isCreated = filePath.createNewFile();
            if (isCreated) {
                //文件不存在->从云端下载
                String url = BmobUtils.obtaiFileUrlByLocalPath(fileFullPath);
                System.out.println("创建了文件");

                BmobUtils.downloadFile(filename, url, filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileFullPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(fileFullPath, options);
        /*DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int w_width = w_screen;
        int b_width = bitmap.getWidth();
        int b_height = bitmap.getHeight();
        int w_height = w_width * b_height / b_width;
        bitmap = Bitmap.createScaledBitmap(bitmap, w_width, w_height, false)*/
        ;
        return bitmap;
    }

    //计算图片的缩放值
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize >= reqHeight) && (halfWidth / inSampleSize >= reqWidth)) {
                inSampleSize *= 2;
            }

            /*final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;*/
        }
        return inSampleSize;
    }
}

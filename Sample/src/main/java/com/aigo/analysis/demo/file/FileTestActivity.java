package com.aigo.analysis.demo.file;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aigo.analysis.demo.R;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import timber.log.Timber;

public class FileTestActivity extends AppCompatActivity {

    LinearLayout layout;
    ImageView ivSnap;

    String fileName = "testFile";
    String fileContent = "this is a test file";

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_test);
        mActivity = this;

        layout = findViewById(R.id.layout);
        ivSnap = findViewById(R.id.iv_snap);

        findViewById(R.id.btn_create).setOnClickListener(v -> {

            XXPermissions.with(this)
                    .permission(Permission.WRITE_EXTERNAL_STORAGE)
                    .request((permissions, all) -> {
                        if (all) {
                            saveLayout();
                        }
                    });
        });

        findViewById(R.id.btn_read).setOnClickListener(v -> {
            getImagePath(mActivity);
        });
    }

    private void saveLayout() {
        layout.buildDrawingCache();
        Bitmap bitmap = layout.getDrawingCache();

        File file = FileUtil.getSnapshotDir("test11");
        if (file == null) return;
        String path = FileUtil.getSavedScreenShotPath(file, "iotId11", 1);
        FileUtil.savePic(bitmap, path);
    }

    private void getSnapPic() {
        File file = FileUtil.getSnapshotDir("test11");
        if (file == null)
            return;
        String path = FileUtil.getSavedScreenShotPath(file, "iotId11", 1);
        File file2 = new File(path);

        Bitmap bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());
        ivSnap.setImageBitmap(bitmap);
    }

    private void getImagePath(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {

            try {
                //取出路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.RELATIVE_PATH));
                Timber.tag("TAG").v("Sending: %s", path);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
            } catch (Exception e) {

            }
            break;
        }
    }
}
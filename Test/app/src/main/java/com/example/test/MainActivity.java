package com.example.test;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.alertdialog.AlertDialog;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

import cn.qqtheme.framework.picker.FilePicker;
import cn.qqtheme.framework.util.StorageUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE = 100;

    private static final int REQUEST_CODE_SETTING = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.open).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (StorageUtils.externalMounted()) {
            checkoutPermisson();
        } else {
            Toast.makeText(this, "SD卡不可用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检测是否有读写SD卡的权限
     */
    private void checkoutPermisson() {
        // 申请权限。
        AndPermission.with(this)
                .requestCode(REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback(permissionListener)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框；
                // 这样避免用户勾选不再提示，导致以后无法申请权限。
                // 你也可以不设置。
                .rationale(rationaleListener)
                .start();
    }

    /**
     * 回调监听。
     */
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE: {
                    openFilePicker();
                    break;
                }
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE: {
                    Toast.makeText(MainActivity.this, "获取读写SD卡权限失败", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, deniedPermissions)) {
                // 第一种：用默认的提示语。
                AndPermission.defaultSettingDialog(MainActivity.this, REQUEST_CODE_SETTING).show();

                // 第二种：用自定义的提示语。
//             AndPermission.defaultSettingDialog(this, REQUEST_CODE_SETTING)
//                     .setTitle("权限申请失败")
//                     .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
//                     .setPositiveButton("好，去设置")
//                     .show();

//            第三种：自定义dialog样式。
//            SettingService settingHandle = AndPermission.defineSettingDialog(this, REQUEST_CODE_SETTING);
//            你的dialog点击了确定调用：
//            settingHandle.execute();
//            你的dialog点击了取消调用：
//            settingHandle.cancel();
            }
        }
    };

    /**
     * 打开文件管理器
     */
    private void openFilePicker() {
        FilePicker picker = new FilePicker(this, FilePicker.FILE);
        picker.setRootPath(StorageUtils.getExternalRootPath());
        //picker.setAllowExtensions(new String[]{".apk"});
        picker.setShowHideDir(false);
        picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
            @Override
            public void onFilePicked(String currentPath) {
                Toast.makeText(MainActivity.this, currentPath, Toast.LENGTH_LONG).show();
            }
        });
        picker.show();
    }

    /**
     * Rationale支持，这里自定义对话框。
     */
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            // 这里使用自定义对话框，如果不想自定义，用AndPermission默认对话框：
            // AndPermission.rationaleDialog(Context, Rationale).show();

            // 自定义对话框。
            AlertDialog.newBuilder(MainActivity.this)
                    .setTitle("友情提示")
                    .setMessage("您已经拒绝过获取读写SD卡权限，没有该权限无法正常使用文件管理器，赶快把权限给我吧")
                    .setPositiveButton("不，我拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.resume();
                        }
                    })
                    .setNegativeButton("好，我给你", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.cancel();
                        }
                    }).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SETTING: {
//                Toast.makeText(this, "用户从设置回来了", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }


}

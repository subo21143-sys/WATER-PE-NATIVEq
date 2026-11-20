package com.waterclient.overlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_OVERLAY = 410;
    private TextView permissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
        updatePermissionStatus();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(22));
        root.setBackgroundColor(Color.rgb(17, 10, 27));

        TextView title = new TextView(this);
        title.setText("WATER  PE");
        title.setTextColor(Color.rgb(220, 170, 255));
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("NATIVE ANDROID OVERLAY  •  V1.1");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        root.addView(subtitle, subParams);

        TextView description = new TextView(this);
        description.setText(
                "\nMenu nổi đồng hành cùng Minecraft Bedrock.\n" +
                "Có nút W kéo thả, tâm ngắm, đồng hồ và bộ đếm phiên chơi.\n\n" +
                "Đây là overlay bên ngoài game; ứng dụng không đọc bộ nhớ, " +
                "không sửa engine Minecraft và không cung cấp tọa độ/FPS nội bộ.");
        description.setTextColor(Color.rgb(220, 214, 228));
        description.setTextSize(15);
        root.addView(description);

        permissionStatus = new TextView(this);
        permissionStatus.setTextColor(Color.WHITE);
        permissionStatus.setTextSize(14);
        permissionStatus.setPadding(dp(12), dp(12), dp(12), dp(12));
        permissionStatus.setBackgroundColor(Color.rgb(43, 29, 58));
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dp(18);
        root.addView(permissionStatus, statusParams);

        Button permissionButton = makeButton("1. CẤP QUYỀN HIỂN THỊ");
        permissionButton.setOnClickListener(v -> requestOverlayPermission());
        addButton(root, permissionButton);

        Button startButton = makeButton("2. BẬT MENU NỔI");
        startButton.setOnClickListener(v -> startOverlay());
        addButton(root, startButton);

        Button stopButton = makeButton("TẮT MENU NỔI");
        stopButton.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            Toast.makeText(this, "Đã gửi lệnh tắt overlay", Toast.LENGTH_SHORT).show();
        });
        addButton(root, stopButton);

        TextView footer = new TextView(this);
        footer.setText("\nThiết bị mục tiêu: Android 8 trở lên • Redmi 12C / Android 12");
        footer.setTextColor(Color.GRAY);
        footer.setTextSize(12);
        root.addView(footer);

        setContentView(root);
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.rgb(113, 55, 156)));
        return button;
    }

    private void addButton(LinearLayout root, Button button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        params.topMargin = dp(12);
        root.addView(button, params);
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY);
        } else {
            updatePermissionStatus();
            Toast.makeText(this, "Đã có quyền hiển thị", Toast.LENGTH_SHORT).show();
        }
    }

    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            Toast.makeText(this, "Hãy cấp quyền rồi bấm BẬT MENU NỔI lần nữa",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent service = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }
        Toast.makeText(this, "WATER overlay đang chạy", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY) updatePermissionStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    private void updatePermissionStatus() {
        if (permissionStatus == null) return;
        boolean granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
        permissionStatus.setText(granted
                ? "● QUYỀN OVERLAY: ĐÃ CẤP"
                : "● QUYỀN OVERLAY: CHƯA CẤP");
        permissionStatus.setTextColor(granted
                ? Color.rgb(130, 255, 170) : Color.rgb(255, 190, 110));
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

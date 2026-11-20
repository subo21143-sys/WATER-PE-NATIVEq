package com.waterclient.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OverlayService extends Service {
    private static final String CHANNEL_ID = "water_overlay_service";
    private static final int NOTIFICATION_ID = 731;

    private WindowManager windowManager;
    private TextView bubble;
    private LinearLayout panel;
    private TextView crosshair;
    private TextView hud;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;
    private WindowManager.LayoutParams crosshairParams;
    private WindowManager.LayoutParams hudParams;

    private boolean crosshairEnabled = false;
    private boolean clockEnabled = true;
    private boolean timerEnabled = true;
    private long sessionStart = 0L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hudUpdater = new Runnable() {
        @Override public void run() {
            updateHudText();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        sessionStart = SystemClock.elapsedRealtime();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        if (canDrawOverlay()) {
            createBubble();
            handler.post(hudUpdater);
        } else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (bubble == null && canDrawOverlay()) {
            createBubble();
            handler.removeCallbacks(hudUpdater);
            handler.post(hudUpdater);
        }
        return START_STICKY;
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(hudUpdater);
        removeView(panel);
        removeView(bubble);
        removeView(crosshair);
        removeView(hud);
        super.onDestroy();
    }

    private boolean canDrawOverlay() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "WATER PE Overlay", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Thông báo khi menu nổi WATER đang hoạt động");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent openIntent = new Intent(this, MainActivity.class);
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openIntent, pendingFlags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder.setContentTitle("WATER PE Overlay")
                .setContentText("Menu nổi đang hoạt động")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createBubble() {
        if (bubble != null) return;
        bubble = new TextView(this);
        bubble.setText("W");
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(20);
        bubble.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bubble.setGravity(Gravity.CENTER);
        bubble.setBackground(round(Color.rgb(125, 55, 180), 28));

        bubbleParams = new WindowManager.LayoutParams(
                dp(54), dp(54), overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(14);
        bubbleParams.y = dp(180);

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float downX, downY;
            private boolean moved;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = bubbleParams.x;
                        startY = bubbleParams.y;
                        downX = event.getRawX();
                        downY = event.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved = true;
                        bubbleParams.x = startX + (int) dx;
                        bubbleParams.y = startY + (int) dy;
                        try { windowManager.updateViewLayout(bubble, bubbleParams); }
                        catch (Exception ignored) { }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) togglePanel();
                        return true;
                }
                return false;
            }
        });
        try { windowManager.addView(bubble, bubbleParams); }
        catch (Exception e) { bubble = null; }
    }

    private void togglePanel() {
        if (panel != null) removeView(panel);
        else createPanel();
    }

    private void createPanel() {
        if (panel != null || !canDrawOverlay()) return;
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(12));
        panel.setBackground(round(Color.rgb(25, 16, 39), 18));

        TextView header = new TextView(this);
        header.setText("WATER CLIENT  •  V1.1\nBedrock companion overlay");
        header.setTextColor(Color.rgb(224, 181, 255));
        header.setTextSize(16);
        header.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.setPadding(dp(4), dp(4), dp(4), dp(12));
        panel.addView(header);

        addToggle("Crosshair", crosshairEnabled, checked -> {
            crosshairEnabled = checked;
            updateCrosshair();
        });
        addToggle("Clock HUD", clockEnabled, checked -> {
            clockEnabled = checked;
            updateHudVisibility();
            updateHudText();
        });
        addToggle("Session timer", timerEnabled, checked -> {
            timerEnabled = checked;
            updateHudVisibility();
            updateHudText();
        });

        TextView note = new TextView(this);
        note.setText("Overlay ngoài game. Không đọc XYZ/FPS nội bộ hoặc điều khiển nhân vật.");
        note.setTextColor(Color.LTGRAY);
        note.setTextSize(11);
        note.setPadding(dp(4), dp(10), dp(4), dp(8));
        panel.addView(note);

        TextView close = new TextView(this);
        close.setText("ĐÓNG MENU");
        close.setTextColor(Color.WHITE);
        close.setTextSize(13);
        close.setGravity(Gravity.CENTER);
        close.setPadding(dp(10), dp(12), dp(10), dp(12));
        close.setBackground(round(Color.rgb(112, 49, 153), 12));
        close.setOnClickListener(v -> removeView(panel));
        panel.addView(close);

        panelParams = new WindowManager.LayoutParams(
                dp(292), WindowManager.LayoutParams.WRAP_CONTENT, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = dp(18);
        panelParams.y = dp(100);

        header.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float downX, downY;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = panelParams.x;
                    startY = panelParams.y;
                    downX = e.getRawX();
                    downY = e.getRawY();
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    panelParams.x = startX + (int) (e.getRawX() - downX);
                    panelParams.y = startY + (int) (e.getRawY() - downY);
                    try { windowManager.updateViewLayout(panel, panelParams); }
                    catch (Exception ignored) { }
                    return true;
                }
                return true;
            }
        });

        try { windowManager.addView(panel, panelParams); }
        catch (Exception e) { panel = null; }
    }

    private interface ToggleAction { void onChange(boolean checked); }

    private void addToggle(String label, boolean initial, ToggleAction action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(5), dp(4), dp(5));

        TextView name = new TextView(this);
        name.setText(label);
        name.setTextColor(Color.WHITE);
        name.setTextSize(14);
        row.addView(name, new LinearLayout.LayoutParams(0, dp(42), 1));

        Switch toggle = new Switch(this);
        toggle.setChecked(initial);
        toggle.setOnCheckedChangeListener((button, checked) -> action.onChange(checked));
        row.addView(toggle);
        panel.addView(row);
    }

    private void updateCrosshair() {
        if (crosshairEnabled && crosshair == null) {
            crosshair = new TextView(this);
            crosshair.setText("+");
            crosshair.setTextColor(Color.WHITE);
            crosshair.setTextSize(28);
            crosshair.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            crosshair.setGravity(Gravity.CENTER);
            crosshairParams = new WindowManager.LayoutParams(
                    dp(42), dp(42), overlayType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            crosshairParams.gravity = Gravity.CENTER;
            try { windowManager.addView(crosshair, crosshairParams); }
            catch (Exception e) { crosshair = null; }
        } else if (!crosshairEnabled) {
            removeView(crosshair);
        }
    }

    private void updateHudVisibility() {
        boolean show = clockEnabled || timerEnabled;
        if (show && hud == null) {
            hud = new TextView(this);
            hud.setTextColor(Color.WHITE);
            hud.setTextSize(11);
            hud.setPadding(dp(10), dp(7), dp(10), dp(7));
            hud.setBackground(round(0xCC1A1027, 10));
            hudParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    overlayType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            hudParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            hudParams.y = dp(38);
            try { windowManager.addView(hud, hudParams); }
            catch (Exception e) { hud = null; }
        } else if (!show) {
            removeView(hud);
        }
    }

    private void updateHudText() {
        updateHudVisibility();
        if (hud == null) return;
        StringBuilder text = new StringBuilder();
        if (clockEnabled) {
            text.append("TIME  ")
                    .append(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
        }
        if (timerEnabled) {
            if (text.length() > 0) text.append("\n");
            long elapsed = Math.max(0, (SystemClock.elapsedRealtime() - sessionStart) / 1000);
            long hours = elapsed / 3600;
            long minutes = (elapsed % 3600) / 60;
            long seconds = elapsed % 60;
            text.append(String.format(Locale.getDefault(), "SESSION  %02d:%02d:%02d",
                    hours, minutes, seconds));
        }
        hud.setText(text.toString());
    }

    private void removeView(View view) {
        if (view == null || windowManager == null) return;
        try { windowManager.removeView(view); } catch (Exception ignored) { }
        if (view == panel) panel = null;
        if (view == bubble) bubble = null;
        if (view == crosshair) crosshair = null;
        if (view == hud) hud = null;
    }

    private int overlayType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

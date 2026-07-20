package com.autostart;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
public class BootReceiver extends BroadcastReceiver {
    private static final String TARGET_PACKAGE = "com.oversea.aslauncher";
    private static final String TARGET_ACTIVITY = "com.oversea.aslauncher.ui.main.MainActivity";
    private static final long DELAY = 3000;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("BootLaunch", "Receive boot action: " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            new Handler().postDelayed(() -> startTargetApp(context), DELAY);
        }
    }
    private void startTargetApp(Context context) {
        try {
            Intent launchIntent = new Intent();
            launchIntent.setClassName(TARGET_PACKAGE, TARGET_ACTIVITY);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(launchIntent);
            Log.i("BootLaunch", "Launch target app success");
        } catch (Exception e) {
            Log.e("BootLaunch", "Launch failed", e);
        }
    }
}
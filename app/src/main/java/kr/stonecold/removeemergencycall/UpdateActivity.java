package kr.stonecold.removeemergencycall;

import android.app.Activity;
import android.os.Bundle;

public final class UpdateActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        android.widget.Button button = new android.widget.Button(this);
        button.setText("업데이트 확인");
        button.setOnClickListener(v -> AppUpdater.showUpdateSettingsDialog(this));
        setContentView(button);
        AppUpdater.checkAndPrompt(this, false);
    }
    @Override protected void onResume() {
        super.onResume();
        AppUpdater.onResume(this);
    }
}

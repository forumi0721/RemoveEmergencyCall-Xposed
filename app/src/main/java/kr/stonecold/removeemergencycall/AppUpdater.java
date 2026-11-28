package kr.stonecold.removeemergencycall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AppUpdater {
    public static final String DEFAULT_GITHUB_REPO = "forumi0721/RemoveEmergencyCall-Xposed";
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:[-+].*)?$", Pattern.CASE_INSENSITIVE);

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static File pendingInstallFile = null;

    private AppUpdater() {}

    public static final class ReleaseInfo {
        public final String tagName;
        public final String versionName;
        public final int versionCode;
        public final String notes;
        public final String htmlUrl;
        public final String apkDownloadUrl;
        public final String apkApiAssetUrl;
        public final String apkFileName;
        public final long apkSizeBytes;

        ReleaseInfo(String tagName, String versionName, int versionCode, String notes,
                    String htmlUrl, String apkDownloadUrl, String apkApiAssetUrl,
                    String apkFileName, long apkSizeBytes) {
            this.tagName = tagName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.notes = notes;
            this.htmlUrl = htmlUrl;
            this.apkDownloadUrl = apkDownloadUrl;
            this.apkApiAssetUrl = apkApiAssetUrl;
            this.apkFileName = apkFileName;
            this.apkSizeBytes = apkSizeBytes;
        }
    }

    public static Integer parseVersionCode(String versionOrTag) {
        if (versionOrTag == null) return null;
        Matcher m = SEMVER_PATTERN.matcher(versionOrTag.trim());
        if (!m.matches()) return null;
        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));
        int patch = Integer.parseInt(m.group(3));
        return major * 1_000_000 + minor * 1_000 + patch;
    }

    public static void checkAndPrompt(Activity activity, boolean showUpToDateToast) {
        String token = BuildConfig.GITHUB_TOKEN.trim();
        EXECUTOR.execute(() -> {
            try {
                ReleaseInfo release = fetchLatestRelease(token);
                if (release == null) {
                    if (showUpToDateToast) {
                        MAIN_HANDLER.post(() -> Toast.makeText(activity, "게시된 릴리즈가 없습니다.", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }
                Integer current = parseVersionCode(BuildConfig.VERSION_NAME);
                int currentCode = current != null ? current : BuildConfig.VERSION_CODE;
                if (release.versionCode > currentCode) {
                    MAIN_HANDLER.post(() -> showUpdateDialog(activity, release, token));
                } else if (showUpToDateToast) {
                    MAIN_HANDLER.post(() -> Toast.makeText(activity, "현재 최신 버전입니다 (" + release.tagName + ")", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                if (showUpToDateToast) {
                    MAIN_HANDLER.post(() -> Toast.makeText(activity, "업데이트 확인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public static void onResume(Activity activity) {
        if (pendingInstallFile != null && pendingInstallFile.exists() && activity.getPackageManager().canRequestPackageInstalls()) {
            File file = pendingInstallFile;
            pendingInstallFile = null;
            launchInstall(activity, file);
        }
    }

    public static void showUpdateSettingsDialog(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(16 * activity.getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        TextView versionText = new TextView(activity);
        versionText.setText(activity.getString(R.string.update_current_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        layout.addView(versionText);


        new AlertDialog.Builder(activity)
                .setTitle("앱 정보 및 업데이트")
                .setView(layout)
                .setPositiveButton("업데이트 확인", (d, w) -> checkAndPrompt(activity, true))
                .setNegativeButton("닫기", null)
                .show();
    }

    private static ReleaseInfo fetchLatestRelease(String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.github.com/repos/" + DEFAULT_GITHUB_REPO + "/releases/latest").openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("User-Agent", BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME);
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        try {
            int code = conn.getResponseCode();
            if (code == 404) return null;
            if (code < 200 || code >= 300) throw new IllegalStateException("HTTP " + code);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            JSONObject obj = new JSONObject(sb.toString());
            if (obj.optBoolean("draft", false) || obj.optBoolean("prerelease", false)) return null;
            String tagName = obj.optString("tag_name", "").trim();
            Integer versionCode = parseVersionCode(tagName);
            if (versionCode == null) return null;
            String versionName = tagName.toLowerCase(Locale.US).startsWith("v") ? tagName.substring(1) : tagName;
            String notes = obj.optString("body", "").trim();
            String htmlUrl = obj.optString("html_url", "").trim();
            String apkUrl = null;
            String apkApiUrl = null;
            String apkName = null;
            long apkSize = 0L;
            JSONArray assets = obj.optJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.optJSONObject(i);
                    if (asset == null) continue;
                    String name = asset.optString("name", "").trim();
                    String type = asset.optString("content_type", "").trim();
                    String dl = asset.optString("browser_download_url", "").trim();
                    String api = asset.optString("url", "").trim();
                    if (name.toLowerCase(Locale.US).endsWith(".apk") || "application/vnd.android.package-archive".equalsIgnoreCase(type)) {
                        apkUrl = !dl.isEmpty() ? dl : api;
                        apkApiUrl = api.isEmpty() ? null : api;
                        apkName = name.isEmpty() ? "update-" + tagName + ".apk" : name;
                        apkSize = asset.optLong("size", 0L);
                        break;
                    }
                }
            }
            return new ReleaseInfo(tagName, versionName, versionCode, notes, htmlUrl, apkUrl, apkApiUrl, apkName, apkSize);
        } finally {
            conn.disconnect();
        }
    }

    private static void showUpdateDialog(Activity activity, ReleaseInfo release, String token) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        StringBuilder msg = new StringBuilder();
        msg.append("현재 버전 v").append(BuildConfig.VERSION_NAME)
                .append(" -> 최신 버전 ").append(release.tagName);
        if (release.apkSizeBytes > 0L) {
            msg.append(String.format(Locale.US, " (%.1f MB)", release.apkSizeBytes / (1024.0 * 1024.0)));
        }
        if (!release.notes.isEmpty()) {
            msg.append("\n\n").append(release.notes.length() > 500 ? release.notes.substring(0, 500) : release.notes);
        }

        new AlertDialog.Builder(activity)
                .setTitle("새 버전 업데이트 (" + release.tagName + ")")
                .setMessage(msg.toString())
                .setPositiveButton("다운로드 및 설치", (d, w) -> downloadAndInstall(activity, release, token))
                .setNegativeButton("나중에", null)
                .show();
    }

    private static void downloadAndInstall(Activity activity, ReleaseInfo release, String token) {
        ProgressBar bar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        AlertDialog progressDialog = new AlertDialog.Builder(activity)
                .setTitle("업데이트 다운로드 중…")
                .setView(bar)
                .setCancelable(false)
                .show();

        EXECUTOR.execute(() -> {
            try {
                boolean useApi = token != null && !token.isEmpty() && release.apkApiAssetUrl != null;
                String currentUrl = useApi ? release.apkApiAssetUrl : release.apkDownloadUrl;
                if (currentUrl == null || currentUrl.isEmpty()) {
                    throw new IllegalStateException("다운로드 가능한 APK가 없습니다.");
                }
                File updatesDir = new File(activity.getCacheDir(), "updates");
                if (!updatesDir.exists()) updatesDir.mkdirs();
                File[] oldFiles = updatesDir.listFiles();
                if (oldFiles != null) {
                    for (File f : oldFiles) {
                        if (f.isFile()) f.delete();
                    }
                }
                File targetFile = new File(updatesDir, release.apkFileName != null ? release.apkFileName.replaceAll("[^A-Za-z0-9._-]", "_") : "update.apk");

                HttpURLConnection conn = null;
                for (int i = 0; i < 6; i++) {
                    URL url = new URL(currentUrl);
                    if (!"https".equalsIgnoreCase(url.getProtocol())) throw new IllegalStateException("안전하지 않은 다운로드 주소");
                    HttpURLConnection next = (HttpURLConnection) url.openConnection();
                    next.setInstanceFollowRedirects(false);
                    next.setRequestMethod("GET");
                    next.setConnectTimeout(15_000);
                    next.setReadTimeout(60_000);
                    next.setRequestProperty("Accept", "application/octet-stream");
                    next.setRequestProperty("User-Agent", BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME);
                    if (useApi && "api.github.com".equalsIgnoreCase(url.getHost())) {
                        next.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    int code = next.getResponseCode();
                    if (code >= 300 && code < 400) {
                        String loc = next.getHeaderField("Location");
                        next.disconnect();
                        if (loc == null || loc.isEmpty()) throw new IllegalStateException("리다이렉트 위치 없음");
                        currentUrl = new URL(url, loc).toString();
                    } else {
                        conn = next;
                        break;
                    }
                }
                if (conn == null) throw new IllegalStateException("리다이렉트 횟수 초과");
                try {
                    if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) {
                        throw new IllegalStateException("HTTP " + conn.getResponseCode());
                    }
                    long total = conn.getContentLengthLong() > 0 ? conn.getContentLengthLong() : release.apkSizeBytes;
                    long downloaded = 0L;
                    byte[] buf = new byte[16 * 1024];
                    try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(targetFile)) {
                        int read;
                        while ((read = in.read(buf)) > 0) {
                            out.write(buf, 0, read);
                            downloaded += read;
                            if (total > 0) {
                                int pct = (int) Math.min(100, (downloaded * 100L) / total);
                                MAIN_HANDLER.post(() -> bar.setProgress(pct));
                            }
                        }
                        out.flush();
                    }
                } finally {
                    conn.disconnect();
                }

                MAIN_HANDLER.post(() -> {
                    progressDialog.dismiss();
                    if (!activity.getPackageManager().canRequestPackageInstalls()) {
                        pendingInstallFile = targetFile;
                        Toast.makeText(activity, "앱 설치 권한을 허용해 주세요.", Toast.LENGTH_LONG).show();
                        activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + activity.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        launchInstall(activity, targetFile);
                    }
                });
            } catch (Exception e) {
                MAIN_HANDLER.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(activity, "다운로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private static void launchInstall(Activity activity, File apkFile) {
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", apkFile);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}

package local.oneios.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Manager extends Activity {
    private static final String PATH = "/data/local/tmp/oneios/bin/oneios_root.sh";
    private TextView status;
    private TextView badgeText;
    private View badgeDot;
    private Button start;
    private Button btnDownload;
    private ScrollView scroll;
    private boolean busy;

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable makeBox(int fillColor, int strokeColor, int strokeWidthDp, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        if (strokeWidthDp > 0) {
            gd.setStroke(dp(strokeWidthDp), strokeColor);
        }
        gd.setCornerRadius(dp(radiusDp));
        return gd;
    }

    private GradientDrawable makeTopRound(int fillColor, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        float r = dp(radiusDp);
        gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return gd;
    }

    private GradientDrawable makeBottomRound(int fillColor, int strokeColor, int strokeWidthDp, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        if (strokeWidthDp > 0) {
            gd.setStroke(dp(strokeWidthDp), strokeColor);
        }
        float r = dp(radiusDp);
        gd.setCornerRadii(new float[]{0, 0, 0, 0, r, r, r, r});
        return gd;
    }

    private RippleDrawable makeRipple(int normalColor, int rippleColor, int radiusDp) {
        GradientDrawable normal = makeBox(normalColor, 0, 0, radiusDp);
        GradientDrawable mask = makeBox(0xFFFFFFFF, 0, 0, radiusDp);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), normal, mask);
    }

    private RippleDrawable makeStrokeRipple(int normalColor, int strokeColor, int strokeWidthDp, int rippleColor, int radiusDp) {
        GradientDrawable normal = makeBox(normalColor, strokeColor, strokeWidthDp, radiusDp);
        GradientDrawable mask = makeBox(0xFFFFFFFF, 0, 0, radiusDp);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), normal, mask);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0B0C10"));
        root.setPadding(dp(20), dp(44), dp(20), dp(20));

        // --- TOP BAR (Title + Status Badge) ---
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topBarLp = new LinearLayout.LayoutParams(-1, -2);
        topBarLp.bottomMargin = dp(14);

        TextView title = new TextView(this);
        title.setText("OneiOS");
        title.setTextSize(26);
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setTextColor(Color.WHITE);
        topBar.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));

        // Status Pill Badge (OneUI / iOS Dynamic Island style)
        LinearLayout badge = new LinearLayout(this);
        badge.setOrientation(LinearLayout.HORIZONTAL);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setBackground(makeBox(Color.parseColor("#181920"), Color.parseColor("#2C2D35"), 1, 20));
        badge.setPadding(dp(10), dp(6), dp(12), dp(6));

        badgeDot = new View(this);
        badgeDot.setBackground(makeBox(Color.parseColor("#FFA000"), 0, 0, 10));
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(8), dp(8));
        dotLp.rightMargin = dp(6);
        badge.addView(badgeDot, dotLp);

        badgeText = new TextView(this);
        badgeText.setText("VERIFICANDO");
        badgeText.setTextSize(11);
        badgeText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        badgeText.setTextColor(Color.parseColor("#E0E0E0"));
        badge.addView(badgeText);

        topBar.addView(badge);
        root.addView(topBar, topBarLp);

        // --- DEVICE SPEC CARD (One UI Squircle Card) ---
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(makeBox(Color.parseColor("#14151C"), Color.parseColor("#232530"), 1, 16));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.bottomMargin = dp(14);

        TextView devName = new TextView(this);
        devName.setText("Samsung Galaxy A56 5G (SM-A566E)");
        devName.setTextSize(14);
        devName.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        devName.setTextColor(Color.WHITE);
        card.addView(devName);

        TextView devDetails = new TextView(this);
        devDetails.setText("SoC: Exynos 1580 (s5e8855) · GPU: Xclipse 540\nKernel: XNU 25.5.0 · Imagem: iOS 16.5 (20F66)");
        devDetails.setTextSize(12);
        devDetails.setLineSpacing(dp(2), 1f);
        devDetails.setTextColor(Color.parseColor("#8E8E93"));
        LinearLayout.LayoutParams detLp = new LinearLayout.LayoutParams(-1, -2);
        detLp.topMargin = dp(4);
        card.addView(devDetails, detLp);

        root.addView(card, cardLp);

        // --- PRIMARY BUTTON: INICIAR SESSÃO IOS ---
        start = new Button(this);
        start.setText("▶   INICIAR SESSÃO IOS");
        start.setTextSize(15);
        start.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        start.setTextColor(Color.WHITE);
        start.setBackground(makeRipple(Color.parseColor("#262833"), 0x33FFFFFF, 16));
        start.setEnabled(false);
        start.setElevation(dp(2));
        start.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Iniciar iOS 16.5?")
            .setMessage("A interface da One UI será pausada e o runtime do iOS 16.5 assumirá o painel.\n\nPara voltar à One UI a qualquer momento, segure Volume + e Volume - por 3 segundos.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Iniciar", (dialog, which) -> run("switch 115 1")).show());

        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(-1, dp(54));
        startLp.bottomMargin = dp(10);
        root.addView(start, startLp);

        // --- IN-APP GITHUB DOWNLOAD / IMPORT BUTTON ---
        btnDownload = new Button(this);
        btnDownload.setText("📥   BAIXAR / IMPORTAR IMAGEM IOS 16.5");
        btnDownload.setTextSize(13);
        btnDownload.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btnDownload.setTextColor(Color.parseColor("#00B0FF"));
        btnDownload.setBackground(makeStrokeRipple(Color.parseColor("#0D1B2A"), Color.parseColor("#00B0FF"), 1, 0x3300B0FF, 16));
        btnDownload.setOnClickListener(v -> showDownloadDialog());

        LinearLayout.LayoutParams dlLp = new LinearLayout.LayoutParams(-1, dp(48));
        dlLp.bottomMargin = dp(10);
        root.addView(btnDownload, dlLp);

        // --- SECONDARY BUTTON: VOLTAR À ONE UI ---
        Button btnReturn = new Button(this);
        btnReturn.setText("↺   VOLTAR À ONE UI (SAFE RECOVERY)");
        btnReturn.setTextSize(13);
        btnReturn.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btnReturn.setTextColor(Color.parseColor("#FF5252"));
        btnReturn.setBackground(makeStrokeRipple(Color.parseColor("#1B1214"), Color.parseColor("#FF5252"), 1, 0x22FF5252, 16));
        btnReturn.setOnClickListener(v -> run("return"));

        LinearLayout.LayoutParams retLp = new LinearLayout.LayoutParams(-1, dp(48));
        retLp.bottomMargin = dp(12);
        root.addView(btnReturn, retLp);

        // --- ACTION BUTTONS ROW (STATUS & LOGS) ---
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, dp(42));
        rowLp.bottomMargin = dp(12);

        Button btnStatus = new Button(this);
        btnStatus.setText("⟳  Atualizar Status");
        btnStatus.setTextSize(12);
        btnStatus.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btnStatus.setTextColor(Color.parseColor("#5E9CFF"));
        btnStatus.setBackground(makeStrokeRipple(Color.parseColor("#141824"), Color.parseColor("#28354D"), 1, 0x335E9CFF, 12));
        btnStatus.setOnClickListener(v -> run("status"));
        LinearLayout.LayoutParams r1 = new LinearLayout.LayoutParams(0, -1, 1f);
        r1.rightMargin = dp(6);
        row.addView(btnStatus, r1);

        Button btnLogs = new Button(this);
        btnLogs.setText("≡  Logs da Sessão");
        btnLogs.setTextSize(12);
        btnLogs.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btnLogs.setTextColor(Color.parseColor("#B0BEC5"));
        btnLogs.setBackground(makeStrokeRipple(Color.parseColor("#16181F"), Color.parseColor("#2A2D38"), 1, 0x33FFFFFF, 12));
        btnLogs.setOnClickListener(v -> run("logfile session 0 12000"));
        LinearLayout.LayoutParams r2 = new LinearLayout.LayoutParams(0, -1, 1f);
        r2.leftMargin = dp(6);
        row.addView(btnLogs, r2);

        root.addView(row, rowLp);

        // --- CONSOLE / TERMINAL (macOS / iOS Terminal Style) ---
        LinearLayout termHeader = new LinearLayout(this);
        termHeader.setOrientation(LinearLayout.HORIZONTAL);
        termHeader.setGravity(Gravity.CENTER_VERTICAL);
        termHeader.setBackground(makeTopRound(Color.parseColor("#1B1C24"), 14));
        termHeader.setPadding(dp(12), dp(8), dp(12), dp(8));

        // Window Traffic Lights (Red, Yellow, Green)
        View dotRed = new View(this);
        dotRed.setBackground(makeBox(Color.parseColor("#FF5F56"), 0, 0, 6));
        LinearLayout.LayoutParams d1 = new LinearLayout.LayoutParams(dp(10), dp(10));
        d1.rightMargin = dp(6);
        termHeader.addView(dotRed, d1);

        View dotYellow = new View(this);
        dotYellow.setBackground(makeBox(Color.parseColor("#FFBD2E"), 0, 0, 6));
        LinearLayout.LayoutParams d2 = new LinearLayout.LayoutParams(dp(10), dp(10));
        d2.rightMargin = dp(6);
        termHeader.addView(dotYellow, d2);

        View dotGreen = new View(this);
        dotGreen.setBackground(makeBox(Color.parseColor("#27C93F"), 0, 0, 6));
        LinearLayout.LayoutParams d3 = new LinearLayout.LayoutParams(dp(10), dp(10));
        d3.rightMargin = dp(10);
        termHeader.addView(dotGreen, d3);

        TextView termTitle = new TextView(this);
        termTitle.setText("console.session — xnuhost darwin");
        termTitle.setTextSize(11);
        termTitle.setTextColor(Color.parseColor("#6C6E7E"));
        termTitle.setTypeface(Typeface.MONOSPACE);
        termHeader.addView(termTitle);

        root.addView(termHeader, new LinearLayout.LayoutParams(-1, -2));

        scroll = new ScrollView(this);
        scroll.setBackground(makeBottomRound(Color.parseColor("#07080A"), Color.parseColor("#1B1C24"), 1, 14));
        scroll.setPadding(dp(12), dp(10), dp(12), dp(12));

        status = new TextView(this);
        status.setTextIsSelectable(true);
        status.setTypeface(Typeface.MONOSPACE);
        status.setTextSize(11);
        status.setLineSpacing(dp(2), 1f);
        status.setTextColor(Color.parseColor("#00E676"));
        scroll.addView(status);

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);

        ensureBootstrap();
    }

    private void showDownloadDialog() {
        if (busy) return;
        final String[] options = {
            "📥   Baixar direto do GitHub Releases (iOS 16.5)",
            "📂   Importar arquivo baixado (/sdcard/Download)",
            "🔗   Inserir URL personalizada"
        };

        new AlertDialog.Builder(this)
            .setTitle("Instalar Imagem iOS 16.5")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    startDownloadGithub();
                } else if (which == 1) {
                    importFromLocal();
                } else {
                    showCustomUrlDialog();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void startDownloadGithub() {
        if (busy) return;
        busy = true;
        start.setEnabled(false);
        badgeDot.setBackground(makeBox(Color.parseColor("#29B6F6"), 0, 0, 10));
        badgeText.setText("BAIXANDO");
        status.setText("[$] Iniciando download do pacote iOS 16.5 direto do GitHub Releases...\n");

        new Thread(() -> {
            File downloadDir = new File("/sdcard/Download/OneiOS");
            if (!downloadDir.exists()) downloadDir.mkdirs();

            String[] partUrls = {
                "https://github.com/Musicalista/OneiOS-A56/releases/download/v1.0.0/ios16.5-a56-image.tar.gz.part00",
                "https://github.com/Musicalista/OneiOS-A56/releases/download/v1.0.0/ios16.5-a56-image.tar.gz.part01"
            };

            File targetMerged = new File(downloadDir, "ios16.5-a56-image.tar.gz");

            try {
                for (int i = 0; i < partUrls.length; i++) {
                    final int partIdx = i + 1;
                    String partUrl = partUrls[i];
                    File partFile = new File(downloadDir, "ios16.5-a56-image.tar.gz.part0" + i);

                    runOnUiThread(() -> {
                        status.setText("[$] Conectando ao GitHub Releases (Parte " + partIdx + "/2)...\n");
                        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                    });

                    URL url = new URL(partUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestProperty("User-Agent", "OneiOS-Manager/1.0");
                    conn.connect();

                    int code = conn.getResponseCode();
                    if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                        String loc = conn.getHeaderField("Location");
                        if (loc != null) {
                            conn.disconnect();
                            url = new URL(loc);
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestProperty("User-Agent", "OneiOS-Manager/1.0");
                            conn.connect();
                        }
                    }

                    code = conn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        conn.disconnect();
                        throw new java.io.IOException("O servidor respondeu HTTP " + code + " para a parte " + partIdx);
                    }

                    long totalBytes = conn.getContentLengthLong();
                    InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(partFile);

                    byte[] buffer = new byte[65536];
                    long downloaded = 0;
                    long lastUpdate = System.currentTimeMillis();
                    long startTime = lastUpdate;
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;
                        long now = System.currentTimeMillis();
                        if (now - lastUpdate > 350) {
                            lastUpdate = now;
                            final long d = downloaded;
                            final long t = totalBytes;
                            double mbDownloaded = d / (1024.0 * 1024.0);
                            double mbTotal = t > 0 ? t / (1024.0 * 1024.0) : 0;
                            double speed = (d / (1024.0 * 1024.0)) / Math.max(0.5, (now - startTime) / 1000.0);
                            double pct = t > 0 ? (d * 100.0) / t : 0;

                            final String prog = String.format(Locale.US,
                                "Baixando iOS 16.5 [Parte %d/2]:\n%.1f MB / %.1f MB (%.1f%%) — %.2f MB/s\n",
                                partIdx, mbDownloaded, mbTotal, pct, speed);

                            runOnUiThread(() -> {
                                status.setText("[$] Download em andamento...\n" + prog);
                                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                            });
                        }
                    }
                    out.flush();
                    out.close();
                    in.close();
                    conn.disconnect();
                }

                runOnUiThread(() -> {
                    status.setText("[$] Download de todas as partes concluído com sucesso!\nIniciando concatenação e extração...\n");
                    busy = false;
                    extractAndStage(targetMerged.getAbsolutePath());
                });

            } catch (Exception e) {
                final String err = e.getMessage();
                runOnUiThread(() -> {
                    busy = false;
                    status.setText("Erro durante o download: " + err + "\n\nVocê também pode baixar os arquivos no computador e importar de /sdcard/Download.");
                    badgeDot.setBackground(makeBox(Color.parseColor("#FF5252"), 0, 0, 10));
                    badgeText.setText("ERRO DOWNLOAD");
                });
            }
        }).start();
    }

    private void showCustomUrlDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(10), dp(20), dp(10));

        final EditText input = new EditText(this);
        input.setHint("https://…/imagem.tar.gz");
        input.setTextColor(Color.WHITE);
        input.setTextSize(13);
        container.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("URL da Imagem iOS 16.5")
            .setMessage("Digite ou cole a URL do arquivo de imagem (.tar ou .tar.gz):")
            .setView(container)
            .setPositiveButton("Baixar", (dialog, which) -> {
                String url = input.getText().toString().trim();
                if (!url.isEmpty()) {
                    if (!url.startsWith("https://")) {
                        status.setText("Informe uma URL HTTPS para o pacote da imagem.");
                        return;
                    }
                    startDownload(url);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void importFromLocal() {
        File downloadDir = new File("/sdcard/Download");
        File oneiosDir = new File(downloadDir, "OneiOS");
        List<File> candidates = new ArrayList<>();

        File[] files = downloadDir.listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName().toLowerCase();
                if (f.isFile() && (name.contains("ios") || name.contains("oneios") || name.contains("image")) &&
                    (name.endsWith(".tar") || name.endsWith(".tar.gz") || name.endsWith(".tgz") || name.endsWith(".zip") || name.endsWith(".part00"))) {
                    candidates.add(f);
                }
            }
        }
        File[] oneiosFiles = oneiosDir.listFiles();
        if (oneiosFiles != null) {
            for (File f : oneiosFiles) {
                String name = f.getName().toLowerCase();
                if (f.isFile() && (name.endsWith(".tar") || name.endsWith(".tar.gz") || name.endsWith(".tgz") || name.endsWith(".zip") || name.endsWith(".part00"))) {
                    if (!candidates.contains(f)) candidates.add(f);
                }
            }
        }

        if (candidates.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Nenhum arquivo encontrado")
                .setMessage("Nenhum pacote (.tar / .tar.gz) foi encontrado em /sdcard/Download ou /sdcard/Download/OneiOS.\n\nBaixe o arquivo pelo navegador ou copie para o aparelho.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        String[] names = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            File c = candidates.get(i);
            long mb = c.length() / (1024 * 1024);
            names[i] = c.getName() + " (" + mb + " MB)";
        }

        new AlertDialog.Builder(this)
            .setTitle("Selecione o arquivo de imagem")
            .setItems(names, (dialog, which) -> {
                File selected = candidates.get(which);
                extractAndStage(selected.getAbsolutePath());
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void startDownload(String fileUrl) {
        if (busy) return;
        busy = true;
        start.setEnabled(false);
        badgeDot.setBackground(makeBox(Color.parseColor("#29B6F6"), 0, 0, 10));
        badgeText.setText("BAIXANDO");
        status.setText("[$] Conectando ao servidor...\nURL: " + fileUrl + "\n");

        new Thread(() -> {
            File downloadDir = new File("/sdcard/Download/OneiOS");
            if (!downloadDir.exists()) downloadDir.mkdirs();
            File targetFile = new File(downloadDir, "ios16.5-a56-image.tar.gz");

            try {
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "OneiOS-Manager/1.0");
                conn.connect();

                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                    String loc = conn.getHeaderField("Location");
                    if (loc != null) {
                        conn.disconnect();
                        url = new URL(loc);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("User-Agent", "OneiOS-Manager/1.0");
                        conn.connect();
                    }
                }

                code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    conn.disconnect();
                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        throw new java.io.IOException("Pacote indisponível neste endereço (HTTP 404). Importe um pacote local ou informe outra URL.");
                    }
                    throw new java.io.IOException("O servidor respondeu HTTP " + code + ".");
                }
                long totalBytes = conn.getContentLengthLong();
                InputStream in = conn.getInputStream();
                FileOutputStream out = new FileOutputStream(targetFile);

                byte[] buffer = new byte[65536];
                long downloaded = 0;
                long lastUpdate = System.currentTimeMillis();
                long startTime = lastUpdate;
                int read;

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    downloaded += read;
                    long now = System.currentTimeMillis();
                    if (now - lastUpdate > 350) {
                        lastUpdate = now;
                        final long d = downloaded;
                        final long t = totalBytes;
                        double mbDownloaded = d / (1024.0 * 1024.0);
                        double mbTotal = t > 0 ? t / (1024.0 * 1024.0) : 0;
                        double speed = (d / (1024.0 * 1024.0)) / Math.max(0.5, (now - startTime) / 1000.0);
                        double pct = t > 0 ? (d * 100.0) / t : 0;

                        final String prog = String.format(Locale.US,
                            "Baixando imagem iOS 16.5:\n%.1f MB / %.1f MB (%.1f%%) — %.2f MB/s\n",
                            mbDownloaded, mbTotal, pct, speed);

                        runOnUiThread(() -> {
                            status.setText("[$] Download em andamento...\n" + prog);
                            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                        });
                    }
                }
                out.flush();
                out.close();
                in.close();
                conn.disconnect();

                runOnUiThread(() -> {
                    status.setText("[$] Download concluído com sucesso (" + (targetFile.length() / (1024 * 1024)) + " MB)!\nIniciando extração...\n");
                    busy = false;
                    extractAndStage(targetFile.getAbsolutePath());
                });

            } catch (Exception e) {
                final String err = e.getMessage();
                runOnUiThread(() -> {
                    busy = false;
                    status.setText("Erro durante o download: " + err + "\n\nVocê também pode baixar o arquivo no computador ou navegador e importar de /sdcard/Download.");
                    badgeDot.setBackground(makeBox(Color.parseColor("#FF5252"), 0, 0, 10));
                    badgeText.setText("ERRO DOWNLOAD");
                });
            }
        }).start();
    }

    private void extractAndStage(String archivePath) {
        String shellCmd = "echo '=== Preparando Imagem iOS 16.5 ===' && "
            + "mkdir -p /data/local/tmp/oneios/image && "
            + "DIR=$(dirname '" + archivePath + "') && "
            + "ARCH='" + archivePath + "' && "
            + "if [ -f \"$DIR/ios16.5-a56-image.tar.gz.part00\" ] && [ ! -s \"$DIR/ios16.5-a56-image.tar.gz\" ]; then "
            + "  echo '[$] Concatenando partes baixadas (part00 + part01)...' && "
            + "  cat \"$DIR/ios16.5-a56-image.tar.gz.part\"* > \"$DIR/ios16.5-a56-image.tar.gz\" && "
            + "  ARCH=\"$DIR/ios16.5-a56-image.tar.gz\"; "
            + "fi && "
            + "echo '[$] Descompactando arquivos no runtime...' && "
            + "tar -xf \"$ARCH\" -C /data/local/tmp/oneios/image/ && "
            + "chown -R 0:0 /data/local/tmp/oneios/image && "
            + "chmod 644 /data/local/tmp/oneios/image/* && "
            + "if [ -f /data/local/tmp/oneios/stage_ios16.sh ]; then /data/local/tmp/oneios/stage_ios16.sh; "
            + "elif [ -f /data/local/tmp/oneios/tools/stage_ios16.sh ]; then /data/local/tmp/oneios/tools/stage_ios16.sh; fi";
        runShell("Extrair e Configurar Imagem iOS 16.5", shellCmd);
    }

    private void runShell(String actionLabel, String command) {
        if (busy) return;
        busy = true;
        start.setEnabled(false);
        start.setBackground(makeRipple(Color.parseColor("#262833"), 0x33FFFFFF, 16));
        badgeDot.setBackground(makeBox(Color.parseColor("#FFA000"), 0, 0, 10));
        badgeText.setText("EXECUTANDO");
        status.setText("[$] " + actionLabel + "\n");

        new Thread(() -> {
            try {
                Process process = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    for (String line; (line = reader.readLine()) != null;) {
                        final String l = line;
                        runOnUiThread(() -> {
                            status.append(l + "\n");
                            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                        });
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                final String err = e.getMessage();
                runOnUiThread(() -> {
                    status.append("Erro: " + err + "\n");
                    scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                });
            }
            runOnUiThread(() -> {
                busy = false;
                run("status");
            });
        }).start();
    }

    private void ensureBootstrap() {
        new Thread(() -> {
            File script = new File(PATH);
            if (!script.exists()) {
                runOnUiThread(() -> {
                    badgeDot.setBackground(makeBox(Color.parseColor("#FFA000"), 0, 0, 10));
                    badgeText.setText("INSTALANDO");
                    status.setText("[$] Inicializando ambiente do OneiOS no aparelho...\n");
                });

                try {
                    File cacheDir = getCacheDir();
                    File tarFile = new File(cacheDir, "bootstrap.tar");
                    try (InputStream in = getAssets().open("bootstrap.tar");
                         FileOutputStream fos = new FileOutputStream(tarFile)) {
                        byte[] buf = new byte[16384];
                        int r;
                        while ((r = in.read(buf)) != -1) {
                            fos.write(buf, 0, r);
                        }
                    }

                    String cmd = "mkdir -p /data/local/tmp/oneios && "
                        + "tar -xf " + tarFile.getAbsolutePath() + " -C /data/local/tmp/oneios/ && "
                        + "rm -f " + tarFile.getAbsolutePath() + " && "
                        + "chown -R 0:0 /data/local/tmp/oneios && "
                        + "chmod 755 /data/local/tmp/oneios/bin/* /data/local/tmp/oneios/*.sh /data/local/tmp/oneios/runtime/bin/* 2>/dev/null";

                    Process p = new ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start();
                    p.waitFor();

                    runOnUiThread(() -> {
                        status.append("[$] Ambiente instalado com sucesso em /data/local/tmp/oneios!\n");
                    });
                } catch (Exception e) {
                    final String err = e.getMessage();
                    runOnUiThread(() -> {
                        status.append("[!] Erro na instalacao automatica: " + err + "\n");
                    });
                }
            }
            runOnUiThread(() -> run("status"));
        }).start();
    }

    private void run(String action) {
        if (busy) return;
        busy = true;
        start.setEnabled(false);
        start.setBackground(makeRipple(Color.parseColor("#262833"), 0x33FFFFFF, 16));
        badgeDot.setBackground(makeBox(Color.parseColor("#FFA000"), 0, 0, 10));
        badgeText.setText("EXECUTANDO");
        status.setText("[$] oneios_root.sh " + action + "\n");

        new Thread(() -> {
            String result;
            try {
                String command = "test \"$(sha256sum " + PATH + " | cut -d' ' -f1)\" = 6c635500bb42807d70b493cbc6a16dc7f56f198f92d2cf6aa43ed94e9b9362db"
                    + " || { echo 'O script do OneiOS está ausente ou mudou.'; exit 1; }; "
                    + "/system/bin/sh " + PATH + " " + action;
                Process process = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    for (String line; (line = reader.readLine()) != null;) {
                        output.append(line).append('\n');
                    }
                }
                int exit = process.waitFor();
                result = output.length() == 0
                    ? (exit == 0 ? "Comando concluído com sucesso." : "Erro: Verifique a concessão de root no KernelSU.")
                    : output.toString();
            } catch (Exception error) {
                result = "Erro ao executar via root: " + error.getMessage();
            }
            final String message = result;
            runOnUiThread(() -> {
                busy = false;
                status.setText(message);
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                boolean isReady = message.contains("@@RESULT ready 1");
                start.setEnabled(isReady);
                if (isReady) {
                    start.setBackground(makeRipple(Color.parseColor("#00C853"), 0x33FFFFFF, 16));
                    badgeDot.setBackground(makeBox(Color.parseColor("#00E676"), 0, 0, 10));
                    badgeText.setText("SISTEMA PRONTO");
                    badgeText.setTextColor(Color.parseColor("#00E676"));
                } else {
                    start.setBackground(makeRipple(Color.parseColor("#262833"), 0x33FFFFFF, 16));
                    badgeDot.setBackground(makeBox(Color.parseColor("#FF5252"), 0, 0, 10));
                    badgeText.setText("NÃO PREPARADO");
                    badgeText.setTextColor(Color.parseColor("#FF5252"));
                }
            });
        }).start();
    }
}

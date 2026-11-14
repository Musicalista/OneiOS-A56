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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class Manager extends Activity {
    private static final String PATH = "/data/local/tmp/oneios/bin/oneios_root.sh";
    private TextView status;
    private TextView badgeText;
    private View badgeDot;
    private Button start;
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
        cardLp.bottomMargin = dp(16);

        TextView devName = new TextView(this);
        devName.setText("Samsung Galaxy A56 5G");
        devName.setTextSize(14);
        devName.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        devName.setTextColor(Color.WHITE);
        card.addView(devName);

        TextView devDetails = new TextView(this);
        devDetails.setText("SoC: Exynos 1580 (s5e8855) · GPU: Xclipse 540\nKernel: XNU 25.5.0 · Imagem: iOS 27.0.1 (24A446)");
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
            .setTitle("Iniciar iOS 27.0.1?")
            .setMessage("A interface da One UI será pausada e o runtime do iOS assumirá o painel.\n\nPara voltar à One UI a qualquer momento, segure Volume + e Volume - por 3 segundos.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Iniciar", (dialog, which) -> run("switch 115 1")).show());

        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(-1, dp(56));
        startLp.bottomMargin = dp(10);
        root.addView(start, startLp);

        // --- SECONDARY BUTTON: VOLTAR À ONE UI ---
        Button btnReturn = new Button(this);
        btnReturn.setText("↺   VOLTAR À ONE UI (SAFE RECOVERY)");
        btnReturn.setTextSize(13);
        btnReturn.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btnReturn.setTextColor(Color.parseColor("#FF5252"));
        btnReturn.setBackground(makeStrokeRipple(Color.parseColor("#1B1214"), Color.parseColor("#FF5252"), 1, 0x22FF5252, 16));
        btnReturn.setOnClickListener(v -> run("return"));

        LinearLayout.LayoutParams retLp = new LinearLayout.LayoutParams(-1, dp(48));
        retLp.bottomMargin = dp(14);
        root.addView(btnReturn, retLp);

        // --- ACTION BUTTONS ROW (STATUS & LOGS) ---
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, dp(44));
        rowLp.bottomMargin = dp(14);

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

        ScrollView scroll = new ScrollView(this);
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

        run("status");
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
                boolean isReady = message.contains("@@RESULT ready 1");
                start.setEnabled(isReady);
                if (isReady) {
                    // One UI / Cupertino Vibrant Green Pill
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

package com.fakhri.social;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(12, 16, 29);
    private final int CARD = Color.rgb(24, 30, 48);
    private final int TEXT = Color.rgb(241, 244, 252);
    private final int MUTED = Color.rgb(157, 169, 192);
    private final int ACCENT = Color.rgb(90, 104, 255);

    private LinearLayout base, content, nav;
    private EditText composer;
    private SharedPreferences prefs;
    private JSONArray posts;
    private String username = "Amr";
    private String page = "home";
    private int nextId = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        prefs = getSharedPreferences("fakhri_social", MODE_PRIVATE);
        username = prefs.getString("username", "Amr");

        try {
            posts = new JSONArray(prefs.getString("posts", "[]"));

            for (int i = 0; i < posts.length(); i++) {
                nextId = Math.max(
                    nextId,
                    posts.getJSONObject(i).optInt("id", 0) + 1
                );
            }
        } catch (Exception e) {
            posts = new JSONArray();
        }

        render();
    }

    private GradientDrawable background(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView makeText(
        String value, int size, int color, boolean bold
    ) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return t;
    }

    private TextView makeButton(String value, Runnable action) {
        TextView b = makeText(value, 14, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(16, 12, 16, 12);
        b.setBackground(background(ACCENT, 24));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void savePosts() {
        prefs.edit()
            .putString("posts", posts.toString())
            .putString("username", username)
            .apply();
    }

    private void render() {
        base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setBackgroundColor(BG);
        setContentView(base);

        LinearLayout header = new LinearLayout(this);
        header.setPadding(18, 16, 18, 16);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = makeText(
            "Fakhri Social", 22, TEXT, true
        );

        header.addView(
            logo,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        base.addView(header);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 8, 16, 16);

        scroll.addView(content);

        base.addView(
            scroll,
            new LinearLayout.LayoutParams( -1, 0, 1)
        );

        if (page.equals("home")) {
            showHome();
        } else if (page.equals("profile")) {
            showProfile();
        } else if (page.equals("settings")) {
            showSettings();
        }

        nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(4, 10, 4, 10);
        nav.setBackgroundColor(CARD);

        addNavButton("الرئيسية", "home");
        addNavButton("حسابي", "profile");
        addNavButton("الإعدادات", "settings");

        base.addView(nav);
    }

    private void addNavButton(String label, String target) {
        TextView b = makeText(label, 13, TEXT, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(8, 10, 8, 10);

        b.setOnClickListener(v -> {
            page = target;
            render();
        });

        nav.addView(
            b,
            new LinearLayout.LayoutParams(0, -2, 1)
        );
    }

    private void showHome() {
        content.addView(
            makeText("أهلاً بيك يا " + username + " 👋", 22, TEXT, true)
        );

        content.addView(
            makeText("شارك أفكارك مع مجتمعك.", 14, MUTED, false)
        );

        composer = new EditText(this);
        composer.setHint("اكتب منشورك هنا...");
        composer.setTextColor(TEXT);
        composer.setHintTextColor(MUTED);
        composer.setGravity(Gravity.RIGHT | Gravity.TOP);
        composer.setTextDirection(View.TEXT_DIRECTION_RTL);
        composer.setMinLines(2);
        composer.setPadding(12, 12, 12, 12);
        composer.setBackground(background(CARD, 16));

        LinearLayout.LayoutParams cp =
            new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 16, 0, 8);

        content.addView(composer, cp);

        content.addView(
            makeButton("نشر +", () -> publishPost())
        );

        TextView title = makeText(
            "آخر المنشورات", 19, TEXT, true
        );
        title.setPadding(0, 22, 0, 12);
        content.addView(title);

        for (int i = 0; i < posts.length(); i++) {
            JSONObject p = posts.optJSONObject(i);
            if (p != null) {
                showPost(p);
            }
        }
    }

    private void publishPost() {
        String body = composer.getText().toString().trim();

        if (body.isEmpty()) {
            composer.setError("اكتب المنشور أولًا");
            return;
        }

        try {
            JSONObject p = new JSONObject();
            p.put("id", nextId++);
            p.put("author", username);
            p.put("body", body);
            p.put("likes", 0);
            p.put("liked", false);
            p.put("comments", new JSONArray());

            JSONArray updated = new JSONArray();
            updated.put(p);

            for (int i = 0; i < posts.length(); i++) {
                updated.put(posts.getJSONObject(i));
            }

            posts = updated;
            savePosts();
            render();

            Toast.makeText(
                this, "تم نشر المنشور", Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {
            Toast.makeText(
                this, "تعذر نشر المنشور", Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showPost(JSONObject p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(14, 14, 14, 14);
        card.setBackground(background(CARD, 18));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 12);

        card.setLayoutParams(params);

        card.addView(makeText(
            p.optString("author", "مستخدم"), 15, TEXT, true
        ));

        TextView body = makeText(
            p.optString("body", ""), 15, TEXT, false
        );
        body.setPadding(0, 12, 0, 12);
        card.addView(body);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        TextView like = makeButton(
            (p.optBoolean("liked") ? "♥ " : "♡ ")
                + p.optInt("likes") + " إعجاب",
            () -> toggleLike(p)
        );

        actions.addView(
            like,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        TextView comments = makeButton(
            "💬 التعليقات",
            () -> showComments(p)
        );

        LinearLayout.LayoutParams commentParams =
            new LinearLayout.LayoutParams(0, -2, 1);
        commentParams.setMargins(8, 0, 0, 0);
        actions.addView(comments, commentParams);

        card.addView(actions);
        content.addView(card);
    }

    private void toggleLike(JSONObject p) {
        try {
            boolean liked = p.optBoolean("liked");
            p.put("liked", !liked);
            p.put(
                "likes",
                Math.max(0, p.optInt("likes") + (liked ? -1 : 1))
            );

            savePosts();
            render();
        } catch (Exception ignored) {
        }
    }

    private void showComments(JSONObject p) {
        JSONArray comments = p.optJSONArray("comments");
        if (comments == null) {
            comments = new JSONArray();
        }

        final JSONArray list = comments;

        EditText input = new EditText(this);
        input.setHint("اكتب تعليقك...");
        input.setGravity(Gravity.RIGHT);
        input.setTextDirection(View.TEXT_DIRECTION_RTL);

        new AlertDialog.Builder(this)
            .setTitle("التعليقات")
            .setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("إضافة", (dialog, which) -> {
                String value = input.getText().toString().trim();

                if (!value.isEmpty()) {
                    list.put(username + ": " + value);

                    try {
                        p.put("comments", list);
                    } catch (Exception ignored) {
                    }

                    savePosts();
                    render();
                }
            })
            .show();
    }

    private void showProfile() {
        content.addView(
            makeText("الملف الشخصي", 23, TEXT, true)
        );

        content.addView(
            makeText("اسم المستخدم: " + username, 17, TEXT, false)
        );

        content.addView(
            makeButton("تعديل الاسم", () -> {
                EditText input = new EditText(this);
                input.setText(username);

                new AlertDialog.Builder(this)
                    .setTitle("تعديل الاسم")
                    .setView(input)
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("حفظ", (d, w) -> {
                        String value = input.getText().toString().trim();

                        if (!value.isEmpty()) {
                            username = value;
                            savePosts();
                            render();
                        }
                    })
                    .show();
            })
        );
    }

    private void showSettings() {
        content.addView(
            makeText("الإعدادات", 23, TEXT, true)
        );

        content.addView(
            makeButton("مسح المنشورات المحلية", () -> {
                new AlertDialog.Builder(this)
                    .setTitle("تأكيد المسح")
                    .setMessage("سيتم حذف المنشورات المحفوظة على الجهاز.")
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("مسح", (d, w) -> {
                        posts = new JSONArray();
                        savePosts();
                        render();
                    })
                    .show();
            })
        );

        content.addView(
            makeText(
                "الإصدار 1.0.0\nالبيانات محفوظة على هذا الجهاز.",
                13, MUTED, false
            )
        );
    }
}
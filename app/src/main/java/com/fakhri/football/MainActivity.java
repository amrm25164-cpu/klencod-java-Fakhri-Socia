
package com.fakhri.social;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private LinearLayout root, postsLayout;
    private EditText postInput, searchInput;
    private TextView status;
    private JSONArray posts = new JSONArray();
    private android.content.SharedPreferences prefs;
    private String profileName = "مستخدم فخري";
    private String currentTab = "الرئيسية";
    private int background = Color.rgb(18, 22, 32);
    private int panel = Color.rgb(30, 36, 50);
    private int accent = Color.rgb(95, 120, 255);
    private int white = Color.WHITE;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        prefs = getSharedPreferences("fakhri_social", MODE_PRIVATE);
        profileName = prefs.getString("profile_name", "مستخدم فخري");

        try {
            posts = new JSONArray(prefs.getString("posts", "[]"));
        } catch (Exception e) {
            posts = new JSONArray();
        }

        buildUI();
        showHome();
    }

    private TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(white);
        t.setPadding(12, 10, 12, 10);
        return t;
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(white);
        b.setAllCaps(false);
        b.setBackground(shape(accent, 18));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void buildUI() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        TextView title = text("FAKHRI SOCIAL", 24);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(accent);
        root.addView(title);

        status = text("تواصل، شارك، واكتشف", 14);
        root.addView(status);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);

        String[] tabs = {"الرئيسية", "بحث", "حسابي", "الإعدادات"};
        for (String tab : tabs) {
            Button b = new Button(this);
            b.setText(tab);
            b.setTextSize(11);
            b.setAllCaps(false);
            b.setTextColor(white);
            b.setBackgroundColor(panel);
            b.setOnClickListener(v -> openTab(tab));

            nav.addView(b, new LinearLayout.LayoutParams(
                    0, dp(48), 1));
        }

        root.addView(nav);

        FrameLayout content = new FrameLayout(this);
        content.setId(View.generateViewId());

        ScrollView scroll = new ScrollView(this);
        postsLayout = new LinearLayout(this);
        postsLayout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(postsLayout);

        content.addView(scroll);
        root.addView(content, new LinearLayout.LayoutParams(
                -1, 0, 1));

        setContentView(root);
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void openTab(String tab) {
        currentTab = tab;

        if (tab.equals("الرئيسية")) {
            showHome();
        } else if (tab.equals("بحث")) {
            showSearch();
        } else if (tab.equals("حسابي")) {
            showProfile();
        } else {
            showSettings();
        }
    }

    private void clearContent(String subtitle) {
        postsLayout.removeAllViews();
        status.setText(subtitle);
    }

    private void showHome() {
        clearContent("آخر المنشورات");

        postInput = new EditText(this);
        postInput.setHint("بماذا تفكر؟");
        postInput.setTextColor(white);
        postInput.setHintTextColor(Color.LTGRAY);
        postInput.setMinLines(2);
        postInput.setGravity(Gravity.TOP);
        postInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        postInput.setBackground(shape(panel, 18));

        postsLayout.addView(postInput, matchParams());

        Button publish = button("نشر منشور", this::publishPost);
        postsLayout.addView(publish, matchParams());

        addSpace();

        for (int i = posts.length() - 1; i >= 0; i--) {
            try {
                JSONObject post = posts.getJSONObject(i);
                addPostCard(post);
            } catch (Exception ignored) {}
        }

        if (posts.length() == 0) {
            postsLayout.addView(text(
                    "لا توجد منشورات حتى الآن. اكتب أول منشور!",
                    16));
        }
    }

    private void publishPost() {
        String body = postInput.getText().toString().trim();

        if (body.isEmpty()) {
            Toast.makeText(this, "اكتب شيئًا قبل النشر",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject post = new JSONObject();
            post.put("id", System.currentTimeMillis());
            post.put("author", profileName);
            post.put("body", body);
            post.put("likes", 0);
            post.put("liked", false);
            post.put("comments", new JSONArray());

            posts.put(post);
            savePosts();

            ((InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    postInput.getWindowToken(), 0);

            showHome();

        } catch (Exception e) {
            Toast.makeText(this, "تعذّر نشر المنشور",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void addPostCard(JSONObject post) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        card.setBackground(shape(panel, 18));

        LinearLayout.LayoutParams cardParams = matchParams();
        cardParams.setMargins(0, dp(5), 0, dp(8));

        TextView author = text(
                "👤 " + post.optString("author", "مستخدم"), 15);
        author.setTypeface(null, Typeface.BOLD);
        card.addView(author);

        TextView body = text(post.optString("body"), 16);
        card.addView(body);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button like = new Button(this);
        boolean liked = post.optBoolean("liked", false);
        like.setText((liked ? "❤️" : "🤍") + " "
                + post.optInt("likes", 0));
        like.setAllCaps(false);
        like.setTextColor(white);
        like.setBackgroundColor(panel);

        like.setOnClickListener(v -> {
            boolean wasLiked = post.optBoolean("liked", false);
            postLikes(post, wasLiked);
            savePosts();
            showHome();
        });

        Button comment = new Button(this);
        comment.setText("💬 تعليق");
        comment.setAllCaps(false);
        comment.setTextColor(white);
        comment.setBackgroundColor(panel);
        comment.setOnClickListener(v -> showComments(post));

        Button share = new Button(this);
        share.setText("مشاركة");
        share.setAllCaps(false);
        share.setTextColor(white);
        share.setBackgroundColor(panel);
        share.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,
                    post.optString("author") + "\n"
                    + post.optString("body"));
            startActivity(Intent.createChooser(intent, "مشاركة المنشور"));
        });

        actions.addView(like, new LinearLayout.LayoutParams(0, dp(48), 1));
        actions.addView(comment, new LinearLayout.LayoutParams(0, dp(48), 1));
        actions.addView(share, new LinearLayout.LayoutParams(0, dp(48), 1));
        card.addView(actions);

        if (profileName.equals(post.optString("author"))) {
            Button delete = button("حذف المنشور", () -> {
                removePost(post.optLong("id"));
                savePosts();
                showHome();
            });
            card.addView(delete);
        }

        postsLayout.addView(card, cardParams);
    }

    private void postLikes(JSONObject post, boolean wasLiked) {
        int count = post.optInt("likes", 0);
        postRemoveLike(post, wasLiked, count);
    }

    private void postRemoveLike(JSONObject post, boolean wasLiked, int count) {
        try {
            post.put("liked", !wasLiked);
            post.put("likes", Math.max(0, count + (wasLiked ? -1 : 1)));
        } catch (Exception ignored) {}
    }

    private void showComments(JSONObject post) {
        EditText input = new EditText(this);
        input.setHint("اكتب تعليقك");

        JSONArray comments = post.optJSONArray("comments");
        if (comments == null) comments = new JSONArray();

        StringBuilder history = new StringBuilder();
        for (int i = 0; i < comments.length(); i++) {
            history.append("• ").append(comments.optString(i)).append("\n");
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(new TextView(this) {{
            setText(history.length() == 0 ? "لا توجد تعليقات" : history.toString());
            setTextSize(16);
            setPadding(16, 16, 16, 16);
        }});
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("التعليقات")
                .setView(box)
                .setNegativeButton("إغلاق", null)
                .setPositiveButton("إضافة", (d, w) -> {
                    String value = input.getText().toString().trim();
                    if (!value.isEmpty()) {
                        JSONArray list = post.optJSONArray("comments");
                        if (list == null) list = new JSONArray();
                        list.put(profileName + ": " + value);
                        try {
                            post.put("comments", list);
                        } catch (Exception ignored) {}
                        savePosts();
                        showHome();
                    }
                })
                .show();
    }

    private void showSearch() {
        clearContent("ابحث في المنشورات");

        searchInput = new EditText(this);
        searchInput.setHint("اكتب كلمة للبحث");
        searchInput.setTextColor(white);
        searchInput.setHintTextColor(Color.LTGRAY);
        searchInput.setSingleLine(true);
        searchInput.setBackground(shape(panel, 16));

        postsLayout.addView(searchInput, matchParams());

        postsLayout.addView(button("بحث", () -> {
            String query = searchInput.getText().toString().trim()
                    .toLowerCase();

            postsLayout.removeAllViews();
            postsLayout.addView(searchInput, matchParams());

            for (int i = posts.length() - 1; i >= 0; i--) {
                JSONObject post = posts.optJSONObject(i);
                if (post == null) continue;

                String body = post.optString("body", "");
                String author = post.optString("author", "");

                if (body.toLowerCase().contains(query)
                        || author.toLowerCase().contains(query)) {
                    addPostCard(post);
                }
            }
        }), matchParams());
    }

    private void showProfile() {
        clearContent("حسابك الشخصي");

        postsLayout.addView(text("👤 " + profileName, 24));

        EditText name = new EditText(this);
        name.setHint("اسمك");
        name.setText(profileName);
        name.setTextColor(white);
        name.setBackground(shape(panel, 16));
        postsLayout.addView(name, matchParams());

        postsLayout.addView(button("حفظ الاسم", () -> {
            String newName = name.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "اكتب اسمًا صحيحًا",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String oldName = profileName;
            profileName = newName;
            prefs.edit().putString("profile_name", profileName).apply();

            for (int i = 0; i < posts.length(); i++) {
                JSONObject p = posts.optJSONObject(i);
                if (p != null && p.optString("author").equals(oldName)) {
                    try {
                        p.put("author", newName);
                    } catch (Exception ignored) {}
                }
            }

            savePosts();
            showProfile();
        }), matchParams());

        postsLayout.addView(text("عدد المنشورات: " + posts.length(), 16));
    }

    private void showSettings() {
        clearContent("إعدادات التطبيق");

        postsLayout.addView(text("Fakhri Social", 22));
        postsLayout.addView(text(
                "نسخة تجريبية تحفظ المنشورات على هذا الجهاز.", 15));

        postsLayout.addView(button("حذف جميع المنشورات", () ->
                new AlertDialog.Builder(this)
                        .setTitle("تأكيد الحذف")
                        .setMessage("سيتم حذف جميع المنشورات من هذا الجهاز.")
                        .setNegativeButton("إلغاء", null)
                        .setPositiveButton("حذف", (d, w) -> {
                            posts = new JSONArray();
                            savePosts();
                            showHome();
                        })
                        .show()), matchParams());
    }

    private void removePost(long id) {
        JSONArray result = new JSONArray();

        for (int i = 0; i < posts.length(); i++) {
            JSONObject p = posts.optJSONObject(i);
            if (p != null && p.optLong("id") != id) {
                result.put(p);
            }
        }

        posts = result;
    }

    private void savePosts() {
        prefs.edit().putString("posts", posts.toString()).apply();
    }

    private LinearLayout.LayoutParams matchParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void addSpace() {
        View space = new View(this);
        postsLayout.addView(space,
                new LinearLayout.LayoutParams(1, dp(8)));
    }
}

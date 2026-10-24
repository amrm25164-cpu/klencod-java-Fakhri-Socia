
package com.fakhri.social;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout feed;
    private EditText postInput;
    private TextView status;

    private final ArrayList<Post> posts = new ArrayList<>();
    private int nextId = 1;

    private static final int BG = Color.rgb(15, 23, 42);
    private static final int CARD = Color.rgb(30, 41, 59);
    private static final int GREEN = Color.rgb(74, 222, 128);

    private static class Post {
        int id;
        String text;
        int likes;
        boolean liked;

        Post(int id, String text) {
            this.id = id;
            this.text = text;
            this.likes = 0;
            this.liked = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 12, 16, 8);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("Fakhri Social");
        title.setTextSize(27);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(GREEN);
        title.setGravity(Gravity.CENTER);
        title.setPadding(8, 12, 8, 16);
        root.addView(title);

        postInput = new EditText(this);
        postInput.setHint("بماذا تفكر؟");
        postInput.setTextColor(Color.WHITE);
        postInput.setHintTextColor(Color.LTGRAY);
        postInput.setTextSize(16);
        postInput.setMinLines(2);
        postInput.setGravity(Gravity.TOP | Gravity.RIGHT);
        root.addView(postInput);

        Button publish = new Button(this);
        publish.setText("نشر المنشور");
        root.addView(publish);

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setPadding(4, 8, 4, 8);
        root.addView(status);

        ScrollView scroll = new ScrollView(this);
        feed = new LinearLayout(this);
        feed.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(feed);

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        addNavButton(nav, "الرئيسية");
        addNavButton(nav, "حسابي");
        addNavButton(nav, "حول التطبيق");
        root.addView(nav);

        setContentView(root);

        publish.setOnClickListener(v -> publishPost());

        // بيانات تجريبية محلية لتوضيح شكل المنشورات
        posts.add(new Post(nextId++, "أهلًا بكم في Fakhri Social!"));
        posts.add(new Post(nextId++, "مساحة جديدة للتواصل ومشاركة الأفكار."));
        renderFeed();
    }

    private void publishPost() {
        String text = postInput.getText().toString().trim();

        if (text.isEmpty()) {
            postInput.setError("اكتب شيئًا قبل النشر");
            return;
        }

        if (text.length() > 2000) {
            postInput.setError("الحد الأقصى 2000 حرف");
            return;
        }

        posts.add(0, new Post(nextId++, text));
        postInput.setText("");
        renderFeed();
        status.setText("تمت إضافة المنشور محليًا");
    }

    private void renderFeed() {
        feed.removeAllViews();

        status.setText("عدد المنشورات: " + posts.size());

        for (Post post : new ArrayList<>(posts)) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(14, 12, 14, 12);
            card.setBackgroundColor(CARD);

            LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(-1, -2);
            cp.setMargins(0, 6, 0, 10);
            feed.addView(card, cp);

            TextView author = new TextView(this);
            author.setText("👤 مستخدم Fakhri");
            author.setTextColor(GREEN);
            author.setTextSize(14);
            author.setTypeface(null, Typeface.BOLD);
            card.addView(author);

            TextView content = new TextView(this);
            content.setText(post.text);
            content.setTextColor(Color.WHITE);
            content.setTextSize(17);
            content.setPadding(0, 12, 0, 12);
            card.addView(content);

            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.CENTER_VERTICAL);

            Button like = new Button(this);
            like.setText((post.liked ? "❤️" : "🤍")
                    + " " + post.likes);
            actions.addView(like);

            like.setOnClickListener(v -> {
                post.liked = !post.liked;
                post.likes += post.liked ? 1 : -1;
                renderFeed();
            });

            Button delete = new Button(this);
            delete.setText("حذف");
            actions.addView(delete);

            delete.setOnClickListener(v -> {
                posts.remove(post);
                renderFeed();
            });

            card.addView(actions);
        }
    }

    private void addNavButton(LinearLayout nav, String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(11);
        nav.addView(button, new LinearLayout.LayoutParams(
                0, -2, 1
        ));

        button.setOnClickListener(v -> {
            if (label.equals("الرئيسية")) {
                renderFeed();
            } else if (label.equals("حسابي")) {
                new AlertDialog.Builder(this)
                    .setTitle("حسابي")
                    .setMessage(
                        "ملف تجريبي محلي. تسجيل الحسابات " +
                        "سيُضاف عند ربط الخادم."
                    )
                    .setPositiveButton("حسنًا", null)
                    .show();
            } else {
                new AlertDialog.Builder(this)
                    .setTitle("Fakhri Social")
                    .setMessage("الإصدار الأولي من تطبيق التواصل الاجتماعي.")
                    .setPositiveButton("إغلاق", null)
                    .show();
            }
        });
    }
}

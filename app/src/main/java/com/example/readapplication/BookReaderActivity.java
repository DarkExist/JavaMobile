package com.example.readapplication;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.readapplication.Databases.AppDatabase;
import com.example.readapplication.data.Book;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class BookReaderActivity extends AppCompatActivity {
    private WebView webView;
    private ImageButton bookmarkImage;
    private ImageButton backArrowImage;
    private AppDatabase db;
    private Book currentBook;

    private boolean isBookmarked;
    private TextView bookTitle, bookAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reader);

        webView = findViewById(R.id.web_view);
        bookmarkImage = findViewById(R.id.bookmarkImage);
        backArrowImage = findViewById(R.id.backArrowImage);
        bookTitle = findViewById(R.id.bookTitle);
        bookAuthor = findViewById(R.id.bookAuthor);
        db = AppDatabase.create(this);

        String fb2Path = getIntent().getStringExtra("FB2_PATH");
        String author = getIntent().getStringExtra("BOOK_AUTHOR");
        String bookName = getIntent().getStringExtra("BOOK_TITLE");
        bookAuthor.setText(author);
        bookTitle.setText(bookName);
        new Thread(() -> {
            currentBook = db.bookDao().getBookByPath(fb2Path);
            runOnUiThread(() -> {
                updateBookmarkIcon();
            });
        }).start();

        bookmarkImage.setOnClickListener(v -> toggleBookmark());
        backArrowImage.setOnClickListener(v -> onBackPressed());

        new Thread(() -> {
            String htmlContent = parseFb2ToHtml(fb2Path);
            runOnUiThread(() -> {
//                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadDataWithBaseURL(
                        null,
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                );
            });
        }).start();


    }


    private void toggleBookmark() {
        new Thread(() -> {
            if (currentBook != null) {
                currentBook.setBookmarked(!currentBook.isBookmarked());
                db.bookDao().update(currentBook);
                runOnUiThread(() -> {
                    updateBookmarkIcon();
                    showBookmarkToast();
                });
            }
        }).start();
    }

    private void updateBookmarkIcon() {
        if (currentBook != null) {
            int iconRes = currentBook.isBookmarked() ?
                    R.drawable.bookmark_fill_76_100 : R.drawable.bookmark_empty_76_100;
            bookmarkImage.setImageResource(iconRes);
        }
    }

    private void showBookmarkToast() {
        String message = currentBook.isBookmarked() ?
                "Книга добавлена в закладки" : "Книга удалена из закладок";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String parseFb2ToHtml(String filePath) {
        try {
            File fb2File = new File(filePath);
            if (!fb2File.exists()) {
                Log.e("FB2Parser", "File not found: " + filePath);
                return errorHtml("Файл не найден");
            }

            Document doc = Jsoup.parse(fb2File, "UTF-8", "", Parser.xmlParser());
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            StringBuilder html = new StringBuilder();

            html.append("<html><head>")
                    .append("<meta charset=\"UTF-8\">")
                    .append("<style>")
                    .append("body { padding: 20px; line-height: 1.6; font-size: 18px; }")
                    .append("h1, h2, h3 { color: #333; }")
                    .append("p { margin: 1em 0; }")
                    .append(".verse-line { margin-bottom: 0.5em; display: block; }")
                    .append("h1, h2, h3 { color: #333; }")
                    .append("p { margin: 1em 0; }")
                    .append("</style></head><body>");

            Element body = doc.select("body").first();
            if (body != null) {
                processBodyContent(body, html, doc);
            } else {
                html.append("<p>Содержимое книги не найдено</p>");
            }

            html.append("</body></html>");
            return html.toString();
        } catch (IOException e) {
            Log.e("FB2Parser", "Error reading file", e);
            return errorHtml("Ошибка чтения файла");
        }
    }

    private void processBodyContent(Element parent, StringBuilder html, Document doc) {
        for (Element element : parent.children()) {
            String tagName = element.tagName();
            Log.d("FB2_TAG", "Processing: " + tagName);

            switch (tagName) {
                case "section":
                    html.append("<div class='section'>");
                    processBodyContent(element, html, doc);
                    html.append("</div>");
                    break;

                case "title":
                    html.append("<h2>").append(element.text()).append("</h2>");
                    break;

                case "p":
                    html.append("<p>").append(processText(element)).append("</p>");
                    break;

                case "v":
                    html.append("<div class='verse'>").append(processText(element)).append("</div>");
                    break;

                case "stanza":
                    html.append("<div class='stanza'>");
                    processBodyContent(element, html, doc);
                    html.append("</div>");
                    break;

                case "empty-line":
                    html.append("<br/><br/>");
                    break;

                case "image":
                    processImage(element, html, doc);
                    break;

                default:
                    processBodyContent(element, html, doc);
            }
        }
    }

    private String processText(Element element) {
        return element.html()
                .replaceAll("\n", "<br/>")
                .replaceAll("<emphasis>", "<em>")
                .replaceAll("</emphasis>", "</em>");
    }

    private void processImage(Element element, StringBuilder html, Document doc) {
        String href = element.attr("xlink:href");
        if (href.startsWith("#")) {
            Element binary = doc.select("binary[id=" + href.substring(1) + "]").first();
            if (binary != null) {
                String base64 = binary.text().trim();
                html.append("<img src='data:image/jpeg;base64,")
                        .append(base64)
                        .append("' style='max-width: 100%;'/>");
            }
        }
    }

    private String errorHtml(String message) {
        return "<html><body>" + message + "</body></html>";
    }
}
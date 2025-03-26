package com.example.readapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readapplication.Databases.AppDatabase;
import com.example.readapplication.adapters.BookAdapter;
import com.example.readapplication.data.Book;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity {
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private AppDatabase db;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText searchTextField;
    private ImageButton searchButton;
    private String currentSearchQuery = "";
    List<Book> books = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        searchTextField = findViewById(R.id.searchTextField);
        searchButton = findViewById(R.id.searchButton);

        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = AppDatabase.create(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.sideMenuButton);

        menuButton.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );



        setupSearch();
        loadBookmarkedBooks();
        setupNavigationDrawer();
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchTextField.getWindowToken(), 0);
            performSearch();
        });

        searchTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        currentSearchQuery = searchTextField.getText().toString().trim();
        loadBookmarkedBooks();
    }

    private void loadBookmarkedBooks() {
        new Thread(() -> {
            List<Book> bookmarkedBooks;
            if (currentSearchQuery.isEmpty()) {
                bookmarkedBooks = db.bookDao().getBookmarkedBooks();
            } else {
                bookmarkedBooks = db.bookDao().searchBookmarkedBooks(currentSearchQuery);
            }

            runOnUiThread(() -> {
                setupRecyclerView(bookmarkedBooks);
            });
        }).start();
    }

    private void setupRecyclerView(List<Book> bookmarkedBooks) {
        FrameLayout emptyState = findViewById(R.id.booksEmptyField);

        if(bookmarkedBooks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            booksRecyclerView.setVisibility(View.GONE);

            TextView emptyText = findViewById(R.id.booksEmptyText);
            if (!currentSearchQuery.isEmpty()) {
                emptyText.setText("Ничего не найдено по запросу: " + currentSearchQuery);
            } else {
                emptyText.setText("Нет книг в закладках :(");
            }
        }
        else {
            bookAdapter = new BookAdapter(bookmarkedBooks, this::onBookClick);
            booksRecyclerView.setAdapter(bookAdapter);
            emptyState.setVisibility(View.GONE);
            booksRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void onBookClick(Book book) {
        Intent intent = new Intent(this, BookReaderActivity.class);
        intent.putExtra("FB2_PATH", book.getFb2Path());
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
        startActivity(intent);
    }

    public void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_back) {
                // Закрытие меню
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if (id == R.id.nav_home) {
                Intent intent = new Intent(BookmarksActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_library) {
                Intent intent = new Intent(BookmarksActivity.this, BookmarksActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}

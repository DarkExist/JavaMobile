package com.example.readapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.readapplication.Contracts.QuoteResponse;
import com.example.readapplication.Databases.AppDatabase;
import com.example.readapplication.adapters.BookAdapter;
import com.example.readapplication.data.Book;
import com.example.readapplication.services.QuoteApiService;
import com.example.readapplication.ui.ExpandableTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements BookAdapter.OnBookClickListener{
    private RecyclerView booksRecyclerView;
    private FrameLayout mapContainer;
    private BookAdapter bookAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppDatabase db;
    private static final int PICK_FB2_REQUEST = 101, PICK_IMAGE_REQUEST = 102;
    private NestedScrollView nestedScrollView;
    private Uri selectedFb2Uri, selectedImageUri;
    private MapView mapView;
    private AlertDialog currentDialog;
    private EditText searchTextField;
    private ImageButton searchButton;
    private String currentSearchQuery = "";
    private ExpandableTextView expandableQuote;

    List<Book> books = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mapContainer = findViewById(R.id.mapContainer);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        ImageButton menuButton = findViewById(R.id.sideMenuButton);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        searchTextField = findViewById(R.id.searchTextField);
        searchButton = findViewById(R.id.searchButton);
        expandableQuote = findViewById(R.id.expandableQuote);



        menuButton.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        fabAdd.setOnClickListener(v -> showAddBookDialog());

        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        booksRecyclerView.setHasFixedSize(true);
        booksRecyclerView.setNestedScrollingEnabled(false);

        db = AppDatabase.create(this);

        if(savedInstanceState == null) {
            MapKitFactory.getInstance().onStart();
            mapView.onStart();
        }

        setupSearch();
        loadBooks();

        loadQuoteFromApi();
        setupNavigationDrawer();
        mapView.getMap().move(new CameraPosition(
                        new Point(56.0153, 92.8932),
                        10.0f,
                        0.0f,
                        0.0f
                )
        );

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
        loadBooks();
    }

    public void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_back) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if (id == R.id.nav_home) {
            } else if (id == R.id.nav_library) {
                Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    public void onBookClick(Book book) {
        Intent intent = new Intent(this, BookReaderActivity.class);
        intent.putExtra("FB2_PATH", book.getFb2Path());
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void loadBooks() {
        new Thread(() -> {
            List<Book> books;
            if (currentSearchQuery.isEmpty()) {
                books = db.bookDao().getAllBooks();
            } else {
                books = db.bookDao().searchBooks(currentSearchQuery);
            }
            runOnUiThread(() -> setupRecyclerView(books));
        }).start();
    }

    private void setupRecyclerView(List<Book> books) {
        this.books = books;
        FrameLayout emptyState = findViewById(R.id.booksEmptyField);

        if(books.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            booksRecyclerView.setVisibility(View.GONE);

            TextView emptyText = findViewById(R.id.booksEmptyText);
            if (!currentSearchQuery.isEmpty()) {
                emptyText.setText("Ничего не найдено по запросу: " + currentSearchQuery);
            } else {
                emptyText.setText("Нет книг в библиотеке :(");
            }
        }
        else {
            bookAdapter = new BookAdapter(books, this);
            booksRecyclerView.setAdapter(bookAdapter);
            setupItemTouchHelper();
            emptyState.setVisibility(View.GONE);
            booksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Book bookToDelete = books.get(position);
                showDeleteConfirmationDialog(bookToDelete, position);
            }
        }).attachToRecyclerView(booksRecyclerView);
    }

    private void deleteBook(Book book, int position) {
        new Thread(() -> {
            db.bookDao().delete(book);
            runOnUiThread(() -> {
                bookAdapter.notifyItemRemoved(position);
                loadBooks();
            });
        }).start();
    }

    private void showDeleteConfirmationDialog(Book book, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление книги")
                .setMessage("Вы уверены, что хотите удалить книгу " + book.getTitle() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteBook(book, position))
                .setNegativeButton("Отмена", (dialog, which) -> {
                    bookAdapter.notifyItemChanged(position);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void showAddBookDialog() {
        resetFileUris();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить книгу");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_book, null);
        Button btnSelectFb2 = view.findViewById(R.id.btn_select_fb2);
        Button btnSelectImage = view.findViewById(R.id.btn_select_image);
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etAuthor = view.findViewById(R.id.et_author);
        TextView fb2Selected = view.findViewById(R.id.fb2_selected);
        TextView imageSelected = view.findViewById(R.id.image_selected);
        fb2Selected.setVisibility(View.INVISIBLE);
        imageSelected.setVisibility(View.INVISIBLE);

        builder.setView(view)
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();
        currentDialog = dialog;

        dialog.setOnDismissListener(d -> currentDialog = null);

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);

            positiveButton.setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String author = etAuthor.getText().toString().trim();
                if (!validateInput(title)) return;
                saveNewBook(title, author);
                dialog.dismiss();
            });

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateAddButtonState();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            etTitle.addTextChangedListener(textWatcher);
        });

        btnSelectFb2.setOnClickListener(v -> openFilePicker(PICK_FB2_REQUEST, "application/fb2"));
        btnSelectImage.setOnClickListener(v -> openFilePicker(PICK_IMAGE_REQUEST, "image/*"));

        dialog.show();
    }

    private void updateAddButtonState() {
        if (currentDialog != null && currentDialog.isShowing()) {
            EditText etTitle = currentDialog.findViewById(R.id.et_title);
            Button positiveButton = currentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (etTitle != null && positiveButton != null) {
                String title = etTitle.getText().toString().trim();
                boolean isValid = !TextUtils.isEmpty(title) && selectedFb2Uri != null;
                positiveButton.setEnabled(isValid);
            }
        }
    }


    private void loadQuoteFromApi() {
        QuoteApiService apiService = ApiClient.getApiService();
        apiService.getRandomQuote(
                "getQuote",
                "json",
                "ru"
                ).enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuoteResponse quote = response.body();
                    String formattedText = formatQuote(quote);
                    expandableQuote.setText(formattedText);
                } else {
                    showDefaultQuote();
                }
            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                showDefaultQuote();
            }
        });
    }

    private String formatQuote(QuoteResponse quote) {
        return String.format("«%s»\n\n— %s", quote.getQuoteText(), quote.getQuoteAuthor());
    }

    private void showDefaultQuote() {
        String defaultQuote = "«Книги — корабли мысли, странствующие по волнам времени»\n\n— Фрэнсис Бэкон";
        expandableQuote.setText(defaultQuote);
    }
    private boolean validateInput(String title) {
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Введите название книги", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedFb2Uri == null) {
            Toast.makeText(this, "Выберите FB2 файл", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openFilePicker(int requestCode, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_FB2_REQUEST) {
                selectedFb2Uri = uri;
                if (currentDialog != null && currentDialog.isShowing()) {
                    TextView fb2TV = currentDialog.findViewById(R.id.fb2_selected);
                    if (fb2TV != null) {
                        String fileName = getFileName(selectedFb2Uri);
                        fb2TV.setText("Выбранный файл: " + fileName);
                        fb2TV.setVisibility(View.VISIBLE);
                    }
                }
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = uri;
                if (currentDialog != null && currentDialog.isShowing()) {
                    TextView imageTV = currentDialog.findViewById(R.id.image_selected);
                    if (imageTV != null) {
                        String fileName = getFileName(selectedImageUri);
                        imageTV.setText("Выбранная обложка: " + fileName);
                        imageTV.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        updateAddButtonState();
    }

    private void saveNewBook(String title, String author) {
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Введите название книги", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFb2Uri == null) {
            Toast.makeText(this, "Выберите FB2 файл", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                String fileName = getFileName(selectedFb2Uri);
                Book existingBook = db.bookDao().getBookByFileName(fileName);


                if (existingBook != null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Эта книга уже добавлена", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                String fb2Path = copyFileToStorage(selectedFb2Uri, "books");
                String imagePath = selectedImageUri != null ?
                        copyFileToStorage(selectedImageUri, "covers") : "";

                Book newBook = new Book(
                        title.trim(),
                        !TextUtils.isEmpty(author) ? author.trim() : "Неизвестный автор",
                        imagePath,
                        fb2Path,
                        fileName,
                        false);

                db.bookDao().insert(newBook);

                runOnUiThread(() -> {
                    loadBooks();
                    resetFileUris();
                    Toast.makeText(this, "Книга добавлена", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Ошибка при добавлении книги", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void resetFileUris() {
        selectedFb2Uri = null;
        selectedImageUri = null;
    }

    private String copyFileToStorage(Uri uri, String subDir) throws IOException {
        if (uri == null) return "";

        InputStream in = getContentResolver().openInputStream(uri);
        File outputDir = new File(getFilesDir(), subDir);
        if (!outputDir.exists()) outputDir.mkdirs();

        String fileName = getFileName(uri);
        File outputFile = new File(outputDir, fileName);

        try (OutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return outputFile.getAbsolutePath();
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        return name != null ? name : "file_" + System.currentTimeMillis();
    }

}
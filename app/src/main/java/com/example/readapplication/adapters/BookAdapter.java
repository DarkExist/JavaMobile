package com.example.readapplication.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.readapplication.R;
import com.example.readapplication.data.Book;

import java.io.File;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOOK = 0;
    private static final int TYPE_BUTTON = 1;

    private final List<Book> books;
    private int currentVisibleCount = 5;
    private OnBookClickListener listener;


    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < currentVisibleCount && position < books.size()) {
            return TYPE_BOOK;
        } else {
            return TYPE_BUTTON;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_BOOK) {
            View view = inflater.inflate(R.layout.item_book, parent, false);
            return new BookViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_load_more_button, parent, false);
            return new ButtonViewHolder(view);
        }
    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BookViewHolder) {
            Book book = books.get(position);
            BookViewHolder bookHolder = (BookViewHolder) holder;

            bookHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });

            if (!TextUtils.isEmpty(book.getImagePath())) {
                File coverFile = new File(book.getImagePath());
                if (coverFile.exists()) {
                    Glide.with(bookHolder.itemView.getContext())
                            .load(coverFile)
                            .into(bookHolder.ivCover);
                } else {
                    bookHolder.ivCover.setImageResource(R.drawable.default_icon);
                }
            } else {
                bookHolder.ivCover.setImageResource(R.drawable.default_icon);
            }

            bookHolder.tvTitle.setText(book.getTitle());
            bookHolder.tvAuthor.setText(book.getAuthor());




        } else if (holder instanceof ButtonViewHolder) {
            ButtonViewHolder buttonHolder = (ButtonViewHolder) holder;
            buttonHolder.btnLoadMore.setOnClickListener(v -> {
                currentVisibleCount += 5;
                if (currentVisibleCount > books.size()) {
                    currentVisibleCount = books.size();
                }
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        if (books.size() > currentVisibleCount) {
            return currentVisibleCount + 1;
        } else {
            return books.size();
        }
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAuthor;
        ImageView ivCover;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            ivCover = itemView.findViewById(R.id.imageBook);
        }
    }

    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        Button btnLoadMore;

        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            btnLoadMore = itemView.findViewById(R.id.btnLoadMore);
        }
    }
}
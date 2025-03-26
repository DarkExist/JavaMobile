package com.example.readapplication.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;


import androidx.appcompat.widget.LinearLayoutCompat;

import com.example.readapplication.R;

import java.text.AttributedCharacterIterator;

public class ExpandableTextView extends LinearLayoutCompat {
    private TextView textView;
    private ImageView toggleButton;
    private boolean isExpanded;

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.item_expandable_text, this);
        textView = findViewById(R.id.QuoteTextView);
        toggleButton = findViewById(R.id.QuoteToggleButton);

        //wait till full load
        textView.post(() -> {
            if (textView.getLineCount() > 3) {
                toggleButton.setVisibility(View.VISIBLE);
            }
        });

        toggleButton.setOnClickListener(v -> toggleText());
    }

    private void toggleText() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            textView.setMaxLines(Integer.MAX_VALUE);
            toggleButton.setImageResource(R.drawable.arrow_up_82_26);
        } else {
            textView.setMaxLines(3);
            toggleButton.setImageResource(R.drawable.arrow_down_82_26);
        }
    }

    public void setText(String text) {
        textView.setText(text);
    }
}

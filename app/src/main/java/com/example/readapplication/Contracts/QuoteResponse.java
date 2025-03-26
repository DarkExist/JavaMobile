package com.example.readapplication.Contracts;

public class QuoteResponse {
    private String quoteText;
    private String quoteAuthor;
    private String senderName;
    private String senderLink;
    private String quoteLink;

    public String getQuoteText() { return quoteText; }
    public String getQuoteAuthor() { return quoteAuthor; }
    public String getSenderName() { return senderName; }
    public String getSenderLink() { return senderLink; }
    public String getQuoteLink() { return quoteLink; }
}

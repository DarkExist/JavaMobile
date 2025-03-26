package com.example.readapplication.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Fb2Parser {

    public static String parseBookTitle(File fb2File) {
        try {
            byte[] data = java.nio.file.Files.readAllBytes(fb2File.toPath());
            String xmlContent = new String(data, detectCharset(data));

            Document doc = Jsoup.parse(xmlContent);


            Elements titles = doc.getElementsByTag("book-title");
            if (!titles.isEmpty()) {
                return titles.get(0).text();
            }

            return "Неизвестное название";
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка чтения файла";
        }
    }

    private static Charset detectCharset(byte[] data) {
        // Простая проверка на UTF-8
        if (new String(data, Charset.forName("UTF-8")).contains("<?xml")) {
            return Charset.forName("UTF-8");
        }
        return Charset.forName("Windows-1251"); // Для русских книг
    }
}
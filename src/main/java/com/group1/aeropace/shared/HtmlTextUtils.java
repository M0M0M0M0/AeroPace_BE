package com.group1.aeropace.shared;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.util.Set;

public class HtmlTextUtils {

    private HtmlTextUtils() {}

    public static String htmlToPlainTextWithPunctuation(String html) {
        if (html == null || html.isBlank()) return "";

        Document doc = Jsoup.parseBodyFragment(html);
        StringBuilder sb = new StringBuilder();
        Set<String> blockTags = Set.of(
                "p", "li", "div", "h1", "h2", "h3", "h4", "h5", "h6",
                "blockquote", "tr", "dt", "dd", "pre"
        );

        doc.body().traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode textNode) {
                    sb.append(textNode.text());
                } else if (node instanceof Element el && el.tagName().equals("br")) {
                    sb.append(" ");
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element el && blockTags.contains(el.tagName())) {
                    int i = sb.length() - 1;
                    while (i >= 0 && Character.isWhitespace(sb.charAt(i))) i--;
                    if (i >= 0) {
                        char last = sb.charAt(i);
                        if (last != '.' && last != '?' && last != '!') {
                            sb.append(". ");
                        } else {
                            sb.append(" ");
                        }
                    }
                }
            }
        });

        return sb.toString().replaceAll("\\s+", " ").trim();
    }
}

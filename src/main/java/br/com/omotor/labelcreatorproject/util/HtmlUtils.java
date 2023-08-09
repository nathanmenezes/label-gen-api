package br.com.omotor.labelcreatorproject.util;

import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    public static void main(String[] args) {
        String input = "<p [formControl]=\"ex\">\nGestor\n</p>";

        Document document = new Document("");
        document.append(input);


    }

    public static String htmlFormat(String html) {
        Pattern pattern = Pattern.compile("(<.*?>)(.*?)(</.*?>)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        StringBuilder stringBuffer = new StringBuilder();

        while (matcher.find()) {
            String openTag = matcher.group(1);
            String content = matcher.group(2);
            String closeTag = matcher.group(3);

            String newOpenTag = openTag + "\n" + content;
            String newCloseTag = "\n" + closeTag;

            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(newOpenTag + newCloseTag));
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    public static String reformatHtml(String newHtml, String oldHtml, Set<String> strings) {


        String[] newHtmlLines = htmlFormat(newHtml).split("\n");
        String[] oldHtmlLines = htmlFormat(oldHtml).split("\n");

        String resultHtml = "";



        for (int i = 0; i < newHtmlLines.length; i++) {
            if (newHtml.contains(oldHtmlLines[i].toLowerCase())) {
                resultHtml = newHtml.replaceAll(newHtmlLines[i], oldHtmlLines[i]);
            }
        }

//        StringBuilder stringBuilder = new StringBuilder();
//        for (String newHtmlLine : newHtmlLines) {
//            stringBuilder.append(newHtmlLine).append("\n");
//        }

        return resultHtml;
    }

    public static String removeHtmlTags(String input) {
        String htmlTagRemoved = input.replaceAll("<html[^>]*>", "");
        String headTagRemoved = htmlTagRemoved.replaceAll("<head[^>]*>.*?</head>", "");
        String bodyTagRemoved = headTagRemoved.replaceAll("<body[^>]*>", "");
        return bodyTagRemoved.replaceAll("</html>|</body>", "").replaceAll("\n", "");
    }
}

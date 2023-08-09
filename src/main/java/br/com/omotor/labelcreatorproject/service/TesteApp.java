package br.com.omotor.labelcreatorproject.service;

import br.com.omotor.labelcreatorproject.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TesteApp {


    public static void main(String[] args) {
        String input = "<p [formControl]=\"ex\">\nGestor\n</p>"+
                "\n<p [formControl]=\"ex\">\nGestor\n</p>";

        Document document = new Document("");
        document.append(input);

        String[] linhasInput = input.split("\n");


        String output = document.toString();

        System.out.println(Arrays.toString(linhasInput));
        System.out.println(StringUtil.htmlFormat(output));

    }
}

package br.com.omotor.labelcreatorproject.service;

import br.com.omotor.labelcreatorproject.util.HtmlUtils;
import org.jsoup.nodes.Document;

import java.util.Arrays;

public class TesteApp {


    public static void main(String[] args) {
        String input = "<p [formControl]=\"ex\">\nGestor\n</p>"+
                "\n<p [formControl]=\"ex\">\nGestor\n</p>";

        Document document = new Document("");
        document.append(input);

        String[] linhasInput = input.split("\n");


        String output = document.toString();

        System.out.println(Arrays.toString(linhasInput));
        System.out.println(HtmlUtils.htmlFormat(output));

    }
}

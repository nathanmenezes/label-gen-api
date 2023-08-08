package br.com.omotor.labelcreatorproject.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.Scanner;

public class TesteApp {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String html = "<div><p ngFor=\"item in items\">Original Text</p></div>";
        Document doc = Jsoup.parse(html);

        // Selecione o elemento onde deseja substituir o texto
        Element element = doc.select("p").first();

        // Substitua apenas o conteúdo de texto
        String novoTexto = "Novo Texto";
        element.text(novoTexto);

        System.out.println(doc);
    }
}

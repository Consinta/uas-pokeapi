package com.swingjava;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PokeAPI {

    private static final String POKEAPI_URL = "https://pokeapi.co/api/v2/pokemon";
    private static final int LIMIT = 10; // Jumlah maksimal hasil per halaman

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pokémon Data");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // Ambil data Pokémon
            List<JsonObject> pokemons = fetchAllPokemonData();

            // Buat panel untuk menampilkan kartu Pokémon
            JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
            for (JsonObject pokemon : pokemons) {
                JPanel cardPanel = new JPanel();
                cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
                cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cardPanel.setPreferredSize(new Dimension(100, 50)); // Ukuran card

                String name = pokemon.get("name").getAsString();
                String url = pokemon.get("url").getAsString();

                JLabel nameLabel = new JLabel("Name: " + name);
                JLabel urlLabel = new JLabel("URL: " + url);

                cardPanel.add(nameLabel);
                cardPanel.add(urlLabel);

                cardPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showPokemonDetails(url);
                    }
                });
                panel.add(cardPanel);
            }
            JScrollPane scrollPane = new JScrollPane(panel);
            frame.add(scrollPane);

            frame.setSize(400, 600);
            frame.setVisible(true);
        });
    }

    public static List<JsonObject> fetchAllPokemonData() {
        List<JsonObject> pokemons = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();
            int offset = 0;

            // Buat URL dengan parameter limit dan offset
            String url = POKEAPI_URL + "?offset=" + offset + "&limit=" + LIMIT;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Deserialisasi JSON Response
            JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = jsonObject.getAsJsonArray("results");

            for (JsonElement result : results) {
                JsonObject pokemonSummary = result.getAsJsonObject();
                pokemons.add(pokemonSummary);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pokemons;
    }

    private static void showPokemonDetails(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new Gson();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Deserialisasi JSON Response
            JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            String name = jsonObject.get("name").getAsString();
            JsonArray abilitiesArray = jsonObject.getAsJsonArray("abilities");
            StringBuilder abilities = new StringBuilder();
            for (JsonElement abilityElement : abilitiesArray) {
                JsonObject abilityObject = abilityElement.getAsJsonObject().get("ability").getAsJsonObject();
                abilities.append(abilityObject.get("name").getAsString()).append(", ");
            }
            if (abilities.length() > 0) {
                abilities.setLength(abilities.length() - 2);
            }

            // Tampilkan detail menggunakan JOptionPane
            JOptionPane.showMessageDialog(null, "Name: " + name + "\nAbilities: " + abilities.toString(),
                    "Pokémon Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

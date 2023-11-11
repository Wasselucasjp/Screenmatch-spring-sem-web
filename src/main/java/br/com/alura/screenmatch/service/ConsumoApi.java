package br.com.alura.screenmatch.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsumoApi {

    // Este método é usado para obter dados de um determinado URL e retorná-los como uma String.
    public String obterDados(String endereco) {
        // Cria um novo HttpClient.
        HttpClient client = HttpClient.newHttpClient();

        // Constrói um novo HttpRequest.
        HttpRequest request = HttpRequest.newBuilder()
                // Define a URI da solicitação para o endereço fornecido.
                .uri(URI.create(endereco))
                .build();

        // Declara uma variável HttpResponse.
        HttpResponse<String> response = null;
        try {
            // Envia a solicitação e obtém a resposta.
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // Lança uma nova RuntimeException se ocorrer uma IOException.
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // Lança uma nova RuntimeException se ocorrer uma InterruptedException.
            throw new RuntimeException(e);
        }

        // Obtém o corpo da resposta como uma String.
        String json = response.body();

        // Retorna a String.
        return json;
    }

}


package br.com.alura.screenmatch.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// A classe ConverteDados implementa a interface IConverteDados.
public class ConverteDados implements IConverteDados {

    // Cria uma instância do ObjectMapper que será usada para converter JSON em objetos Java e vice-versa.
    private ObjectMapper mapper = new ObjectMapper();

    // Este método é usado para converter uma String JSON em um objeto Java do tipo especificado.
    @Override
    public <T> T ObterDados(String json, Class<T> classe) {
        try{
            // Tenta converter a String JSON em um objeto Java do tipo especificado.
            return mapper.readValue(json, classe);
        }catch (JsonProcessingException e){
            // Se ocorrer um erro durante o processamento do JSON, uma RuntimeException é lançada.
            throw new RuntimeException(e);
        }

    }
}

package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private  List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();
    private SerieRepository repository;
    private String categoriaOmdb;
    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas   
                    4 - Buscar série por titulo 
                    5 - Buscar séries por ator
                    6 - Buscar top 5 séries
                    7 - Buscar série por categoria 
                    8 -  Filtrar séries
                    0 - Sair                                 
                    """;

            System.out.println(menu);
             opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
       // dadosSeries.add(dados);
        Serie serie = new Serie(dados);
        repository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie =  leitura.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        }else {
            System.out.println("Série não encontrada! ");
        }
    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
        //dadosSeries.forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolhar uma série por nome: ");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieBuscada =  repository.findByTituloContainingIgnoreCase(nomeSerie);

        if ( serieBuscada.isPresent()){
            System.out.println("Dados da série: " + serieBuscada.get());
        }else {
            System.out.println("Série não encontrada !!!!");
        }
    }


    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome para busca? ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações apartir de que valor ? ");
        var avaliacao =  leitura.nextDouble();
        List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries em que " + nomeAtor + "trabalhou: " );
        seriesEncontradas.forEach(serie -> System.out.println(serie.getTitulo() + "avaliação " + serie.getAvaliacao()));
    }

    private void buscarTop5Series(){
        List<Serie> seriesTop5 =  repository.findTop5ByOrderByAvaliacaoDesc();
        seriesTop5.forEach(
                serie -> System.out.println(serie.getTitulo() + " avaliação " + serie.getAvaliacao())
        );
    }

    private void buscarSeriePorCategoria(){
        System.out.println("Deseja buscar série de que categoria/gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromSPortugues(nomeGenero);
        List<Serie> seriesPorCategoria =  repository.findByGenero(categoria);
        System.out.println("Séries da categoria " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void  filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação apartir de qual valor? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> filtroSeries = repository.seriesPorTemporadaEAvaliacao(totalTemporadas,avaliacao);
        System.out.println("*** Séries filtradas ***");
        filtroSeries.forEach(
                s -> System.out.println(s.getTitulo() + " - avaliação " + s.getAvaliacao())
        );
    }

}
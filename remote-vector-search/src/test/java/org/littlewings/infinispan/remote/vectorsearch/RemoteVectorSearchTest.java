package org.littlewings.infinispan.remote.vectorsearch;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.api.query.QueryResult;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.IndexStartupMode;
import org.infinispan.configuration.cache.IndexStorage;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteVectorSearchTest {
    EmbeddingClient embeddingClient;

    String createUri(String userName, String password) {
        return String.format(
                "hotrod://%s:%s@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222"
                        + "?context-initializers=org.littlewings.infinispan.remote.vectorsearch.EntitiesInitializerImpl",
                userName,
                password
        );
    }

    @BeforeEach
    void setUp() {
        embeddingClient = EmbeddingClient.create("localhost", 8000);

        String uri = createUri("ispn-admin", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            manager.getConfiguration().getContextInitializers().forEach(serializationContextInitializer -> {
                RemoteCache<String, String> protoCache = manager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
                protoCache.put("entities.proto", serializationContextInitializer.getProtoFile());
            });

            RemoteCacheManagerAdmin admin = manager.administration();

            // インデックスありのDistributed Cache
            org.infinispan.configuration.cache.Configuration indexedDistCacheConfiguration =
                    new org.infinispan.configuration.cache.ConfigurationBuilder()
                            .clustering()
                            .cacheMode(CacheMode.DIST_SYNC)
                            .encoding().key().mediaType("application/x-protostream")
                            .encoding().value().mediaType("application/x-protostream")
                            // indexing
                            .indexing()
                            .enable()
                            .addIndexedEntities("entity.Movie")
                            .storage(IndexStorage.FILESYSTEM)
                            .path("${infinispan.server.data.path}/index/movieCache")
                            .startupMode(IndexStartupMode.REINDEX)
                            .reader().refreshInterval(0L)  // default 0
                            .writer().commitInterval(1000)  // default null
                            .build();

            // キャッシュがない場合は作成、すでにある場合はデータを削除
            admin.getOrCreateCache("movieCache", indexedDistCacheConfiguration)
                    .clear();
        }
    }

    @AfterEach
    void tearDown() {
        embeddingClient.close();
    }

    <K, V> void withCache(String cacheName, Consumer<RemoteCache<K, V>> func) {
        String uri = createUri("ispn-user", "password");

        try (RemoteCacheManager manager = new RemoteCacheManager(uri)) {
            RemoteCache<K, V> cache = manager.getCache(cacheName);

            func.accept(cache);
        }
    }

    float[] textToVector(String text) {
        EmbeddingClient.EmbeddingRequest request =
                new EmbeddingClient.EmbeddingRequest(
                        "intfloat/multilingual-e5-base",
                        text
                );

        EmbeddingClient.EmbeddingResponse response = embeddingClient.execute(request);

        float[] vector = new float[response.embedding().size()];
        for (int i = 0; i < response.embedding().size(); i++) {
            vector[i] = response.embedding().get(i);
        }

        return vector;
    }

    Movie createMovie(String name, String description, String author, int year) {
        return new Movie(
                name,
                description,
                textToVector("passage: " + description),
                author,
                year
        );
    }

    List<Movie> createMovies() {
        return List.of(
                createMovie(
                        "The Time Machine",
                        "A man travels through time and witnesses the evolution of humanity.",
                        "H.G. Wells",
                        1895
                ),
                createMovie(
                        "Ender's Game",
                        "A young boy is trained to become a military leader in a war against an alien race.",
                        "Orson Scott Card",
                        1985
                ),
                createMovie(
                        "Brave New World",
                        "A dystopian society where people are genetically engineered and conditioned to conform to a strict social hierarchy.",
                        "Aldous Huxley",
                        1932
                ),
                createMovie(
                        "The Hitchhiker's Guide to the Galaxy",
                        "A comedic science fiction series following the misadventures of an unwitting human and his alien friend.",
                        "Douglas Adams",
                        1979
                ),
                createMovie(
                        "Dune",
                        "A desert planet is the site of political intrigue and power struggles.",
                        "Frank Herbert",
                        1965
                ),
                createMovie(
                        "Foundation",
                        "A mathematician develops a science to predict the future of humanity and works to save civilization from collapse.",
                        "Isaac Asimov",
                        1951
                ),
                createMovie(
                        "Snow Crash",
                        "A futuristic world where the internet has evolved into a virtual reality metaverse.",
                        "Neal Stephenson",
                        1992
                ),
                createMovie(
                        "Neuromancer",
                        "A hacker is hired to pull off a near-impossible hack and gets pulled into a web of intrigue.",
                        "William Gibson",
                        1984
                ),
                createMovie(
                        "The War of the Worlds",
                        "A Martian invasion of Earth throws humanity into chaos.",
                        "H.G. Wells",
                        1898
                ),
                createMovie(
                        "The Hunger Games",
                        "A dystopian society where teenagers are forced to fight to the death in a televised spectacle.",
                        "Suzanne Collins",
                        2008
                ),
                createMovie(
                        "The Andromeda Strain",
                        "A deadly virus from outer space threatens to wipe out humanity.",
                        "Michael Crichton",
                        1969
                ),
                createMovie(
                        "The Left Hand of Darkness",
                        "A human ambassador is sent to a planet where the inhabitants are genderless and can change gender at will.",
                        "Ursula K. Le Guin",
                        1969
                ),
                createMovie(
                        "The Three-Body Problem",
                        "Humans encounter an alien civilization that lives in a dying system.",
                        "Liu Cixin",
                        2008
                )
        );
    }

    @Test
    void knnSearch() {
        List<Movie> movies = createMovies();

        this.withCache("movieCache", remoteCache -> {
            movies.forEach(movie -> remoteCache.put(movie.name(), movie));

            int k = movies.size(); // all document
            float[] vector = textToVector("query: alien invasion");

            Query<Movie> query = remoteCache.query("from entity.Movie where descriptionVector <-> [:vector]~:k");
            query.maxResults(3); // result size
            query.setParameter("vector", vector);
            query.setParameter("k", k);

            QueryResult<Movie> result = query.execute();
            assertThat(result.count().value()).isEqualTo(movies.size()); // all document
            assertThat(result.count().isExact()).isTrue();

            List<Movie> resultMovies = result.list();
            assertThat(resultMovies).hasSize(3);

            assertThat(resultMovies.get(0).name())
                    .isEqualTo("The Hitchhiker's Guide to the Galaxy");
            assertThat(resultMovies.get(0).year())
                    .isEqualTo(1979);
            assertThat(resultMovies.get(1).name())
                    .isEqualTo("The Three-Body Problem");
            assertThat(resultMovies.get(1).year())
                    .isEqualTo(2008);
            assertThat(resultMovies.get(2).name())
                    .isEqualTo("The Andromeda Strain");
            assertThat(resultMovies.get(2).year())
                    .isEqualTo(1969);
        });
    }

    @Test
    void annSearch() {
        List<Movie> movies = createMovies();

        this.withCache("movieCache", remoteCache -> {
            movies.forEach(movie -> remoteCache.put(movie.name(), movie));

            int k = movies.size() - 1; // all documents - 1
            float[] vector = textToVector("query: alien invasion");

            Query<Movie> query = remoteCache.query("from entity.Movie where descriptionVector <-> [:vector]~:k");
            query.maxResults(3); // result size
            query.setParameter("vector", vector);
            query.setParameter("k", k);

            QueryResult<Movie> result = query.execute();
            assertThat(result.count().value()).isEqualTo(movies.size()); // all document
            assertThat(result.count().isExact()).isTrue();

            List<Movie> resultMovies = result.list();
            assertThat(resultMovies).hasSize(3);

            assertThat(resultMovies.get(0).name())
                    .isEqualTo("The Hitchhiker's Guide to the Galaxy");
            assertThat(resultMovies.get(0).year())
                    .isEqualTo(1979);
            assertThat(resultMovies.get(1).name())
                    .isEqualTo("The Three-Body Problem");
            assertThat(resultMovies.get(1).year())
                    .isEqualTo(2008);
            assertThat(resultMovies.get(2).name())
                    .isEqualTo("The Andromeda Strain");
            assertThat(resultMovies.get(2).year())
                    .isEqualTo(1969);
        });
    }

    @Test
    void knnSearchWithFilter() {
        List<Movie> movies = createMovies();

        this.withCache("movieCache", remoteCache -> {
            movies.forEach(movie -> remoteCache.put(movie.name(), movie));

            int k = movies.size(); // all document
            float[] vector = textToVector("query: alien invasion");

            Query<Movie> query =
                    remoteCache.query("from entity.Movie where descriptionVector <-> [:vector]~:k filtering (year: [2000 to *])");
            query.maxResults(3); // result size
            query.setParameter("vector", vector);
            query.setParameter("k", k);
            // filteringの方はパラメーター化できなかったので

            QueryResult<Movie> result = query.execute();
            assertThat(result.count().value()).isEqualTo(2);
            assertThat(result.count().isExact()).isTrue();

            List<Movie> resultMovies = result.list();
            assertThat(resultMovies).hasSize(2);

            assertThat(resultMovies.get(0).name())
                    .isEqualTo("The Three-Body Problem");
            assertThat(resultMovies.get(0).year())
                    .isEqualTo(2008);
            assertThat(resultMovies.get(1).name())
                    .isEqualTo("The Hunger Games");
            assertThat(resultMovies.get(1).year())
                    .isEqualTo(2008);
        });
    }
}

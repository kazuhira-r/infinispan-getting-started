package org.littlewings.infinispan.wordcount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.infinispan.Cache;
import org.infinispan.CacheStream;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stream.CacheCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WordCount implements Serializable {
    // Logback's Logger is Serializable
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    transient EmbeddedCacheManager cacheManager;

    static final JapaneseAnalyzer analyzer =
            new JapaneseAnalyzer(null,
                    JapaneseTokenizer.Mode.NORMAL,
                    JapaneseAnalyzer.getDefaultStopSet(),
                    JapaneseAnalyzer.getDefaultStopTags());

    @GetMapping("load")
    public String load() {
        Cache<Integer, String> wordsCache = cacheManager.getCache("wordsCache");

        logger.info("start load words");

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bocchan.txt");
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr);
             Stream<String> lines = reader.lines()) {
            AtomicInteger currentLines = new AtomicInteger();
            lines.forEach(line -> wordsCache.put(currentLines.incrementAndGet(), line));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        logger.info("end load words");

        return "Finish!!";
    }

    @GetMapping("wordcount")
    public List<Map.Entry<String, Integer>> wordcount(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        Cache<Integer, String> wordsCache = cacheManager.getCache("wordsCache");

        try (CacheStream<String> stream = wordsCache.values().parallelStream()) {
            logger.info("start wordcount");

            Map<String, Integer> collected =
                    stream
                            .timeout(30, TimeUnit.SECONDS)  // timeout
                            .flatMap((Function<String, Stream<Token>> & Serializable) line -> {
                                List<Token> tokens = new ArrayList<>();

                                try (TokenStream tokenStream = analyzer.tokenStream("", line)) {

                                    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
                                    PartOfSpeechAttribute partOfSpeechAttribute = tokenStream.addAttribute(PartOfSpeechAttribute.class);

                                    tokenStream.reset();
                                    while (tokenStream.incrementToken()) {
                                        tokens.add(new Token(charTermAttribute.toString(), partOfSpeechAttribute.getPartOfSpeech()));
                                        tokenStream.end();
                                    }
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }

                                logger.info("[{}] -FlatMap- line = {}, tokens = {}", Thread.currentThread().getName(), line, tokens);

                                return tokens.stream();
                            })
                            .filter((Predicate<Token> & Serializable) token -> {
                                logger.info("[{}] -Filter- token = {}, isPartOfSpeech = {}", Thread.currentThread().getName(), token, token.getPartOfSpeech().contains("名詞"));
                                return token.getPartOfSpeech().contains("名詞");
                            })
                            .map((Function<Token, String> & Serializable) token -> {
                                logger.info("[{}] -Map- token = {}", Thread.currentThread().getName(), token);
                                return token.getValue();
                            }).collect(
                            CacheCollectors.serializableCollector(() ->
                                    Collectors.groupingByConcurrent(s -> {
                                                logger.info("[{}] -Identity- word = {}", Thread.currentThread().getName(), s);
                                                return s;
                                            },
                                            ConcurrentHashMap::new,
                                            Collectors.reducing(0,
                                                    s -> {
                                                        logger.info("[{}] -Reduce-Map- {}", Thread.currentThread().getName(), s);
                                                        return 1;
                                                    },
                                                    (c1, c2) -> {
                                                        logger.info("[{}] -Reduce-Op- {}, {}", Thread.currentThread().getName(), c1, c2);
                                                        return c1 + c2;
                                                    }))));

            logger.info("end wordcount");

            logger.info("exract top - {}", limit);

            return collected
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("wordcount, fail", e);
            throw e;
        }
    }
}

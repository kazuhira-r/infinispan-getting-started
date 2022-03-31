package org.littlewings.infinispan.remote.testcontainers;

import java.util.List;
import java.util.stream.IntStream;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.util.Version;
import org.infinispan.server.test.core.InfinispanContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InfinispanServerContainerTest {
    @Test
    public void gettingStarted() {
        try (InfinispanContainer container = new InfinispanContainer()) {
            container.start();

            try (RemoteCacheManager manager =
                         container.getRemoteCacheManager(new ConfigurationBuilder().addContextInitializer(new EntitiesInitializerImpl()))) {
                RemoteCacheManagerAdmin admin = manager.administration();

                RemoteCache<String, Book> cache = admin.createCache("distCache", DefaultTemplate.DIST_SYNC);

                Book book = Book.create("978-4295008477", "新世代Javaプログラミングガイド[Java SE 10/11/12/13と言語拡張プロジェクト] (impress top gear)", 2860);

                cache.put(book.getIsbn(), book);

                Book b = cache.get(book.getIsbn());
                assertThat(b.getTitle()).isEqualTo("新世代Javaプログラミングガイド[Java SE 10/11/12/13と言語拡張プロジェクト] (impress top gear)");
                assertThat(b.getPrice()).isEqualTo(2860);
            }
        }
    }

    @Test
    public void credentialTest() {
        try (InfinispanContainer container = new InfinispanContainer(InfinispanContainer.IMAGE_BASENAME + ":" + Version.getVersion())
                .withUser("ispn-user")
                .withPassword("ispn-password")) {
            container.start();

            try (RemoteCacheManager manager =
                         container.getRemoteCacheManager(new ConfigurationBuilder().addContextInitializer(new EntitiesInitializerImpl()))) {
                RemoteCacheManagerAdmin admin = manager.administration();

                RemoteCache<String, Book> cache = admin.createCache("distCache", DefaultTemplate.DIST_SYNC);

                Book book = Book.create("978-4295008477", "新世代Javaプログラミングガイド[Java SE 10/11/12/13と言語拡張プロジェクト] (impress top gear)", 2860);

                cache.put(book.getIsbn(), book);

                Book b = cache.get(book.getIsbn());
                assertThat(b.getTitle()).isEqualTo("新世代Javaプログラミングガイド[Java SE 10/11/12/13と言語拡張プロジェクト] (impress top gear)");
                assertThat(b.getPrice()).isEqualTo(2860);
            }
        }
    }

    @Test
    public void clustered() {
        List<InfinispanContainer> containers =
                IntStream
                        .rangeClosed(1, 3)
                        .mapToObj(i -> new InfinispanContainer(InfinispanContainer.IMAGE_BASENAME + ":" + Version.getVersion()))
                        .toList();
        containers.forEach(InfinispanContainer::start);
        try {
            InfinispanContainer container = containers.get(0);

            try (RemoteCacheManager manager =
                         container.getRemoteCacheManager(new ConfigurationBuilder().addContextInitializer(new EntitiesInitializerImpl()))) {
                RemoteCacheManagerAdmin admin = manager.administration();

                RemoteCache<String, Book> cache = admin.createCache("distCache", DefaultTemplate.DIST_SYNC);

                Book book = Book.create("978-4295008477", "新世代Javaプログラミングガイド[Java SE 10/11/12/13と言語拡張プロジェクト] (impress top gear)", 2860);

                cache.put(book.getIsbn(), book);

                Book b = cache.get(book.getIsbn());
                assertThat(b.getTitle()).isEqualTo("新世代Javaプログラミングガイド[Java SE 10/11/12/13と言語拡張プロジェクト] (impress top gear)");
                assertThat(b.getPrice()).isEqualTo(2860);

                assertThat(manager.getServers()).hasSize(4);
            }
        } finally {
            containers.forEach(InfinispanContainer::close);
        }
    }

    @Test
    public void version() {
        assertThat(Version.getVersion()).isEqualTo("13.0.8.Final");
        assertThat(Version.getMajorMinor()).isEqualTo("13.0");
    }
}

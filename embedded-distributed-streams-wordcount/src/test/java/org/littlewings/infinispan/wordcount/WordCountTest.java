package org.littlewings.infinispan.wordcount;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.IntegerAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WordCountTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void wordcount10() {
        String loadResult = restTemplate.getForObject("/load", String.class);
        assertThat(loadResult).isEqualTo("Finish!!");

        @SuppressWarnings("unchecked")
        List<Map<String, Integer>> wordcount =
                restTemplate.getForObject("/wordcount", List.class);

        assertThat(wordcount)
                .hasSize(10);

        assertThat(wordcount.get(0).get("おれ")).isEqualTo(469);
        assertThat(wordcount.get(1).get("事")).isEqualTo(282);
        assertThat(wordcount.get(2).get("人")).isEqualTo(193);
        assertThat(wordcount.get(3).get("君")).isEqualTo(182);
        assertThat(wordcount.get(4).get("赤シャツ")).isEqualTo(168);
        assertThat(wordcount.get(5).get("山嵐")).isEqualTo(155);
        assertThat(wordcount.get(6).get("一")).isEqualTo(152);
        assertThat(wordcount.get(7).get("何")).isEqualTo(143);
        assertThat(wordcount.get(8).get("二")).isEqualTo(115);
        assertThat(wordcount.get(9).get("方")).isEqualTo(112);
    }

    @Test
    public void wordcount5() {
        String loadResult = restTemplate.getForObject("/load", String.class);
        assertThat(loadResult).isEqualTo("Finish!!");

        @SuppressWarnings("unchecked")
        List<Map<String, Integer>> wordcount =
                restTemplate.getForObject("/wordcount?limit=5", List.class);

        assertThat(wordcount)
                .hasSize(5);

        assertThat(wordcount.get(0).get("おれ")).isEqualTo(469);
        assertThat(wordcount.get(1).get("事")).isEqualTo(282);
        assertThat(wordcount.get(2).get("人")).isEqualTo(193);
        assertThat(wordcount.get(3).get("君")).isEqualTo(182);
        assertThat(wordcount.get(4).get("赤シャツ")).isEqualTo(168);
    }
}

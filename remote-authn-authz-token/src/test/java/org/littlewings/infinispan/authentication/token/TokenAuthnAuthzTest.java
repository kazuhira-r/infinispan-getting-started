package org.littlewings.infinispan.authentication.token;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenAuthnAuthzTest {
    @Test
    public void authnAndAuthz() {
        String readWriteUserToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJabXktcDdESTZjT01fZjN1NTFtUXNMT19Dc0ctMFFQNmItZlpjLU5YaHZVIn0.eyJleHAiOjE1OTYzNzUwMjIsImlhdCI6MTU5NjM3NDcyMiwianRpIjoiMmI2ZGI3MWUtMmY5Zi00YTI2LWFkYjctYzgyMmMxNDkxYTdmIiwiaXNzIjoiaHR0cDovLzE3Mi4xNy4wLjI6ODA4MC9hdXRoL3JlYWxtcy9pc3BuLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImJhZDkwMjg4LWNmZDEtNDQyNS04YzNhLWFlZWM3OGZlMjQxOCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImluZmluaXNwYW4tc2VydmVyIiwic2Vzc2lvbl9zdGF0ZSI6ImZiZWQzN2QwLTA0YTAtNDE1NS1hZjM5LTJkYmY4NTM2MTc0OSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJyZWFkLXdyaXRlIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InJlYWQtd3JpdGUtdXNlcjAwMSJ9.P5AnoYkozSgxpd3jryyPWYvswg9BU-DvFKOV1oj5Zqnjmd1Qh8t1iOzjqhEaR61MsguRiIRXxGMj3BE_MEeSK0ooFVKMjty5jvxjzGZ6OVuxOE5RSY_09o4_I0GqUUq6Xe1Vlc8Vb3NmC2TF3yFjWClRGoIHwbls2TdjU4UOKLIPWfQqvCf4p_cJkdVVVolQEsOMTSEDChwhy-ysKf0joRX43XjQg-NtYYZhdwKFlvpaD1FJYLPD-J23pkpV08COr7bavtpmIY6kSVLVv6CFuAoMBDX4TIL0Y3faQJmOPAuhOvKQipu5VIzCan6a-oN-jkQ3Yjtvvhd2KeFlOSB7gw";

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.3:11222")
                        .security()
                        .authentication()
                        .saslMechanism("OAUTHBEARER")
                        .token(readWriteUserToken)
                        .build())) {
            RemoteCache<String, String> cache = cacheManager.getCache("secureCache");

            cache.put("key1", "value1");
            assertThat(cache.get("key1")).isEqualTo("value1");
        }

        String readOnlyUserToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJabXktcDdESTZjT01fZjN1NTFtUXNMT19Dc0ctMFFQNmItZlpjLU5YaHZVIn0.eyJleHAiOjE1OTYzNzUwNDUsImlhdCI6MTU5NjM3NDc0NSwianRpIjoiN2VhMWUxNzUtMmFhZC00NmVhLWFkNDAtM2E0NGUwYzQ1MDNmIiwiaXNzIjoiaHR0cDovLzE3Mi4xNy4wLjI6ODA4MC9hdXRoL3JlYWxtcy9pc3BuLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjYzNDk1ZWE3LTMwYzEtNDIyMy1iNzEzLWM2ZjFkMjUzMGY5NyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImluZmluaXNwYW4tc2VydmVyIiwic2Vzc2lvbl9zdGF0ZSI6ImMwZjI1N2I1LWJhNzUtNDk4OS04ZWU3LTRmZGEwNjk4ZDI2NiIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsInJlYWQtb25seSJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoicmVhZC1vbmx5LXVzZXIwMDEifQ.BTjflXa0GqD1UN538ekjjnIZGNmyFmm3YDwiOgPpLS38mEETDRAyzLUbwmT-AMtdz6gg5z6fRVttKUnrawb8VmBXlZZuLc77OZOLcK5Y3Yph7WGsYInMJFA1qOWxWttUe7fojo-ZN_6eUGLcmQ5Q1RDGdmpjx4_oGsms6ymI2WntH6gKk0zwCR2gJbUO6VSOn_ZToCDSJGqNYYqbF7Tok7v16rE902tagpIpErID0QYRhzuaWAiPlV5Ne84896faJjFQCdTAF41sJl98pRfsqaiiU49eLKQKEnk1WBhOGlrnZhXCPf8CWdNzcLKeO14gq-WOmCRQ3XqCn63Ob_eE1A";

        try (RemoteCacheManager cacheManager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.3:11222")
                        .security()
                        .authentication()
                        .saslMechanism("OAUTHBEARER")
                        .token(readOnlyUserToken)
                        .build())) {
            RemoteCache<String, String> cache = cacheManager.getCache("secureCache");

            assertThat(cache.get("key1")).isEqualTo("value1");

            assertThatThrownBy(() -> cache.put("key2", "value2"))
                    .isInstanceOf(HotRodClientException.class)
                    .hasMessageContainingAll("Unauthorized access", "lacks 'WRITE' permission");
        }
    }

    @Test
    public void noAuth() {
        try (RemoteCacheManager cacheManager = new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServers("172.17.0.3:11222")
                        .build())) {
            RemoteCache<String, String> cache = cacheManager.getCache("secureCache");

            assertThatThrownBy(() -> cache.put("key1", "value1"))
                    .isInstanceOf(HotRodClientException.class)
                    .hasMessageContaining("Unauthorized 'PUT' operation");
        }
    }
}

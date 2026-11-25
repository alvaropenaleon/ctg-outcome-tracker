package org.ctgtracker.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class TargetControllerTest {

    @Test
    void listsAllOfficialTargetDefinitions(@Autowired RestTestClient client) {
        client.get().uri("/api/targets").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].id").value(ids -> assertThat(ids).isEqualTo(List.of(
                        "1", "2", "3", "4", "5", "6", "7", "8", "9a", "9b", "10", "11",
                        "12", "13", "14", "15a", "15b", "16", "17", "pr1", "pr2", "pr3", "pr4")))
                .jsonPath("$[7].title").isEqualTo("Employment")
                .jsonPath("$[7].definition")
                .value(String.class, definition -> assertThat(definition).contains("62%", "2031"))
                .jsonPath("$[7].sourceUrl").isEqualTo("https://www.closingthegap.gov.au/national-agreement/targets")
                .jsonPath("$[9].definition")
                .value(String.class, definition -> assertThat(definition).contains("jurisdictional standard", "town"))
                .jsonPath("$[22].category").isEqualTo("priority-reform")
                .jsonPath("$[22].sourceUrl").isEqualTo("https://www.closingthegap.gov.au/national-agreement/priority-reforms");
    }
}

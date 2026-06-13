package com.inspiremymini.controller;

// TODO: DO ZROBIENIA:
// porównywac cały payload
// poprawić ten rest template żeby działał update
// sprobowac api first


import com.inspiremymini.config.TestSecurityConfig;
import com.inspiremymini.config.TestcontainersConfig;
import com.inspiremymini.api.model.UserRequest;
import com.inspiremymini.api.model.UserResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

//import org.springframework.boot.test.web.client.TestRestTemplate;  TODO: sprobowac mock MVC

@Import(TestcontainersConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
                properties = {"spring.config.name=application-test", "spring.profiles.active=test"})
//@WebMvcTest(UserController.class)
// @Transactional  // nie działa wtedy shouldReturnAllUsers
// a jak dodam tylko czyszczenie kontekstu to nie działa usuwanie
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@AutoConfigureMockMvc(addFilters = false)
@Sql(
        scripts = {
                "/sql/cleanup.sql",
                "/sql/insert_users.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class UserControllerTest {

    @LocalServerPort
    int localServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

//    @AutoConfigureMockMvc
//    @Autowired
//    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int expectedUserCount;

    @BeforeEach
    void setUp() {
        expectedUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user",
                Integer.class);
    }

    @Test
    void shouldReturnAllUsersWhenApiKeyIsValid() throws IOException, JSONException {
        // given
        HttpHeaders header = new HttpHeaders();
        header.set("X-Api-Key", "ABC");
        HttpEntity<Void> entity = new HttpEntity<>(header);
        String url = "http://localhost:" + localServerPort + "/users";
        String expectedJson = StreamUtils.copyToString(
                new ClassPathResource("payload/user/happy-path/GetAllUsersSuccessResponse.json")
                        .getInputStream(), StandardCharsets.UTF_8);

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(
                expectedJson,
                response.getBody(),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    void returnAllUsersShouldThrowExceptionWhenApiKeyIsInValid() {
        // given
        HttpHeaders header = new HttpHeaders();
        header.set("X-Api-Key", "WRONG");
        HttpEntity<Void> entity = new HttpEntity<>(header);
        String url = "http://localhost:" + localServerPort + "/users";

        // when & then
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnCorrectUserById() throws IOException, JSONException {
        // given
        String url = "http://localhost:" + localServerPort + "/users/1";
        String expectedJson = StreamUtils.copyToString(
                new ClassPathResource("payload/user/happy-path/GetUserByIdSuccessResponse.json")
                        .getInputStream(), StandardCharsets.UTF_8);

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                url,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(
                expectedJson,
                response.getBody(),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    void shouldCreateUser() throws IOException, JSONException {
        // given
        UserRequest
                request = new UserRequest("tomasz", "hasloTomasza", "tomasz@gmail.com");
        String url = "http://localhost:" + localServerPort + "/users";
        String expectedJson = StreamUtils.copyToString(
                new ClassPathResource("payload/user/happy-path/CreateUserSuccessResponse.json")
                        .getInputStream(), StandardCharsets.UTF_8);


        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                request,
                String.class
                );

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JSONAssert.assertEquals(
                expectedJson,
                response.getBody(),
                JSONCompareMode.LENIENT
        );

    }

    @Test
    void shouldUpdateUser() throws IOException, JSONException {
        // given
        UserRequest updateRequest = new UserRequest(
                "krzysiekNew", "haslo123New", "krzysiekNew@gmail.com");
        HttpEntity<UserRequest> entity = new HttpEntity<>(updateRequest);
        String url = "http://localhost:" + localServerPort + "/users/1";
        String expectedJson = StreamUtils.copyToString(
                new ClassPathResource("payload/user/happy-path/UpdateUserSuccessResponse.json")
                        .getInputStream(),
                StandardCharsets.UTF_8
        );

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(
                expectedJson,
                response.getBody(),
                JSONCompareMode.LENIENT
        );

    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // given
        HttpHeaders header = new HttpHeaders();
        header.set("X-Api-Key", "ABC");
        HttpEntity<Void> entity = new HttpEntity<>(header);
        Long userIdToDelete = 1L;
        String url = "http://localhost:" + localServerPort + "/users";

        // when
        restTemplate.delete(url + "/" + userIdToDelete);

        // then
        ResponseEntity<UserResponse[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserResponse[].class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(expectedUserCount - 1);
    }
}

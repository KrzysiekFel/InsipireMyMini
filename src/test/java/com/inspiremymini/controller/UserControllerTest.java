package com.inspiremymini.controller;

// TODO: DONE:
// propertisy testowe ze zdefiniowanym portem
// prepared statement w jdbcTemplate chroni przed sql injection (używamy np jdbcTemplate.queryForObject)
// pozbylem  sie magic number
// zmienilem exception jak nie ma API Key: UnauthenticatedException i poprawilem test

// TODO: DO ZROBIENIA:
// poprawić ten rest template żeby działał update
// porównywac cały payload
// sprobowac api first

import com.inspiremymini.config.TestSecurityConfig;
import com.inspiremymini.config.TestcontainersConfig;
import com.inspiremymini.dto.UserRequest;
import com.inspiremymini.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//import org.springframework.boot.test.web.client.TestRestTemplate;  TODO: sprobowac mock MVC

@Import({TestcontainersConfig.class, TestSecurityConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
                properties = "spring.config.name=application-test")
@Transactional
public class UserControllerTest {

    @LocalServerPort
    int localServerPort;

    @Autowired
    private RestTemplate restTemplate;  // NIE DZIALA DLA PATCH

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int expectedUserCount;

    @BeforeEach
    void setUp() {
        expectedUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user",
                Integer.class);
    }

    @Test
    void shouldReturnAllUsersWhenApiKeyIsValid() {
        // given
        HttpHeaders header = new HttpHeaders();
        header.set("X-Api-Key", "ABC");
        HttpEntity<Void> entity = new HttpEntity<>(header);
        String url = "http://localhost:" + localServerPort + "/users";

        // when
        ResponseEntity<UserResponse[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserResponse[].class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(expectedUserCount);
    }

    @Test
    void returnAllUsersShouldThrowExceptionWhenApiKeyIsInValid() {
        // given
        HttpHeaders header = new HttpHeaders();
        header.set("X-Api-Key", "WRONG");
        HttpEntity<Void> entity = new HttpEntity<>(header);
        String url = "http://localhost:" + localServerPort + "/users";

        // when & then
        assertThatThrownBy(() ->
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        UserResponse[].class
                )
        )
                .isInstanceOf(HttpClientErrorException.Unauthorized.class)
                .hasMessageContaining("401");
    }

    @Test
    void shouldReturnCorrectUserById() {
        // given
        String url = "http://localhost:" + localServerPort + "/users/1";

        // when
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                url,
                UserResponse.class);

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("krzysiek");  // TODO: sprawdzić cały obiekt i status
        // TODO: przygotować porownanie z jasone i dodac logike żeby nie porównywało całego jsona z response tylko np bez Id
        // TODO: assertPayload(expectedResponseBody, actualResponseBody, List.of(field1,field2) - fields to omit); sprawdzic czy jest lub stworzyc
    }

    @Test
    void shouldCreateUser() {
        // given
        UserRequest request = new UserRequest("tomasz", "hasloTomasza", "tomasz@gmail.com");
        String url = "http://localhost:" + localServerPort + "/users";

        // when
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                url,
                request,
                UserResponse.class
                );
        UserResponse responseFromDb = restTemplate.getForObject(
                "http://localhost:"+localServerPort+"/users/" + response.getBody().getId(),
                UserResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getUsername()).isEqualTo("tomasz");
        assertThat(responseFromDb.getEmail()).isEqualTo("tomasz@gmail.com");
    }

//    @Test
//    void shouldUpdateUser() {
//        // given
//        UserRequest updateRequest = new UserRequest("krzysiekNew", "haslo123New", "krzysiekNew@gmail.com");
//        HttpEntity<UserRequest> entity = new HttpEntity<>(updateRequest);
//        String url = "http://localhost:" + localServerPort + "/users/1";
//
//        // when
//        ResponseEntity<UserResponse> response = restTemplate.exchange(
//                url,
//                HttpMethod.PATCH,
//                entity,
//                UserResponse.class);
//
//        //then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody().getUsername()).isEqualTo("krzysiekNew");
//
//    }

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
        assertThat(response.getBody()).hasSize(expectedUserCount - 1);
    }
}

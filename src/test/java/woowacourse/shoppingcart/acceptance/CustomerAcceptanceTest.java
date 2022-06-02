package woowacourse.shoppingcart.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import woowacourse.auth.dto.TokenRequest;
import woowacourse.auth.dto.TokenResponse;
import woowacourse.shoppingcart.dto.CustomerRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("회원 관련 기능")
public class CustomerAcceptanceTest extends AcceptanceTest {
    @Test
    void 회원_가입() {
        ExtractableResponse<Response> createResponse = 회원_가입("test", "1234");

        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createResponse.header("Location")).isEqualTo("/api/customers/test");
    }

    @Test
    void 중복된_이름으로_회원_가입() {
        회원_가입("test", "1234");
        ExtractableResponse<Response> createResponse = 회원_가입("test", "1234");

        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 회원가입_시_누락된_필드값_존재() {
        ExtractableResponse<Response> createResponse = 회원_가입("test", null);

        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 내_정보_조회() {
        // given
        회원_가입("test", "1234");
        String accessToken = 로그인_후_토큰_획득("test", "1234");

        // when
        ExtractableResponse<Response> getResponse = RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/api/customers/me")
                .then().log().all()
                .extract();

        // then
        assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(getResponse.body().jsonPath().getString("name")).isEqualTo("test");
    }

    @Test
    void 토큰을_발급받지_않고_내_정보_조회() {
        // when
        ExtractableResponse<Response> getResponse = RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/api/customers/me")
                .then().log().all()
                .extract();

        // then
        assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 내_정보_수정() {
        // given
        회원_가입("test", "1234");
        String accessToken = 로그인_후_토큰_획득("test", "1234");

        // when
        ExtractableResponse<Response> editResponse = RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new CustomerRequest("test", "1255"))
                .when().put("/api/customers/me")
                .then().log().all()
                .extract();

        // then
        ExtractableResponse<Response> loginResponse = RestAssured
                .given().log().all()
                .body(new TokenRequest("test", "1255"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/api/login")
                .then().log().all().extract();

        assertThat(editResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 토큰을_발급받지_않고_내_정보_수정() {
        // when
        ExtractableResponse<Response> editResponse = RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new CustomerRequest("test", "1255"))
                .when().put("/api/customers/me")
                .then().log().all()
                .extract();

        // then
        assertThat(editResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 회원_탈퇴() {
        // given
        회원_가입("test", "1234");
        String accessToken = 로그인_후_토큰_획득("test", "1234");

        // when
        ExtractableResponse<Response> deleteResponse = RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/api/customers/me")
                .then().log().all()
                .extract();

        // then
        ExtractableResponse<Response> getResponse = RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/api/customers/me")
                .then().log().all()
                .extract();

        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 토큰을_발급받지_않고_탈퇴() {
        // when
        ExtractableResponse<Response> deleteResponse = RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/api/customers/me")
                .then().log().all()
                .extract();

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    public static String 로그인_후_토큰_획득(String name, String password) {
        return RestAssured
                .given().log().all()
                .body(new TokenRequest(name, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/api/login")
                .then().log().all().extract().as(TokenResponse.class).getAccessToken();
    }

    public static ExtractableResponse<Response> 회원_가입(String name, String password) {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new CustomerRequest(name, password))
                .when().post("/api/customers")
                .then().log().all()
                .extract();
    }
}

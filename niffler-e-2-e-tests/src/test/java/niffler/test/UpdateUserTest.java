package niffler.test;

import io.qameta.allure.AllureId;
import niffler.api.UserUpdateService;
import niffler.jupiter.annotation.ClasspathUser;
import niffler.jupiter.annotation.UserInfo;
import niffler.model.UserJson;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;


public class UpdateUserTest extends BaseWebTest {

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();

    private static final Retrofit retrofit = new Retrofit.Builder()
            .client(httpClient)
            .baseUrl("http://127.0.0.1:8089")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final UserUpdateService userUpdateService = retrofit.create(UserUpdateService.class);


    @ValueSource(strings = {
            "testdata/dima.json",
            "testdata/emma.json"
    })
    @AllureId("999")
    @ParameterizedTest
    void updateUserTest(@ClasspathUser @UserInfo(firstname = "2", surname = "2", currency = "USD") UserJson user) throws IOException {

        var statusCode = userUpdateService.updateUser(user)
                .execute().code();
        Assertions.assertEquals(200, statusCode);

    }
}

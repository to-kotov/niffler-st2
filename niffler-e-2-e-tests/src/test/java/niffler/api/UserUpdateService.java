package niffler.api;

import niffler.model.UserJson;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserUpdateService {
    @POST("/updateUserInfo")
    Call<UserJson> updateUser(@Body UserJson user);
}

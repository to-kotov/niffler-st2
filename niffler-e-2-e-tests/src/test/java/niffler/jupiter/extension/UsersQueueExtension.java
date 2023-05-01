package niffler.jupiter.extension;

import io.qameta.allure.AllureId;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import niffler.jupiter.annotation.User;
import niffler.jupiter.annotation.User.UserType;
import niffler.model.UserJson;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static niffler.jupiter.annotation.User.UserType.*;

public class UsersQueueExtension implements
        BeforeEachCallback,
        AfterTestExecutionCallback,
        ParameterResolver {

    public static Namespace USER_EXTENSION_NAMESPACE = Namespace.create(UsersQueueExtension.class);

    private static Queue<UserJson> USERS_WITH_FRIENDS_QUEUE = new ConcurrentLinkedQueue<>();
    private static Queue<UserJson> USERS_INVITATION_SENT_QUEUE = new ConcurrentLinkedQueue<>();
    private static Queue<UserJson> USERS_INVITATION_RECEIVED_QUEUE = new ConcurrentLinkedQueue<>();

    static {
        USERS_WITH_FRIENDS_QUEUE.addAll(
                List.of(userJson("dima", "123456"), userJson("barsik", "12345"))
        );
        USERS_INVITATION_SENT_QUEUE.addAll(
                List.of(userJson("emma", "12345"), userJson("emily", "12345"))
        );
        USERS_INVITATION_RECEIVED_QUEUE.addAll(
                List.of(userJson("anna", "12345"), userJson("bill", "12345"))
        );
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        final String testId = getTestId(context);
        Parameter[] testParameters = context.getRequiredTestMethod().getParameters();
        for (Parameter parameter : testParameters) {
            User desiredUser = parameter.getAnnotation(User.class);

            if (desiredUser != null) {
                UserType userType = desiredUser.userType();
                UserJson user = null;

                while (user == null) {
                    switch (userType) {
                        case WITH_FRIENDS -> user = USERS_WITH_FRIENDS_QUEUE.poll();
                        case INVITATION_SENT -> user = USERS_INVITATION_SENT_QUEUE.poll();
                        case INVITATION_RECEIVED -> user = USERS_INVITATION_RECEIVED_QUEUE.poll();
                    }
                }

                if (context.getStore(USER_EXTENSION_NAMESPACE).get(testId) == null) {
                    Map map = new HashMap();
                    List<UserJson> users = new ArrayList<UserJson>();
                    users.add(user);
                    map.put(userType, users);
                    context.getStore(USER_EXTENSION_NAMESPACE).put(testId, map);
                } else {
                    if (((Map<UserType, List>) context.getStore(USER_EXTENSION_NAMESPACE).get(testId)).get(userType) == null) {
                        List<UserJson> users = new ArrayList<UserJson>();
                        users.add(user);
                        ((Map<UserType, List>) context.getStore(USER_EXTENSION_NAMESPACE).get(testId)).put(userType, users);
                    } else {
                        ((Map<UserType, List>) context.getStore(USER_EXTENSION_NAMESPACE).get(testId)).get(userType).add(user);
                    }


                }
                System.out.println();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        final String testId = getTestId(context);
        Map<UserType, List<UserJson>> users = (Map<UserType, List<UserJson>>) context.getStore(USER_EXTENSION_NAMESPACE).get(testId);
        users.forEach((userType, userList) -> {
                    switch (userType) {
                        case WITH_FRIENDS -> USERS_WITH_FRIENDS_QUEUE.addAll(userList);
                        case INVITATION_SENT -> USERS_INVITATION_SENT_QUEUE.addAll(userList);
                        case INVITATION_RECEIVED -> USERS_INVITATION_RECEIVED_QUEUE.addAll(userList);
                    }
                }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(User.class) &&
                parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UserJson resolveParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        final String testId = getTestId(extensionContext);
        UserType userType = extensionContext.getRequiredTestMethod().getParameters()[parameterContext.getIndex()].getAnnotation(User.class).userType();
        List<UserJson> users = ((Map<UserType, List>) extensionContext.getStore(USER_EXTENSION_NAMESPACE).get(testId)).get(userType);
        var user = users.get(0);
        users.remove(0);
        return user;
    }

    private String getTestId(ExtensionContext context) {
        return Objects
                .requireNonNull(context.getRequiredTestMethod().getAnnotation(AllureId.class))
                .value();
    }

    private static UserJson userJson(String userName, String password) {
        UserJson user = new UserJson();
        user.setUsername(userName);
        user.setPassword(password);
        return user;
    }
}

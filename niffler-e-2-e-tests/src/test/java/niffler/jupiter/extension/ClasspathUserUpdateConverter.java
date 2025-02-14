package niffler.jupiter.extension;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import niffler.model.CurrencyValues;
import niffler.model.UserJson;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class ClasspathUserUpdateConverter implements ArgumentConverter {

    private ClassLoader cl = ClasspathUserUpdateConverter.class.getClassLoader();
    private static ObjectMapper om = new ObjectMapper();

    @Override
    public UserJson convert(Object source, ParameterContext context)
            throws ArgumentConversionException {

        if (source instanceof String stringSource) {
            try {
                var user = om.readValue(cl.getResourceAsStream(stringSource), UserJson.class);
                return user;
            } catch (IOException e) {
                throw new ArgumentConversionException(e.getMessage());
            }
        } else {
            throw new ArgumentConversionException("Only string source supported!");
        }
    }
}

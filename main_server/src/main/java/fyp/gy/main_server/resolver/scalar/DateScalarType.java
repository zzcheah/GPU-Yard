package fyp.gy.main_server.resolver.scalar;

import graphql.schema.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static fyp.gy.common.constant.GyConstant.DATE_TIME_FORMAT;

@Component
public class DateScalarType extends GraphQLScalarType {
    public DateScalarType() {
        super("Date", "Java Date", new Coercing() {
            @Override
            public Object serialize(Object o) throws CoercingSerializeException {
                SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.ENGLISH);
                return formatter.format((Date)o);
            }

            @Override
            public Object parseValue(Object o) throws CoercingParseValueException {
                return o;
            }

            @Override
            public Object parseLiteral(Object o) throws CoercingParseLiteralException {
                if (o==null){
                    return null;
                }
                return o.toString();
            }
        });
    }
}

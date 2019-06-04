package handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Validable;
import spark.Request;
import spark.Response;
import spark.Route;
import store.InkstepStore;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRequestHandler<V extends Validable> implements RequestHandler<V>, Route {

    private Class<V> valueClass;
    protected InkstepStore store;

    private static final int HTTP_BAD_REQUEST = 400;

    public AbstractRequestHandler(Class<V> valueClass, InkstepStore store){
        this.valueClass = valueClass;
        this.store = store;
    }

    private static boolean shouldReturnHtml(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains("text/html");
    }

    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e){
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }

    public final Answer process(V value, Map<String, String> queryParams, boolean shouldReturnHtml) {
        if (!value.isValid()) {
            return Answer.empty(HTTP_BAD_REQUEST);
        } else {
            return processImpl(value, queryParams, shouldReturnHtml);
        }
    }

    protected abstract Answer processImpl(V value, Map<String, String> queryParams, boolean shouldReturnHtml);

    @Override
    public Object handle(Request request, Response response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        V value = objectMapper.readValue(request.body(), valueClass);

        Map<String, String> queryParams = new HashMap<>();
        Answer answer = process(value, queryParams, shouldReturnHtml(request));
        response.status(answer.getCode());
        if (shouldReturnHtml(request)) {
            response.type("text/html");
        } else {
            response.type("application/json");
        }
        response.body(answer.getBody());
        return answer.getBody();
    }

}
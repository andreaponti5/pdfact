package pdfact;

import spark.Request;
import spark.Response;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import static spark.Spark.post;

public class PdfApi {
    public static void main(String[] args) {
        PdfService pdfService = new PdfService();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        post("/api/pdf/parse", (request, response) -> parsePdf(request, response, pdfService, gson), gson::toJson);
    }

    private static Object parsePdf(Request request, Response response, PdfService pdfService, Gson gson) {
        String body = request.body();
        RequestPayload requestPayload = gson.fromJson(body, RequestPayload.class);

        if (requestPayload == null || requestPayload.getPath() == null || requestPayload.getPath().isEmpty()) {
            response.status(400);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "File path is required");
            return errorResponse;
        }

        JsonObject jsonResult;

        try {
            String jsonString = pdfService.parsePdf(requestPayload.getPath(), requestPayload.getUnit(), requestPayload.getRoles());
            jsonResult = gson.fromJson(jsonString, JsonObject.class);
            response.status(200);
        } catch (Exception e) {
            response.status(500);
            jsonResult = new JsonObject();
            jsonResult.addProperty("error", "Error processing PDF");
        }

        return jsonResult;
    }

    static class RequestPayload {
        private String path;
        private String unit;
        private List<String> roles;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}

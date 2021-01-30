package za.co.solutions.interactive;

import com.squareup.okhttp.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class SmsPortalTestApplication implements CommandLineRunner {
	@Value("${smsportal.key}")
	public String apiKey;

	@Value("${smsportal.secret}")
	public String apiSecret;

	@Value("${smsportal.auth.address}")
	public String authAddress;

	public static void main(String[] args) {
		SpringApplication.run(SmsPortalTestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		OkHttpClient client = new OkHttpClient();
		String accountApiCredentials = apiKey + ":" + apiSecret;
		byte[] plainTestBytes = accountApiCredentials.getBytes();
		String base64Credentials = Base64Utils.encodeToString(plainTestBytes);

		Request authRequest = new Request.Builder()
				.url(authAddress)
				.header("Authorization", String.format("Basic %s", base64Credentials))
				.get()
				.build();

		Response authResponse = client.newCall(authRequest).execute();
		String authToken = "";
		String jsonObject = "";

		if (authResponse.code() == 200) {
			ResponseBody authResponseBody = authResponse.body();
			jsonObject = authResponseBody.string();
		} else {
			System.out.println(authResponse.message());
		}

		RequestBody sendRequestBody = RequestBody.create(
				com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8"),
				"{\n" +
						"  \"messages\": [\n" +
						"    {\n" +
						"      \"content\": \"" + "Newer Hello SMS World" + "\",\n" +
						"      \"destination\": \"" + "0822832834" + "\"\n" +
						"    }\n" +
						"  ]\n" +
						"}");
		JSONObject jsonObj = new JSONObject(jsonObject);
		System.out.println(jsonObj.getString("token"));

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(jsonObj);
		System.out.println(jsonArray.length());
		for(int i=0; i < jsonArray.length();i++){
			System.out.println(jsonArray.getJSONObject(i).getString("token"));
			authToken = jsonArray.getJSONObject(i).getString("token");
		}
		String authHeader = String.format("Bearer %s", authToken);
		Request sendRequest = new com.squareup.okhttp.Request.Builder()
				.url("https://rest.smsportal.com/v1/bulkmessages")
				.header("Authorization", authHeader)
				.post(sendRequestBody)
				.build();

		try {
			Response sendResponse = client.newCall(sendRequest).execute();
			System.out.println(sendResponse.body().string());
		}
		catch (IOException e) {
			System.out.println("Request failed. Exception: " + e.getMessage());
		}
	}
}

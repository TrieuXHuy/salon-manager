package com.salonnbooking.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.api.dto.AppointmentRequests;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.api.dto.DashboardRequests;
import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.api.dto.ReportRequests;
import com.salonnbooking.domain.UserRole;

/**
 * HTTP Client để gọi API Spring Boot Backend
 * Cung cấp các phương thức CRUD cho Customer, Appointment, Service, etc.
 */
public class ApiClient {
	private static final String BASE_URL = "http://localhost:8080/api";
	private static final HttpClient httpClient = HttpClient.newHttpClient();
	private static final Gson gson;

	static {
		// Cấu hình Gson với custom deserializer cho LocalDateTime
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
		gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
		gson = gsonBuilder.create();
	}

	private static class LocalDateTimeAdapter
			implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
		@Override
		public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			String dateStr = json.getAsString();
			return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		}

		@Override
		public JsonElement serialize(LocalDateTime src, Type typeOfSrc,
				com.google.gson.JsonSerializationContext context) {
			return context.serialize(src.toString());
		}
	}

	private static class LocalDateAdapter
			implements JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {
		@Override
		public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return LocalDate.parse(json.getAsString());
		}

		@Override
		public JsonElement serialize(LocalDate src, Type typeOfSrc,
				com.google.gson.JsonSerializationContext context) {
			return context.serialize(src.toString());
		}
	}

	private static HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + path))
				.GET()
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private static void requireStatus(HttpResponse<String> response, int expectedStatus, String action) {
		if (response.statusCode() != expectedStatus) {
			throw new RuntimeException("Failed to " + action + ": " + response.body());
		}
	}

	private static HttpResponse<String> sendPost(String path, Object body) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + path))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private static HttpResponse<String> sendPut(String path, Object body) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + path))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	// ==================== AUTH API ====================

	public static AuthRequests.Response login(String username, String password) throws Exception {
		HttpResponse<String> response = sendPost("/auth/login", new AuthRequests.Login(username, password));
		requireStatus(response, 200, "login");
		return gson.fromJson(response.body(), AuthRequests.Response.class);
	}

	public static AuthRequests.Response register(String username, String password) throws Exception {
		HttpResponse<String> response = sendPost("/auth/register", new AuthRequests.Register(username, password));
		requireStatus(response, 201, "register");
		return gson.fromJson(response.body(), AuthRequests.Response.class);
	}

	public static void logout() throws Exception {
		HttpResponse<String> response = sendPost("/auth/logout", new AuthRequests.Message("logout"));
		requireStatus(response, 200, "logout");
	}

	public static List<AuthRequests.UserResponse> getUsers(String requesterUsername) throws Exception {
		HttpResponse<String> response = sendGet("/auth/users?requesterUsername=" + requesterUsername);
		requireStatus(response, 200, "fetch users");

		var list = new java.util.ArrayList<AuthRequests.UserResponse>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body()).getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element, AuthRequests.UserResponse.class));
		}
		return list;
	}

	public static AuthRequests.UserResponse changeUserRole(Integer userId, String requesterUsername, UserRole role)
			throws Exception {
		HttpResponse<String> response = sendPut("/auth/users/" + userId + "/role",
				new AuthRequests.ChangeRole(requesterUsername, role));
		requireStatus(response, 200, "change user role");
		return gson.fromJson(response.body(), AuthRequests.UserResponse.class);
	}

	// ==================== CUSTOMER API ====================

	/**
	 * Lấy danh sách tất cả khách hàng
	 */
	public static List<CustomerRequests.Response> getAllCustomers() throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/customers"))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch customers: " + response.body());
		}

		var list = new java.util.ArrayList<CustomerRequests.Response>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body())
				.getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element, CustomerRequests.Response.class));
		}
		return list;
	}

	/**
	 * Lấy thông tin khách hàng theo ID
	 */
	public static CustomerRequests.Response getCustomer(Integer id) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/customers/" + id))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch customer: " + response.body());
		}

		return gson.fromJson(response.body(), CustomerRequests.Response.class);
	}

	/**
	 * Tạo khách hàng mới
	 */
	public static CustomerRequests.Response createCustomer(CustomerRequests.Create createReq)
			throws Exception {
		String json = gson.toJson(createReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/customers"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 201) {
			throw new RuntimeException("Failed to create customer: " + response.body());
		}

		return gson.fromJson(response.body(), CustomerRequests.Response.class);
	}

	/**
	 * Cập nhật khách hàng
	 */
	public static CustomerRequests.Response updateCustomer(Integer id, CustomerRequests.Update updateReq)
			throws Exception {
		String json = gson.toJson(updateReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/customers/" + id))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to update customer: " + response.body());
		}

		return gson.fromJson(response.body(), CustomerRequests.Response.class);
	}

	/**
	 * Xóa khách hàng
	 */
	public static void deleteCustomer(Integer id) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/customers/" + id))
				.DELETE()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 204) {
			throw new RuntimeException("Failed to delete customer: " + response.body());
		}
	}

	// ==================== APPOINTMENT API ====================

	/**
	 * Lấy danh sách tất cả lịch hẹn
	 */
	public static List<AppointmentRequests.Response> getAllAppointments() throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/appointments"))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch appointments: " + response.body());
		}

		var list = new java.util.ArrayList<AppointmentRequests.Response>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body())
				.getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element, AppointmentRequests.Response.class));
		}
		return list;
	}

	/**
	 * Lấy thông tin lịch hẹn theo ID
	 */
	public static AppointmentRequests.Response getAppointment(Integer id) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/appointments/" + id))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch appointment: " + response.body());
		}

		return gson.fromJson(response.body(), AppointmentRequests.Response.class);
	}

	/**
	 * Tạo lịch hẹn mới
	 */
	public static AppointmentRequests.Response createAppointment(AppointmentRequests.Create createReq)
			throws Exception {
		String json = gson.toJson(createReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/appointments"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 201) {
			throw new RuntimeException("Failed to create appointment: " + response.body());
		}

		return gson.fromJson(response.body(), AppointmentRequests.Response.class);
	}

	/**
	 * Cập nhật lịch hẹn
	 */
	public static AppointmentRequests.Response updateAppointment(Integer id,
			AppointmentRequests.Update updateReq) throws Exception {
		String json = gson.toJson(updateReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/appointments/" + id))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to update appointment: " + response.body());
		}

		return gson.fromJson(response.body(), AppointmentRequests.Response.class);
	}

	/**
	 * Xóa lịch hẹn
	 */
	public static void deleteAppointment(Integer id) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/appointments/" + id))
				.DELETE()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 204) {
			throw new RuntimeException("Failed to delete appointment: " + response.body());
		}
	}

	// ==================== SERVICE API ====================

	/**
	 * Lấy danh sách tất cả dịch vụ
	 */
	public static java.util.List<com.salonnbooking.api.dto.ServiceRequests.Response> getAllServices()
			throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/services"))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch services: " + response.body());
		}

		var list = new java.util.ArrayList<com.salonnbooking.api.dto.ServiceRequests.Response>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body())
				.getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element,
					com.salonnbooking.api.dto.ServiceRequests.Response.class));
		}
		return list;
	}

	/**
	 * Lấy danh sách các dịch vụ còn hoạt động
	 */
	public static java.util.List<com.salonnbooking.api.dto.ServiceRequests.Response> getActiveServices()
			throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/services/active"))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch active services: " + response.body());
		}

		var list = new java.util.ArrayList<com.salonnbooking.api.dto.ServiceRequests.Response>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body())
				.getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element,
					com.salonnbooking.api.dto.ServiceRequests.Response.class));
		}
		return list;
	}

	/**
	 * Lấy thông tin dịch vụ theo ID
	 */
	public static com.salonnbooking.api.dto.ServiceRequests.Response getService(Integer id)
			throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/services/" + id))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch service: " + response.body());
		}

		return gson.fromJson(response.body(), com.salonnbooking.api.dto.ServiceRequests.Response.class);
	}

	/**
	 * Tạo dịch vụ mới
	 */
	public static com.salonnbooking.api.dto.ServiceRequests.Response createService(
			com.salonnbooking.api.dto.ServiceRequests.Create createReq) throws Exception {
		String json = gson.toJson(createReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/services"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 201) {
			throw new RuntimeException("Failed to create service: " + response.body());
		}

		return gson.fromJson(response.body(), com.salonnbooking.api.dto.ServiceRequests.Response.class);
	}

	/**
	 * Cập nhật dịch vụ
	 */
	public static com.salonnbooking.api.dto.ServiceRequests.Response updateService(Integer id,
			com.salonnbooking.api.dto.ServiceRequests.Update updateReq) throws Exception {
		String json = gson.toJson(updateReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/services/" + id))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to update service: " + response.body());
		}

		return gson.fromJson(response.body(), com.salonnbooking.api.dto.ServiceRequests.Response.class);
	}

	/**
	 * Xóa dịch vụ
	 */
	public static void deleteService(Integer id) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/services/" + id))
				.DELETE()
				.build();

		HttpResponse<String> response = httpClient.send(request,
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 204) {
			throw new RuntimeException("Failed to delete service: " + response.body());
		}
	}

	// ==================== DASHBOARD API ====================

	public static DashboardRequests.DashboardResponse getDashboard() throws Exception {
		HttpResponse<String> response = sendGet("/dashboard");
		requireStatus(response, 200, "fetch dashboard");
		return gson.fromJson(response.body(), DashboardRequests.DashboardResponse.class);
	}

	public static DashboardRequests.QuickStatsResponse getQuickStats() throws Exception {
		HttpResponse<String> response = sendGet("/dashboard/quick-stats");
		requireStatus(response, 200, "fetch quick stats");
		return gson.fromJson(response.body(), DashboardRequests.QuickStatsResponse.class);
	}

	// ==================== REPORT API ====================

	public static List<ReportRequests.DailyRevenueResponse> getDailyRevenueReport(
			LocalDate startDate, LocalDate endDate) throws Exception {
		HttpResponse<String> response = sendGet("/reports/daily-revenue?startDate=" + startDate + "&endDate=" + endDate);
		requireStatus(response, 200, "fetch daily revenue report");

		var list = new java.util.ArrayList<ReportRequests.DailyRevenueResponse>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body()).getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element, ReportRequests.DailyRevenueResponse.class));
		}
		return list;
	}

	public static List<ReportRequests.ServiceRevenueResponse> getServiceRevenueReport() throws Exception {
		HttpResponse<String> response = sendGet("/reports/service-revenue");
		requireStatus(response, 200, "fetch service revenue report");

		var list = new java.util.ArrayList<ReportRequests.ServiceRevenueResponse>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body()).getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element, ReportRequests.ServiceRevenueResponse.class));
		}
		return list;
	}

	public static List<ReportRequests.PaymentMethodResponse> getPaymentMethodReport() throws Exception {
		HttpResponse<String> response = sendGet("/reports/payment-methods");
		requireStatus(response, 200, "fetch payment method report");

		var list = new java.util.ArrayList<ReportRequests.PaymentMethodResponse>();
		var jsonArray = com.google.gson.JsonParser.parseString(response.body()).getAsJsonArray();
		for (var element : jsonArray) {
			list.add(gson.fromJson(element, ReportRequests.PaymentMethodResponse.class));
		}
		return list;
	}

	public static ReportRequests.AppointmentStatsResponse getAppointmentStats() throws Exception {
		HttpResponse<String> response = sendGet("/reports/appointment-stats");
		requireStatus(response, 200, "fetch appointment stats");
		return gson.fromJson(response.body(), ReportRequests.AppointmentStatsResponse.class);
	}

	// ==================== PAYMENT API ====================

	public static PaymentRequests.Response createPayment(PaymentRequests.Create createReq) throws Exception {
		String json = gson.toJson(createReq);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/payments"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		requireStatus(response, 201, "create payment");
		return gson.fromJson(response.body(), PaymentRequests.Response.class);
	}
}

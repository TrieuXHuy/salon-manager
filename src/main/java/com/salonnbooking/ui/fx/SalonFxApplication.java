package com.salonnbooking.ui.fx;

import com.salonnbooking.api.dto.*;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.domain.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class SalonFxApplication extends Application {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_INPUT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

    private Stage stage;
    private BorderPane shell;
    private StackPane workspace;
    private String username;
    private UserRole role;
    private final List<Button> navButtons = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Salon Manager");
        showLogin();
    }

    private void showLogin() {
        VBox card = new VBox(18);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(420);

        Label brand = new Label("Salon Manager");
        brand.getStyleClass().add("login-title");
        Label subtitle = new Label("Đăng nhập để quản lý lịch hẹn, khách hàng và doanh thu");
        subtitle.getStyleClass().add("muted");

        TextField userField = new TextField();
        userField.setPromptText("Tên đăng nhập");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");

        Button login = primaryButton("Đăng nhập");
        Button register = ghostButton("Đăng ký");
        HBox actions = new HBox(10, login, register);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(login, Priority.ALWAYS);
        HBox.setHgrow(register, Priority.ALWAYS);
        login.setMaxWidth(Double.MAX_VALUE);
        register.setMaxWidth(Double.MAX_VALUE);

        Label status = new Label();
        status.getStyleClass().add("muted");
        card.getChildren().addAll(brand, subtitle, labeled("Tên đăng nhập", userField), labeled("Mật khẩu", passwordField),
                actions, status);

        Consumer<Boolean> submit = isLogin -> {
            String user = userField.getText().trim();
            String password = passwordField.getText();
            if (user.isBlank() || password.isBlank()) {
                warn("Thiếu thông tin", "Vui lòng nhập tên đăng nhập và mật khẩu.");
                return;
            }
            login.setDisable(true);
            register.setDisable(true);
            status.setText(isLogin ? "Đang đăng nhập..." : "Đang tạo tài khoản...");
            runAsync(() -> isLogin ? ApiClient.login(user, password) : ApiClient.register(user, password), response -> {
                this.username = response.username();
                this.role = response.role() == null ? UserRole.CUSTOMER : response.role();
                showShell();
            }, ex -> {
                status.setText("");
                login.setDisable(false);
                register.setDisable(false);
                error("Đăng nhập thất bại", cleanError(ex));
            });
        };
        login.setOnAction(e -> submit.accept(true));
        register.setOnAction(e -> submit.accept(false));
        passwordField.setOnAction(e -> submit.accept(true));

        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-root");
        setScene(root, 980, 680);
    }

    private void showShell() {
        shell = new BorderPane();
        shell.getStyleClass().add("app-root");
        workspace = new StackPane();
        workspace.getStyleClass().add("workspace");
        shell.setLeft(createSidebar());
        shell.setCenter(workspace);
        setScene(shell, 1280, 780);
        navigate("dashboard");
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);

        Label logo = new Label("SP");
        logo.getStyleClass().add("logo");
        VBox brandText = new VBox(2, title("Salon Pro", "brand-title"), new Label("Đặt lịch dễ dàng"));
        brandText.getChildren().get(1).getStyleClass().add("muted");
        HBox brand = new HBox(12, logo, brandText);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("brand");

        VBox menu = new VBox(6);
        addNav(menu, "Tổng quan", "dashboard");
        if (role == UserRole.OWNER) {
            addNav(menu, "Tài khoản", "users");
        }
        if (role == UserRole.OWNER || role == UserRole.STAFF) {
            addNav(menu, "Khách hàng", "customers");
        }
        addNav(menu, "Lịch hẹn", "appointments");
        if (role == UserRole.OWNER || role == UserRole.STAFF) {
            addNav(menu, "Dịch vụ", "services");
            addNav(menu, "Khu vực", "rooms");
        }
        if (role == UserRole.OWNER) {
            addNav(menu, "Báo cáo", "reports");
        }
        VBox.setVgrow(menu, Priority.ALWAYS);

        Button logout = ghostButton("Đăng xuất");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> runAsync(() -> {
            ApiClient.logout();
            return null;
        }, ignored -> showLogin(), ex -> showLogin()));

        Label nameLabel = new Label(username);
        nameLabel.getStyleClass().add("user-name");
        Label roleLabel = new Label(role.getDisplayName());
        roleLabel.getStyleClass().add("user-role");
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4, javafx.scene.paint.Color.web("#10B981"));
        Label onlineLabel = new Label("Online");
        onlineLabel.getStyleClass().add("user-online");
        HBox statusBox = new HBox(6, dot, onlineLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        VBox user = new VBox(4, nameLabel, roleLabel, statusBox);
        user.getStyleClass().add("user-card");

        sidebar.getChildren().addAll(brand, menu, logout, user);
        return sidebar;
    }

    private void addNav(VBox menu, String label, String route) {
        String emoji = switch (route) {
            case "dashboard" -> "📊  ";
            case "users" -> "⚙️  ";
            case "customers" -> "👥  ";
            case "appointments" -> "📅  ";
            case "services" -> "💇  ";
            case "rooms" -> "🚪  ";
            case "reports" -> "📈  ";
            default -> "";
        };
        Button button = new Button(emoji + label);
        button.setUserData(route);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> navigate(route));
        navButtons.add(button);
        menu.getChildren().add(button);
    }

    private void navigate(String route) {
        navButtons.forEach(b -> b.getStyleClass().remove("selected"));
        navButtons.stream().filter(b -> route.equals(b.getUserData()))
                .findFirst()
                .ifPresent(b -> b.getStyleClass().add("selected"));

        Node view = switch (route) {
            case "customers" -> new CustomersView();
            case "appointments" -> new AppointmentsView();
            case "services" -> new ServicesView();
            case "rooms" -> new RoomsView();
            case "reports" -> new ReportsView();
            case "users" -> new UsersView();
            default -> new DashboardView();
        };
        workspace.getChildren().setAll(view);
    }

    private final class DashboardView extends VBox {
        private final FlowPane stats = new FlowPane(14, 14);
        private final TableView<AppointmentRow> table = new TableView<>();
        private final Label status = new Label("Sẵn sàng");

        private DashboardView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Tổng quan", "Theo dõi hoạt động salon hôm nay", "Làm mới", e -> load()),
                    stats, card(table), status);
            VBox.setVgrow(table, Priority.ALWAYS);
            status.getStyleClass().add("muted");
            configureAppointmentTable(table);
            load();
        }

        private void load() {
            status.setText("Đang tải tổng quan...");
            stats.getChildren().setAll(progress());
            runAsync(() -> new DashboardData(ApiClient.getDashboard(), ApiClient.getQuickStats(),
                    ApiClient.getAllCustomers(), ApiClient.getAllServices(), ApiClient.getAllAppointments()), data -> {
                stats.getChildren().setAll(
                        stat("Khách hàng", data.dashboard.totalCustomers(), "stat-customers"),
                        stat("Lịch hẹn hôm nay", data.dashboard.totalAppointmentsToday(), "stat-appointments"),
                        stat("Chờ xử lý", data.dashboard.pendingAppointments(), "stat-pending"),
                        stat("Doanh thu hôm nay", money(data.dashboard.todayRevenue()), "stat-revenue"),
                        stat("Doanh thu tháng", money(data.dashboard.monthlyRevenue()), "stat-revenue"),
                        stat("Dịch vụ nổi bật", orDash(data.dashboard.topServiceName()), "stat-default"),
                        stat("Lịch tháng này", data.quickStats.appointmentsThisMonth(), "stat-appointments"),
                        stat("Hoàn thành", String.format(Locale.US, "%.1f%%",
                                data.dashboard.appointmentCompletionRate() == null ? 0 : data.dashboard.appointmentCompletionRate()), "stat-default"));
                List<AppointmentRow> rows = data.appointments.stream()
                        .filter(a -> a.appointmentTime().toLocalDate().equals(LocalDate.now()))
                        .sorted(Comparator.comparing(AppointmentRequests.Response::appointmentTime))
                        .map(a -> toAppointmentRow(a, data.customers, data.services))
                        .toList();
                table.setItems(FXCollections.observableArrayList(rows));
                status.setText("Đã cập nhật");
            }, ex -> {
                stats.getChildren().clear();
                status.setText("Không tải được tổng quan");
                error("Lỗi tải tổng quan", cleanError(ex));
            });
        }
    }

    private final class CustomersView extends VBox {
        private final TextField name = new TextField();
        private final TextField phone = new TextField();
        private final TextField email = new TextField();
        private final TextField loyaltyPoints = new TextField();
        private final TextArea customerNote = new TextArea();
        private final ComboBox<Gender> gender = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        private final TableView<CustomerRequests.Response> table = new TableView<>();
        private final TextField customerSearch = new TextField();
        private List<CustomerRequests.Response> customerCache = List.of();
        private Integer selectedId;

        private CustomersView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Khách hàng", "Quản lý hồ sơ và thông tin liên hệ", "Làm mới", e -> load()),
                    customerForm(), customerFilters(), card(table));
            VBox.setVgrow(table, Priority.ALWAYS);
            configureCustomerTable();
            table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
                if (value != null) {
                    selectedId = value.id();
                    name.setText(value.fullName());
                    phone.setText(value.phone());
                    email.setText(value.email());
                    gender.setValue(value.gender());
                    loyaltyPoints.setText(String.valueOf(value.loyaltyPoints() == null ? 0 : value.loyaltyPoints()));
                    customerNote.setText(orEmpty(value.note()));
                }
            });
            load();
        }

        private Node customerFilters() {
            customerSearch.setPromptText("Tìm theo tên, điện thoại hoặc email");
            customerSearch.textProperty().addListener((obs, old, value) -> applyCustomerFilter());
            Button clearSearch = secondaryButton("Xóa tìm kiếm");
            clearSearch.setOnAction(e -> customerSearch.clear());
            HBox box = new HBox(10, customerSearch, clearSearch);
            HBox.setHgrow(customerSearch, Priority.ALWAYS);
            return card(box);
        }

        private Node customerForm() {
            GridPane grid = formGrid();
            name.setPromptText("Họ và tên");
            phone.setPromptText("Số điện thoại");
            email.setPromptText("Email");
            loyaltyPoints.setPromptText("Điểm tích lũy");
            customerNote.setPromptText("Sở thích, lưu ý dị ứng, kiểu tóc quen thuộc...");
            customerNote.setPrefRowCount(2);
            gender.setValue(Gender.other);
            grid.addRow(0, labeled("Họ và tên", name), labeled("Số điện thoại", phone));
            grid.addRow(1, labeled("Email", email), labeled("Giới tính", gender));
            grid.addRow(2, labeled("Điểm tích lũy", loyaltyPoints));
            grid.add(labeled("Ghi chú chăm sóc", customerNote), 0, 3, 2, 1);
            HBox actions = actions(primaryButton("Thêm"), secondaryButton("Cập nhật"), dangerButton("Xóa"), ghostButton("Xóa form"));
            ((Button) actions.getChildren().get(0)).setOnAction(e -> save(false));
            ((Button) actions.getChildren().get(1)).setOnAction(e -> save(true));
            ((Button) actions.getChildren().get(2)).setOnAction(e -> delete());
            ((Button) actions.getChildren().get(3)).setOnAction(e -> clear());
            VBox box = new VBox(12, sectionTitle("Thông tin khách hàng"), grid, actions);
            return card(box);
        }

        private void configureCustomerTable() {
            column(table, "ID", CustomerRequests.Response::id, 70);
            column(table, "Họ và tên", CustomerRequests.Response::fullName, 220);
            column(table, "Điện thoại", CustomerRequests.Response::phone, 150);
            column(table, "Email", CustomerRequests.Response::email, 240);
            column(table, "Giới tính", c -> c.gender() == null ? "" : c.gender().getDisplayName(), 120);
            column(table, "Điểm", c -> c.loyaltyPoints() == null ? 0 : c.loyaltyPoints(), 90);

            TableColumn<CustomerRequests.Response, Integer> tierCol = new TableColumn<>("Hạng");
            tierCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().loyaltyPoints()));
            tierCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(Integer points, boolean empty) {
                    super.updateItem(points, empty);
                    if (empty || points == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String tier = customerTier(points);
                        String cssClass = switch (tier) {
                            case "VIP" -> "tier-vip";
                            case "Thân thiết" -> "tier-regular";
                            default -> "tier-new";
                        };
                        setGraphic(createBadge(tier, cssClass));
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            tierCol.setPrefWidth(110);
            table.getColumns().add(tierCol);

            column(table, "Ghi chú", CustomerRequests.Response::note, 220);
        }

        private void load() {
            runAsync(ApiClient::getAllCustomers, list -> {
                        customerCache = list;
                        applyCustomerFilter();
                    },
                    ex -> error("Lỗi tải khách hàng", cleanError(ex)));
        }

        private void applyCustomerFilter() {
            String keyword = customerSearch.getText() == null ? "" : customerSearch.getText().trim().toLowerCase(Locale.ROOT);
            List<CustomerRequests.Response> filtered = customerCache.stream()
                    .filter(customer -> keyword.isBlank()
                            || orEmpty(customer.fullName()).toLowerCase(Locale.ROOT).contains(keyword)
                            || orEmpty(customer.phone()).toLowerCase(Locale.ROOT).contains(keyword)
                            || orEmpty(customer.email()).toLowerCase(Locale.ROOT).contains(keyword))
                    .toList();
            table.setItems(FXCollections.observableArrayList(filtered));
        }

        private void save(boolean update) {
            if (name.getText().trim().isBlank() || phone.getText().trim().isBlank() || email.getText().trim().isBlank()) {
                warn("Thiếu thông tin", "Vui lòng nhập đầy đủ họ tên, điện thoại và email.");
                return;
            }
            if (update && selectedId == null) {
                warn("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để cập nhật.");
                return;
            }
            Integer points;
            try {
                points = loyaltyPoints.getText().trim().isBlank() ? 0 : Integer.parseInt(loyaltyPoints.getText().trim());
            } catch (NumberFormatException ex) {
                warn("Điểm chưa hợp lệ", "Điểm tích lũy phải là số nguyên.");
                return;
            }
            CustomerRequests.Create create = new CustomerRequests.Create(name.getText().trim(), phone.getText().trim(),
                    email.getText().trim(), gender.getValue(), customerNote.getText().trim());
            CustomerRequests.Update edit = new CustomerRequests.Update(create.fullName(), create.phone(), create.email(),
                    create.gender(), points, create.note());
            runAsync(() -> update ? ApiClient.updateCustomer(selectedId, edit) : ApiClient.createCustomer(create), r -> {
                clear();
                load();
            }, ex -> error("Lỗi lưu khách hàng", cleanError(ex)));
        }

        private void delete() {
            if (selectedId == null) {
                warn("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để xóa.");
                return;
            }
            if (!confirm("Xóa khách hàng", "Bạn chắc chắn muốn xóa khách hàng này?")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteCustomer(selectedId);
                return null;
            }, r -> {
                clear();
                load();
            }, ex -> error("Lỗi xóa khách hàng", cleanError(ex)));
        }

        private void clear() {
            selectedId = null;
            name.clear();
            phone.clear();
            email.clear();
            loyaltyPoints.clear();
            customerNote.clear();
            gender.setValue(Gender.other);
            table.getSelectionModel().clearSelection();
        }
    }

    private final class ServicesView extends VBox {
        private final TextField name = new TextField();
        private final TextField price = new TextField();
        private final TextField duration = new TextField();
        private final TextArea description = new TextArea();
        private final CheckBox active = new CheckBox("Đang hoạt động");
        private final TableView<ServiceRequests.Response> table = new TableView<>();
        private Integer selectedId;

        private ServicesView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Dịch vụ", "Cập nhật danh mục, thời lượng và bảng giá", "Làm mới", e -> load()),
                    serviceForm(), card(table));
            VBox.setVgrow(table, Priority.ALWAYS);
            configureServiceTable();
            table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
                if (value != null) {
                    selectedId = value.id();
                    name.setText(value.name());
                    price.setText(value.price() == null ? "" : value.price().toPlainString());
                    duration.setText(value.durationMinutes() == null ? "" : value.durationMinutes().toString());
                    description.setText(orEmpty(value.description()));
                    active.setSelected(Boolean.TRUE.equals(value.isActive()));
                }
            });
            load();
        }

        private Node serviceForm() {
            description.setPrefRowCount(3);
            GridPane grid = formGrid();
            grid.addRow(0, labeled("Tên dịch vụ", name), labeled("Giá (VND)", price));
            grid.addRow(1, labeled("Thời lượng (phút)", duration), active);
            grid.add(labeled("Mô tả", description), 0, 2, 2, 1);
            HBox actions = actions(primaryButton("Thêm"), secondaryButton("Cập nhật"), dangerButton("Xóa"), ghostButton("Xóa form"));
            ((Button) actions.getChildren().get(0)).setOnAction(e -> save(false));
            ((Button) actions.getChildren().get(1)).setOnAction(e -> save(true));
            ((Button) actions.getChildren().get(2)).setOnAction(e -> delete());
            ((Button) actions.getChildren().get(3)).setOnAction(e -> clear());
            return card(new VBox(12, sectionTitle("Thông tin dịch vụ"), grid, actions));
        }

        private void configureServiceTable() {
            column(table, "ID", ServiceRequests.Response::id, 70);
            column(table, "Tên", ServiceRequests.Response::name, 220);
            column(table, "Giá", s -> money(s.price()), 140);
            column(table, "Phút", ServiceRequests.Response::durationMinutes, 90);

            TableColumn<ServiceRequests.Response, Boolean> statusCol = new TableColumn<>("Trạng thái");
            statusCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().isActive()));
            statusCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String labelText = item ? "Đang bán" : "Tạm ẩn";
                        String cssClass = item ? "status-active" : "status-inactive";
                        setGraphic(createBadge(labelText, cssClass));
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            statusCol.setPrefWidth(120);
            table.getColumns().add(statusCol);

            column(table, "Mô tả", ServiceRequests.Response::description, 300);
        }

        private void load() {
            runAsync(ApiClient::getAllServices, list -> table.setItems(FXCollections.observableArrayList(list)),
                    ex -> error("Lỗi tải dịch vụ", cleanError(ex)));
        }

        private void save(boolean update) {
            if (name.getText().trim().isBlank()) {
                warn("Thiếu thông tin", "Tên dịch vụ là bắt buộc.");
                return;
            }
            if (update && selectedId == null) {
                warn("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ để cập nhật.");
                return;
            }
            try {
                BigDecimal amount = new BigDecimal(price.getText().trim());
                Integer minutes = Integer.parseInt(duration.getText().trim());
                ServiceRequests.Create create = new ServiceRequests.Create(name.getText().trim(), amount, minutes,
                        description.getText().trim(), active.isSelected());
                ServiceRequests.Update edit = new ServiceRequests.Update(create.name(), create.price(),
                        create.durationMinutes(), create.description(), create.isActive());
                runAsync(() -> update ? ApiClient.updateService(selectedId, edit) : ApiClient.createService(create), r -> {
                    clear();
                    load();
                }, ex -> error("Lỗi lưu dịch vụ", cleanError(ex)));
            } catch (NumberFormatException ex) {
                warn("Dữ liệu chưa hợp lệ", "Giá và thời lượng phải là số hợp lệ.");
            }
        }

        private void delete() {
            if (selectedId == null) {
                warn("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ để xóa.");
                return;
            }
            if (!confirm("Xóa dịch vụ", "Bạn chắc chắn muốn xóa dịch vụ này?")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteService(selectedId);
                return null;
            }, r -> {
                clear();
                load();
            }, ex -> error("Lỗi xóa dịch vụ", cleanError(ex)));
        }

        private void clear() {
            selectedId = null;
            name.clear();
            price.clear();
            duration.clear();
            description.clear();
            active.setSelected(false);
            table.getSelectionModel().clearSelection();
        }
    }

    private final class RoomsView extends VBox {
        private final TextField name = new TextField();
        private final TextArea description = new TextArea();
        private final CheckBox active = new CheckBox("Đang hoạt động");
        private final TableView<ServiceRoomRequests.Response> table = new TableView<>();
        private Integer selectedId;

        private RoomsView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Khu vực", "Quản lý khu/phòng dùng để chia slot đặt lịch", "Làm mới", e -> load()),
                    roomForm(), card(table));
            VBox.setVgrow(table, Priority.ALWAYS);
            configureRoomTable();
            table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
                if (value != null) {
                    selectedId = value.id();
                    name.setText(value.name());
                    description.setText(orEmpty(value.description()));
                    active.setSelected(Boolean.TRUE.equals(value.isActive()));
                }
            });
            load();
        }

        private Node roomForm() {
            name.setPromptText("Ví dụ: Phòng 1, Khu VIP, Ghế gội 2");
            description.setPromptText("Mô tả ngắn về vị trí hoặc mục đích sử dụng");
            description.setPrefRowCount(3);
            active.setSelected(true);

            GridPane grid = formGrid();
            grid.addRow(0, labeled("Tên phòng/khu", name), active);
            grid.add(labeled("Mô tả", description), 0, 1, 2, 1);

            HBox actions = actions(primaryButton("Thêm"), secondaryButton("Cập nhật"), dangerButton("Xóa"), ghostButton("Xóa form"));
            ((Button) actions.getChildren().get(0)).setOnAction(e -> save(false));
            ((Button) actions.getChildren().get(1)).setOnAction(e -> save(true));
            ((Button) actions.getChildren().get(2)).setOnAction(e -> delete());
            ((Button) actions.getChildren().get(3)).setOnAction(e -> clear());
            return card(new VBox(12, sectionTitle("Thông tin khu phục vụ"), grid, actions));
        }

        private void configureRoomTable() {
            column(table, "ID", ServiceRoomRequests.Response::id, 70);
            column(table, "Tên phòng/khu", ServiceRoomRequests.Response::name, 220);

            TableColumn<ServiceRoomRequests.Response, Boolean> statusCol = new TableColumn<>("Trạng thái");
            statusCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().isActive()));
            statusCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String labelText = item ? "Đang dùng" : "Tạm ẩn";
                        String cssClass = item ? "status-active" : "status-inactive";
                        setGraphic(createBadge(labelText, cssClass));
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            statusCol.setPrefWidth(120);
            table.getColumns().add(statusCol);

            column(table, "Mô tả", ServiceRoomRequests.Response::description, 420);
        }

        private void load() {
            runAsync(ApiClient::getAllServiceRooms, list -> table.setItems(FXCollections.observableArrayList(list)),
                    ex -> error("Lỗi tải phòng phục vụ", cleanError(ex)));
        }

        private void save(boolean update) {
            if (name.getText().trim().isBlank()) {
                warn("Thiếu thông tin", "Tên phòng/khu là bắt buộc.");
                return;
            }
            if (update && selectedId == null) {
                warn("Chưa chọn phòng", "Vui lòng chọn một phòng để cập nhật.");
                return;
            }
            ServiceRoomRequests.Create create = new ServiceRoomRequests.Create(
                    name.getText().trim(),
                    description.getText().trim(),
                    active.isSelected());
            ServiceRoomRequests.Update edit = new ServiceRoomRequests.Update(
                    create.name(),
                    create.description(),
                    create.isActive());
            runAsync(() -> update ? ApiClient.updateServiceRoom(selectedId, edit) : ApiClient.createServiceRoom(create), r -> {
                clear();
                load();
            }, ex -> error("Lỗi lưu phòng phục vụ", cleanError(ex)));
        }

        private void delete() {
            if (selectedId == null) {
                warn("Chưa chọn phòng", "Vui lòng chọn một phòng để xóa.");
                return;
            }
            if (!confirm("Xóa phòng phục vụ", "Bạn chắc chắn muốn xóa phòng này? Lịch hẹn cũ có thể đang tham chiếu phòng này.")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteServiceRoom(selectedId);
                return null;
            }, r -> {
                clear();
                load();
            }, ex -> error("Lỗi xóa phòng phục vụ", cleanError(ex)));
        }

        private void clear() {
            selectedId = null;
            name.clear();
            description.clear();
            active.setSelected(true);
            table.getSelectionModel().clearSelection();
        }
    }

    private final class AppointmentsView extends VBox {
        private final TableView<AppointmentRow> table = new TableView<>();
        private final FlowPane quickStats = new FlowPane(14, 14);
        private final TextField search = new TextField();
        private final ComboBox<AppointmentStatus> statusFilter = new ComboBox<>();
        private final DatePicker start = new DatePicker();
        private final DatePicker end = new DatePicker();
        private final Label status = new Label("Sẵn sàng");
        private List<CustomerRequests.Response> customers = List.of();
        private List<ServiceRequests.Response> services = List.of();
        private List<AppointmentRequests.Response> appointments = List.of();

        private AppointmentsView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Lịch hẹn", "Tạo, lọc, cập nhật và thanh toán lịch hẹn", "Làm mới", e -> load()),
                    filters(), quickStats, card(table), appointmentToolbar());
            VBox.setVgrow(table, Priority.ALWAYS);
            configureAppointmentTable(table);
            load();
        }

        private Node filters() {
            search.setPromptText("Tìm khách hàng hoặc dịch vụ");
            statusFilter.getItems().setAll(AppointmentStatus.values());
            statusFilter.setPromptText("Tất cả trạng thái");
            Button clear = secondaryButton("Xóa lọc");
            Button today = secondaryButton("Hôm nay");
            today.setTooltip(new Tooltip("Lọc nhanh lịch hẹn trong ngày hôm nay"));
            today.setOnAction(e -> {
                LocalDate now = LocalDate.now();
                start.setValue(now);
                end.setValue(now);
                applyFilters();
            });
            clear.setOnAction(e -> {
                search.clear();
                statusFilter.setValue(null);
                start.setValue(null);
                end.setValue(null);
                applyFilters();
            });
            search.textProperty().addListener((obs, o, n) -> applyFilters());
            statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
            start.valueProperty().addListener((obs, o, n) -> applyFilters());
            end.valueProperty().addListener((obs, o, n) -> applyFilters());
            HBox row = new HBox(10, search, statusFilter, start, end, today, clear);
            row.getStyleClass().add("filter-bar");
            HBox.setHgrow(search, Priority.ALWAYS);
            return card(row);
        }

        private Node appointmentToolbar() {
            Button add = primaryButton("Thêm");
            Button edit = secondaryButton("Sửa");
            Button delete = dangerButton("Xóa");
            Button confirm = secondaryButton("Xác nhận");
            Button complete = secondaryButton("Hoàn thành");
            Button pay = secondaryButton("Thanh toán");
            Button remind = secondaryButton("Nhắc lịch");
            add.setOnAction(e -> openAppointmentDialog(null));
            edit.setOnAction(e -> {
                AppointmentRow row = table.getSelectionModel().getSelectedItem();
                if (row == null) {
                    warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để sửa.");
                    return;
                }
                openAppointmentDialog(findAppointment(row.id()));
            });
            delete.setOnAction(e -> deleteSelected());
            confirm.setOnAction(e -> updateSelectedStatus(AppointmentStatus.confirmed));
            complete.setOnAction(e -> updateSelectedStatus(AppointmentStatus.completed));
            pay.setOnAction(e -> paySelected());
            remind.setOnAction(e -> remindSelected());
            status.getStyleClass().add("muted");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return card(new HBox(10, add, edit, delete, confirm, complete, pay, remind, spacer, status));
        }

        private void load() {
            status.setText("Đang tải dữ liệu...");
            runAsync(() -> {
                customers = ApiClient.getAllCustomers();
                services = ApiClient.getAllServices();
                appointments = ApiClient.getAllAppointments().stream()
                        .sorted(Comparator.comparing(AppointmentRequests.Response::appointmentTime)
                                .thenComparing(AppointmentRequests.Response::id))
                        .toList();
                return appointments;
            }, list -> {
                applyFilters();
                status.setText("Sẵn sàng - " + list.size() + " lịch hẹn");
            }, ex -> {
                status.setText("Lỗi tải dữ liệu");
                error("Lỗi tải lịch hẹn", cleanError(ex));
            });
        }

        private void applyFilters() {
            String keyword = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
            AppointmentStatus selectedStatus = statusFilter.getValue();
            LocalDate from = start.getValue();
            LocalDate to = end.getValue();
            List<AppointmentRow> rows = appointments.stream()
                    .map(a -> toAppointmentRow(a, customers, services))
                    .filter(r -> keyword.isBlank() || r.customer().toLowerCase(Locale.ROOT).contains(keyword)
                            || r.service().toLowerCase(Locale.ROOT).contains(keyword))
                    .filter(r -> selectedStatus == null || r.statusEnum() == selectedStatus)
                    .filter(r -> from == null || !r.time().toLocalDate().isBefore(from))
                    .filter(r -> to == null || !r.time().toLocalDate().isAfter(to))
                    .toList();
            table.setItems(FXCollections.observableArrayList(rows));
            renderQuickStats(rows);
            status.setText("Sẵn sàng - " + rows.size() + "/" + appointments.size() + " lịch hẹn");
        }

        private void openAppointmentDialog(AppointmentRequests.Response appointment) {
            AppointmentEdit result = new AppointmentDialogFx(customers, services, appointments, appointment).showAndWait().orElse(null);
            if (result == null) {
                return;
            }
            if (appointment == null) {
                runAsync(() -> ApiClient.createAppointment(new AppointmentRequests.Create(result.customerId(),
                                List.of(result.serviceId()), result.roomId(), result.time(), result.status(), result.note())), r -> load(),
                        ex -> error("Lỗi tạo lịch hẹn", cleanError(ex)));
            } else {
                runAsync(() -> ApiClient.updateAppointment(appointment.id(), new AppointmentRequests.Update(result.customerId(),
                                List.of(result.serviceId()), result.roomId(), result.time(), result.status(), result.note())), r -> load(),
                        ex -> error("Lỗi cập nhật lịch hẹn", cleanError(ex)));
            }
        }

        private void renderQuickStats(List<AppointmentRow> rows) {
            LocalDate today = LocalDate.now();
            long todayCount = rows.stream().filter(row -> row.time().toLocalDate().equals(today)).count();
            long pendingCount = rows.stream().filter(row -> row.statusEnum() == AppointmentStatus.pending).count();
            long paidCount = rows.stream().filter(row -> row.statusEnum() == AppointmentStatus.paid).count();
            BigDecimal visibleRevenue = rows.stream()
                    .filter(row -> row.statusEnum() == AppointmentStatus.paid)
                    .map(AppointmentRow::price)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            quickStats.getChildren().setAll(
                    stat("Hôm nay", todayCount + " lịch", "stat-appointments"),
                    stat("Chờ xử lý", pendingCount + " lịch", "stat-pending"),
                    stat("Đã thanh toán", paidCount + " lịch", "stat-revenue"),
                    stat("Doanh thu đã lọc", money(visibleRevenue), "stat-revenue"));
        }

        private AppointmentRequests.Response findAppointment(Integer id) {
            return appointments.stream().filter(a -> Objects.equals(a.id(), id)).findFirst().orElse(null);
        }

        private void deleteSelected() {
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để xóa.");
                return;
            }
            if (!confirm("Xóa lịch hẹn", "Bạn chắc chắn muốn xóa lịch hẹn này?")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteAppointment(row.id());
                return null;
            }, r -> load(), ex -> error("Lỗi xóa lịch hẹn", cleanError(ex)));
        }

        private void updateSelectedStatus(AppointmentStatus newStatus) {
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để cập nhật trạng thái.");
                return;
            }
            AppointmentRequests.Response appointment = findAppointment(row.id());
            if (appointment == null) {
                warn("Không tìm thấy lịch hẹn", "Dữ liệu lịch hẹn đã thay đổi, vui lòng làm mới.");
                return;
            }
            if (appointment.status() == AppointmentStatus.paid && newStatus != AppointmentStatus.paid) {
                warn("Không thể đổi trạng thái", "Lịch hẹn đã thanh toán không nên chuyển về trạng thái khác.");
                return;
            }
            runAsync(() -> ApiClient.updateAppointment(appointment.id(), new AppointmentRequests.Update(
                    appointment.customerId(),
                    appointment.serviceIds() == null ? List.of() : appointment.serviceIds(),
                    appointment.roomId(),
                    appointment.appointmentTime(),
                    newStatus,
                    appointment.note())), r -> load(), ex -> error("Lỗi cập nhật trạng thái", cleanError(ex)));
        }

        private void remindSelected() {
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để nhắc lịch.");
                return;
            }
            status.setText("Đang gửi email nhắc lịch...");
            runAsync(() -> {
                ApiClient.remindAppointment(row.id());
                return null;
            }, ignored -> {
                status.setText("Sẵn sàng");
                alert(Alert.AlertType.INFORMATION, "Gửi nhắc lịch thành công",
                        "Đã gửi email nhắc lịch hẹn qua Resend cho khách hàng " + row.customer() + " thành công!");
            }, ex -> {
                status.setText("Sẵn sàng");
                error("Lỗi gửi nhắc lịch", cleanError(ex));
            });
        }

        private void paySelected() {
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để thanh toán.");
                return;
            }
            ServiceRequests.Response service = services.stream()
                    .filter(s -> Objects.equals(s.name(), row.service()))
                    .findFirst()
                    .orElse(null);
            BigDecimal amount = service == null || service.price() == null ? BigDecimal.ZERO : service.price();
            if (!showPaymentQrSimulation(row.id(), row.customer(), row.service(), amount, "QR / chuyển khoản")) {
                return;
            }
            PaymentRequests.Create request = new PaymentRequests.Create(row.id(), amount, PaymentMethod.bank_transfer,
                    PaymentStatus.paid, LocalDateTime.now());
            runAsync(() -> ApiClient.createPayment(request), r -> load(), ex -> error("Lỗi thanh toán", cleanError(ex)));
        }
    }

    private final class ReportsView extends VBox {
        private final DatePicker from = new DatePicker(LocalDate.now().minusDays(7));
        private final DatePicker to = new DatePicker(LocalDate.now());
        private final VBox content = new VBox(14);

        private ReportsView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Báo cáo", "Doanh thu, dịch vụ và trạng thái lịch hẹn", "Tải báo cáo", e -> load()),
                    card(new HBox(10, labeled("Từ ngày", from), labeled("Đến ngày", to))), content);
            VBox.setVgrow(content, Priority.ALWAYS);
            load();
        }

        private void load() {
            content.getChildren().setAll(progress());
            runAsync(() -> new ReportData(ApiClient.getDailyRevenueReport(from.getValue(), to.getValue()),
                            ApiClient.getServiceRevenueReport(), ApiClient.getPaymentMethodReport(), ApiClient.getAppointmentStats()),
                    data -> render(data), ex -> error("Lỗi tải báo cáo", cleanError(ex)));
        }

        private void render(ReportData data) {
            TableView<ReportRequests.DailyRevenueResponse> daily = new TableView<>(FXCollections.observableArrayList(data.daily));
            column(daily, "Ngày", r -> r.date().toString(), 140);
            column(daily, "Doanh thu", r -> money(r.totalRevenue()), 160);
            column(daily, "Lịch hẹn", ReportRequests.DailyRevenueResponse::appointmentCount, 100);
            column(daily, "Hoàn thành", ReportRequests.DailyRevenueResponse::completedCount, 120);

            TableView<ReportRequests.ServiceRevenueResponse> services = new TableView<>(FXCollections.observableArrayList(data.services));
            column(services, "Dịch vụ", ReportRequests.ServiceRevenueResponse::serviceName, 220);
            column(services, "Lượt đặt", ReportRequests.ServiceRevenueResponse::appointmentCount, 100);
            column(services, "Doanh thu", r -> money(r.totalRevenue()), 160);
            column(services, "Trung bình", r -> money(r.avgRevenue()), 160);

            FlowPane stats = new FlowPane(14, 14,
                    stat("Tổng lịch", data.stats.totalAppointments(), "stat-appointments"),
                    stat("Chờ xử lý", data.stats.pendingAppointments(), "stat-pending"),
                    stat("Đã xác nhận", data.stats.confirmedAppointments(), "stat-appointments"),
                    stat("Hoàn thành", data.stats.completedAppointments(), "stat-revenue"),
                    stat("Đã hủy", data.stats.cancelledAppointments(), "stat-pending"));
            content.getChildren().setAll(stats, cardWithTitle("Doanh thu theo ngày", daily),
                    cardWithTitle("Doanh thu theo dịch vụ", services));
            VBox.setVgrow(daily, Priority.ALWAYS);
            VBox.setVgrow(services, Priority.ALWAYS);
        }
    }

    private final class UsersView extends VBox {
        private final TableView<AuthRequests.UserResponse> table = new TableView<>();
        private final TextField newUsername = new TextField();
        private final PasswordField newPassword = new PasswordField();
        private final ComboBox<UserRole> newRole = new ComboBox<>(
                FXCollections.observableArrayList(UserRole.OWNER, UserRole.STAFF));

        private UsersView() {
            getStyleClass().add("page");
            getChildren().addAll(pageHeader("Tài khoản", "Quản lý người dùng và phân quyền", "Làm mới", e -> load()),
                    userForm(), card(table));
            VBox.setVgrow(table, Priority.ALWAYS);
            column(table, "ID", AuthRequests.UserResponse::id, 80);
            column(table, "Tên đăng nhập", AuthRequests.UserResponse::username, 220);
            column(table, "Vai trò", u -> u.role() == null ? "" : u.role().getDisplayName(), 180);
            load();
        }

        private Node userForm() {
            newUsername.setPromptText("Tên đăng nhập");
            newPassword.setPromptText("Mật khẩu");
            newRole.setValue(UserRole.STAFF);
            Button create = primaryButton("Tạo tài khoản");
            create.setOnAction(e -> {
                if (newUsername.getText().trim().isBlank() || newPassword.getText().isBlank()) {
                    warn("Thiếu thông tin", "Vui lòng nhập tên đăng nhập và mật khẩu.");
                    return;
                }
                if (newRole.getValue() == null) {
                    warn("Thiếu thông tin", "Vui lòng chọn vai trò.");
                    return;
                }
                runAsync(() -> ApiClient.createUser(username, newUsername.getText().trim(),
                        newPassword.getText(), newRole.getValue()), r -> {
                    newUsername.clear();
                    newPassword.clear();
                    newRole.setValue(UserRole.STAFF);
                    load();
                }, ex -> error("Lỗi tạo tài khoản", cleanError(ex)));
            });
            GridPane grid = formGrid();
            grid.addRow(0,
                    labeled("Tên đăng nhập", newUsername),
                    labeled("Mật khẩu", newPassword));
            grid.addRow(1,
                    labeled("Vai trò", newRole),
                    new Pane());
            HBox actionBox = new HBox(create);
            actionBox.setAlignment(Pos.BOTTOM_LEFT);
            grid.add(actionBox, 0, 2, 2, 1);
            VBox form = new VBox(12, sectionTitle("Tạo tài khoản"), grid);
            return card(form);
        }

        private void load() {
            runAsync(() -> ApiClient.getUsers(username), list -> table.setItems(FXCollections.observableArrayList(list)),
                    ex -> error("Lỗi tải tài khoản", cleanError(ex)));
        }
    }

    private final class AppointmentDialogFx extends Dialog<AppointmentEdit> {
        private AppointmentDialogFx(List<CustomerRequests.Response> customers, List<ServiceRequests.Response> services,
                                    List<AppointmentRequests.Response> appointments, AppointmentRequests.Response appointment) {
            setTitle(appointment == null ? "Thêm lịch hẹn" : "Cập nhật lịch hẹn");
            initOwner(stage);
            initModality(Modality.APPLICATION_MODAL);
            ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            getDialogPane().getStylesheets().add(stylesheet());

            ComboBox<CustomerRequests.Response> customer = new ComboBox<>(FXCollections.observableArrayList(customers));
            customer.setConverter(stringConverter(CustomerRequests.Response::fullName));
            customer.setMaxWidth(Double.MAX_VALUE);
            ComboBox<ServiceRequests.Response> service = new ComboBox<>(FXCollections.observableArrayList(services));
            service.setConverter(stringConverter(ServiceRequests.Response::name));
            service.setMaxWidth(Double.MAX_VALUE);
            List<ServiceRoomRequests.Response> rooms;
            try {
                rooms = ApiClient.getActiveServiceRooms();
            } catch (Exception ex) {
                rooms = List.of();
            }
            final List<ServiceRoomRequests.Response> finalRooms = rooms;
            ComboBox<ServiceRoomRequests.Response> room = new ComboBox<>(FXCollections.observableArrayList(rooms));
            room.setConverter(stringConverter(ServiceRoomRequests.Response::name));
            room.setPromptText("Tất cả phòng");
            room.setMaxWidth(Double.MAX_VALUE);
            DatePicker date = new DatePicker(LocalDate.now());
            date.setMaxWidth(Double.MAX_VALUE);
            TextField time = new TextField("09:00");
            time.setMaxWidth(Double.MAX_VALUE);
            ComboBox<String> quickTime = new ComboBox<>(FXCollections.observableArrayList(
                    "08:00", "09:00", "10:00", "13:30", "15:00", "17:30", "19:00"));
            quickTime.setPromptText("Khung giờ nhanh");
            quickTime.setMaxWidth(Double.MAX_VALUE);
            ComboBox<AppointmentStatus> status = new ComboBox<>(FXCollections.observableArrayList(AppointmentStatus.values()));
            status.setValue(AppointmentStatus.pending);
            status.setMaxWidth(Double.MAX_VALUE);
            TextArea note = new TextArea();
            note.setPrefRowCount(3);
            TilePane slotPane = new TilePane(8, 8);
            slotPane.setPrefColumns(3);
            slotPane.setPrefTileWidth(160);
            slotPane.setPrefTileHeight(54);
            ScrollPane slotScroll = new ScrollPane(slotPane);
            slotScroll.setFitToWidth(true);
            slotScroll.setPrefViewportHeight(240);
            slotScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            Label serviceSummary = new Label("Chọn dịch vụ để xem giá, thời lượng và giờ kết thúc.");
            serviceSummary.getStyleClass().add("muted");
            Label businessHint = new Label("Giờ mở cửa: 08:00 - 20:00. Hệ thống sẽ cảnh báo trùng lịch.");
            businessHint.getStyleClass().add("muted");
            room.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(ServiceRoomRequests.Response item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.name());
                    }
                }
            });
            room.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(ServiceRoomRequests.Response item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.name());
                    }
                }
            });

            quickTime.valueProperty().addListener((obs, old, value) -> {
                if (value != null) {
                    time.setText(value);
                }
            });

            if (appointment != null) {
                customer.setValue(customers.stream().filter(c -> Objects.equals(c.id(), appointment.customerId())).findFirst().orElse(null));
                Integer serviceId = appointment.serviceIds() == null || appointment.serviceIds().isEmpty() ? null
                        : appointment.serviceIds().get(0);
                service.setValue(services.stream().filter(s -> Objects.equals(s.id(), serviceId)).findFirst().orElse(null));
                room.setValue(rooms.stream().filter(r -> Objects.equals(r.id(), appointment.roomId())).findFirst().orElse(null));
                date.setValue(appointment.appointmentTime().toLocalDate());
                time.setText(appointment.appointmentTime().toLocalTime().format(TIME_INPUT));
                status.setValue(appointment.status());
                note.setText(orEmpty(appointment.note()));
            }
            Runnable updateSummary = () -> {
                ServiceRequests.Response selectedService = service.getValue();
                if (selectedService == null) {
                    serviceSummary.setText("Chọn dịch vụ để xem giá, thời lượng và giờ kết thúc.");
                    return;
                }
                String endText = "";
                try {
                    LocalTime startTime = LocalTime.parse(time.getText().trim(), TIME_INPUT);
                    endText = " | Kết thúc: " + startTime.plusMinutes(serviceDuration(selectedService)).format(TIME_INPUT);
                } catch (Exception ignored) {
                    endText = " | Kết thúc: chưa xác định";
                }
                serviceSummary.setText("Giá: " + money(selectedService.price())
                        + " | Thời lượng: " + serviceDuration(selectedService) + " phút" + endText);
            };
            service.valueProperty().addListener((obs, old, value) -> updateSummary.run());
            time.textProperty().addListener((obs, old, value) -> updateSummary.run());
            List<ScheduleRequests.AvailableSlotResponse> currentSlots = new ArrayList<>();
            final boolean[] isSettingRoomProgrammatically = {false};

            Runnable renderSlots = () -> {
                slotPane.getChildren().clear();
                ServiceRoomRequests.Response selectedRoom = room.getValue();
                Integer selectedRoomId = selectedRoom != null ? selectedRoom.id() : null;
                for (ScheduleRequests.AvailableSlotResponse slot : currentSlots) {
                    if (selectedRoomId != null && !Objects.equals(selectedRoomId, slot.roomId())) {
                        continue;
                    }
                    Button slotButton = new Button(slot.roomName() + "\n" + slot.slotTime().toLocalTime().format(TIME_INPUT));
                    slotButton.setPrefWidth(160);
                    slotButton.setPrefHeight(54);
                    slotButton.getProperties().put("slotData", slot);
                    slotButton.setDisable(!Boolean.TRUE.equals(slot.isAvailable()));
                    slotButton.setTooltip(new Tooltip(Boolean.TRUE.equals(slot.isAvailable())
                            ? "Còn trống, thời lượng " + slot.durationMinutes() + " phút"
                            : "Đã kín"));

                    slotButton.setOnAction(event -> {
                        isSettingRoomProgrammatically[0] = true;
                        time.setText(slot.slotTime().toLocalTime().format(TIME_INPUT));
                        room.setValue(finalRooms.stream()
                                .filter(r -> Objects.equals(r.id(), slot.roomId()))
                                .findFirst()
                                .orElse(null));
                        isSettingRoomProgrammatically[0] = false;

                        for (Node node : slotPane.getChildren()) {
                            if (node instanceof Button btn) {
                                ScheduleRequests.AvailableSlotResponse btnSlot = (ScheduleRequests.AvailableSlotResponse) btn.getProperties().get("slotData");
                                if (btnSlot != null) {
                                    boolean isSelected = Objects.equals(slot.roomId(), btnSlot.roomId()) &&
                                            Objects.equals(slot.slotTime().toLocalTime(), btnSlot.slotTime().toLocalTime());
                                    btn.getStyleClass().removeAll("slot-btn-available", "slot-btn-unavailable", "slot-btn-selected");
                                    if (isSelected) {
                                        btn.getStyleClass().add("slot-btn-selected");
                                    } else if (!Boolean.TRUE.equals(btnSlot.isAvailable())) {
                                        btn.getStyleClass().add("slot-btn-unavailable");
                                    } else {
                                        btn.getStyleClass().add("slot-btn-available");
                                    }
                                }
                            }
                        }
                    });
                    slotPane.getChildren().add(slotButton);
                }
                if (slotPane.getChildren().isEmpty()) {
                    slotPane.getChildren().add(new Label("Không có slot phù hợp trong ngày này."));
                }
            };

            Runnable updateSlotSelectionVisuals = () -> {
                ServiceRoomRequests.Response selectedRoom = room.getValue();
                Integer selectedRoomId = selectedRoom != null ? selectedRoom.id() : null;
                String timeText = time.getText().trim();
                LocalTime parsedTime = null;
                try {
                    parsedTime = LocalTime.parse(timeText, TIME_INPUT);
                } catch (Exception ignored) {
                }

                for (Node node : slotPane.getChildren()) {
                    if (node instanceof Button btn) {
                        ScheduleRequests.AvailableSlotResponse slot = (ScheduleRequests.AvailableSlotResponse) btn.getProperties().get("slotData");
                        if (slot == null) continue;

                        boolean isSelected = false;
                        if (selectedRoomId != null && Objects.equals(selectedRoomId, slot.roomId()) && parsedTime != null) {
                            if (Objects.equals(parsedTime, slot.slotTime().toLocalTime())) {
                                isSelected = true;
                            }
                        }

                        btn.getStyleClass().removeAll("slot-btn-available", "slot-btn-unavailable", "slot-btn-selected");
                        if (isSelected) {
                            btn.getStyleClass().add("slot-btn-selected");
                        } else if (!Boolean.TRUE.equals(slot.isAvailable())) {
                            btn.getStyleClass().add("slot-btn-unavailable");
                        } else {
                            btn.getStyleClass().add("slot-btn-available");
                        }
                    }
                }
            };

            Runnable updateRoomOptions = () -> {
                ServiceRoomRequests.Response selectedRoom = room.getValue();
                ServiceRequests.Response selectedService = service.getValue();
                LocalDate selectedDate = date.getValue();
                if (selectedRoom != null && selectedService != null && selectedDate != null) {
                    try {
                        LocalTime selectedTime = LocalTime.parse(time.getText().trim(), TIME_INPUT);
                        LocalDateTime startTime = LocalDateTime.of(selectedDate, selectedTime);
                        int duration = serviceDuration(selectedService);
                        LocalDateTime endTime = startTime.plusMinutes(duration);

                        String conflict = findAppointmentConflict(appointments, appointment, null, selectedRoom.id(), startTime, endTime, services);
                        if (conflict != null) {
                            room.setStyle("-fx-border-color: #ef4444; -fx-border-width: 1.5px;");
                            room.setTooltip(new Tooltip("Khu vực này đã có lịch hẹn khác tại khung giờ được chọn."));
                        } else {
                            room.setStyle("");
                            room.setTooltip(null);
                        }
                    } catch (Exception ex) {
                        room.setStyle("");
                        room.setTooltip(null);
                    }
                } else {
                    room.setStyle("");
                    room.setTooltip(null);
                }
            };

            Runnable refreshSlots = () -> {
                ServiceRequests.Response selectedService = service.getValue();
                if (selectedService == null || date.getValue() == null) {
                    slotPane.getChildren().setAll(new Label("Chọn dịch vụ và ngày để xem slot trống."));
                    return;
                }
                slotPane.getChildren().setAll(new Label("Đang tải slot..."));
                runAsync(() -> ApiClient.getAvailableSlots(date.getValue(), selectedService.id()), slots -> {
                    currentSlots.clear();
                    currentSlots.addAll(slots);
                    renderSlots.run();
                    updateSlotSelectionVisuals.run();
                }, ex -> slotPane.getChildren().setAll(new Label("Không tải được slot: " + cleanError(ex))));
            };

            service.valueProperty().addListener((obs, old, value) -> {
                refreshSlots.run();
                updateRoomOptions.run();
            });
            date.valueProperty().addListener((obs, old, value) -> {
                refreshSlots.run();
                updateRoomOptions.run();
            });
            room.valueProperty().addListener((obs, old, value) -> {
                if (!isSettingRoomProgrammatically[0]) {
                    renderSlots.run();
                }
                updateSlotSelectionVisuals.run();
            });
            time.textProperty().addListener((obs, old, value) -> {
                updateRoomOptions.run();
                updateSlotSelectionVisuals.run();
            });
            updateSummary.run();
            refreshSlots.run();
            updateRoomOptions.run();
            updateSlotSelectionVisuals.run();


            GridPane grid = formGrid();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.addRow(0, labeled("Khách hàng", customer), labeled("Dịch vụ", service));
            grid.addRow(1, labeled("Ngày", date), labeled("Khu vực", room));
            grid.addRow(2, labeled("Giờ", time), labeled("Gợi ý giờ", quickTime));
            grid.add(labeled("Slot trống theo phòng", slotScroll), 0, 3, 2, 1);
            grid.addRow(4, labeled("Trạng thái", status));
            grid.add(serviceSummary, 0, 5, 2, 1);
            grid.add(businessHint, 0, 6, 2, 1);
            grid.add(labeled("Ghi chú", note), 0, 7, 2, 1);
            getDialogPane().setContent(grid);
            getDialogPane().setPrefWidth(900);
            Node saveButton = getDialogPane().lookupButton(saveButtonType);
            saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (customer.getValue() == null || service.getValue() == null || room.getValue() == null || date.getValue() == null) {
                    warn("Thiếu thông tin", "Vui lòng chọn khách hàng, dịch vụ, phòng và ngày giờ.");
                    event.consume();
                    return;
                }
                try {
                    LocalTime parsedTime = LocalTime.parse(time.getText().trim(), TIME_INPUT);
                    LocalDateTime startTime = LocalDateTime.of(date.getValue(), parsedTime);
                    int duration = serviceDuration(service.getValue());
                    LocalDateTime endTime = startTime.plusMinutes(duration);
                    if (startTime.isBefore(LocalDateTime.now())) {
                        warn("Lịch hẹn chưa hợp lệ", "Không thể đặt lịch trong quá khứ.");
                        event.consume();
                        return;
                    }
                    if (parsedTime.isBefore(OPEN_TIME) || endTime.toLocalTime().isAfter(CLOSE_TIME)
                            || !endTime.toLocalDate().equals(startTime.toLocalDate())) {
                        warn("Ngoài giờ làm việc", "Lịch hẹn phải nằm trong khung 08:00 - 20:00.");
                        event.consume();
                        return;
                    }
                    String conflict = findAppointmentConflict(appointments, appointment, customer.getValue().id(),
                            room.getValue().id(), startTime, endTime, services);
                    if (conflict != null) {
                        warn("Trùng lịch", conflict);
                        event.consume();
                    }
                } catch (Exception ex) {
                    warn("Giờ chưa hợp lệ", "Vui lòng nhập giờ theo định dạng HH:mm, ví dụ 09:30.");
                    event.consume();
                }
            });
            setResultConverter(button -> {
                if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                    return null;
                }
                LocalTime localTime = LocalTime.parse(time.getText().trim(), TIME_INPUT);
                return new AppointmentEdit(customer.getValue().id(), service.getValue().id(), room.getValue().id(),
                        LocalDateTime.of(date.getValue(), localTime), status.getValue(), note.getText().trim());
            });
        }
    }

    private void configureAppointmentTable(TableView<AppointmentRow> table) {
        column(table, "ID", AppointmentRow::id, 70);
        column(table, "Khách hàng", AppointmentRow::customer, 190);
        column(table, "Dịch vụ", AppointmentRow::service, 190);
        column(table, "Phòng", AppointmentRow::room, 120);
        column(table, "Thời gian", r -> r.time().format(DATE_TIME), 150);
        column(table, "Kết thúc", r -> r.endTime().format(TIME_INPUT), 90);
        column(table, "Giá", r -> money(r.price()), 120);

        TableColumn<AppointmentRow, AppointmentStatus> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().statusEnum()));
        statusCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(AppointmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String cssClass = switch (item) {
                        case pending -> "status-pending";
                        case confirmed -> "status-confirmed";
                        case completed -> "status-completed";
                        case paid -> "status-paid";
                        case cancelled -> "status-cancelled";
                    };
                    setGraphic(createBadge(item.getDisplayName(), cssClass));
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statusCol.setPrefWidth(140);
        table.getColumns().add(statusCol);

        column(table, "Ghi chú", AppointmentRow::note, 260);
    }

    private AppointmentRow toAppointmentRow(AppointmentRequests.Response a, List<CustomerRequests.Response> customers,
                                            List<ServiceRequests.Response> services) {
        String customer = customers.stream().filter(c -> Objects.equals(c.id(), a.customerId()))
                .map(CustomerRequests.Response::fullName).findFirst().orElse("Không rõ");
        Integer serviceId = a.serviceIds() == null || a.serviceIds().isEmpty() ? null : a.serviceIds().get(0);
        ServiceRequests.Response serviceResponse = services.stream().filter(s -> Objects.equals(s.id(), serviceId))
                .findFirst().orElse(null);
        String service = serviceResponse == null ? "Không rõ" : serviceResponse.name();
        AppointmentStatus st = a.status() == null ? AppointmentStatus.pending : a.status();
        int duration = serviceDuration(serviceResponse);
        BigDecimal price = serviceResponse == null ? BigDecimal.ZERO : serviceResponse.price();
        return new AppointmentRow(a.id(), customer, service, orDash(a.roomName()), a.appointmentTime(), a.appointmentTime().plusMinutes(duration),
                price, st.getDisplayName(), st, orEmpty(a.note()));
    }

    private int serviceDuration(ServiceRequests.Response service) {
        return service == null || service.durationMinutes() == null ? 60 : service.durationMinutes();
    }

    private String findAppointmentConflict(List<AppointmentRequests.Response> appointments,
                                           AppointmentRequests.Response currentAppointment, Integer customerId, Integer roomId,
                                           LocalDateTime startTime, LocalDateTime endTime, List<ServiceRequests.Response> services) {
        for (AppointmentRequests.Response existing : appointments) {
            if (currentAppointment != null && Objects.equals(existing.id(), currentAppointment.id())) {
                continue;
            }
            if (existing.status() == AppointmentStatus.cancelled || existing.status() == AppointmentStatus.paid) {
                continue;
            }
            Integer existingServiceId = existing.serviceIds() == null || existing.serviceIds().isEmpty()
                    ? null
                    : existing.serviceIds().get(0);
            ServiceRequests.Response existingService = services.stream()
                    .filter(service -> Objects.equals(service.id(), existingServiceId))
                    .findFirst()
                    .orElse(null);
            LocalDateTime existingStart = existing.appointmentTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(serviceDuration(existingService));
            if (!overlaps(startTime, endTime, existingStart, existingEnd)) {
                continue;
            }
            if (Objects.equals(existing.customerId(), customerId)) {
                return "Khách hàng này đã có lịch trong khung giờ "
                        + existingStart.format(DATE_TIME) + " - " + existingEnd.toLocalTime().format(TIME_INPUT) + ".";
            }
            if (Objects.equals(existing.roomId(), roomId)) {
                return "Dịch vụ này đang có lịch khác trong khung giờ "
                        + existingStart.format(DATE_TIME) + " - " + existingEnd.toLocalTime().format(TIME_INPUT) + ".";
            }
        }
        return null;
    }

    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    private Node pageHeader(String title, String subtitle, String actionText, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Label h1 = title(title, "page-title");
        Label sub = new Label(subtitle);
        sub.getStyleClass().add("muted");
        VBox copy = new VBox(4, h1, sub);
        Button button = primaryButton(actionText);
        button.setOnAction(action);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(16, copy, spacer, button);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private Node card(Node content) {
        StackPane card = new StackPane(content);
        card.getStyleClass().add("card");
        return card;
    }

    private Node cardWithTitle(String title, Node content) {
        return card(new VBox(10, sectionTitle(title), content));
    }

    private Label sectionTitle(String text) {
        return title(text, "section-title");
    }

    private Label title(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }

    private Label createBadge(String text, String cssClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("status-badge", cssClass);
        return label;
    }

    private Node stat(String label, Object value) {
        return stat(label, value, "stat-default");
    }

    private Node stat(String label, Object value, String cssClass) {
        VBox box = new VBox(6, new Label(label), new Label(String.valueOf(value == null ? "-" : value)));
        box.getStyleClass().addAll("stat-card", cssClass);
        box.getChildren().get(0).getStyleClass().add("muted");
        box.getChildren().get(1).getStyleClass().add("stat-value");
        return box;
    }

    private Node labeled(String label, Node input) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        if (input instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        GridPane.setHgrow(input, Priority.ALWAYS);
        VBox box = new VBox(6, l, input);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private HBox actions(Button... buttons) {
        HBox box = new HBox(10, buttons);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);
        left.setFillWidth(true);
        left.setMinWidth(0);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);
        right.setFillWidth(true);
        right.setMinWidth(0);
        grid.getColumnConstraints().addAll(left, right);
        return grid;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    private Button dangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("danger-button");
        return button;
    }

    private Button ghostButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("ghost-button");
        return button;
    }

    private ProgressIndicator progress() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(42, 42);
        return progress;
    }

    private <S, T> void column(TableView<S> table, String title, java.util.function.Function<S, T> getter, int width) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue())));
        col.setPrefWidth(width);
        table.getColumns().add(col);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private <S> void column(TableView<S> table, String title, java.util.function.Function<S, String> getter, int width,
                            boolean stringColumn) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        col.setPrefWidth(width);
        table.getColumns().add(col);
    }

    private <T> void runAsync(Callable<T> call, Consumer<T> success, Consumer<Throwable> failure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return call.call();
            }
        };
        task.setOnSucceeded(e -> success.accept(task.getValue()));
        task.setOnFailed(e -> failure.accept(task.getException()));
        Thread thread = new Thread(task, "salon-fx-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void setScene(Node root, int width, int height) {
        Scene scene = new Scene((javafx.scene.Parent) root, width, height);
        scene.getStylesheets().add(stylesheet());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private String stylesheet() {
        return Objects.requireNonNull(getClass().getResource("/salon-fx.css")).toExternalForm();
    }

    private void warn(String title, String text) {
        alert(Alert.AlertType.WARNING, title, text);
    }

    private void error(String title, String text) {
        alert(Alert.AlertType.ERROR, title, text);
    }

    private void alert(Alert.AlertType type, String title, String text) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type, text, ButtonType.OK);
            alert.initOwner(stage);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.getDialogPane().getStylesheets().add(stylesheet());
            alert.showAndWait();
        });
    }

    private boolean confirm(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.CANCEL, ButtonType.OK);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.getDialogPane().getStylesheets().add(stylesheet());
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private boolean showPaymentQrSimulation(Integer appointmentId, String customerName, String serviceName,
                                            BigDecimal amount, String methodLabel) {
        ButtonType paidButtonType = new ButtonType("Đã thanh toán", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Thanh toán mô phỏng");

        Label title = new Label("Quét QR để thanh toán");
        title.getStyleClass().add("section-title");

        String payload = "SALON|APT=" + appointmentId + "|AMOUNT=" + amount + "|METHOD=" + methodLabel;
        javafx.scene.canvas.Canvas qrCanvas = new javafx.scene.canvas.Canvas(220, 220);
        drawFakeQr(qrCanvas, payload);
        StackPane qrHolder = new StackPane(qrCanvas);
        qrHolder.setMinSize(244, 244);
        qrHolder.setPrefSize(244, 244);
        qrHolder.setMaxSize(244, 244);
        qrHolder.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 8px; -fx-padding: 12px;");

        Label summary = new Label("""
                Khách hàng: %s
                Dịch vụ: %s
                Số tiền: %s
                Phương thức: %s

                Đây là thanh toán mô phỏng. Hệ thống sẽ tự xác nhận sau 2 giây.
                """.formatted(orDash(customerName), orDash(serviceName), money(amount), methodLabel));
        summary.setWrapText(true);
        summary.getStyleClass().add("muted");

        ProgressBar progress = new ProgressBar();
        progress.setPrefWidth(300);
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        Label status = new Label("Đang chờ thanh toán...");
        status.getStyleClass().add("muted");

        VBox content = new VBox(12, title, qrHolder, summary, progress, status);
        content.setAlignment(Pos.CENTER);
        content.setMinWidth(340);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(stylesheet());
        pane.setContent(content);
        pane.getButtonTypes().addAll(cancelButtonType, paidButtonType);
        Button paidButton = (Button) pane.lookupButton(paidButtonType);
        paidButton.setDisable(true);

        javafx.animation.PauseTransition autoSuccess =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        autoSuccess.setOnFinished(e -> {
            progress.setProgress(1);
            status.setText("Thanh toán thành công");
            paidButton.setDisable(false);
            dialog.setResult(paidButtonType);
            dialog.close();
        });
        autoSuccess.play();

        Optional<ButtonType> result = dialog.showAndWait();
        autoSuccess.stop();
        return result.orElse(cancelButtonType) == paidButtonType;
    }

    private void drawFakeQr(javafx.scene.canvas.Canvas canvas, String payload) {
        final int cells = 29;
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double size = Math.min(width, height) - 24;
        double cell = Math.floor(size / cells);
        double startX = (width - cell * cells) / 2;
        double startY = (height - cell * cells) / 2;

        javafx.scene.canvas.GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setFill(javafx.scene.paint.Color.BLACK);
        drawQrFinder(g, startX, startY, cell);
        drawQrFinder(g, startX + cell * 22, startY, cell);
        drawQrFinder(g, startX, startY + cell * 22, cell);

        int hash = payload.hashCode();
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                if (isQrFinderCell(x, y)) {
                    continue;
                }
                int bit = Integer.rotateLeft(hash ^ (x * 73856093) ^ (y * 19349663), (x + y) % 16);
                if ((bit & 0x3) == 0) {
                    g.fillRect(startX + x * cell, startY + y * cell, cell, cell);
                }
            }
        }
    }

    private void drawQrFinder(javafx.scene.canvas.GraphicsContext g, double x, double y, double cell) {
        g.setFill(javafx.scene.paint.Color.BLACK);
        g.fillRect(x, y, cell * 7, cell * 7);
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(x + cell, y + cell, cell * 5, cell * 5);
        g.setFill(javafx.scene.paint.Color.BLACK);
        g.fillRect(x + cell * 2, y + cell * 2, cell * 3, cell * 3);
    }

    private boolean isQrFinderCell(int x, int y) {
        return (x < 8 && y < 8) || (x > 20 && y < 8) || (x < 8 && y > 20);
    }

    private String money(BigDecimal value) {
        return CURRENCY.format(value == null ? BigDecimal.ZERO : value);
    }

    private String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String customerTier(Integer points) {
        int value = points == null ? 0 : points;
        if (value >= 100) {
            return "VIP";
        }
        if (value >= 50) {
            return "Thân thiết";
        }
        return "Mới";
    }

    private String cleanError(Throwable ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getSimpleName() : message;
    }

    private <T> javafx.util.StringConverter<T> stringConverter(java.util.function.Function<T, String> labeler) {
        return new javafx.util.StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : labeler.apply(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    private record AppointmentRow(Integer id, String customer, String service, String room, LocalDateTime time,
                                  LocalDateTime endTime, BigDecimal price, String status, AppointmentStatus statusEnum,
                                  String note) {
    }

    private record AppointmentEdit(Integer customerId, Integer serviceId, Integer roomId, LocalDateTime time,
                                   AppointmentStatus status,
                                   String note) {
    }

    private record DashboardData(DashboardRequests.DashboardResponse dashboard,
                                 DashboardRequests.QuickStatsResponse quickStats,
                                 List<CustomerRequests.Response> customers,
                                 List<ServiceRequests.Response> services,
                                 List<AppointmentRequests.Response> appointments) {
    }

    private record ReportData(List<ReportRequests.DailyRevenueResponse> daily,
                              List<ReportRequests.ServiceRevenueResponse> services,
                              List<ReportRequests.PaymentMethodResponse> payments,
                              ReportRequests.AppointmentStatsResponse stats) {
    }
}

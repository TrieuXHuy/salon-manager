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
import javafx.geometry.Insets;
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
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_INPUT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

    private Stage stage;
    private BorderPane shell;
    private StackPane workspace;
    private String username;
    private UserRole role;
    private CustomerRequests.Response customerProfile;
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
                afterAuthenticated();
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

    private void afterAuthenticated() {
        if (role != UserRole.CUSTOMER) {
            customerProfile = null;
            showShell();
            return;
        }
        runAsync(() -> ApiClient.getCustomerProfile(username), profile -> {
            customerProfile = profile;
            if (isProfileComplete(profile)) {
                showShell();
            } else {
                showProfileCompletion(profile);
            }
        }, ex -> {
            customerProfile = null;
            showProfileCompletion(null);
        });
    }

    private boolean isProfileComplete(CustomerRequests.Response profile) {
        return profile != null && Boolean.TRUE.equals(profile.profileComplete());
    }

    private void showProfileCompletion(CustomerRequests.Response profile) {
        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(560);

        Label title = new Label("Hoàn tất hồ sơ");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Vui lòng bổ sung thông tin để đặt lịch hẹn.");
        subtitle.getStyleClass().add("muted");

        TextField fullName = new TextField(orEmpty(profile == null ? null : profile.fullName()));
        TextField phone = new TextField(orEmpty(profile == null ? null : profile.phone()));
        TextField email = new TextField(orEmpty(profile == null ? null : profile.email()));
        TextArea note = new TextArea(orEmpty(profile == null ? null : profile.note()));
        ComboBox<Gender> gender = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        gender.setConverter(stringConverter(Gender::getDisplayName));
        gender.setValue(profile == null || profile.gender() == null ? Gender.other : profile.gender());
        note.setPrefRowCount(3);

        Button save = primaryButton("Lưu hồ sơ");
        Button logout = ghostButton("Đăng xuất");
        HBox actions = new HBox(10, save, logout);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Label status = new Label();
        status.getStyleClass().add("muted");

        GridPane grid = formGrid();
        grid.addRow(0, labeled("Họ và tên", fullName), labeled("Số điện thoại", phone));
        grid.addRow(1, labeled("Email", email), labeled("Giới tính", gender));
        grid.add(labeled("Ghi chú", note), 0, 2, 2, 1);
        card.getChildren().addAll(title, subtitle, grid, actions, status);

        save.setOnAction(e -> {
            if (fullName.getText().trim().isBlank() || phone.getText().trim().isBlank()) {
                warn("Thiếu thông tin", "Vui lòng nhập họ tên và số điện thoại.");
                return;
            }
            save.setDisable(true);
            logout.setDisable(true);
            status.setText("Đang lưu hồ sơ...");
            CustomerRequests.CompleteProfile request = new CustomerRequests.CompleteProfile(username,
                    fullName.getText().trim(), phone.getText().trim(), email.getText().trim(), gender.getValue(),
                    note.getText().trim());
            runAsync(() -> ApiClient.completeCustomerProfile(request), saved -> {
                customerProfile = saved;
                showShell();
            }, ex -> {
                save.setDisable(false);
                logout.setDisable(false);
                status.setText("");
                error("Lỗi lưu hồ sơ", cleanError(ex));
            });
        });

        logout.setOnAction(e -> {
            username = null;
            role = null;
            customerProfile = null;
            showLogin();
        });

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
        navigate(role == UserRole.OWNER ? "dashboard" : "appointments");
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
        if (role == UserRole.OWNER) {
            addNav(menu, "Tổng quan", "dashboard");
        }
        if (role == UserRole.OWNER) {
            addNav(menu, "Tài khoản", "users");
        }
        if (role == UserRole.OWNER || role == UserRole.STAFF) {
            addNav(menu, "Khách hàng", "customers");
        }
        if (role == UserRole.CUSTOMER) {
            addNav(menu, "Tài khoản", "account");
        }
        addNav(menu, "Lịch hẹn", "appointments");
        if (role == UserRole.OWNER) {
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
        }, ignored -> {
            customerProfile = null;
            showLogin();
        }, ex -> {
            customerProfile = null;
            showLogin();
        }));

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
            case "account" -> "👤  ";
            case "users" -> "⚙️  ";
            case "customers" -> "👥  ";
            case "appointments" -> "📅  ";
            case "services" -> "💇  ";
            case "rooms" -> "🚪  ";
            case "reports" -> "📈  ";
            default -> "";
        };
        Button button = new Button(label);
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
            case "customers" -> new CustomersViewV2();
            case "appointments" -> new AppointmentsViewV2();
            case "services" -> new ServicesViewV2();
            case "rooms" -> new RoomsView();
            case "reports" -> new ReportsView();
            case "account" -> new AccountView();
            case "users" -> new UsersViewV2();
            default -> new DashboardView();
        };
        workspace.getChildren().setAll(view);
    }

    private final class DashboardView extends VBox {
        private final FlowPane stats = new FlowPane(14, 14);
        private final TableView<AppointmentRow> table = new TableView<>();
        private final Label status = new Label();

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
                long confirmedCount = data.appointments.stream()
                        .map(AppointmentRequests.Response::status)
                        .filter(Objects::nonNull)
                        .filter(status -> status == AppointmentStatus.confirmed)
                        .count();
                stats.getChildren().setAll(
                        stat("Khách hàng", data.dashboard.totalCustomers(), "stat-customers"),
                        stat("Lịch hẹn hôm nay", data.dashboard.totalAppointmentsToday(), "stat-appointments"),
                        stat("Chờ đặt cọc", data.dashboard.pendingAppointments(), "stat-pending"),
                        stat("Đã giữ chỗ", confirmedCount, "stat-revenue"),
                        stat("Đã thu hôm nay", money(data.dashboard.todayRevenue()), "stat-revenue"),
                        stat("Đã thu tháng", money(data.dashboard.monthlyRevenue()), "stat-revenue"),
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

    private final class AccountView extends VBox {
        private final TextField fullName = new TextField();
        private final TextField phone = new TextField();
        private final TextField email = new TextField();
        private final ComboBox<Gender> gender = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        private final TextArea note = new TextArea();
        private final Label loyalty = new Label("-");
        private final Label status = new Label("Sẵn sàng");

        private AccountView() {
            getStyleClass().add("page");
            gender.setConverter(stringConverter(Gender::getDisplayName));
            note.setPrefRowCount(4);
            status.getStyleClass().add("muted");

            Button save = primaryButton("Lưu thay đổi");
            save.setOnAction(e -> saveProfile());

            GridPane grid = formGrid();
            grid.addRow(0, labeled("Tên đăng nhập", readOnlyText(username)), labeled("Điểm tích lũy", loyalty));
            grid.addRow(1, labeled("Họ và tên", fullName), labeled("Số điện thoại", phone));
            grid.addRow(2, labeled("Email", email), labeled("Giới tính", gender));
            grid.add(labeled("Ghi chú", note), 0, 3, 2, 1);

            HBox actions = new HBox(10, save);
            actions.setAlignment(Pos.CENTER_LEFT);

            getChildren().addAll(
                    pageHeader("Tài khoản", "Cập nhật thông tin cá nhân của bạn", "Làm mới", e -> loadProfile()),
                    card(new VBox(16, grid, actions)),
                    status);
            loadProfile();
        }

        private void loadProfile() {
            status.setText("Đang tải tài khoản...");
            runAsync(() -> ApiClient.getCustomerProfile(username), profile -> {
                customerProfile = profile;
                fill(profile);
                status.setText("");
            }, ex -> {
                status.setText("Không tải được tài khoản");
                error("Lỗi tải tài khoản", cleanError(ex));
            });
        }

        private void fill(CustomerRequests.Response profile) {
            fullName.setText(orEmpty(profile == null ? null : profile.fullName()));
            phone.setText(orEmpty(profile == null ? null : profile.phone()));
            email.setText(orEmpty(profile == null ? null : profile.email()));
            gender.setValue(profile == null || profile.gender() == null ? Gender.other : profile.gender());
            note.setText(orEmpty(profile == null ? null : profile.note()));
            loyalty.setText(String.valueOf(profile == null || profile.loyaltyPoints() == null ? 0 : profile.loyaltyPoints()));
        }

        private void saveProfile() {
            if (fullName.getText().trim().isBlank() || phone.getText().trim().isBlank()) {
                warn("Thiếu thông tin", "Vui lòng nhập họ tên và số điện thoại.");
                return;
            }
            status.setText("Đang lưu tài khoản...");
            CustomerRequests.CompleteProfile request = new CustomerRequests.CompleteProfile(
                    username,
                    fullName.getText().trim(),
                    phone.getText().trim(),
                    email.getText().trim(),
                    gender.getValue(),
                    note.getText().trim());
            runAsync(() -> ApiClient.completeCustomerProfile(request), saved -> {
                customerProfile = saved;
                fill(saved);
                status.setText("Đã lưu thay đổi");
            }, ex -> {
                status.setText("Lưu thất bại");
                error("Lỗi lưu tài khoản", cleanError(ex));
            });
        }

        private TextField readOnlyText(String value) {
            TextField field = new TextField(orEmpty(value));
            field.setEditable(false);
            field.setFocusTraversable(false);
            return field;
        }
    }

    private final class CustomersViewV2 extends VBox {
        private int pageSize = 6;

        private final TableView<CustomerRequests.Response> table = new TableView<>();
        private final TextField customerSearch = new TextField();
        private final ComboBox<String> tierFilter = new ComboBox<>();
        private final Label summaryLabel = new Label("0 khách hàng");
        private final FlowPane pageButtons = new FlowPane(6, 6);
        private final Button createButton = primaryButton("Thêm khách hàng");
        private final Button editButton = secondaryButton("Cập nhật khách hàng");
        private final Button deleteButton = dangerButton("Xóa khách hàng");
        private final Button clearButton = ghostButton("Xóa form / bỏ chọn");
        private List<CustomerRequests.Response> allCustomers = List.of();
        private List<CustomerRequests.Response> filteredCustomers = List.of();
        private int currentPage = 0;
        private BorderPane tableCard;

        private CustomersViewV2() {
            getStyleClass().add("page");
            pageButtons.getStyleClass().add("page-buttons");

            VBox top = new VBox(20,
                    pageHeader("Khách hàng", "Quản lý hồ sơ và thông tin liên hệ", "Làm mới", e -> load()),
                    customerFilters());
            Node tableSection = tableCard();
            Node actionSection = actionsBar();

            BorderPane layout = new BorderPane();
            layout.setTop(top);
            layout.setCenter(tableSection);
            layout.setBottom(actionSection);
            BorderPane.setMargin(top, new Insets(0, 0, 12, 0));
            BorderPane.setMargin(tableSection, new Insets(0, 0, 12, 0));
            layout.setMaxWidth(Double.MAX_VALUE);
            layout.setMaxHeight(Double.MAX_VALUE);
            BorderPane.setAlignment(actionSection, Pos.BOTTOM_LEFT);
            VBox.setVgrow(layout, Priority.ALWAYS);
            getChildren().add(layout);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);

            configureCustomerTable();
            load();
        }

        private Node customerFilters() {
            customerSearch.setPromptText("Tìm theo tên, điện thoại hoặc email");
            tierFilter.getItems().setAll("Tất cả hạng", "VIP", "Thân thiết", "Mới");
            tierFilter.setValue("Tất cả hạng");
            tierFilter.setPromptText("Tất cả hạng");
            tierFilter.setMaxWidth(180);

            Button clearFilters = secondaryButton("Xóa lọc");
            clearFilters.setOnAction(e -> {
                customerSearch.clear();
                tierFilter.setValue("Tất cả hạng");
                applyFilters();
            });

            customerSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
            tierFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

            HBox row = new HBox(10, customerSearch);
            if (role == UserRole.OWNER) {
                row.getChildren().add(tierFilter);
            }
            row.getChildren().add(clearFilters);
            row.getStyleClass().add("filter-bar");
            HBox.setHgrow(customerSearch, Priority.ALWAYS);
            return card(row);
        }

        private Node tableCard() {
            VBox box = new VBox(table, paginationBar());
            box.setFillWidth(true);
            box.setAlignment(Pos.TOP_CENTER);
            VBox.setVgrow(table, Priority.ALWAYS);
            table.setMaxHeight(Double.MAX_VALUE);

            tableCard = new BorderPane();
            tableCard.getStyleClass().addAll("card", "table-card");
            tableCard.setCenter(box);
            tableCard.setMaxWidth(Double.MAX_VALUE);
            tableCard.setMaxHeight(Double.MAX_VALUE);
            tableCard.heightProperty().addListener((obs, oldHeight, newHeight) -> recalculatePageSize(newHeight.doubleValue()));
            return tableCard;
        }

        private Node paginationBar() {
            BorderPane bar = new BorderPane();
            summaryLabel.getStyleClass().add("table-summary");
            bar.setLeft(summaryLabel);
            bar.setCenter(pageButtons);
            BorderPane.setAlignment(summaryLabel, Pos.CENTER_LEFT);
            BorderPane.setAlignment(pageButtons, Pos.CENTER);
            bar.getStyleClass().add("table-footer");
            return bar;
        }

        private Node actionsBar() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox row = new HBox(10, createButton, editButton);
            if (role == UserRole.OWNER) {
                row.getChildren().add(deleteButton);
            }
            row.getChildren().addAll(clearButton, spacer);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);

            createButton.setOnAction(e -> openCreateDialog());
            editButton.setOnAction(e -> openEditDialog(selectedCustomer()));
            deleteButton.setOnAction(e -> deleteSelected());
            clearButton.setOnAction(e -> clearSelection());
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

            return card(row);
        }

        private void configureCustomerTable() {
            column(table, "ID", CustomerRequests.Response::id, 70);
            column(table, "Họ và tên", CustomerRequests.Response::fullName, 220);
            column(table, "Điện thoại", CustomerRequests.Response::phone, 150);
            column(table, "Email", CustomerRequests.Response::email, 240);
            column(table, "Giới tính", c -> c.gender() == null ? "" : c.gender().getDisplayName(), 120);
            if (role == UserRole.OWNER) {
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
            }

            column(table, "Ghi chú", CustomerRequests.Response::note, 220);
            table.setPlaceholder(new Label("Không có khách hàng phù hợp"));
            table.setFixedCellSize(44);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }

        private void load() {
            runAsync(ApiClient::getAllCustomers, list -> {
                        allCustomers = list == null ? List.of() : list.stream()
                                .sorted(Comparator.comparing(CustomerRequests.Response::id, Comparator.nullsLast(Integer::compareTo)))
                                .toList();
                        currentPage = 0;
                        applyFilters();
                    },
                    ex -> error("Lỗi tải khách hàng", cleanError(ex)));
        }

        private void applyFilters() {
            String keyword = customerSearch.getText() == null ? "" : customerSearch.getText().trim().toLowerCase(Locale.ROOT);
            String tier = tierFilter.getValue();
            filteredCustomers = allCustomers.stream()
                    .filter(customer -> keyword.isBlank()
                            || orEmpty(customer.fullName()).toLowerCase(Locale.ROOT).contains(keyword)
                            || orEmpty(customer.phone()).toLowerCase(Locale.ROOT).contains(keyword)
                            || orEmpty(customer.email()).toLowerCase(Locale.ROOT).contains(keyword))
                    .filter(customer -> tier == null
                            || "Tất cả hạng".equals(tier)
                            || customerTier(customer.loyaltyPoints()).equals(tier))
                    .toList();

            int totalPages = Math.max(1, (int) Math.ceil(filteredCustomers.size() / (double) pageSize));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            if (currentPage < 0) {
                currentPage = 0;
            }
            refreshTablePage();
        }

        private void refreshTablePage() {
            int totalPages = Math.max(1, (int) Math.ceil(filteredCustomers.size() / (double) pageSize));
            int fromIndex = Math.min(currentPage * pageSize, filteredCustomers.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredCustomers.size());
            List<CustomerRequests.Response> pageItems = filteredCustomers.subList(fromIndex, toIndex);
            table.setItems(FXCollections.observableArrayList(pageItems));
            summaryLabel.setText(filteredCustomers.size() + " khách hàng");
            rebuildPageButtons(totalPages);
        }

        private void goToPage(int page) {
            int totalPages = Math.max(1, (int) Math.ceil(filteredCustomers.size() / (double) pageSize));
            currentPage = Math.max(0, Math.min(page, totalPages - 1));
            refreshTablePage();
        }

        private void recalculatePageSize(double cardHeight) {
            double footerHeight = 52;
            double padding = 24;
            double headerHeight = 44;
            double rowHeight = table.getFixedCellSize() > 0 ? table.getFixedCellSize() : 44;
            int newPageSize = Math.max(1, (int) Math.floor(
                    (cardHeight - footerHeight - padding - headerHeight) / rowHeight
            ));
            if (newPageSize != pageSize) {
                pageSize = newPageSize;
                currentPage = 0;
                applyFilters();
            }
        }

        private void rebuildPageButtons(int totalPages) {
            pageButtons.getChildren().clear();
            pageButtons.setVisible(totalPages > 1);
            pageButtons.setManaged(totalPages > 1);

            pageButtons.getChildren().add(pageJumpButton("«", 0, currentPage == 0));
            pageButtons.getChildren().add(pageJumpButton("‹", Math.max(0, currentPage - 1), currentPage == 0));

            int visibleWindow = 2;
            int start = Math.max(0, currentPage - visibleWindow);
            int end = Math.min(totalPages - 1, currentPage + visibleWindow);

            if (start > 0) {
                addPageButton(0);
                if (start > 1) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
            }

            for (int i = start; i <= end; i++) {
                addPageButton(i);
            }

            if (end < totalPages - 1) {
                if (end < totalPages - 2) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
                addPageButton(totalPages - 1);
            }

            pageButtons.getChildren().add(pageJumpButton("›", Math.min(totalPages - 1, currentPage + 1), currentPage >= totalPages - 1));
            pageButtons.getChildren().add(pageJumpButton("»", totalPages - 1, currentPage >= totalPages - 1));
        }

        private void addPageButton(int pageIndex) {
            Button pageButton = secondaryButton(String.valueOf(pageIndex + 1));
            pageButton.getStyleClass().add("page-number");
            if (pageIndex == currentPage) {
                pageButton.getStyleClass().add("selected-page");
                pageButton.setDisable(true);
            }
            pageButton.setOnAction(e -> goToPage(pageIndex));
            pageButtons.getChildren().add(pageButton);
        }

        private Label pageEllipsis() {
            Label ellipsis = new Label("...");
            ellipsis.getStyleClass().add("page-ellipsis");
            return ellipsis;
        }

        private Button pageJumpButton(String text, int targetPage, boolean disabled) {
            Button button = secondaryButton(text);
            button.getStyleClass().add("page-jump");
            button.setDisable(disabled);
            button.setOnAction(e -> goToPage(targetPage));
            return button;
        }

        private CustomerRequests.Response selectedCustomer() {
            return table.getSelectionModel().getSelectedItem();
        }

        private void openCreateDialog() {
            showCustomerDialog("Thêm khách hàng", null, false);
        }

        private void openEditDialog(CustomerRequests.Response customer) {
            if (customer == null) {
                warn("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để cập nhật.");
                return;
            }
            showCustomerDialog("Cập nhật khách hàng", customer, true);
        }

        private void showCustomerDialog(String title, CustomerRequests.Response customer, boolean editing) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);

            ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(stylesheet());

            TextField fullNameField = new TextField();
            TextField phoneField = new TextField();
            TextField emailField = new TextField();
            ComboBox<Gender> genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
            TextField pointsField = new TextField();
            TextArea noteField = new TextArea();

            fullNameField.setPromptText("Họ và tên");
            phoneField.setPromptText("Số điện thoại");
            emailField.setPromptText("Email");
            genderBox.setConverter(stringConverter(Gender::getDisplayName));
            genderBox.setPromptText("Giới tính");
            pointsField.setPromptText("Điểm tích lũy");
            noteField.setPromptText("Ghi chú chăm sóc, sở thích, lưu ý...");
            noteField.setPrefRowCount(3);
            noteField.setWrapText(true);

            if (customer != null) {
                fullNameField.setText(orEmpty(customer.fullName()));
                phoneField.setText(orEmpty(customer.phone()));
                emailField.setText(orEmpty(customer.email()));
                genderBox.setValue(customer.gender() == null ? Gender.other : customer.gender());
                pointsField.setText(String.valueOf(customer.loyaltyPoints() == null ? 0 : customer.loyaltyPoints()));
                noteField.setText(orEmpty(customer.note()));
            } else {
                genderBox.setValue(Gender.other);
                pointsField.setText("0");
            }

            pointsField.setDisable(!editing || role != UserRole.OWNER);

            Label hint = new Label(role == UserRole.OWNER
                    ? (editing ? "Điểm tích lũy có thể chỉnh trong chế độ cập nhật."
                    : "Khách hàng mới sẽ được tạo với điểm mặc định 0.")
                    : "Nhân viên chỉ cập nhật thông tin liên hệ và ghi chú chăm sóc.");
            hint.getStyleClass().add("muted");

            GridPane grid = formGrid();
            grid.addRow(0, labeled("Họ và tên", fullNameField), labeled("Số điện thoại", phoneField));
            grid.addRow(1, labeled("Email", emailField), labeled("Giới tính", genderBox));
            if (role == UserRole.OWNER) {
                grid.addRow(2, labeled("Điểm tích lũy", pointsField), new Pane());
                grid.add(labeled("Ghi chú", noteField), 0, 3, 2, 1);
            } else {
                grid.add(labeled("Ghi chú", noteField), 0, 2, 2, 1);
            }

            VBox content = new VBox(12, sectionTitle(title), hint, grid);
            dialog.getDialogPane().setContent(content);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != saveType) {
                return;
            }

            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            Gender selectedGender = genderBox.getValue();
            String note = noteField.getText().trim();

            if (fullName.isBlank() || phone.isBlank() || email.isBlank()) {
                warn("Thiếu thông tin", "Vui lòng nhập đầy đủ họ tên, điện thoại và email.");
                return;
            }

            Integer points = customer == null || customer.loyaltyPoints() == null ? 0 : customer.loyaltyPoints();
            if (editing && role == UserRole.OWNER) {
                try {
                    points = pointsField.getText().trim().isBlank() ? 0 : Integer.parseInt(pointsField.getText().trim());
                } catch (NumberFormatException ex) {
                    warn("Điểm chưa hợp lệ", "Điểm tích lũy phải là số nguyên.");
                    return;
                }
            }

            CustomerRequests.Create createReq = new CustomerRequests.Create(fullName, phone, email, selectedGender, note);
            CustomerRequests.Update updateReq = new CustomerRequests.Update(fullName, phone, email, selectedGender, points, note);

            if (editing && customer != null) {
                runAsync(() -> ApiClient.updateCustomer(customer.id(), updateReq), saved -> {
                    clearSelection();
                    load();
                }, ex -> error("Lỗi cập nhật khách hàng", cleanError(ex)));
            } else {
                runAsync(() -> ApiClient.createCustomer(createReq), saved -> load(),
                        ex -> error("Lỗi lưu khách hàng", cleanError(ex)));
            }
        }

        private void deleteSelected() {
            CustomerRequests.Response customer = selectedCustomer();
            if (customer == null) {
                warn("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để xóa.");
                return;
            }
            if (!confirm("Xóa khách hàng", "Bạn chắc chắn muốn xóa khách hàng này?")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteCustomer(customer.id());
                return null;
            }, r -> {
                clearSelection();
                load();
            }, ex -> error("Lỗi xóa khách hàng", cleanError(ex)));
        }

        private void clearSelection() {
            table.getSelectionModel().clearSelection();
        }
    }

    private final class ServicesViewV2 extends VBox {
        private int pageSize = 6;

        private final TableView<ServiceRequests.Response> table = new TableView<>();
        private final TextField serviceSearch = new TextField();
        private final ComboBox<String> statusFilter = new ComboBox<>();
        private final Label summaryLabel = new Label("0 dịch vụ");
        private final FlowPane pageButtons = new FlowPane(6, 6);

        private final Button createButton = primaryButton("Thêm dịch vụ");
        private final Button editButton = secondaryButton("Cập nhật dịch vụ");
        private final Button deleteButton = dangerButton("Xóa dịch vụ");
        private final Button clearButton = ghostButton("Bỏ chọn");

        private List<ServiceRequests.Response> allServices = List.of();
        private List<ServiceRequests.Response> filteredServices = List.of();
        private int currentPage = 0;

        private ServicesViewV2() {
            getStyleClass().add("page");
            pageButtons.getStyleClass().add("page-buttons");

            VBox top = new VBox(20,
                    pageHeader("Dịch vụ", "Cập nhật danh mục, thời lượng và bảng giá", "Làm mới", e -> load()),
                    filters());

            Node tableSection = tableCard();
            Node actionSection = actionsBar();

            BorderPane layout = new BorderPane();
            layout.setTop(top);
            layout.setCenter(tableSection);
            layout.setBottom(actionSection);
            BorderPane.setMargin(top, new Insets(0, 0, 12, 0));
            BorderPane.setMargin(tableSection, new Insets(0, 0, 12, 0));
            layout.setMaxWidth(Double.MAX_VALUE);
            layout.setMaxHeight(Double.MAX_VALUE);
            BorderPane.setAlignment(actionSection, Pos.BOTTOM_LEFT);
            VBox.setVgrow(layout, Priority.ALWAYS);
            getChildren().add(layout);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);

            configureServiceTable();
            load();
        }

        private Node filters() {
            serviceSearch.setPromptText("Tìm theo tên dịch vụ hoặc mô tả");

            statusFilter.getItems().setAll("Tất cả trạng thái", "Hoạt động", "Không hoạt động");
            statusFilter.setValue("Tất cả trạng thái");
            statusFilter.setPromptText("Tất cả trạng thái");
            statusFilter.setMaxWidth(180);

            Button clear = secondaryButton("Xóa lọc");
            clear.setOnAction(e -> {
                serviceSearch.clear();
                statusFilter.setValue("Tất cả trạng thái");
                applyFilters();
            });

            serviceSearch.textProperty().addListener((obs, oldValue, newValue) -> {
                currentPage = 0;
                applyFilters();
            });
            statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
                currentPage = 0;
                applyFilters();
            });

            HBox row = new HBox(10, serviceSearch, statusFilter, clear);
            row.getStyleClass().add("filter-bar");
            HBox.setHgrow(serviceSearch, Priority.ALWAYS);
            return card(row);
        }

        private Node tableCard() {
            VBox box = new VBox(table, paginationBar());
            box.setFillWidth(true);
            box.setAlignment(Pos.TOP_CENTER);

            VBox.setVgrow(table, Priority.ALWAYS);
            table.setMaxHeight(Double.MAX_VALUE);

            BorderPane tableCard = new BorderPane();
            tableCard.getStyleClass().addAll("card", "table-card");
            tableCard.setCenter(box);
            tableCard.setMaxWidth(Double.MAX_VALUE);
            tableCard.setMaxHeight(Double.MAX_VALUE);

            tableCard.heightProperty().addListener((obs, oldHeight, newHeight) ->
                    recalculatePageSize(newHeight.doubleValue()));

            return tableCard;
        }

        private Node paginationBar() {
            BorderPane bar = new BorderPane();
            summaryLabel.getStyleClass().add("table-summary");
            bar.setLeft(summaryLabel);
            bar.setCenter(pageButtons);
            BorderPane.setAlignment(summaryLabel, Pos.CENTER_LEFT);
            BorderPane.setAlignment(pageButtons, Pos.CENTER);
            bar.getStyleClass().add("table-footer");
            return bar;
        }

        private Node actionsBar() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(10, createButton, editButton, deleteButton, clearButton, spacer);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);

            createButton.setOnAction(e -> openCreateDialog());
            editButton.setOnAction(e -> openEditDialog(selectedService()));
            deleteButton.setOnAction(e -> deleteSelected());
            clearButton.setOnAction(e -> table.getSelectionModel().clearSelection());

            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            clearButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

            return card(row);
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
                        setGraphic(createBadge(item ? "Hoạt động" : "Không hoạt động",
                                item ? "status-active" : "status-inactive"));
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            statusCol.setPrefWidth(130);
            table.getColumns().add(statusCol);

            column(table, "Mô tả", ServiceRequests.Response::description, 300);

            table.setPlaceholder(new Label("Không có dịch vụ phù hợp"));
            table.setFixedCellSize(44);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }

        private void load() {
            runAsync(ApiClient::getAllServices, list -> {
                allServices = list == null ? List.of() : list.stream()
                        .sorted(Comparator.comparing(ServiceRequests.Response::id, Comparator.nullsLast(Integer::compareTo)))
                        .toList();
                currentPage = 0;
                applyFilters();
            }, ex -> error("Lỗi tải dịch vụ", cleanError(ex)));
        }

        private void applyFilters() {
            String keyword = serviceSearch.getText() == null ? "" : serviceSearch.getText().trim().toLowerCase(Locale.ROOT);
            String selectedStatus = statusFilter.getValue();

            filteredServices = allServices.stream()
                    .filter(service -> keyword.isBlank()
                            || orEmpty(service.name()).toLowerCase(Locale.ROOT).contains(keyword)
                            || orEmpty(service.description()).toLowerCase(Locale.ROOT).contains(keyword))
                    .filter(service -> selectedStatus == null
                            || "Tất cả trạng thái".equals(selectedStatus)
                            || ("Hoạt động".equals(selectedStatus) && Boolean.TRUE.equals(service.isActive()))
                            || ("Không hoạt động".equals(selectedStatus) && !Boolean.TRUE.equals(service.isActive())))
                    .toList();

            int totalPages = Math.max(1, (int) Math.ceil(filteredServices.size() / (double) pageSize));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            if (currentPage < 0) {
                currentPage = 0;
            }
            refreshTablePage();
        }

        private void refreshTablePage() {
            int totalPages = Math.max(1, (int) Math.ceil(filteredServices.size() / (double) pageSize));
            int fromIndex = Math.min(currentPage * pageSize, filteredServices.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredServices.size());
            List<ServiceRequests.Response> pageItems = filteredServices.subList(fromIndex, toIndex);
            table.setItems(FXCollections.observableArrayList(pageItems));
            summaryLabel.setText(filteredServices.size() + " dịch vụ");
            rebuildPageButtons(totalPages);
        }

        private void goToPage(int page) {
            int totalPages = Math.max(1, (int) Math.ceil(filteredServices.size() / (double) pageSize));
            currentPage = Math.max(0, Math.min(page, totalPages - 1));
            refreshTablePage();
        }

        private void recalculatePageSize(double cardHeight) {
            double footerHeight = 52;
            double padding = 24;
            double headerHeight = 44;
            double rowHeight = table.getFixedCellSize() > 0 ? table.getFixedCellSize() : 44;
            int newPageSize = Math.max(1, (int) Math.floor(
                    (cardHeight - footerHeight - padding - headerHeight) / rowHeight
            ));
            if (newPageSize != pageSize) {
                pageSize = newPageSize;
                currentPage = 0;
                applyFilters();
            }
        }

        private void rebuildPageButtons(int totalPages) {
            pageButtons.getChildren().clear();
            pageButtons.setVisible(totalPages > 1);
            pageButtons.setManaged(totalPages > 1);

            pageButtons.getChildren().add(pageJumpButton("«", 0, currentPage == 0));
            pageButtons.getChildren().add(pageJumpButton("‹", Math.max(0, currentPage - 1), currentPage == 0));

            int visibleWindow = 2;
            int start = Math.max(0, currentPage - visibleWindow);
            int end = Math.min(totalPages - 1, currentPage + visibleWindow);

            for (int i = start; i <= end; i++) {
                addPageButton(i);
            }

            pageButtons.getChildren().add(pageJumpButton("›", Math.min(totalPages - 1, currentPage + 1), currentPage >= totalPages - 1));
            pageButtons.getChildren().add(pageJumpButton("»", totalPages - 1, currentPage >= totalPages - 1));
        }

        private void addPageButton(int pageIndex) {
            Button pageButton = secondaryButton(String.valueOf(pageIndex + 1));
            pageButton.getStyleClass().add("page-number");
            if (pageIndex == currentPage) {
                pageButton.getStyleClass().add("selected-page");
                pageButton.setDisable(true);
            }
            pageButton.setOnAction(e -> goToPage(pageIndex));
            pageButtons.getChildren().add(pageButton);
        }

        private Button pageJumpButton(String text, int targetPage, boolean disabled) {
            Button button = secondaryButton(text);
            button.getStyleClass().add("page-jump");
            button.setDisable(disabled);
            button.setOnAction(e -> goToPage(targetPage));
            return button;
        }

        private ServiceRequests.Response selectedService() {
            return table.getSelectionModel().getSelectedItem();
        }

        private void openCreateDialog() {
            showServiceDialog("Thêm dịch vụ", null, false);
        }

        private void openEditDialog(ServiceRequests.Response service) {
            if (service == null) {
                warn("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ để cập nhật.");
                return;
            }
            showServiceDialog("Cập nhật dịch vụ", service, true);
        }

        private void showServiceDialog(String title, ServiceRequests.Response service, boolean editing) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);

            ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(stylesheet());

            TextField nameField = new TextField();
            TextField priceField = new TextField();
            TextField durationField = new TextField();
            TextArea descriptionField = new TextArea();
            CheckBox activeBox = new CheckBox("Đang hoạt động");

            nameField.setPromptText("Tên dịch vụ");
            priceField.setPromptText("Giá (VND)");
            durationField.setPromptText("Thời lượng (phút)");
            descriptionField.setPromptText("Mô tả");
            descriptionField.setPrefRowCount(3);
            descriptionField.setWrapText(true);

            if (service != null) {
                nameField.setText(orEmpty(service.name()));
                priceField.setText(service.price() == null ? "" : service.price().toPlainString());
                durationField.setText(service.durationMinutes() == null ? "" : service.durationMinutes().toString());
                descriptionField.setText(orEmpty(service.description()));
                activeBox.setSelected(Boolean.TRUE.equals(service.isActive()));
            } else {
                activeBox.setSelected(true);
            }

            GridPane grid = formGrid();
            grid.addRow(0, labeled("Tên dịch vụ", nameField), labeled("Giá (VND)", priceField));
            grid.addRow(1, labeled("Thời lượng (phút)", durationField), activeBox);
            grid.add(labeled("Mô tả", descriptionField), 0, 2, 2, 1);

            VBox content = new VBox(12, sectionTitle(title), grid);
            dialog.getDialogPane().setContent(content);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != saveType) return;

            saveService(
                    editing,
                    service == null ? null : service.id(),
                    nameField.getText(),
                    priceField.getText(),
                    durationField.getText(),
                    descriptionField.getText(),
                    activeBox.isSelected()
            );
        }

        private void saveService(
                boolean update,
                Integer serviceId,
                String nameValue,
                String priceValue,
                String durationValue,
                String descriptionValue,
                boolean activeValue
        ) {
            if (nameValue == null || nameValue.trim().isBlank()) {
                warn("Thiếu thông tin", "Tên dịch vụ là bắt buộc.");
                return;
            }

            if (update && serviceId == null) {
                warn("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ để cập nhật.");
                return;
            }

            try {
                BigDecimal amount = new BigDecimal(priceValue.trim());
                Integer minutes = Integer.parseInt(durationValue.trim());

                ServiceRequests.Create create = new ServiceRequests.Create(
                        nameValue.trim(),
                        amount,
                        minutes,
                        descriptionValue == null ? "" : descriptionValue.trim(),
                        activeValue
                );

                ServiceRequests.Update edit = new ServiceRequests.Update(
                        create.name(),
                        create.price(),
                        create.durationMinutes(),
                        create.description(),
                        create.isActive()
                );

                runAsync(() -> update
                                ? ApiClient.updateService(serviceId, edit)
                                : ApiClient.createService(create),
                        r -> load(),
                        ex -> error("Lỗi lưu dịch vụ", cleanError(ex)));

            } catch (NumberFormatException ex) {
                warn("Dữ liệu chưa hợp lệ", "Giá và thời lượng phải là số hợp lệ.");
            }
        }

        private void deleteSelected() {
            ServiceRequests.Response service = selectedService();

            if (service == null) {
                warn("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ để xóa.");
                return;
            }

            if (!confirm("Xóa dịch vụ", "Bạn chắc chắn muốn xóa dịch vụ \"" + service.name() + "\" không?")) {
                return;
            }

            runAsync(() -> {
                ApiClient.deleteService(service.id());
                return null;
            }, r -> load(), ex -> error("Lỗi xóa dịch vụ", cleanError(ex)));
        }
    }

    private final class RoomsView extends VBox {
        private int pageSize = 6;

        private final TableView<ServiceRoomRequests.Response> table = new TableView<>();
        private final TextField roomSearch = new TextField();
        private final ComboBox<String> statusFilter = new ComboBox<>();
        private final Label summaryLabel = new Label("0 khu vực");
        private final FlowPane pageButtons = new FlowPane(6, 6);
        private final Button createButton = primaryButton("Thêm khu vực");
        private final Button editButton = secondaryButton("Cập nhật khu vực");
        private final Button deleteButton = dangerButton("Xóa khu vực");
        private final Button clearButton = ghostButton("Bỏ chọn");
        private List<ServiceRoomRequests.Response> allRooms = List.of();
        private List<ServiceRoomRequests.Response> filteredRooms = List.of();
        private int currentPage = 0;

        private RoomsView() {
            getStyleClass().add("page");
            pageButtons.getStyleClass().add("page-buttons");

            VBox top = new VBox(20,
                    pageHeader("Khu vực", "Quản lý khu/phòng dùng để chia slot đặt lịch", "Làm mới", e -> load()),
                    filters());

            Node tableSection = tableCard();
            Node actionSection = actionsBar();

            BorderPane layout = new BorderPane();
            layout.setTop(top);
            layout.setCenter(tableSection);
            layout.setBottom(actionSection);
            BorderPane.setMargin(top, new Insets(0, 0, 12, 0));
            BorderPane.setMargin(tableSection, new Insets(0, 0, 12, 0));
            layout.setMaxWidth(Double.MAX_VALUE);
            layout.setMaxHeight(Double.MAX_VALUE);
            BorderPane.setAlignment(actionSection, Pos.BOTTOM_LEFT);
            VBox.setVgrow(layout, Priority.ALWAYS);
            getChildren().add(layout);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);

            configureRoomTable();
            load();
        }

        private Node filters() {
            roomSearch.setPromptText("Tìm theo tên phòng/khu hoặc mô tả");

            statusFilter.getItems().setAll("Tất cả trạng thái", "Hoạt động", "Không hoạt động");
            statusFilter.setValue("Tất cả trạng thái");
            statusFilter.setPromptText("Tất cả trạng thái");
            statusFilter.setMaxWidth(180);

            Button clear = secondaryButton("Xóa lọc");
            clear.setOnAction(e -> {
                roomSearch.clear();
                statusFilter.setValue("Tất cả trạng thái");
                applyFilters();
            });

            roomSearch.textProperty().addListener((obs, oldValue, newValue) -> {
                currentPage = 0;
                applyFilters();
            });
            statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
                currentPage = 0;
                applyFilters();
            });

            HBox row = new HBox(10, roomSearch, statusFilter, clear);
            row.getStyleClass().add("filter-bar");
            HBox.setHgrow(roomSearch, Priority.ALWAYS);
            return card(row);
        }

        private Node tableCard() {
            VBox box = new VBox(table, paginationBar());
            box.setFillWidth(true);
            box.setAlignment(Pos.TOP_CENTER);
            VBox.setVgrow(table, Priority.ALWAYS);
            table.setMaxHeight(Double.MAX_VALUE);

            BorderPane card = new BorderPane();
            card.getStyleClass().addAll("card", "table-card");
            card.setCenter(box);
            card.setMaxWidth(Double.MAX_VALUE);
            card.setMaxHeight(Double.MAX_VALUE);
            card.heightProperty().addListener((obs, oldHeight, newHeight) ->
                    recalculatePageSize(newHeight.doubleValue()));
            return card;
        }

        private Node paginationBar() {
            BorderPane bar = new BorderPane();
            summaryLabel.getStyleClass().add("table-summary");
            bar.setLeft(summaryLabel);
            bar.setCenter(pageButtons);
            BorderPane.setAlignment(summaryLabel, Pos.CENTER_LEFT);
            BorderPane.setAlignment(pageButtons, Pos.CENTER);
            bar.getStyleClass().add("table-footer");
            return bar;
        }

        private Node actionsBar() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(10, createButton, editButton, deleteButton, clearButton, spacer);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);

            createButton.setOnAction(e -> openCreateDialog());
            editButton.setOnAction(e -> openEditDialog(selectedRoom()));
            deleteButton.setOnAction(e -> deleteSelected());
            clearButton.setOnAction(e -> table.getSelectionModel().clearSelection());

            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            clearButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

            return card(row);
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
                        String labelText = item ? "Hoạt động" : "Không hoạt động";
                        String cssClass = item ? "status-active" : "status-inactive";
                        setGraphic(createBadge(labelText, cssClass));
                        setAlignment(Pos.CENTER);
                    }
                }
            });
            statusCol.setPrefWidth(120);
            table.getColumns().add(statusCol);

            column(table, "Mô tả", ServiceRoomRequests.Response::description, 420);

            table.setPlaceholder(new Label("Không có khu vực phù hợp"));
            table.setFixedCellSize(44);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }

        private void load() {
            runAsync(ApiClient::getAllServiceRooms, list -> {
                allRooms = list == null ? List.of() : list.stream()
                        .sorted(Comparator.comparing(ServiceRoomRequests.Response::id, Comparator.nullsLast(Integer::compareTo)))
                        .toList();
                currentPage = 0;
                applyFilters();
            },
                    ex -> error("Lỗi tải phòng phục vụ", cleanError(ex)));
        }

        private void applyFilters() {
            String keyword = roomSearch.getText() == null ? "" : roomSearch.getText().trim().toLowerCase(Locale.ROOT);
            String selectedStatus = statusFilter.getValue();

            filteredRooms = allRooms.stream()
                    .filter(room -> keyword.isBlank()
                            || orEmpty(room.name()).toLowerCase(Locale.ROOT).contains(keyword)
                            || orEmpty(room.description()).toLowerCase(Locale.ROOT).contains(keyword))
                    .filter(room -> selectedStatus == null
                            || "Tất cả trạng thái".equals(selectedStatus)
                            || ("Hoạt động".equals(selectedStatus) && Boolean.TRUE.equals(room.isActive()))
                            || ("Không hoạt động".equals(selectedStatus) && !Boolean.TRUE.equals(room.isActive())))
                    .toList();

            int totalPages = Math.max(1, (int) Math.ceil(filteredRooms.size() / (double) pageSize));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            if (currentPage < 0) {
                currentPage = 0;
            }
            refreshTablePage();
        }

        private void refreshTablePage() {
            int totalPages = Math.max(1, (int) Math.ceil(filteredRooms.size() / (double) pageSize));
            int fromIndex = Math.min(currentPage * pageSize, filteredRooms.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredRooms.size());
            List<ServiceRoomRequests.Response> pageItems = filteredRooms.subList(fromIndex, toIndex);
            table.setItems(FXCollections.observableArrayList(pageItems));
            summaryLabel.setText(filteredRooms.size() + " khu vực");
            rebuildPageButtons(totalPages);
        }

        private void goToPage(int page) {
            int totalPages = Math.max(1, (int) Math.ceil(filteredRooms.size() / (double) pageSize));
            currentPage = Math.max(0, Math.min(page, totalPages - 1));
            refreshTablePage();
        }

        private void recalculatePageSize(double cardHeight) {
            double footerHeight = 52;
            double padding = 24;
            double headerHeight = 44;
            double rowHeight = table.getFixedCellSize() > 0 ? table.getFixedCellSize() : 44;
            int newPageSize = Math.max(1, (int) Math.floor(
                    (cardHeight - footerHeight - padding - headerHeight) / rowHeight
            ));
            if (newPageSize != pageSize) {
                pageSize = newPageSize;
                currentPage = 0;
                applyFilters();
            }
        }

        private void rebuildPageButtons(int totalPages) {
            pageButtons.getChildren().clear();
            pageButtons.setVisible(totalPages > 1);
            pageButtons.setManaged(totalPages > 1);

            pageButtons.getChildren().add(pageJumpButton("«", 0, currentPage == 0));
            pageButtons.getChildren().add(pageJumpButton("‹", Math.max(0, currentPage - 1), currentPage == 0));

            int visibleWindow = 2;
            int start = Math.max(0, currentPage - visibleWindow);
            int end = Math.min(totalPages - 1, currentPage + visibleWindow);

            if (start > 0) {
                addPageButton(0);
                if (start > 1) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
            }

            for (int i = start; i <= end; i++) {
                addPageButton(i);
            }

            if (end < totalPages - 1) {
                if (end < totalPages - 2) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
                addPageButton(totalPages - 1);
            }

            pageButtons.getChildren().add(pageJumpButton("›", Math.min(totalPages - 1, currentPage + 1), currentPage >= totalPages - 1));
            pageButtons.getChildren().add(pageJumpButton("»", totalPages - 1, currentPage >= totalPages - 1));
        }

        private void addPageButton(int pageIndex) {
            Button pageButton = secondaryButton(String.valueOf(pageIndex + 1));
            pageButton.getStyleClass().add("page-number");
            if (pageIndex == currentPage) {
                pageButton.getStyleClass().add("selected-page");
                pageButton.setDisable(true);
            }
            pageButton.setOnAction(e -> goToPage(pageIndex));
            pageButtons.getChildren().add(pageButton);
        }

        private Label pageEllipsis() {
            Label ellipsis = new Label("...");
            ellipsis.getStyleClass().add("page-ellipsis");
            return ellipsis;
        }

        private Button pageJumpButton(String text, int targetPage, boolean disabled) {
            Button button = secondaryButton(text);
            button.getStyleClass().add("page-jump");
            button.setDisable(disabled);
            button.setOnAction(e -> goToPage(targetPage));
            return button;
        }

        private ServiceRoomRequests.Response selectedRoom() {
            return table.getSelectionModel().getSelectedItem();
        }

        private void openCreateDialog() {
            showRoomDialog("Thêm khu vực", null, false);
        }

        private void openEditDialog(ServiceRoomRequests.Response room) {
            if (room == null) {
                warn("Chưa chọn phòng", "Vui lòng chọn một phòng để cập nhật.");
                return;
            }
            showRoomDialog("Cập nhật khu vực", room, true);
        }

        private void showRoomDialog(String title, ServiceRoomRequests.Response room, boolean editing) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);

            ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(stylesheet());

            TextField nameField = new TextField();
            TextArea descriptionField = new TextArea();
            CheckBox activeBox = new CheckBox("Đang hoạt động");

            nameField.setPromptText("Ví dụ: Phòng 1, Khu VIP, Ghế gội 2");
            descriptionField.setPromptText("Mô tả ngắn về vị trí hoặc mục đích sử dụng");
            descriptionField.setPrefRowCount(3);
            descriptionField.setWrapText(true);

            if (room != null) {
                nameField.setText(orEmpty(room.name()));
                descriptionField.setText(orEmpty(room.description()));
                activeBox.setSelected(Boolean.TRUE.equals(room.isActive()));
            } else {
                activeBox.setSelected(true);
            }

            GridPane grid = formGrid();
            grid.addRow(0, labeled("Tên phòng/khu", nameField), activeBox);
            grid.add(labeled("Mô tả", descriptionField), 0, 1, 2, 1);

            VBox content = new VBox(12, sectionTitle(title), grid);
            dialog.getDialogPane().setContent(content);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != saveType) {
                return;
            }

            saveRoom(
                    editing,
                    room == null ? null : room.id(),
                    nameField.getText(),
                    descriptionField.getText(),
                    activeBox.isSelected()
            );
        }

        private void saveRoom(boolean update, Integer roomId, String nameValue, String descriptionValue, boolean activeValue) {
            if (nameValue == null || nameValue.trim().isBlank()) {
                warn("Thiếu thông tin", "Tên phòng/khu là bắt buộc.");
                return;
            }
            if (update && roomId == null) {
                warn("Chưa chọn phòng", "Vui lòng chọn một phòng để cập nhật.");
                return;
            }
            ServiceRoomRequests.Create create = new ServiceRoomRequests.Create(
                    nameValue.trim(),
                    descriptionValue == null ? "" : descriptionValue.trim(),
                    activeValue);
            ServiceRoomRequests.Update edit = new ServiceRoomRequests.Update(
                    create.name(),
                    create.description(),
                    create.isActive());
            runAsync(() -> update ? ApiClient.updateServiceRoom(roomId, edit) : ApiClient.createServiceRoom(create),
                    r -> load(),
                    ex -> error("Lỗi lưu phòng phục vụ", cleanError(ex)));
        }

        private void deleteSelected() {
            ServiceRoomRequests.Response room = selectedRoom();
            if (room == null) {
                warn("Chưa chọn phòng", "Vui lòng chọn một phòng để xóa.");
                return;
            }
            if (!confirm("Xóa phòng phục vụ", "Bạn chắc chắn muốn xóa phòng \"" + room.name() + "\" không? Lịch hẹn cũ có thể đang tham chiếu phòng này.")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteServiceRoom(room.id());
                return null;
            }, r -> load(), ex -> error("Lỗi xóa phòng phục vụ", cleanError(ex)));
        }
    }

    private final class AppointmentsViewV2 extends VBox {
        private int pageSize = 6;

        private final TableView<AppointmentRow> table = new TableView<>();
        private final FlowPane quickStats = new FlowPane(14, 14);
        private final TextField search = new TextField();
        private final ComboBox<AppointmentStatus> statusFilter = new ComboBox<>();
        private final DatePicker start = new DatePicker();
        private final DatePicker end = new DatePicker();
        private final Label status = new Label("Sẵn sàng");
        private final Label summaryLabel = new Label("0 lịch hẹn");
        private final FlowPane pageButtons = new FlowPane(6, 6);
        private List<CustomerRequests.Response> customers = List.of();
        private List<ServiceRequests.Response> services = List.of();
        private List<AppointmentRequests.Response> appointments = List.of();
        private List<AppointmentRow> filteredRows = List.of();
        private int currentPage = 0;
        private BorderPane tableCard;
        private HBox actionRow;
        private Region actionSpacer;
        private Button addButton;
        private Button editButton;
        private Button cancelButton;
        private Button startServiceButton;
        private Button finishServiceButton;
        private Button customerDepositButton;
        private Button balanceButton;
        private Button remindButton;

        private AppointmentsViewV2() {
            getStyleClass().add("page");
            pageButtons.getStyleClass().add("page-buttons");

            VBox top = new VBox(20,
                    pageHeader("Lịch hẹn", "Tạo, lọc, cập nhật và thanh toán lịch hẹn", "Làm mới", e -> load()),
                    quickStats,
                    filters());

            Node tableSection = tableCard();
            Node actionSection = appointmentToolbar();

            BorderPane layout = new BorderPane();
            layout.setTop(top);
            layout.setCenter(tableSection);
            layout.setBottom(actionSection);
            BorderPane.setMargin(top, new Insets(0, 0, 12, 0));
            BorderPane.setMargin(tableSection, new Insets(0, 0, 12, 0));
            layout.setMaxWidth(Double.MAX_VALUE);
            layout.setMaxHeight(Double.MAX_VALUE);
            BorderPane.setAlignment(actionSection, Pos.BOTTOM_LEFT);
            VBox.setVgrow(layout, Priority.ALWAYS);
            getChildren().add(layout);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);

            configureAppointmentTable(table);
            table.setFixedCellSize(64);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("Không có lịch hẹn phù hợp"));
            load();
        }

        private Node filters() {
            search.setPromptText("Tìm khách hàng hoặc dịch vụ");
            statusFilter.getItems().setAll(AppointmentStatus.values());
            statusFilter.setConverter(stringConverter(AppointmentStatus::getDisplayName));
            statusFilter.setPromptText("Tất cả trạng thái");

            Button today = secondaryButton("Hôm nay");
            today.setTooltip(new Tooltip("Lọc nhanh lịch hẹn trong ngày hôm nay"));
            today.setOnAction(e -> {
                LocalDate now = LocalDate.now();
                start.setValue(now);
                end.setValue(now);
                currentPage = 0;
                applyFilters();
            });

            Button clear = secondaryButton("Xóa lọc");
            clear.setOnAction(e -> {
                search.clear();
                statusFilter.setValue(null);
                start.setValue(null);
                end.setValue(null);
                currentPage = 0;
                applyFilters();
            });

            search.textProperty().addListener((obs, o, n) -> {
                currentPage = 0;
                applyFilters();
            });
            statusFilter.valueProperty().addListener((obs, o, n) -> {
                currentPage = 0;
                applyFilters();
            });
            start.valueProperty().addListener((obs, o, n) -> {
                currentPage = 0;
                applyFilters();
            });
            end.valueProperty().addListener((obs, o, n) -> {
                currentPage = 0;
                applyFilters();
            });

            HBox row = new HBox(10, search, statusFilter, start, end, today, clear);
            row.getStyleClass().add("filter-bar");
            HBox.setHgrow(search, Priority.ALWAYS);
            return card(row);
        }

        private Node tableCard() {
            VBox box = new VBox(table, paginationBar());
            box.setFillWidth(true);
            box.setAlignment(Pos.TOP_CENTER);
            VBox.setVgrow(table, Priority.ALWAYS);
            table.setMaxHeight(Double.MAX_VALUE);

            tableCard = new BorderPane();
            tableCard.getStyleClass().addAll("card", "table-card");
            tableCard.setCenter(box);
            tableCard.setMaxWidth(Double.MAX_VALUE);
            tableCard.setMaxHeight(Double.MAX_VALUE);
            tableCard.heightProperty().addListener((obs, oldH, newH) -> recalculatePageSize(newH.doubleValue()));
            return tableCard;
        }

        private Node paginationBar() {
            BorderPane bar = new BorderPane();
            summaryLabel.getStyleClass().add("table-summary");
            bar.setLeft(summaryLabel);
            bar.setCenter(pageButtons);
            BorderPane.setAlignment(summaryLabel, Pos.CENTER_LEFT);
            BorderPane.setAlignment(pageButtons, Pos.CENTER);
            bar.getStyleClass().add("table-footer");
            return bar;
        }

        private Node appointmentToolbar() {
            addButton = primaryButton(role == UserRole.CUSTOMER ? "Đặt lịch" : "Thêm");
            editButton = secondaryButton("Sửa");
            cancelButton = dangerButton("Hủy lịch");
            startServiceButton = secondaryButton("Bắt đầu phục vụ");
            finishServiceButton = secondaryButton("Kết thúc dịch vụ");
            customerDepositButton = secondaryButton("Đặt cọc");
            balanceButton = secondaryButton(role == UserRole.CUSTOMER ? "Thu còn lại" : "Hoàn thành");
            remindButton = secondaryButton("Nhắc lịch");
            addButton.setOnAction(e -> openAppointmentDialog(null));
            editButton.setOnAction(e -> {
                AppointmentRow row = table.getSelectionModel().getSelectedItem();
                if (row == null) {
                    warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để sửa.");
                    return;
                }
                openAppointmentDialog(findAppointment(row.id()));
            });
            cancelButton.setOnAction(e -> cancelSelected());
            startServiceButton.setOnAction(e -> updateSelectedStatus(AppointmentStatus.in_progress));
            finishServiceButton.setOnAction(e -> updateSelectedStatus(AppointmentStatus.awaiting_payment));
            customerDepositButton.setOnAction(e -> paySelected(PaymentStage.deposit));
            balanceButton.setOnAction(e -> paySelected(PaymentStage.balance));
            remindButton.setOnAction(e -> remindSelected());
            table.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> refreshAppointmentActions());
            status.getStyleClass().add("muted");
            actionSpacer = new Region();
            HBox.setHgrow(actionSpacer, Priority.ALWAYS);
            actionRow = new HBox(10);
            refreshAppointmentActions();
            return card(actionRow);
        }

        private void load() {
            status.setText("Đang tải dữ liệu...");
            runAsync(() -> {
                if (role == UserRole.CUSTOMER) {
                    if (!isProfileComplete(customerProfile)) {
                        customerProfile = ApiClient.getCustomerProfile(username);
                    }
                    customers = List.of(customerProfile);
                    appointments = ApiClient.getMyAppointments(username).stream()
                            .sorted(Comparator.comparing(AppointmentRequests.Response::appointmentTime)
                                    .thenComparing(AppointmentRequests.Response::id))
                            .toList();
                } else {
                    customers = ApiClient.getAllCustomers();
                    appointments = ApiClient.getAllAppointments().stream()
                            .sorted(Comparator.comparing(AppointmentRequests.Response::appointmentTime)
                                    .thenComparing(AppointmentRequests.Response::id))
                            .toList();
                }
                services = ApiClient.getAllServices();
                return appointments;
            }, list -> {
                currentPage = 0;
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
            filteredRows = appointments.stream()
                    .map(a -> toAppointmentRow(a, customers, services))
                    .filter(r -> keyword.isBlank() || r.customer().toLowerCase(Locale.ROOT).contains(keyword)
                            || r.service().toLowerCase(Locale.ROOT).contains(keyword))
                    .filter(r -> selectedStatus == null || r.statusEnum() == selectedStatus)
                    .filter(r -> from == null || !r.time().toLocalDate().isBefore(from))
                    .filter(r -> to == null || !r.time().toLocalDate().isAfter(to))
                    .sorted(Comparator.comparing(AppointmentRow::time).thenComparing(AppointmentRow::id))
                    .toList();
            renderQuickStats(filteredRows);

            int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) pageSize));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            if (currentPage < 0) {
                currentPage = 0;
            }
            refreshTablePage();
            status.setText("Sẵn sàng - " + filteredRows.size() + "/" + appointments.size() + " lịch hẹn");
        }

        private void refreshTablePage() {
            int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) pageSize));
            int fromIndex = Math.min(currentPage * pageSize, filteredRows.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredRows.size());
            table.setItems(FXCollections.observableArrayList(filteredRows.subList(fromIndex, toIndex)));
            summaryLabel.setText(filteredRows.size() + " lịch hẹn");
            rebuildPageButtons(totalPages);
            refreshAppointmentActions();
        }

        private void recalculatePageSize(double cardHeight) {
            double footerHeight = 52;
            double padding = 24;
            double headerHeight = 44;
            double rowHeight = table.getFixedCellSize() > 0 ? table.getFixedCellSize() : 44;
            int newPageSize = Math.max(1, (int) Math.floor(
                    (cardHeight - footerHeight - padding - headerHeight) / rowHeight
            ));
            if (newPageSize != pageSize) {
                pageSize = newPageSize;
                currentPage = 0;
                applyFilters();
            }
        }

        private void goToPage(int page) {
            int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) pageSize));
            currentPage = Math.max(0, Math.min(page, totalPages - 1));
            refreshTablePage();
        }

        private void rebuildPageButtons(int totalPages) {
            pageButtons.getChildren().clear();
            pageButtons.setVisible(totalPages > 1);
            pageButtons.setManaged(totalPages > 1);

            pageButtons.getChildren().add(pageJumpButton("«", 0, currentPage == 0));
            pageButtons.getChildren().add(pageJumpButton("‹", Math.max(0, currentPage - 1), currentPage == 0));

            int visibleWindow = 2;
            int startPage = Math.max(0, currentPage - visibleWindow);
            int endPage = Math.min(totalPages - 1, currentPage + visibleWindow);

            if (startPage > 0) {
                addPageButton(0);
                if (startPage > 1) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
            }

            for (int i = startPage; i <= endPage; i++) {
                addPageButton(i);
            }

            if (endPage < totalPages - 1) {
                if (endPage < totalPages - 2) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
                addPageButton(totalPages - 1);
            }

            pageButtons.getChildren().add(pageJumpButton("›", Math.min(totalPages - 1, currentPage + 1), currentPage >= totalPages - 1));
            pageButtons.getChildren().add(pageJumpButton("»", totalPages - 1, currentPage >= totalPages - 1));
        }

        private void addPageButton(int pageIndex) {
            Button pageButton = secondaryButton(String.valueOf(pageIndex + 1));
            pageButton.getStyleClass().add("page-number");
            if (pageIndex == currentPage) {
                pageButton.getStyleClass().add("selected-page");
                pageButton.setDisable(true);
            }
            pageButton.setOnAction(e -> goToPage(pageIndex));
            pageButtons.getChildren().add(pageButton);
        }

        private Label pageEllipsis() {
            Label ellipsis = new Label("...");
            ellipsis.getStyleClass().add("page-ellipsis");
            return ellipsis;
        }

        private Button pageJumpButton(String text, int targetPage, boolean disabled) {
            Button button = secondaryButton(text);
            button.getStyleClass().add("page-jump");
            button.setDisable(disabled);
            button.setOnAction(e -> goToPage(targetPage));
            return button;
        }

        private void openAppointmentDialog(AppointmentRequests.Response appointment) {
            AppointmentEdit result = new AppointmentDialogFx(customers, services, appointments, appointment,
                    role == UserRole.CUSTOMER).showAndWait().orElse(null);
            if (result == null) {
                return;
            }
            Integer customerId = role == UserRole.CUSTOMER && customerProfile != null ? customerProfile.id() : result.customerId();
            if (appointment == null) {
                runAsync(() -> ApiClient.createAppointment(new AppointmentRequests.Create(customerId,
                                List.of(result.serviceId()), result.roomId(), result.time(), result.status(), result.note())), r -> load(),
                        ex -> error("Lỗi tạo lịch hẹn", cleanError(ex)));
            } else {
                runAsync(() -> ApiClient.updateAppointment(appointment.id(), new AppointmentRequests.Update(customerId,
                                List.of(result.serviceId()), result.roomId(), result.time(), result.status(), result.note())), r -> load(),
                        ex -> error("Lỗi cập nhật lịch hẹn", cleanError(ex)));
            }
        }

        private void renderQuickStats(List<AppointmentRow> rows) {
            LocalDate today = LocalDate.now();
            long todayCount = rows.stream().filter(row -> row.time().toLocalDate().equals(today)).count();
            long pendingCount = rows.stream().filter(row -> row.statusEnum() == AppointmentStatus.pending).count();
            long confirmedCount = rows.stream().filter(row -> row.statusEnum() == AppointmentStatus.confirmed).count();
            BigDecimal visibleRevenue = rows.stream()
                    .map(AppointmentRow::amountPaid)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            quickStats.getChildren().setAll(
                    stat("Hôm nay", todayCount + " lịch", "stat-appointments"),
                    stat("Chờ đặt cọc", pendingCount + " lịch", "stat-pending"),
                    stat("Đã giữ chỗ", confirmedCount + " lịch", "stat-revenue"),
                    stat("Đã thu", money(visibleRevenue), "stat-revenue"));
        }

        private AppointmentRequests.Response findAppointment(Integer id) {
            return appointments.stream().filter(a -> Objects.equals(a.id(), id)).findFirst().orElse(null);
        }

        private void refreshAppointmentActions() {
            if (actionRow == null) {
                return;
            }
            editButton.setText("Sửa");
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            AppointmentRequests.Response appointment = row == null ? null : findAppointment(row.id());
            actionRow.getChildren().clear();
            if (appointment == null) {
                actionRow.getChildren().add(addButton);
            } else if (role == UserRole.CUSTOMER) {
                addCustomerActions(appointment);
            } else {
                addAdminActions(appointment);
            }
            actionRow.getChildren().addAll(actionSpacer, status);
        }

        private void addCustomerActions(AppointmentRequests.Response appointment) {
            AppointmentStatus currentStatus = appointment.status() == null ? AppointmentStatus.pending : appointment.status();
            switch (currentStatus) {
                case pending -> actionRow.getChildren().addAll(editButton, cancelButton, customerDepositButton);
                case awaiting_payment -> actionRow.getChildren().addAll(balanceButton);
                default -> {
                    // Customer chỉ được thao tác thanh toán ở 2 trạng thái này.
                }
            }
        }

        private void addAdminActions(AppointmentRequests.Response appointment) {
            AppointmentStatus currentStatus = appointment.status() == null ? AppointmentStatus.pending : appointment.status();
            switch (currentStatus) {
                case pending -> actionRow.getChildren().addAll(editButton, cancelButton);
                case confirmed -> actionRow.getChildren().addAll(editButton, cancelButton, startServiceButton, remindButton);
                case in_progress -> actionRow.getChildren().addAll(editButton, finishServiceButton);
                case awaiting_payment -> actionRow.getChildren().addAll(editButton, balanceButton);
                case completed, paid, cancelled -> {
                    editButton.setText("Sửa ghi chú");
                    actionRow.getChildren().add(editButton);
                    return;
                }
            }
            editButton.setText("Sửa");
        }

        private void cancelSelected() {
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để hủy.");
                return;
            }
            if (!confirm("Hủy lịch hẹn", "Bạn chắc chắn muốn hủy lịch hẹn này?")) {
                return;
            }
            updateSelectedStatus(AppointmentStatus.cancelled);
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
            if (newStatus == AppointmentStatus.confirmed || newStatus == AppointmentStatus.completed || newStatus == AppointmentStatus.paid) {
                warn("Không thể cập nhật trực tiếp", "Đặt cọc và hoàn thành thanh toán phải đi qua nút thanh toán.");
                return;
            }
            if (newStatus == AppointmentStatus.awaiting_payment && safeAmount(appointment.amountPaid()).compareTo(BigDecimal.ZERO) <= 0) {
                warn("Chưa đặt cọc", "Chỉ có thể chuyển sang chờ thanh toán sau khi khách đã đặt cọc.");
                return;
            }
            if ((appointment.status() == AppointmentStatus.completed || appointment.status() == AppointmentStatus.paid)
                    && newStatus != AppointmentStatus.completed && newStatus != AppointmentStatus.paid) {
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

        private void paySelected(PaymentStage requestedStage) {
            AppointmentRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) {
                warn("Chưa chọn lịch hẹn", "Vui lòng chọn lịch hẹn để thu tiền.");
                return;
            }
            AppointmentRequests.Response appointment = findAppointment(row.id());
            if (appointment == null) {
                warn("Không tìm thấy lịch hẹn", "Dữ liệu đã thay đổi, vui lòng tải lại.");
                return;
            }
            PaymentStage stage = requestedStage == null
                    ? (safeAmount(appointment.amountPaid()).compareTo(BigDecimal.ZERO) <= 0
                    ? PaymentStage.deposit
                    : PaymentStage.balance)
                    : requestedStage;
            BigDecimal amount = stage == PaymentStage.deposit
                    ? safeAmount(appointment.depositAmount())
                    : safeAmount(appointment.remainingAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                warn("Hoàn thành", "Lịch hẹn này không còn số tiền cần thu.");
                return;
            }
            if (stage == PaymentStage.deposit && safeAmount(appointment.amountPaid()).compareTo(BigDecimal.ZERO) > 0) {
                warn("Đã thu cọc", "Lịch hẹn này đã có thanh toán cọc.");
                return;
            }
            if (stage == PaymentStage.balance && safeAmount(appointment.amountPaid()).compareTo(BigDecimal.ZERO) <= 0) {
                warn("Chưa thu cọc", "Cần thu cọc trước khi thu phần còn lại.");
                return;
            }
            String methodLabel = stage == PaymentStage.deposit ? "Cọc 20% / QR" : "Thanh toán phần còn lại / QR";
            if (!showPaymentQrSimulation(row.id(), row.customer(), row.service(), amount, methodLabel)) {
                return;
            }
            PaymentRequests.Create request = new PaymentRequests.Create(row.id(), amount, PaymentMethod.bank_transfer,
                    PaymentStatus.paid, LocalDateTime.now(), stage);
            runAsync(() -> ApiClient.createPayment(request), r -> load(), ex -> error("Lỗi thanh toán", cleanError(ex)));
        }
    }

    private final class ReportsView extends VBox {
        private final DatePicker from = new DatePicker(LocalDate.now().minusDays(7));
        private final DatePicker to = new DatePicker(LocalDate.now());
        private final VBox content = new VBox(14);
        private final StackPane reportBody = new StackPane();

        private ReportsView() {
            getStyleClass().add("page");
            content.getStyleClass().add("report-content");
            VBox reportBox = new VBox(14, reportFilters(), reportBody);
            reportBox.getStyleClass().add("report-box");
            reportBox.setFillWidth(true);
            VBox.setVgrow(reportBody, Priority.ALWAYS);
            content.getChildren().setAll(card(reportBox));
            getChildren().addAll(
                    pageHeader("Báo cáo", "Doanh thu, dịch vụ và trạng thái lịch hẹn", "Tải báo cáo", e -> load()),
                    content);
            VBox.setVgrow(content, Priority.ALWAYS);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);
            load();
        }

        private Node reportFilters() {
            from.setMaxWidth(180);
            to.setMaxWidth(180);

            Button last7 = secondaryButton("7 ngày");
            Button last30 = secondaryButton("30 ngày");
            Button month = secondaryButton("Tháng này");
            Button clear = ghostButton("Xóa lọc");

            last7.setOnAction(e -> {
                to.setValue(LocalDate.now());
                from.setValue(LocalDate.now().minusDays(7));
                load();
            });
            last30.setOnAction(e -> {
                to.setValue(LocalDate.now());
                from.setValue(LocalDate.now().minusDays(30));
                load();
            });
            month.setOnAction(e -> {
                LocalDate today = LocalDate.now();
                from.setValue(today.withDayOfMonth(1));
                to.setValue(today);
                load();
            });
            clear.setOnAction(e -> {
                to.setValue(LocalDate.now());
                from.setValue(LocalDate.now().minusDays(7));
                load();
            });

            HBox row = new HBox(10,
                    labeled("Từ ngày", from),
                    labeled("Đến ngày", to),
                    last7,
                    last30,
                    month,
                    clear);
            row.getStyleClass().addAll("filter-bar", "report-filter-bar");
            row.setAlignment(Pos.BOTTOM_LEFT);
            return row;
        }

        private void load() {
            if (from.getValue() == null || to.getValue() == null) {
                warn("Thiếu khoảng ngày", "Vui lòng chọn đủ Từ ngày và Đến ngày để tải báo cáo.");
                return;
            }
            if (from.getValue() != null && to.getValue() != null && from.getValue().isAfter(to.getValue())) {
                warn("Khoảng ngày chưa hợp lệ", "Từ ngày không được lớn hơn Đến ngày.");
                return;
            }
            reportBody.getChildren().setAll(progress());
            runAsync(() -> new ReportData(ApiClient.getDailyRevenueReport(from.getValue(), to.getValue()),
                            ApiClient.getServiceRevenueReport(), ApiClient.getPaymentMethodReport(),
                            ApiClient.getAppointmentStats(), ApiClient.getAllPayments()),
                    data -> render(data), ex -> error("Lỗi tải báo cáo", cleanError(ex)));
        }

        private void render(ReportData data) {
            List<PaymentRequests.Response> paymentRows = filteredPaymentRows(data.paymentRows);

            TableView<ReportRequests.DailyRevenueResponse> daily = new TableView<>(
                    FXCollections.observableArrayList(data.daily == null ? List.of() : data.daily));
            column(daily, "Ngày", r -> r.date().toString(), 140);
            column(daily, "Doanh thu", r -> money(r.totalRevenue()), 160);
            column(daily, "Lịch hẹn", ReportRequests.DailyRevenueResponse::appointmentCount, 100);
            column(daily, "Hoàn thành", ReportRequests.DailyRevenueResponse::completedCount, 120);
            prepareReportTable(daily, "Không có dữ liệu doanh thu theo ngày", 390);

            List<ReportRequests.ServiceRevenueResponse> serviceRows = (data.services == null ? List.<ReportRequests.ServiceRevenueResponse>of() : data.services)
                    .stream()
                    .sorted(Comparator.comparing((ReportRequests.ServiceRevenueResponse r) -> safeAmount(r.totalRevenue()))
                            .thenComparing(r -> valueOrZero(r.appointmentCount()))
                            .reversed())
                    .toList();
            TableView<ReportRequests.ServiceRevenueResponse> services = new TableView<>(FXCollections.observableArrayList(serviceRows));
            column(services, "Dịch vụ", ReportRequests.ServiceRevenueResponse::serviceName, 220);
            column(services, "Lượt đặt", ReportRequests.ServiceRevenueResponse::appointmentCount, 100);
            column(services, "Doanh thu", r -> money(r.totalRevenue()), 160);
            column(services, "Trung bình", r -> money(r.avgRevenue()), 160);
            prepareReportTable(services, "Không có dữ liệu doanh thu theo dịch vụ", 390);

            Node paymentMethods = paymentMethodReport(data.payments);
            VBox paymentTabContent = new VBox(14, paymentMethods, reportPanel("Chi tiết thanh toán", paymentTable(paymentRows)));
            paymentTabContent.getStyleClass().add("report-tab-content");
            paymentTabContent.setFillWidth(true);

            TabPane tabs = new TabPane();
            tabs.getStyleClass().add("report-tabs");
            tabs.setFocusTraversable(false);
            tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabs.getTabs().addAll(
                    reportTab("Doanh thu theo ngày", reportPanel("Doanh thu theo ngày", daily)),
                    reportTab("Dịch vụ", reportPanel("Doanh thu theo dịch vụ", services)),
                    reportTab("Phương thức thanh toán", paymentTabContent));
            tabs.setMaxWidth(Double.MAX_VALUE);
            tabs.setMaxHeight(Double.MAX_VALUE);

            VBox.setVgrow(tabs, Priority.ALWAYS);
            reportBody.getChildren().setAll(tabs);
        }

        private Tab reportTab(String title, Node content) {
            Tab tab = new Tab(title);
            tab.setContent(content);
            return tab;
        }

        private Node reportPanel(String title, Node node) {
            VBox box = new VBox(10, sectionTitle(title), node);
            box.getStyleClass().add("report-section");
            box.setFillWidth(true);
            VBox.setVgrow(node, Priority.ALWAYS);
            return box;
        }

        private Node paymentMethodReport(List<ReportRequests.PaymentMethodResponse> payments) {
            List<ReportRequests.PaymentMethodResponse> rows = payments == null ? List.of() : payments.stream()
                    .sorted(Comparator.comparing((ReportRequests.PaymentMethodResponse p) -> safeAmount(p.totalAmount())).reversed())
                    .toList();

            TableView<ReportRequests.PaymentMethodResponse> table = new TableView<>(FXCollections.observableArrayList(rows));
            column(table, "Phương thức", p -> paymentMethodLabel(p.paymentMethod()), 160);
            column(table, "Số giao dịch", ReportRequests.PaymentMethodResponse::count, 120);
            column(table, "Tổng tiền", p -> money(p.totalAmount()), 160);
            column(table, "Tỷ trọng", p -> String.format(Locale.US, "%.1f%%", p.percentage() == null ? 0 : p.percentage()), 100);
            prepareReportTable(table, "Không có dữ liệu phương thức thanh toán", 180);
            return reportPanel("Phương thức thanh toán", table);
        }

        private TableView<PaymentRequests.Response> paymentTable(List<PaymentRequests.Response> rows) {
            TableView<PaymentRequests.Response> table = new TableView<>(FXCollections.observableArrayList(rows));
            column(table, "Lịch", PaymentRequests.Response::appointmentId, 90);
            column(table, "Giai đoạn", p -> p.paymentStage() == null ? "-" : (p.paymentStage() == PaymentStage.deposit ? "Cọc" : "Còn lại"), 120);
            column(table, "Phương thức", p -> paymentMethodLabel(p.paymentMethod() == null ? null : p.paymentMethod().name()), 140);
            column(table, "Số tiền", r -> money(r.amount()), 140);
            column(table, "Thời gian", r -> r.paidAt() == null ? "-" : r.paidAt().format(DATE_TIME), 170);
            prepareReportTable(table, "Không có thanh toán trong khoảng ngày này", 230);
            return table;
        }

        private List<PaymentRequests.Response> filteredPaymentRows(List<PaymentRequests.Response> payments) {
            return (payments == null ? List.<PaymentRequests.Response>of() : payments).stream()
                    .filter(p -> p.paidAt() != null)
                    .filter(p -> from.getValue() == null || !p.paidAt().toLocalDate().isBefore(from.getValue()))
                    .filter(p -> to.getValue() == null || !p.paidAt().toLocalDate().isAfter(to.getValue()))
                    .sorted(Comparator.comparing(PaymentRequests.Response::paidAt).reversed())
                    .toList();
        }

        private BigDecimal paymentTotal(List<PaymentRequests.Response> rows, PaymentStage stage) {
            return rows.stream()
                    .filter(p -> p.paymentStage() == stage)
                    .map(PaymentRequests.Response::amount)
                    .map(SalonFxApplication.this::safeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private <T> void prepareReportTable(TableView<T> table, String placeholder, double height) {
            table.setPlaceholder(new Label(placeholder));
            table.setFixedCellSize(44);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setMinHeight(height);
            table.setPrefHeight(height);
            table.setMaxHeight(height);
        }

        private int valueOrZero(Integer value) {
            return value == null ? 0 : value;
        }

        private String paymentMethodLabel(String value) {
            if (value == null || value.isBlank()) {
                return "-";
            }
            return switch (value) {
                case "bank_transfer" -> "Chuyển khoản";
                case "cash" -> "Tiền mặt";
                case "momo" -> "MoMo";
                case "card" -> "Thẻ";
                default -> value;
            };
        }
    }

    private final class UsersViewV2 extends VBox {
        private int pageSize = 6;

        private final TableView<AuthRequests.UserResponse> table = new TableView<>();
        private final TextField usernameFilter = new TextField();
        private final ComboBox<UserRole> roleFilter = new ComboBox<>();
        private final Label summaryLabel = new Label("0 tài khoản");
        private final FlowPane pageButtons = new FlowPane(6, 6);
        private final Button createButton = primaryButton("Tạo tài khoản");
        private final Button editButton = secondaryButton("Sửa tài khoản");
        private final Button deleteButton = dangerButton("Xóa tài khoản");
        private List<AuthRequests.UserResponse> allUsers = List.of();
        private List<AuthRequests.UserResponse> filteredUsers = List.of();
        private int currentPage = 0;
        private BorderPane tableCard;

        private UsersViewV2() {
            getStyleClass().add("page");
            pageButtons.getStyleClass().add("page-buttons");
            VBox top = new VBox(20,
                    pageHeader("Tài khoản", "Quản lý người dùng và phân quyền", "Làm mới", e -> load()),
                    filters());
            Node tableSection = tableCard();
            Node actionSection = actionsBar();
            BorderPane layout = new BorderPane();
            layout.setTop(top);
            layout.setCenter(tableSection);
            layout.setBottom(actionSection);
            BorderPane.setMargin(top, new Insets(0, 0, 12, 0));
            BorderPane.setMargin(tableSection, new Insets(0, 0, 12, 0));
            BorderPane.setMargin(actionSection, new Insets(0, 0, 0, 0));
            layout.setMaxWidth(Double.MAX_VALUE);
            layout.setMaxHeight(Double.MAX_VALUE);
            BorderPane.setAlignment(actionSection, Pos.BOTTOM_LEFT);
            VBox.setVgrow(layout, Priority.ALWAYS);
            getChildren().add(layout);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);
            configureTable();
            load();
        }

        private Node filters() {
            usernameFilter.setPromptText("Tìm theo tên đăng nhập");
            roleFilter.getItems().setAll(UserRole.values());
            roleFilter.setConverter(stringConverter(UserRole::getDisplayName));
            roleFilter.setPromptText("Tất cả vai trò");

            Button clear = secondaryButton("Xóa lọc");
            clear.setOnAction(e -> {
                usernameFilter.clear();
                roleFilter.setValue(null);
                applyFilters();
            });

            usernameFilter.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
            roleFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

            HBox row = new HBox(10, usernameFilter, roleFilter, clear);
            row.getStyleClass().add("filter-bar");
            HBox.setHgrow(usernameFilter, Priority.ALWAYS);
            return card(row);
        }

        private Node tableCard() {
            VBox box = new VBox(table, paginationBar());
            box.setFillWidth(true);
            box.setAlignment(Pos.TOP_CENTER);
            VBox.setVgrow(table, Priority.ALWAYS);
            table.setMaxHeight(Double.MAX_VALUE);
            tableCard = new BorderPane();
            BorderPane card = tableCard;
            card.getStyleClass().addAll("card", "table-card");
            card.setCenter(box);
            card.setMaxWidth(Double.MAX_VALUE);
            card.setMaxHeight(Double.MAX_VALUE);
            card.heightProperty().addListener((obs, oldH, newH) -> recalculatePageSize(newH.doubleValue()));
            return card;
        }

        private Node paginationBar() {
            BorderPane bar = new BorderPane();
            summaryLabel.getStyleClass().add("table-summary");
            bar.setLeft(summaryLabel);
            bar.setCenter(pageButtons);
            BorderPane.setAlignment(summaryLabel, Pos.CENTER_LEFT);
            BorderPane.setAlignment(pageButtons, Pos.CENTER);
            bar.getStyleClass().add("table-footer");
            return bar;
        }

        private Node actionsBar() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox row = new HBox(10, createButton, editButton, deleteButton, spacer);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);
            createButton.setOnAction(e -> openCreateDialog());
            editButton.setOnAction(e -> openEditDialog(selectedUser()));
            deleteButton.setOnAction(e -> deleteSelected());
            editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
            return card(row);
        }

        private void configureTable() {
            column(table, "ID", AuthRequests.UserResponse::id, 80);
            column(table, "Tên đăng nhập", AuthRequests.UserResponse::username, 220);
            column(table, "Vai trò", u -> u.role() == null ? "" : u.role().getDisplayName(), 180);
            table.setPlaceholder(new Label("Không có tài khoản phù hợp"));
            table.setFixedCellSize(44);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }

        private void load() {
            runAsync(() -> ApiClient.getUsers(username), list -> {
                allUsers = list == null ? List.of() : list.stream()
                        .sorted(Comparator.comparing(AuthRequests.UserResponse::id, Comparator.nullsLast(Integer::compareTo)))
                        .toList();
                currentPage = 0;
                applyFilters();
            }, ex -> error("Lỗi tải tài khoản", cleanError(ex)));
        }

        private void applyFilters() {
            String keyword = usernameFilter.getText() == null ? "" : usernameFilter.getText().trim().toLowerCase(Locale.ROOT);
            UserRole selectedRole = roleFilter.getValue();
            filteredUsers = allUsers.stream()
                    .filter(user -> keyword.isBlank() || (user.username() != null
                            && user.username().toLowerCase(Locale.ROOT).contains(keyword)))
                    .filter(user -> selectedRole == null || user.role() == selectedRole)
                    .toList();

            int totalPages = Math.max(1, (int) Math.ceil(filteredUsers.size() / (double) pageSize));
            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }
            if (currentPage < 0) {
                currentPage = 0;
            }
            refreshTablePage();
        }

        private void refreshTablePage() {
            int totalPages = Math.max(1, (int) Math.ceil(filteredUsers.size() / (double) pageSize));
            int fromIndex = Math.min(currentPage * pageSize, filteredUsers.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredUsers.size());
            List<AuthRequests.UserResponse> pageItems = filteredUsers.subList(fromIndex, toIndex);
            table.setItems(FXCollections.observableArrayList(pageItems));
            summaryLabel.setText(filteredUsers.size() + " tài khoản");
            rebuildPageButtons(totalPages);
        }

        private void goToPage(int page) {
            int totalPages = Math.max(1, (int) Math.ceil(filteredUsers.size() / (double) pageSize));
            currentPage = Math.max(0, Math.min(page, totalPages - 1));
            refreshTablePage();
        }

        private void recalculatePageSize(double cardHeight) {
            double footerHeight = 52;
            double padding = 24;
            double headerHeight = 44;
            double rowHeight = table.getFixedCellSize() > 0 ? table.getFixedCellSize() : 44;
            int newPageSize = Math.max(1, (int) Math.floor(
                    (cardHeight - footerHeight - padding - headerHeight) / rowHeight
            ));
            if (newPageSize != pageSize) {
                pageSize = newPageSize;
                currentPage = 0;
                applyFilters();
            }
        }

        private void rebuildPageButtons(int totalPages) {
            pageButtons.getChildren().clear();
            pageButtons.setVisible(totalPages > 1);
            pageButtons.setManaged(totalPages > 1);

            pageButtons.getChildren().add(pageJumpButton("«", 0, currentPage == 0));
            pageButtons.getChildren().add(pageJumpButton("‹", Math.max(0, currentPage - 1), currentPage == 0));

            int visibleWindow = 2;
            int start = Math.max(0, currentPage - visibleWindow);
            int end = Math.min(totalPages - 1, currentPage + visibleWindow);

            if (start > 0) {
                addPageButton(0, totalPages);
                if (start > 1) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
            }

            for (int i = start; i <= end; i++) {
                addPageButton(i, totalPages);
            }

            if (end < totalPages - 1) {
                if (end < totalPages - 2) {
                    pageButtons.getChildren().add(pageEllipsis());
                }
                addPageButton(totalPages - 1, totalPages);
            }

            pageButtons.getChildren().add(pageJumpButton("›", Math.min(totalPages - 1, currentPage + 1), currentPage >= totalPages - 1));
            pageButtons.getChildren().add(pageJumpButton("»", totalPages - 1, currentPage >= totalPages - 1));
        }

        private void addPageButton(int pageIndex, int totalPages) {
            Button pageButton = secondaryButton(String.valueOf(pageIndex + 1));
            pageButton.getStyleClass().add("page-number");
            if (pageIndex == currentPage) {
                pageButton.getStyleClass().add("selected-page");
                pageButton.setDisable(true);
            }
            pageButton.setOnAction(e -> goToPage(pageIndex));
            pageButtons.getChildren().add(pageButton);
        }

        private Label pageEllipsis() {
            Label ellipsis = new Label("...");
            ellipsis.getStyleClass().add("page-ellipsis");
            return ellipsis;
        }

        private Button pageJumpButton(String text, int targetPage, boolean disabled) {
            Button button = secondaryButton(text);
            button.getStyleClass().add("page-jump");
            button.setDisable(disabled);
            button.setOnAction(e -> goToPage(targetPage));
            return button;
        }

        private AuthRequests.UserResponse selectedUser() {
            return table.getSelectionModel().getSelectedItem();
        }

        private void openCreateDialog() {
            showUserDialog("Tạo tài khoản", null, false);
        }

        private void openEditDialog(AuthRequests.UserResponse user) {
            if (user == null) {
                warn("Chưa chọn tài khoản", "Vui lòng chọn tài khoản để sửa.");
                return;
            }
            showUserDialog("Sửa tài khoản", user, true);
        }

        private void showUserDialog(String title, AuthRequests.UserResponse user, boolean editing) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);

            ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(stylesheet());

            TextField usernameField = new TextField();
            PasswordField passwordField = new PasswordField();
            ComboBox<UserRole> roleBox = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
            roleBox.setConverter(stringConverter(UserRole::getDisplayName));
            roleBox.setMaxWidth(Double.MAX_VALUE);

            usernameField.setPromptText("Tên đăng nhập");
            passwordField.setPromptText(editing ? "Nhập mật khẩu mới (để trống nếu không đổi)" : "Mật khẩu");
            roleBox.setPromptText("Chọn vai trò");

            if (user != null) {
                usernameField.setText(user.username());
                roleBox.setValue(user.role());
            } else {
                roleBox.setValue(UserRole.STAFF);
            }

            boolean selfEdit = editing && user != null && Objects.equals(user.username(), username);
            if (selfEdit) {
                roleBox.setDisable(true);
            }

            Label hint = new Label(editing
                    ? "Mật khẩu có thể để trống để giữ nguyên."
                    : "Tài khoản mới sẽ được thêm vào danh sách.");
            hint.getStyleClass().add("muted");

            GridPane grid = formGrid();
            grid.addRow(0, labeled("Tên đăng nhập", usernameField), labeled("Mật khẩu", passwordField));
            grid.addRow(1, labeled("Vai trò", roleBox), new Pane());

            VBox content = new VBox(12, sectionTitle(title), hint, grid);
            dialog.getDialogPane().setContent(content);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != saveType) {
                return;
            }

            String newUsername = usernameField.getText().trim();
            String password = passwordField.getText();
            UserRole selectedRole = roleBox.getValue();
            if (newUsername.isBlank()) {
                warn("Thiếu thông tin", "Vui lòng nhập tên đăng nhập.");
                return;
            }
            if (!editing && (password == null || password.isBlank())) {
                warn("Thiếu thông tin", "Vui lòng nhập mật khẩu.");
                return;
            }
            if (selectedRole == null) {
                warn("Thiếu thông tin", "Vui lòng chọn vai trò.");
                return;
            }

            if (editing && user != null) {
                runAsync(() -> ApiClient.updateUser(user.id(), username, newUsername, password, selectedRole), saved -> {
                    if (Objects.equals(user.username(), username) && !Objects.equals(newUsername, user.username())) {
                        SalonFxApplication.this.username = newUsername;
                    }
                    load();
                }, ex -> error("Lỗi cập nhật tài khoản", cleanError(ex)));
            } else {
                runAsync(() -> ApiClient.createUser(username, newUsername, password, selectedRole), saved -> load(),
                        ex -> error("Lỗi tạo tài khoản", cleanError(ex)));
            }
        }

        private void deleteSelected() {
            AuthRequests.UserResponse user = selectedUser();
            if (user == null) {
                warn("Chưa chọn tài khoản", "Vui lòng chọn tài khoản để xóa.");
                return;
            }
            if (Objects.equals(user.username(), username)) {
                warn("Không thể xóa", "Không thể xóa tài khoản đang đăng nhập.");
                return;
            }
            if (!confirm("Xóa tài khoản", "Bạn có chắc chắn muốn xóa tài khoản \"" + user.username() + "\" không?")) {
                return;
            }
            runAsync(() -> {
                ApiClient.deleteUser(user.id(), username);
                return null;
            }, ignored -> load(), ex -> error("Lỗi xóa tài khoản", cleanError(ex)));
        }
    }

    private final class AppointmentDialogFx extends Dialog<AppointmentEdit> {
        private AppointmentDialogFx(List<CustomerRequests.Response> customers, List<ServiceRequests.Response> services,
                                    List<AppointmentRequests.Response> appointments, AppointmentRequests.Response appointment,
                                    boolean customerMode) {
            setTitle(appointment == null ? "Thêm lịch hẹn" : "Cập nhật lịch hẹn");
            initOwner(stage);
            initModality(Modality.APPLICATION_MODAL);
            ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            getDialogPane().getStylesheets().add(stylesheet());

            ComboBox<CustomerRequests.Response> customer = new ComboBox<>(FXCollections.observableArrayList(customers));
            customer.setConverter(stringConverter(CustomerRequests.Response::fullName));
            customer.setMaxWidth(Double.MAX_VALUE);
            if (customerMode && !customers.isEmpty()) {
                customer.setValue(customers.get(0));
                customer.setDisable(true);
            }
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
            date.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(empty || item.isBefore(LocalDate.now()));
                }
            });
            date.setMaxWidth(Double.MAX_VALUE);
            TextField time = new TextField("09:00");
            time.setMaxWidth(Double.MAX_VALUE);
            ComboBox<String> quickTime = new ComboBox<>(FXCollections.observableArrayList(
                    "08:00", "09:00", "10:00", "13:30", "15:00", "17:30", "19:00"));
            quickTime.setPromptText("Khung giờ nhanh");
            quickTime.setMaxWidth(Double.MAX_VALUE);
            ComboBox<AppointmentStatus> status = new ComboBox<>();
            status.setConverter(stringConverter(AppointmentStatus::getDisplayName));
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
            List<AppointmentStatus> allowedStatuses = new ArrayList<>();
            if (appointment == null) {
                allowedStatuses.add(AppointmentStatus.pending);
            } else if (customerMode) {
                allowedStatuses.addAll(List.of(AppointmentStatus.pending, AppointmentStatus.cancelled));
                if (appointment.status() != null && !allowedStatuses.contains(appointment.status())) {
                    allowedStatuses.add(appointment.status());
                }
            } else {
                allowedStatuses.addAll(List.of(
                        AppointmentStatus.pending,
                        AppointmentStatus.in_progress,
                        AppointmentStatus.awaiting_payment,
                        AppointmentStatus.cancelled));
                if (appointment.status() != null && !allowedStatuses.contains(appointment.status())) {
                    allowedStatuses.add(appointment.status());
                }
            }
            status.getItems().setAll(allowedStatuses);
            if (status.getValue() == null && !status.getItems().isEmpty()) {
                status.setValue(status.getItems().get(0));
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
        if (role != UserRole.CUSTOMER) {
            column(table, "Khách hàng", AppointmentRow::customer, 190);
        }
        column(table, "Dịch vụ", AppointmentRow::service, 210);
        column(table, "Phòng", AppointmentRow::room, 120);

        TableColumn<AppointmentRow, LocalDateTime> timeCol = new TableColumn<>("Lịch hẹn");
        timeCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().time()));
        timeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                AppointmentRow row = empty ? null : getTableRow().getItem();
                setText(row == null || item == null
                        ? null
                        : item.toLocalDate().format(DATE_ONLY) + "\n"
                        + item.toLocalTime().format(TIME_INPUT) + " - " + row.endTime().format(TIME_INPUT));
            }
        });
        timeCol.setPrefWidth(150);
        timeCol.setSortType(TableColumn.SortType.ASCENDING);
        table.getColumns().add(timeCol);
        table.getSortOrder().setAll(timeCol);

        column(table, "Thanh toán", r -> "Tổng " + money(r.totalAmount()) + "\n"
                + "Đã thu " + money(r.amountPaid()), 170, true);

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
                        case in_progress -> "status-confirmed";
                        case awaiting_payment -> "status-paid";
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
                .map(CustomerRequests.Response::fullName).findFirst().orElse("Kh?ng r?");
        Integer serviceId = a.serviceIds() == null || a.serviceIds().isEmpty() ? null : a.serviceIds().get(0);
        ServiceRequests.Response serviceResponse = services.stream().filter(s -> Objects.equals(s.id(), serviceId))
                .findFirst().orElse(null);
        String service = serviceResponse == null ? "Kh?ng r?" : serviceResponse.name();
        AppointmentStatus st = a.status() == null ? AppointmentStatus.pending : a.status();
        int duration = serviceDuration(serviceResponse);
        BigDecimal price = serviceResponse == null ? BigDecimal.ZERO : serviceResponse.price();
        return new AppointmentRow(a.id(), customer, service, orDash(a.roomName()), a.appointmentTime(), a.appointmentTime().plusMinutes(duration),
                price, a.totalAmount(), a.depositAmount(), a.amountPaid(), a.remainingAmount(), st.getDisplayName(), st, orEmpty(a.note()));
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
            if (existing.status() == AppointmentStatus.pending || existing.status() == AppointmentStatus.cancelled) {
                continue;
            }
            if (existing.status() != AppointmentStatus.confirmed
                    && existing.status() != AppointmentStatus.in_progress
                    && existing.status() != AppointmentStatus.awaiting_payment
                    && existing.status() != AppointmentStatus.completed
                    && existing.status() != AppointmentStatus.paid) {
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
                return "Kh?ch h?ng n?y ?? c? l?ch trong khung gi? "
                        + existingStart.format(DATE_TIME) + " - " + existingEnd.toLocalTime().format(TIME_INPUT) + ".";
            }
            if (Objects.equals(existing.roomId(), roomId)) {
                return "D?ch v? n?y ?ang c? l?ch kh?c trong khung gi? "
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
        if (content instanceof TableView<?>) {
            card.getStyleClass().add("table-card");
        }
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
        ButtonType paidButtonType = new ButtonType("Hoàn tất thanh toán", ButtonBar.ButtonData.OK_DONE);
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

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
                                  LocalDateTime endTime, BigDecimal price, BigDecimal totalAmount, BigDecimal depositAmount,
                                  BigDecimal amountPaid, BigDecimal remainingAmount, String status, AppointmentStatus statusEnum,
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
                              ReportRequests.AppointmentStatsResponse stats,
                              List<PaymentRequests.Response> paymentRows) {
    }
}

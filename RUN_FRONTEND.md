# Running the Salon Booking System Swing Client

## Prerequisites

1. **Java 17 or higher** - Required for Spring Boot and Swing application
2. **Spring Boot Backend** - Must be running on `localhost:8080`
3. **Maven** - For building the project (included as mvnw.cmd)

## Step 1: Verify Backend is Running

Start the Spring Boot backend first:

```bash
cd d:\salonn-booking
.\mvnw.cmd spring-boot:run
```

The backend should display:
```
Started SalonnBookingApplication in X.XXX seconds
```

## Step 2: Build the Project

Compile the entire project including Swing client:

```bash
cd d:\salonn-booking
.\mvnw.cmd clean compile
```

## Step 3: Run the Swing Client

### Option A: Using Maven (Recommended)

```bash
cd d:\salonn-booking
.\mvnw.cmd exec:java -Dexec.mainClass="com.salonnbooking.SwingClient"
```

### Option B: Using Java Directly

First, package the application:

```bash
cd d:\salonn-booking
.\mvnw.cmd clean package -DskipTests
```

Then run:

```bash
java -cp target/salonn-booking-0.0.1-SNAPSHOT.jar com.salonnbooking.SwingClient
```

## Troubleshooting

### Issue: "Connection refused" when loading data
- **Cause**: Backend is not running on localhost:8080
- **Solution**: Start the Spring Boot backend first (see Step 1)

### Issue: "ClassNotFoundException" for SwingClient
- **Cause**: Project not compiled
- **Solution**: Run `mvnw clean compile` first

### Issue: GUI doesn't appear or freezes
- **Cause**: Backend took too long to respond or there's a network issue
- **Solution**: Check backend console for errors, ensure database is accessible

### Issue: "ResourceNotFoundException" when loading appointments
- **Cause**: Some appointments were deleted from database, but UI still references them
- **Solution**: Click "Refresh" button to reload data from backend

## Architecture

The Swing client uses:

1. **Non-blocking API calls**: All REST calls use `SwingWorker` to prevent EDT (Event Dispatch Thread) blocking
2. **AppointmentDialog**: Modal form dialog for creating/editing appointments with validation
3. **AppointmentPanel**: Main panel with JTable for viewing appointments, CRUD buttons
4. **ApiClient**: Central REST client for all Spring Boot communication

## Features Implemented

### Dashboard Navigation
- Sidebar with navigation buttons (Dashboard, Appointment, Customer, Service, Report)
- CardLayout for switching between panels
- FlatLaf theme (IntelliJ Light)

### Appointment Management
- View all appointments in a sortable JTable
- Create new appointments with customer/service selection
- Edit existing appointments
- Delete appointments with confirmation dialog
- Auto-refresh table after CRUD operations
- Comprehensive error handling with user-friendly messages

### Customer Management
- View all customers
- Add new customers
- Edit customer information
- Delete customers

### Service Management
- View all services with pricing
- Add new services
- Edit services
- Delete services

## Testing CRUD Operations

### Create Appointment
1. Click "Add" button in Appointment panel
2. Select customer from dropdown
3. Select service from dropdown
4. Enter appointment date/time (format: yyyy-MM-dd HH:mm)
5. Select status
6. Enter optional note
7. Click "Create" button
8. Verify success message and table refresh

### Edit Appointment
1. Click on appointment row in table
2. Click "Edit" button
3. Modify fields as needed
4. Click "Update" button
5. Verify success message and table refresh

### Delete Appointment
1. Click on appointment row in table
2. Click "Delete" button
3. Confirm deletion
4. Verify success message and table refresh

## Performance Notes

- Initial data load may take 1-2 seconds (loading customers, services, appointments)
- Each CRUD operation is non-blocking and shows status updates
- Table auto-resizes columns to fit content
- Swing UI is responsive during API calls

## Database

The application connects to SQL Server database configured in `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost;databaseName=SalonBookingDB
    username: sa
    password: YourPassword123!
```

Ensure the database is running and accessible before starting the application.

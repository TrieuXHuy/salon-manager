# AppointmentPanel - Complete Implementation Guide

## Overview

`AppointmentPanel.java` is the main panel for managing salon appointments with a modern, non-blocking UI. It integrates with `AppointmentDialog.java` for form input and uses `SwingWorker` for all API communication to prevent UI freezing.

## Architecture Pattern

```
┌─────────────────────────────────────────────┐
│        AppointmentPanel (JPanel)             │
│  ┌────────────────────────────────────────┐  │
│  │  Header (Title)                        │  │
│  ├────────────────────────────────────────┤  │
│  │  JTable (Read-only appointment list)   │  │
│  │  - ID, Customer, Service, Date/Time,  │  │
│  │    Status, Note                        │  │
│  ├────────────────────────────────────────┤  │
│  │  Toolbar: Add | Edit | Delete | Refresh│  │
│  │  Status label: "Ready", "Loading..."   │  │
│  └────────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
        │
        ├─ Opens AppointmentDialog (Modal)
        │   on Add/Edit button click
        │
        └─ Calls ApiClient methods
            (Non-blocking via SwingWorker)
```

## Component Structure

### UI Components

1. **JTable** (table)
   - Read-only, single-row selection
   - Columns: ID, Customer, Service, Date/Time, Status, Note
   - Auto-resizes columns to content
   - Triggers btnEdit/btnDelete enable on row selection

2. **Buttons** (btnAdd, btnEdit, btnDelete, btnRefresh)
   - Added to FlowLayout toolbar panel
   - All button clicks trigger non-blocking SwingWorker tasks
   - btnEdit & btnDelete disabled until row selected

3. **Status Label** (lblStatus)
   - Shows current operation status: "Ready", "Loading...", "Error", etc.
   - Updates via `setStatus()` method

### Data Models

```java
// Data storage for appointments
List<AppointmentRequests.Response> appointments;
List<CustomerRequests.Response> customers;
List<ServiceRequests.Response> services;

// Currently selected appointment
Integer selectedAppointmentId = null;
```

## Method Breakdown

### Initialization & Loading

#### `loadInitialData()` (Non-blocking)
Loads customers and services on first panel initialization.

```java
private void loadInitialData() {
    setStatus("Loading initial data...");
    disableButtons();
    
    SwingWorker<Void, Void> worker = new SwingWorker<>() {
        @Override
        protected Void doInBackground() throws Exception {
            // Runs on background thread - no EDT blocking
            customers = ApiClient.getAllCustomers();
            services = ApiClient.getAllServices();
            return null;
        }
        
        @Override
        protected void done() {
            try {
                get(); // Check for exceptions
                loadAppointments(); // Chain to next load
            } catch (Exception e) {
                handleException("Error loading initial data", e);
            }
        }
    };
    
    worker.execute();
}
```

**Pattern Notes:**
- Uses `SwingWorker<Result, Progress>` with Result=Void (no progress updates)
- `doInBackground()` runs on separate thread (background pool)
- `done()` runs on EDT (safe for UI updates)
- `get()` re-throws checked exceptions as ExecutionException

#### `loadAppointments()` (Non-blocking)
Loads all appointments from API and refreshes table.

```java
private void loadAppointments() {
    setStatus("Loading appointments...");
    
    SwingWorker<List<AppointmentRequests.Response>, Void> worker = 
            new SwingWorker<>() {
        @Override
        protected List<AppointmentRequests.Response> doInBackground() {
            return ApiClient.getAllAppointments();
        }
        
        @Override
        protected void done() {
            try {
                appointments = get(); // Unwrap result
                refreshTable(appointments);
                setStatus("Ready - " + appointments.size() + " appointments");
                enableButtons();
            } catch (Exception e) {
                handleException("Error loading appointments", e);
            }
        }
    };
    
    worker.execute();
}
```

#### `refreshTable(List<AppointmentRequests.Response>)`
Populates JTable with appointment data, performing customer/service name lookups.

```java
private void refreshTable(List<AppointmentRequests.Response> appointments) {
    tableModel.setRowCount(0); // Clear existing rows
    
    for (AppointmentRequests.Response apt : appointments) {
        // Lookup customer name
        String customerName = customers.stream()
                .filter(c -> c.id().equals(apt.customerId()))
                .map(CustomerRequests.Response::fullName)
                .findFirst()
                .orElse("Unknown");
        
        // Lookup service name
        String serviceName = services.stream()
                .filter(s -> s.id().equals(apt.serviceId()))
                .map(ServiceRequests.Response::name)
                .findFirst()
                .orElse("Unknown");
        
        // Add row with formatted date/time
        tableModel.addRow(new Object[] {
            apt.id(),
            customerName,
            serviceName,
            apt.appointmentTime().format(DATE_FORMATTER),
            apt.status(),
            apt.note() != null ? apt.note() : ""
        });
    }
    
    // Reset selection
    selectedAppointmentId = null;
    table.clearSelection();
    btnEdit.setEnabled(false);
    btnDelete.setEnabled(false);
}
```

### Button Event Handlers

#### `onAddButtonClicked()`
Opens modal dialog for new appointment creation.

```java
private void onAddButtonClicked() {
    // Create dialog in "Add mode" (no existing appointment)
    AppointmentDialog dialog = new AppointmentDialog(
            SwingUtilities.getWindowAncestor(this),
            customers, services);
    dialog.setVisible(true); // Blocks until user closes
    
    if (dialog.isApproved()) { // User clicked Save
        addAppointment(dialog.getAppointmentCreateRequest());
    }
    // If user clicked Cancel, dialog.isApproved() returns false
}
```

**Dialog Flow:**
1. Dialog created in add mode (no pre-filled data)
2. `setVisible(true)` blocks until closed
3. User fills form and clicks Save or Cancel
4. If approved, extract data and call CRUD method

#### `onEditButtonClicked()`
Opens modal dialog for editing selected appointment.

```java
private void onEditButtonClicked() {
    if (selectedAppointmentId == null) {
        JOptionPane.showMessageDialog(this,
                "Please select an appointment to edit",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    // Find appointment in local list
    AppointmentRequests.Response selectedApt = appointments.stream()
            .filter(a -> a.id().equals(selectedAppointmentId))
            .findFirst()
            .orElse(null);
    
    if (selectedApt == null) {
        showError("Appointment not found");
        return;
    }
    
    // Create dialog in "Edit mode" (pre-fill with existing data)
    AppointmentDialog dialog = new AppointmentDialog(
            SwingUtilities.getWindowAncestor(this),
            customers, services, selectedApt);
    dialog.setVisible(true);
    
    if (dialog.isApproved()) {
        updateAppointment(selectedAppointmentId, 
                dialog.getAppointmentUpdateRequest());
    }
}
```

#### `onDeleteButtonClicked()`
Shows confirmation dialog before deletion.

```java
private void onDeleteButtonClicked() {
    if (selectedAppointmentId == null) {
        JOptionPane.showMessageDialog(this,
                "Please select an appointment to delete",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    // Confirm deletion
    int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this appointment?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
    
    if (confirm == JOptionPane.YES_OPTION) {
        deleteAppointment(selectedAppointmentId);
    }
}
```

### CRUD Methods (Non-blocking)

#### `addAppointment(AppointmentRequests.Create)`
Creates new appointment via API in background thread.

```java
private void addAppointment(AppointmentRequests.Create createReq) {
    setStatus("Creating appointment...");
    disableButtons(); // Prevent concurrent operations
    
    SwingWorker<AppointmentRequests.Response, Void> worker = 
            new SwingWorker<>() {
        @Override
        protected AppointmentRequests.Response doInBackground() throws Exception {
            // API call on background thread
            return ApiClient.createAppointment(createReq);
        }
        
        @Override
        protected void done() {
            try {
                get(); // Check for exceptions from API
                JOptionPane.showMessageDialog(AppointmentPanel.this,
                        "Appointment created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments(); // Refresh table from server
            } catch (Exception e) {
                handleException("Error creating appointment", e);
                enableButtons(); // Allow retry
                setStatus("Error");
            }
        }
    };
    
    worker.execute();
}
```

**Error Handling:**
- `handleException()` extracts error message and shows JOptionPane
- Buttons re-enabled on error to allow retry
- `loadAppointments()` called on success to sync UI with server state

#### `updateAppointment(Integer, AppointmentRequests.Update)`
Updates existing appointment via API.

```java
private void updateAppointment(Integer appointmentId, 
        AppointmentRequests.Update updateReq) {
    setStatus("Updating appointment...");
    disableButtons();
    
    SwingWorker<AppointmentRequests.Response, Void> worker = 
            new SwingWorker<>() {
        @Override
        protected AppointmentRequests.Response doInBackground() throws Exception {
            return ApiClient.updateAppointment(appointmentId, updateReq);
        }
        
        @Override
        protected void done() {
            try {
                get();
                JOptionPane.showMessageDialog(AppointmentPanel.this,
                        "Appointment updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments(); // Refresh table
            } catch (Exception e) {
                handleException("Error updating appointment", e);
                enableButtons();
                setStatus("Error");
            }
        }
    };
    
    worker.execute();
}
```

#### `deleteAppointment(Integer)`
Deletes appointment via API after user confirmation.

```java
private void deleteAppointment(Integer appointmentId) {
    setStatus("Deleting appointment...");
    disableButtons();
    
    SwingWorker<Void, Void> worker = new SwingWorker<>() {
        @Override
        protected Void doInBackground() throws Exception {
            ApiClient.deleteAppointment(appointmentId); // Returns void
            return null;
        }
        
        @Override
        protected void done() {
            try {
                get();
                JOptionPane.showMessageDialog(AppointmentPanel.this,
                        "Appointment deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments(); // Refresh table
            } catch (Exception e) {
                handleException("Error deleting appointment", e);
                enableButtons();
                setStatus("Error");
            }
        }
    };
    
    worker.execute();
}
```

### Helper Methods

#### `handleException(String, Exception)`
Extracts error message and displays JOptionPane.

```java
private void handleException(String title, Exception e) {
    String message = e.getMessage();
    if (message == null || message.isEmpty()) {
        message = e.getClass().getSimpleName();
    }
    
    JOptionPane.showMessageDialog(this,
            title + ": " + message,
            "Error", JOptionPane.ERROR_MESSAGE);
}
```

#### `setStatus(String status)`
Updates status label to show operation progress.

#### `disableButtons()` / `enableButtons()`
Prevents concurrent API operations by disabling all buttons during operations.

## Integration with AppointmentDialog

The `AppointmentDialog` class handles:

1. **Form Validation**
   - Ensures customer, service, and date/time selected
   - Validates date format (yyyy-MM-dd HH:mm)

2. **Data Population** (Edit mode)
   - Pre-fills form with existing appointment data
   - Looks up customer/service by ID in combobox

3. **Data Extraction**
   - `getAppointmentCreateRequest()` - Returns AppointmentRequests.Create
   - `getAppointmentUpdateRequest()` - Returns AppointmentRequests.Update

4. **Modal Behavior**
   - Blocks parent window until closed
   - Returns `approved` flag to indicate Save vs Cancel

## Error Handling Strategy

### Exception Types & Handling

1. **ResourceNotFoundException** (404)
   - Occurs when appointment/customer/service no longer exists
   - Message: "Resource not found"
   - Action: Auto-refresh table to sync UI

2. **Network Errors** (Connection refused)
   - Occurs when backend not running
   - Message: "Connection refused" or "No route to host"
   - Action: Show error, enable buttons for retry

3. **Validation Errors**
   - Form validation fails before API call
   - Message: "Invalid date/time format"
   - Action: Stay in dialog, user can correct

4. **Server Errors** (500)
   - Database error, logic error on backend
   - Message: Exception message from server
   - Action: Show error, enable buttons for retry

### Error Recovery

All CRUD methods follow this pattern:

```java
try {
    get(); // May throw ExecutionException
    // Success: show message, refresh table
} catch (Exception e) {
    handleException(title, e); // Show error
    enableButtons(); // Allow retry
    setStatus("Error");
}
```

## UI State Management

### Button Enable/Disable Logic

| Condition | btnAdd | btnEdit | btnDelete | btnRefresh |
|-----------|--------|---------|-----------|-----------|
| Initializing | ❌ | ❌ | ❌ | ❌ |
| Ready (no selection) | ✅ | ❌ | ❌ | ✅ |
| Ready (row selected) | ✅ | ✅ | ✅ | ✅ |
| Loading/Saving | ❌ | ❌ | ❌ | ❌ |
| Error | ✅ | ✅ | ✅ | ✅ |

### Status Label Updates

```
Initial:         "Ready"
User clicks Add: "Loading..." → Dialog opens
API call:        "Creating appointment..."
Success:         "Ready - 5 appointments"
Error:           "Error"
```

## Performance Considerations

1. **Non-blocking API Calls**
   - All REST calls via `SwingWorker.doInBackground()`
   - Main thread (EDT) never blocked by network I/O
   - UI remains responsive during data load

2. **Table Refresh**
   - `tableModel.setRowCount(0)` clears efficiently
   - `stream().filter().findFirst()` for name lookups (O(n²) with small lists)
   - Could optimize with HashMap<Integer, Customer> if needed

3. **Button Management**
   - `disableButtons()` prevents double-clicks
   - Prevents concurrent API calls that would cause inconsistency

## Testing Checklist

- [ ] Load appointments on panel initialization
- [ ] Add appointment with all fields filled
- [ ] Add appointment with missing field (should show validation error)
- [ ] Edit appointment, change customer/service/date
- [ ] Delete appointment, confirm deletion
- [ ] Delete appointment, cancel deletion
- [ ] Click Refresh to reload appointments
- [ ] Try operations while backend is down (should show connection error)
- [ ] Try deleting appointment that was deleted elsewhere (should show not found)
- [ ] Table scrollable if more than 10 appointments
- [ ] Status label updates during operations

## Extension Points

Future enhancements could include:

1. **Appointment Search/Filter** - Add search box to filter by customer name
2. **Date Range Picker** - Replace text field with DateTimeChooser
3. **Appointment Status Colors** - Color table rows by status (confirmed, pending, cancelled)
4. **Pagination** - For large number of appointments
5. **Column Sorting** - Click column header to sort
6. **Bulk Operations** - Select multiple rows for batch delete
7. **Time Slot Availability** - Check conflicts before booking
8. **Notifications** - Toast notifications for success/error (instead of JOptionPane)

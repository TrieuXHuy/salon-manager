package com.salonnbooking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment_services")
public class AppointmentServiceItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false)
	private Appointment appointment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id", nullable = false)
	private ServiceEntity service;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_employee_id")
	private Employee assignedEmployee;

	@Column(name = "line_no", nullable = false)
	private Integer lineNo;

	@Column(name = "service_name_snapshot", nullable = false, columnDefinition = "nvarchar(255)")
	private String serviceNameSnapshot;

	@Column(nullable = false, precision = 18, scale = 2)
	private java.math.BigDecimal price;

	@Column(name = "duration_minutes", nullable = false)
	private Integer durationMinutes;

	@Column(columnDefinition = "nvarchar(30)")
	private String status;

	@Column(columnDefinition = "nvarchar(500)")
	private String note;

	public Integer getId() {
		return id;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public Employee getAssignedEmployee() {
		return assignedEmployee;
	}

	public void setAssignedEmployee(Employee assignedEmployee) {
		this.assignedEmployee = assignedEmployee;
	}

	public Integer getLineNo() {
		return lineNo;
	}

	public void setLineNo(Integer lineNo) {
		this.lineNo = lineNo;
	}

	public String getServiceNameSnapshot() {
		return serviceNameSnapshot;
	}

	public void setServiceNameSnapshot(String serviceNameSnapshot) {
		this.serviceNameSnapshot = serviceNameSnapshot;
	}

	public java.math.BigDecimal getPrice() {
		return price;
	}

	public void setPrice(java.math.BigDecimal price) {
		this.price = price;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}

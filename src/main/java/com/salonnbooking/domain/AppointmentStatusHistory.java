package com.salonnbooking.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false)
	private Appointment appointment;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", columnDefinition = "nvarchar(30)")
	private AppointmentStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, columnDefinition = "nvarchar(30)")
	private AppointmentStatus toStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	private UserAccount changedBy;

	@CreationTimestamp
	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;

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

	public AppointmentStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(AppointmentStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public AppointmentStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(AppointmentStatus toStatus) {
		this.toStatus = toStatus;
	}

	public UserAccount getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(UserAccount changedBy) {
		this.changedBy = changedBy;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}

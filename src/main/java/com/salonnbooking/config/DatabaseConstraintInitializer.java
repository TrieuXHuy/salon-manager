package com.salonnbooking.config;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConstraintInitializer {

	@Bean
	ApplicationRunner appointmentStatusConstraintRunner(JdbcTemplate jdbcTemplate) {
		return args -> {
			ensureCustomerColumns(jdbcTemplate);
			ensureServiceRooms(jdbcTemplate);
			ensureAppointmentFinancialColumns(jdbcTemplate);
			ensurePaymentStageColumn(jdbcTemplate);
			backfillAppointmentFinancialColumns(jdbcTemplate);
			backfillPaymentStages(jdbcTemplate);

			List<ConstraintInfo> constraints = jdbcTemplate.query("""
					SELECT SCHEMA_NAME(t.schema_id) AS schema_name, cc.name AS constraint_name, cc.definition
					FROM sys.check_constraints cc
					JOIN sys.tables t ON cc.parent_object_id = t.object_id
					WHERE t.name = 'appointments' AND cc.definition LIKE '%status%'
					""",
					(rs, rowNum) -> new ConstraintInfo(
							rs.getString("schema_name"),
							rs.getString("constraint_name"),
							rs.getString("definition")));

			boolean alreadyAllowsPaid = constraints.stream()
					.anyMatch(constraint -> constraint.definition().contains("'paid'"));
			if (alreadyAllowsPaid) {
				return;
			}

			String schema = constraints.isEmpty() ? "dbo" : constraints.get(0).schemaName();
			String tableName = quote(schema) + ".[appointments]";
			for (ConstraintInfo constraint : constraints) {
				jdbcTemplate.execute("ALTER TABLE " + quote(constraint.schemaName())
						+ ".[appointments] DROP CONSTRAINT " + quote(constraint.constraintName()));
			}

			jdbcTemplate.execute("ALTER TABLE " + tableName
					+ " ADD CONSTRAINT [CK_appointment_status] CHECK ([status] IN "
					+ "('pending','confirmed','completed','cancelled','paid'))");
		};
	}

	private static void ensureCustomerColumns(JdbcTemplate jdbcTemplate) {
		addColumnIfMissing(jdbcTemplate, "customers", "loyalty_points",
				"ALTER TABLE [dbo].[customers] ADD [loyalty_points] int NOT NULL CONSTRAINT [DF_customers_loyalty_points] DEFAULT 0");
		addColumnIfMissing(jdbcTemplate, "customers", "note",
				"ALTER TABLE [dbo].[customers] ADD [note] nvarchar(500) NULL");
	}

	private static void ensureServiceRooms(JdbcTemplate jdbcTemplate) {
		Integer tableCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM sys.tables
				WHERE name = 'service_rooms'
				""", Integer.class);
		if (tableCount == null || tableCount == 0) {
			return;
		}
		Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM [dbo].[service_rooms]", Integer.class);
		if (roomCount != null && roomCount > 0) {
			return;
		}
		jdbcTemplate.update("""
				INSERT INTO [dbo].[service_rooms] ([name], [description], [is_active])
				VALUES
				(N'Phòng 1', N'Khu phục vụ tiêu chuẩn', 1),
				(N'Phòng 2', N'Khu phục vụ tiêu chuẩn', 1),
				(N'Phòng 3', N'Khu phục vụ VIP / linh hoạt', 1)
				""");
	}

	private static void ensureAppointmentFinancialColumns(JdbcTemplate jdbcTemplate) {
		addColumnIfMissing(jdbcTemplate, "appointments", "total_amount",
				"ALTER TABLE [dbo].[appointments] ADD [total_amount] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_total_amount] DEFAULT 0");
		addColumnIfMissing(jdbcTemplate, "appointments", "deposit_amount",
				"ALTER TABLE [dbo].[appointments] ADD [deposit_amount] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_deposit_amount] DEFAULT 0");
		addColumnIfMissing(jdbcTemplate, "appointments", "amount_paid",
				"ALTER TABLE [dbo].[appointments] ADD [amount_paid] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_amount_paid] DEFAULT 0");
		addColumnIfMissing(jdbcTemplate, "appointments", "remaining_amount",
				"ALTER TABLE [dbo].[appointments] ADD [remaining_amount] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_remaining_amount] DEFAULT 0");
	}

	private static void ensurePaymentStageColumn(JdbcTemplate jdbcTemplate) {
		addColumnIfMissing(jdbcTemplate, "payments", "payment_stage",
				"ALTER TABLE [dbo].[payments] ADD [payment_stage] nvarchar(50) NOT NULL CONSTRAINT [DF_payments_payment_stage] DEFAULT N'deposit'");
		Integer constraintCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM sys.check_constraints
				WHERE name = 'CK_payment_stage'
				""", Integer.class);
		if (constraintCount == null || constraintCount == 0) {
			jdbcTemplate.execute("""
					ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payment_stage]
					CHECK ([payment_stage] IN (N'deposit', N'balance'))
					""");
		}
	}

	private static void backfillAppointmentFinancialColumns(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.update("""
				UPDATE a
				SET
					a.total_amount = COALESCE(a.total_amount, s.price, 0),
					a.deposit_amount = COALESCE(a.deposit_amount, ROUND(COALESCE(s.price, 0) * 0.2, 2)),
					a.amount_paid = COALESCE(a.amount_paid, paid_summary.total_paid, 0),
					a.remaining_amount = COALESCE(a.remaining_amount,
						ROUND(COALESCE(s.price, 0) - COALESCE(paid_summary.total_paid, 0), 2))
				FROM [dbo].[appointments] a
				JOIN [dbo].[services] s ON s.[id] = a.[service_id]
				OUTER APPLY (
					SELECT SUM(CASE WHEN p.[payment_status] = N'paid' THEN p.[amount] ELSE 0 END) AS total_paid
					FROM [dbo].[payments] p
					WHERE p.[appointment_id] = a.[id]
				) paid_summary
				""");
	}

	private static void backfillPaymentStages(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.update("""
				UPDATE [dbo].[payments]
				SET [payment_stage] = COALESCE([payment_stage], N'balance')
				""");
	}

	private static void addColumnIfMissing(JdbcTemplate jdbcTemplate, String tableName, String columnName, String sql) {
		Integer count = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM sys.columns c
				JOIN sys.tables t ON c.object_id = t.object_id
				WHERE t.name = ? AND c.name = ?
				""", Integer.class, tableName, columnName);
		if (count == null || count == 0) {
			jdbcTemplate.execute(sql);
		}
	}

	private static String quote(String identifier) {
		return "[" + identifier.replace("]", "]]") + "]";
	}

	private record ConstraintInfo(String schemaName, String constraintName, String definition) {
	}
}

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

			boolean alreadyMatchesCurrentStatus = constraints.stream()
					.anyMatch(constraint -> constraint.definition().contains("'PENDING'")
							&& constraint.definition().contains("'CONFIRMED'")
							&& constraint.definition().contains("'IN_PROGRESS'")
							&& constraint.definition().contains("'COMPLETED'")
							&& constraint.definition().contains("'CANCELLED'"));
			if (alreadyMatchesCurrentStatus) {
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
					+ "('PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED'))");
		};
	}

	private static String quote(String identifier) {
		return "[" + identifier.replace("]", "]]") + "]";
	}

	private record ConstraintInfo(String schemaName, String constraintName, String definition) {
	}
}

package com.salonnbooking.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerProfileSchemaInitializer implements ApplicationRunner {
	private final JdbcTemplate jdbcTemplate;

	public CustomerProfileSchemaInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		jdbcTemplate.execute("""
				IF COL_LENGTH('dbo.customers', 'username') IS NULL
				    ALTER TABLE [dbo].[customers] ADD [username] varchar(50) NULL
				""");
		jdbcTemplate.execute("ALTER TABLE [dbo].[customers] ALTER COLUMN [full_name] nvarchar(255) NULL");
		jdbcTemplate.execute("ALTER TABLE [dbo].[customers] ALTER COLUMN [phone] varchar(20) NULL");
		jdbcTemplate.execute("""
				IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_customers_phone' AND object_id = OBJECT_ID(N'dbo.customers'))
				    DROP INDEX [UX_customers_phone] ON [dbo].[customers]
				""");
		jdbcTemplate.execute("""
				IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_customers_phone_not_null' AND object_id = OBJECT_ID(N'dbo.customers'))
				    CREATE UNIQUE INDEX [UX_customers_phone_not_null] ON [dbo].[customers] ([phone]) WHERE [phone] IS NOT NULL
				""");
		jdbcTemplate.execute("""
				IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_customers_username_not_null' AND object_id = OBJECT_ID(N'dbo.customers'))
				    CREATE UNIQUE INDEX [UX_customers_username_not_null] ON [dbo].[customers] ([username]) WHERE [username] IS NOT NULL
				""");
	}
}

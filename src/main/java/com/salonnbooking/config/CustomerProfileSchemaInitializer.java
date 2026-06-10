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

		jdbcTemplate.execute("""
        IF EXISTS (SELECT 1 FROM sys.indexes 
                   WHERE name = N'UX_customers_phone'
                   AND object_id = OBJECT_ID(N'dbo.customers'))
            DROP INDEX [UX_customers_phone] ON [dbo].[customers]
    """);

		jdbcTemplate.execute("""
        IF EXISTS (SELECT 1 FROM sys.indexes 
                   WHERE name = N'UX_customers_phone_not_null'
                   AND object_id = OBJECT_ID(N'dbo.customers'))
            DROP INDEX [UX_customers_phone_not_null] ON [dbo].[customers]
    """);

		jdbcTemplate.execute("""
        IF EXISTS (SELECT 1 FROM sys.indexes 
                   WHERE name = N'UKm3iom37efaxd5eucmxjqqcbe9'
                   AND object_id = OBJECT_ID(N'dbo.customers'))
            DROP INDEX [UKm3iom37efaxd5eucmxjqqcbe9] ON [dbo].[customers]
    """);

		jdbcTemplate.execute("ALTER TABLE [dbo].[customers] ALTER COLUMN [full_name] nvarchar(255) NULL");
		jdbcTemplate.execute("ALTER TABLE [dbo].[customers] ALTER COLUMN [phone] varchar(20) NULL");

		jdbcTemplate.execute("""
        IF NOT EXISTS (SELECT 1 FROM sys.indexes 
                       WHERE name = N'UX_customers_phone_not_null'
                       AND object_id = OBJECT_ID(N'dbo.customers'))
            CREATE UNIQUE INDEX [UX_customers_phone_not_null]
            ON [dbo].[customers] ([phone])
            WHERE [phone] IS NOT NULL
    """);

		jdbcTemplate.execute("""
        IF NOT EXISTS (SELECT 1 FROM sys.indexes 
                       WHERE name = N'UX_customers_username_not_null'
                       AND object_id = OBJECT_ID(N'dbo.customers'))
            CREATE UNIQUE INDEX [UX_customers_username_not_null]
            ON [dbo].[customers] ([username])
            WHERE [username] IS NOT NULL
    """);

		jdbcTemplate.execute("""
        DECLARE @constraintName sysname;
        DECLARE status_constraints CURSOR LOCAL FAST_FORWARD FOR
            SELECT cc.[name]
            FROM sys.check_constraints cc
            JOIN sys.columns c
                ON c.[object_id] = cc.[parent_object_id]
               AND c.[column_id] = cc.[parent_column_id]
            WHERE cc.[parent_object_id] = OBJECT_ID(N'dbo.appointments')
              AND c.[name] = N'status';

        OPEN status_constraints;
        FETCH NEXT FROM status_constraints INTO @constraintName;

        WHILE @@FETCH_STATUS = 0
        BEGIN
            EXEC(N'ALTER TABLE [dbo].[appointments] DROP CONSTRAINT [' + @constraintName + N']');
            FETCH NEXT FROM status_constraints INTO @constraintName;
        END

        CLOSE status_constraints;
        DEALLOCATE status_constraints;

        ALTER TABLE [dbo].[appointments] ADD CONSTRAINT [CK_appointment_status]
        CHECK ([status] IN (N'pending', N'confirmed', N'in_progress', N'awaiting_payment', N'completed', N'cancelled', N'paid'));
    """);
	}
}

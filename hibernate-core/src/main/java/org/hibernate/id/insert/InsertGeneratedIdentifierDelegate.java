/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id.insert;

import java.sql.PreparedStatement;

import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.jdbc.mutation.JdbcValueBindings;
import org.hibernate.engine.jdbc.mutation.group.PreparedStatementDetails;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.BasicEntityIdentifierMapping;
import org.hibernate.sql.model.ast.builder.TableInsertBuilder;

/**
 * Each implementation defines a strategy for retrieving a primary key
 * {@linkplain org.hibernate.tuple.InDatabaseGenerator generated by
 * the database} from the database after execution of an {@code insert}
 * statement. The generated primary key is usually an {@code IDENTITY}
 * column, but in principle it might be something else, for example,
 * a value generated by a trigger.
 * <p>
 * An implementation controls:
 * <ul>
 * <li>building the SQL {@code insert} statement, and
 * <li>retrieving the generated identifier value using JDBC.
 * </ul>
 * The implementation should be written to handle any instance of
 * {@link org.hibernate.tuple.InDatabaseGenerator}.
 *
 * @see org.hibernate.tuple.InDatabaseGenerator
 *
 * @author Steve Ebersole
 */
public interface InsertGeneratedIdentifierDelegate {
	/**
	 * Create a {@link TableInsertBuilder} with any specific identity
	 * handling already built in.
	 */
	TableInsertBuilder createTableInsertBuilder(
			BasicEntityIdentifierMapping identifierMapping,
			Expectation expectation,
			SessionFactoryImplementor sessionFactory);

	PreparedStatement prepareStatement(String insertSql, SharedSessionContractImplementor session);

	/**
	 * Perform the {@code insert} and extract the database-generated
	 * primary key value.
	 *
	 * @see #createTableInsertBuilder
	 */
	Object performInsert(
			PreparedStatementDetails insertStatementDetails,
			JdbcValueBindings valueBindings,
			Object entity,
			SharedSessionContractImplementor session);

	/**
	 * Build an {@linkplain org.hibernate.sql.Insert insert statement}
	 * specific to the delegate's mode of handling generated key values.
	 *
	 * @param context A context to help generate SQL strings
	 * @return An {@link IdentifierGeneratingInsert}
	 *
	 * @deprecated this is no longer called
	 */
	@Deprecated(since = "6.2")
	IdentifierGeneratingInsert prepareIdentifierGeneratingInsert(SqlStringGenerationContext context);

	/**
	 * Append SQL specific to this delegate's mode of handling generated
	 * primary key values to the given {@code insert} statement.
	 *
	 * @return The processed {@code insert} statement string
	 */
	default String prepareIdentifierGeneratingInsert(String insertSQL) {
		return insertSQL;
	}

	/**
	 * Execute the given {@code insert} statement and return the generated
	 * key value.
	 *
	 * @param insertSQL The {@code insert} statement string
	 * @param session The session in which we are operating
	 * @param binder The parameter binder
	 * 
	 * @return The generated identifier value
	 */
	Object performInsert(String insertSQL, SharedSessionContractImplementor session, Binder binder);

}

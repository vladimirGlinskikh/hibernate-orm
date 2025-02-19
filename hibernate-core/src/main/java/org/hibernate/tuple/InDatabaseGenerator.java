/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tuple;

import org.hibernate.dialect.Dialect;
import org.hibernate.persister.entity.EntityPersister;

/**
 * A value generated by the database might be generated implicitly, by a trigger, or using
 * a {@code default} column value specified in DDL, for example, or it might be generated
 * by a SQL expression occurring explicitly in the SQL {@code insert} or {@code update}
 * statement. In this case, the generated value is retrieved from the database using a SQL
 * {@code select}.
 * <p>
 * Implementations should override {@link #referenceColumnsInSql(Dialect)},
 * {@link #writePropertyValue()}, and {@link #getReferencedColumnValues(Dialect)} as needed
 * in order to achieve the desired behavior.
 * <p>
 * In implementation of this interface does not specify how the generated value is retrieved
 * from the database after it is generated, this being the responsibility of the coordinating
 * code in {@link org.hibernate.metamodel.mapping.internal.GeneratedValuesProcessor} or in an
 * implementation of {@link org.hibernate.id.insert.InsertGeneratedIdentifierDelegate}.
 *
 * @author Steve Ebersole
 *
 * @since 6.2
 */
public interface InDatabaseGenerator extends Generator {

	/**
	 * Determines if the columns whose values are generated are included in the column list
	 * of the SQL {@code insert} or {@code update} statement. For example, this method should
	 * return:
	 * <ul>
	 * <li>{@code true} if the value is generated by calling a SQL function like
	 *     {@code current_timestamp}, or
	 * <li>{@code false} if the value is generated by a trigger,
	 *     by {@link org.hibernate.annotations.GeneratedColumn generated always as}, or
	 *     using a {@linkplain org.hibernate.annotations.ColumnDefault column default value}.
	 * </ul>
	 *
	 * @return {@code true} if the column is included in the column list of the SQL statement.
	 */
	boolean referenceColumnsInSql(Dialect dialect);

	/**
	 * Determines if the property values are written to JDBC as the argument of a JDBC {@code ?}
	 * parameter.
	 */
	boolean writePropertyValue();

	/**
	 * A SQL expression indicating how to calculate the generated values when the mapped columns
	 * are {@linkplain #referenceColumnsInSql(Dialect) included in the SQL statement}. The SQL
	 * expressions might be:
	 * <ul>
	 * <li>function calls like {@code current_timestamp} or {@code nextval('mysequence')}, or
	 * <li>syntactic markers like {@code default}.
	 * </ul>
	 *
	 * @param dialect The {@linkplain Dialect SQL dialect}, allowing generation of an expression
	 *				  in dialect-specific SQL.
	 * @return The column value to be used in the generated SQL statement.
	 */
	String[] getReferencedColumnValues(Dialect dialect);

	/**
	 * The name of a property of the entity which may be used to locate the just-{@code insert}ed
	 * row containing the generated value. Of course, the columns mapped by this property should
	 * form a unique key of the entity.
	 * <p>
	 * This is ignored by {@link org.hibernate.metamodel.mapping.internal.GeneratedValuesProcessor},
	 * which handles multiple generators at once. This method arguably breaks the separation of
	 * concerns between the generator and the coordinating code.
	 *
	 * @see org.hibernate.id.SelectGenerator
	 */
	default String getUniqueKeyPropertyName(EntityPersister persister) {
		return null;
	}

	default boolean generatedByDatabase() {
		return true;
	}
}

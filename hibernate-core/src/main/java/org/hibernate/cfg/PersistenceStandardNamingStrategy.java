/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cfg;
import java.io.Serializable;

import org.hibernate.AssertionFailure;
import org.hibernate.internal.util.StringHelper;

/**
 * Naming strategy implementing the EJB3 standards
 *
 * @author Emmanuel Bernard
 */
@Deprecated
public class PersistenceStandardNamingStrategy implements NamingStrategy, Serializable {
	public static final NamingStrategy INSTANCE = new PersistenceStandardNamingStrategy();

	public String classToTableName(String className) {
		return StringHelper.unqualify( className );
	}

	public String propertyToColumnName(String propertyName) {
		return StringHelper.unqualify( propertyName );
	}

	public String tableName(String tableName) {
		return tableName;
	}

	public String columnName(String columnName) {
		return columnName;
	}

	public String collectionTableName(
			String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable,
			String propertyName
	) {
		String entityTableName = associatedEntityTable != null
				? associatedEntityTable
				: StringHelper.unqualify(propertyName);
		return tableName( ownerEntityTable + "_" + entityTableName );
	}

	public String joinKeyColumnName(String joinedColumn, String joinedTable) {
		return columnName( joinedColumn );
	}

	public String foreignKeyColumnName(
			String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName
	) {
		String header = propertyName != null ? StringHelper.unqualify( propertyName ) : propertyTableName;
		if ( header == null ) throw new AssertionFailure( "NamingStrategy not properly filled" );
		return columnName( header + "_" + referencedColumnName );
	}

	public String logicalColumnName(String columnName, String propertyName) {
		return StringHelper.isNotEmpty( columnName ) ? columnName : StringHelper.unqualify( propertyName );
	}

	public String logicalCollectionTableName(
			String tableName,
			String ownerEntityTable, String associatedEntityTable, String propertyName
	) {
		if ( tableName != null ) {
			return tableName;
		}
		else {
			String entityTableName = associatedEntityTable != null
					? associatedEntityTable
					: StringHelper.unqualify(propertyName);
			return ownerEntityTable + "_" + entityTableName;
		}
	}

	public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
		return StringHelper.isNotEmpty( columnName ) ?
				columnName :
				StringHelper.unqualify( propertyName ) + "_" + referencedColumn;
	}
}

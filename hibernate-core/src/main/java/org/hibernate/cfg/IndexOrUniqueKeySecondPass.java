/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

/**
 * @author Emmanuel Bernard
 */
public class IndexOrUniqueKeySecondPass implements SecondPass {
	private Table table;
	private final String indexName;
	private final String[] columns;
	private final MetadataBuildingContext buildingContext;
	private final AnnotatedColumn column;
	private final boolean unique;

	/**
	 * Build an index
	 */
	public IndexOrUniqueKeySecondPass(Table table, String indexName, String[] columns, MetadataBuildingContext buildingContext) {
		this.table = table;
		this.indexName = indexName;
		this.columns = columns;
		this.buildingContext = buildingContext;
		this.column = null;
		this.unique = false;
	}

	/**
	 * Build an index if unique is false or a Unique Key if unique is true
	 */
	public IndexOrUniqueKeySecondPass(String indexName, AnnotatedColumn column, MetadataBuildingContext buildingContext, boolean unique) {
		this.indexName = indexName;
		this.column = column;
		this.columns = null;
		this.buildingContext = buildingContext;
		this.unique = unique;
	}

	@Override
	public void doSecondPass(Map<String, PersistentClass> persistentClasses) throws MappingException {
		if ( columns != null ) {
			for ( String columnName : columns ) {
				addConstraintToColumn( columnName );
			}
		}
		if ( column != null ) {
			table = column.getParent().getTable();
			final PropertyHolder propertyHolder = column.getParent().getPropertyHolder();
			final String entityName = propertyHolder.isComponent()
					? propertyHolder.getPersistentClass().getEntityName()
					: propertyHolder.getEntityName();
			final PersistentClass persistentClass = persistentClasses.get( entityName );
			final Property property = persistentClass.getProperty( column.getParent().getPropertyName() );
			if ( property.getValue() instanceof Component ) {
				final Component component = (Component) property.getValue();
				final List<Column> columns = new ArrayList<>();
				for ( Selectable selectable: component.getSelectables() ) {
					if ( selectable instanceof Column ) {
						columns.add( (Column) selectable );
					}
				}
				addConstraintToColumns( columns );
			}
			else {
				addConstraintToColumn(
						buildingContext.getMetadataCollector()
								.getLogicalColumnName( table, column.getMappingColumn().getQuotedName() )
				);
			}
		}
	}

	private void addConstraintToColumn(final String columnName ) {
		Column column = table.getColumn( buildingContext.getMetadataCollector(), columnName );
		if ( column == null ) {
			throw new AnnotationException(
					"Table '" + table.getName() + "' has no column named '" + columnName
							+ "' matching the column specified in '@Index'"
			);
		}
		if ( unique ) {
			table.getOrCreateUniqueKey( indexName ).addColumn( column );
		}
		else {
			table.getOrCreateIndex( indexName ).addColumn( column );
		}
	}

	private void addConstraintToColumns(List<Column> columns) {
		if ( unique ) {
			final UniqueKey uniqueKey = table.getOrCreateUniqueKey( indexName );
			for ( Column column : columns ) {
				uniqueKey.addColumn( column );
			}
		}
		else {
			final Index index = table.getOrCreateIndex( indexName );
			for ( Column column : columns ) {
				index.addColumn( column );
			}
		}
	}
}

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.internal;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.metamodel.mapping.EntityVersionMapping;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.descriptor.java.VersionJavaType;

import org.jboss.logging.Logger;

/**
 * Utilities for dealing with optimistic locking values.
 *
 * @author Gavin King
 */
public final class Versioning {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			Versioning.class.getName()
	);

	/**
	 * Private constructor disallowing instantiation.
	 */
	private Versioning() {
	}

	/**
	 * Create an initial optimistic locking value according the {@link EntityVersionMapping}
	 * contract for the version property.
	 *
	 * @param versionMapping The version mapping
	 * @param session The originating session
	 * @return The initial optimistic locking value
	 */
	private static Object seed(EntityVersionMapping versionMapping, SharedSessionContractImplementor session) {
		final Object seed = versionMapping.getJavaType().seed(
				versionMapping.getLength(),
				versionMapping.getPrecision(),
				versionMapping.getScale(),
				session
		);
		LOG.tracef( "Seeding: %s", seed );
		return seed;
	}

	/**
	 * Create an initial optimistic locking value according the {@link VersionJavaType}
	 * contract for the version property <b>if required</b> and inject it into
	 * the snapshot state.
	 *
	 * @param fields The current snapshot state
	 * @param versionProperty The index of the version property
	 * @param versionMapping The version mapping
	 * @param session The originating session
	 * @return True if we injected a new version value into the fields array; false
	 * otherwise.
	 */
	public static boolean seedVersion(
			Object[] fields,
			int versionProperty,
			EntityVersionMapping versionMapping,
			SharedSessionContractImplementor session) {
		final Object initialVersion = fields[versionProperty];
		if (
			initialVersion==null ||
			// This next bit is to allow for both unsaved-value="negative"
			// and for "older" behavior where version number did not get
			// seeded if it was already set in the object
			// TODO: shift it into unsaved-value strategy
			( (initialVersion instanceof Number) && ( (Number) initialVersion ).longValue()<0 )
		) {
			fields[versionProperty] = seed( versionMapping, session );
			return true;
		}
		LOG.tracev( "Using initial version: {0}", initialVersion );
		return false;
	}


	/**
	 * Generate the next increment in the optimistic locking value according
	 * the {@link VersionJavaType} contract for the version property.
	 *
	 * @param version The current version
	 * @param versionMapping The version mapping
	 * @param session The originating session
	 * @return The incremented optimistic locking value.
	 */
	public static Object increment(Object version, EntityVersionMapping versionMapping, SharedSessionContractImplementor session) {
		@SuppressWarnings("unchecked")
		final VersionJavaType<Object> versionType = (VersionJavaType<Object>) versionMapping.getJavaType();
		final Object next = versionType.next(
				version,
				versionMapping.getLength(),
				versionMapping.getPrecision(),
				versionMapping.getScale(),
				session
		);
		if ( LOG.isTraceEnabled() ) {
			LOG.tracef(
					"Incrementing: %s to %s",
					versionType.toString( version ),
					versionType.toString( next )
			);
		}
		return next;
	}

	/**
	 * Inject the optimistic locking value into the entity state snapshot.
	 *
	 * @param fields The state snapshot
	 * @param version The optimistic locking value
	 * @param persister The entity persister
	 */
	public static void setVersion(Object[] fields, Object version, EntityPersister persister) {
		if ( !persister.isVersioned() ) {
			return;
		}
		fields[ persister.getVersionProperty() ] = version;
	}

	/**
	 * Extract the optimistic locking value out of the entity state snapshot.
	 *
	 * @param fields The state snapshot
	 * @param persister The entity persister
	 * @return The extracted optimistic locking value
	 */
	public static Object getVersion(Object[] fields, EntityPersister persister) {
		if ( !persister.isVersioned() ) {
			return null;
		}
		return fields[ persister.getVersionProperty() ];
	}

	/**
	 * Do we need to increment the version number, given the dirty properties?
	 *
	 * @param dirtyProperties The array of property indexes which were deemed dirty
	 * @param hasDirtyCollections Were any collections found to be dirty (structurally changed)
	 * @param propertyVersionability An array indicating versionability of each property.
	 * @return True if a version increment is required; false otherwise.
	 */
	public static boolean isVersionIncrementRequired(
			final int[] dirtyProperties,
			final boolean hasDirtyCollections,
			final boolean[] propertyVersionability) {
		if ( hasDirtyCollections ) {
			return true;
		}

		if ( dirtyProperties != null ) {
			for ( int dirtyProperty : dirtyProperties ) {
				if ( propertyVersionability[dirtyProperty] ) {
					return true;
				}
			}
		}

		return false;
	}
}

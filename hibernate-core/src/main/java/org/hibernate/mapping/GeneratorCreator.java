/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.mapping;

import org.hibernate.Internal;
import org.hibernate.tuple.Generator;
import org.hibernate.tuple.GeneratorCreationContext;

@Internal
@FunctionalInterface
public interface GeneratorCreator {
	Generator createGenerator(GeneratorCreationContext context);
}

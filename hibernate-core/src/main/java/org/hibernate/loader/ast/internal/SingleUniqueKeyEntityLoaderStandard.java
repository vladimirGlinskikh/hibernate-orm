/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.loader.ast.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.ast.spi.SingleUniqueKeyEntityLoader;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.internal.ToOneAttributeMapping;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.QueryOptionsAdapter;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.internal.CallbackImpl;
import org.hibernate.sql.exec.internal.JdbcParameterBindingsImpl;
import org.hibernate.sql.exec.spi.Callback;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.results.spi.ListResultsConsumer;

/**
 * @author Steve Ebersole
 */
public class SingleUniqueKeyEntityLoaderStandard<T> implements SingleUniqueKeyEntityLoader<T> {
	private final EntityMappingType entityDescriptor;
	private final ModelPart uniqueKeyAttribute;

	public SingleUniqueKeyEntityLoaderStandard(
			EntityMappingType entityDescriptor,
			SingularAttributeMapping uniqueKeyAttribute) {
		this.entityDescriptor = entityDescriptor;
		if ( uniqueKeyAttribute instanceof ToOneAttributeMapping ) {
			this.uniqueKeyAttribute = ( (ToOneAttributeMapping) uniqueKeyAttribute ).getForeignKeyDescriptor();
		}
		else {
			this.uniqueKeyAttribute = uniqueKeyAttribute;
		}
	}

	@Override
	public EntityMappingType getLoadable() {
		return entityDescriptor;
	}

	@Override
	public T load(
			Object ukValue,
			LockOptions lockOptions,
			Boolean readOnly,
			SharedSessionContractImplementor session) {
		final SessionFactoryImplementor sessionFactory = session.getFactory();

		// todo (6.0) : cache the SQL AST and JdbcParameters
		final List<JdbcParameter> jdbcParameters = new ArrayList<>();
		final SelectStatement sqlAst = LoaderSelectBuilder.createSelectByUniqueKey(
				entityDescriptor,
				Collections.emptyList(),
				uniqueKeyAttribute,
				null,
				1,
				LoadQueryInfluencers.NONE,
				LockOptions.NONE,
				jdbcParameters::add,
				sessionFactory
		);

		final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
		final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
		final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

		final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl( jdbcParameters.size() );
		int offset = jdbcParameterBindings.registerParametersForEachJdbcValue(
				ukValue,
				Clause.WHERE,
				uniqueKeyAttribute,
				jdbcParameters,
				session
		);
		assert offset == jdbcParameters.size();
		final JdbcOperationQuerySelect jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator( sessionFactory, sqlAst )
				.translate( jdbcParameterBindings, QueryOptions.NONE );

		final List<Object> list = sessionFactory.getJdbcServices().getJdbcSelectExecutor().list(
				jdbcSelect,
				jdbcParameterBindings,
				new ExecutionContext() {
					private final Callback callback = new CallbackImpl();
					@Override
					public SharedSessionContractImplementor getSession() {
						return session;
					}

					@Override
					public QueryOptions getQueryOptions() {
						return new QueryOptionsAdapter() {
							@Override
							public Boolean isReadOnly() {
								return readOnly;
							}
						};
					}

					@Override
					public String getQueryIdentifier(String sql) {
						return sql;
					}

					@Override
					public QueryParameterBindings getQueryParameterBindings() {
						return QueryParameterBindings.NO_PARAM_BINDINGS;
					}

					@Override
					public Callback getCallback() {
						return callback;
					}

				},
				row -> row[0],
				ListResultsConsumer.UniqueSemantic.FILTER
		);

		switch ( list.size() ) {
			case 0:
				return null;
			case 1:
				//noinspection unchecked
				return (T) list.get( 0 );
		}
		throw new HibernateException(
				"More than one row with the given identifier was found: " +
						ukValue +
						", for class: " +
						entityDescriptor.getEntityName()
		);
	}

	@Override
	public Object resolveId(Object ukValue, SharedSessionContractImplementor session) {
		final SessionFactoryImplementor sessionFactory = session.getFactory();

		// todo (6.0) : cache the SQL AST and JdbcParameters
		final List<JdbcParameter> jdbcParameters = new ArrayList<>();
		final SelectStatement sqlAst = LoaderSelectBuilder.createSelectByUniqueKey(
				entityDescriptor,
				Collections.singletonList( entityDescriptor.getIdentifierMapping() ),
				uniqueKeyAttribute,
				null,
				1,
				LoadQueryInfluencers.NONE,
				LockOptions.NONE,
				jdbcParameters::add,
				sessionFactory
		);

		final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
		final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
		final SqlAstTranslatorFactory sqlAstTranslatorFactory = jdbcEnvironment.getSqlAstTranslatorFactory();

		final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl( jdbcParameters.size() );
		int offset = jdbcParameterBindings.registerParametersForEachJdbcValue(
				ukValue,
				Clause.WHERE,
				uniqueKeyAttribute,
				jdbcParameters,
				session
		);
		assert offset == jdbcParameters.size();
		final JdbcOperationQuerySelect jdbcSelect = sqlAstTranslatorFactory.buildSelectTranslator( sessionFactory, sqlAst )
				.translate( jdbcParameterBindings, QueryOptions.NONE );

		final List<Object> list = sessionFactory.getJdbcServices().getJdbcSelectExecutor().list(
				jdbcSelect,
				jdbcParameterBindings,
				new ExecutionContext() {
					@Override
					public SharedSessionContractImplementor getSession() {
						return session;
					}

					@Override
					public QueryOptions getQueryOptions() {
						return QueryOptions.NONE;
					}

					@Override
					public String getQueryIdentifier(String sql) {
						return sql;
					}

					@Override
					public QueryParameterBindings getQueryParameterBindings() {
						return QueryParameterBindings.NO_PARAM_BINDINGS;
					}

					@Override
					public Callback getCallback() {
						throw new UnsupportedOperationException( "Follow-on locking not supported yet" );
					}

				},
				row -> row[0],
				ListResultsConsumer.UniqueSemantic.FILTER
		);

		assert list.size() == 1;

		return list.get( 0 );
	}
}

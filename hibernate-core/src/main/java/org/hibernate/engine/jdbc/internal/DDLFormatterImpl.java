/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.jdbc.internal;

import java.util.Locale;
import java.util.StringTokenizer;

import org.hibernate.internal.util.StringHelper;

/**
 * Performs formatting of DDL SQL statements.
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class DDLFormatterImpl implements Formatter {

	private static final String INITIAL_LINE = System.lineSeparator() + "    ";
	private static final String OTHER_LINES = System.lineSeparator() + "       ";
	/**
	 * Singleton access
	 */
	public static final DDLFormatterImpl INSTANCE = new DDLFormatterImpl();

	@Override
	public String format(String sql) {
		if ( StringHelper.isEmpty( sql ) ) {
			return sql;
		}

		String lowerCaseSql = sql.toLowerCase(Locale.ROOT);
		if ( lowerCaseSql.startsWith( "create table" ) ) {
			return formatCreateTable( sql );
		}
		else if ( lowerCaseSql.startsWith( "create index" )
				|| lowerCaseSql.startsWith("create unique") ) {
			return formatAlterTable( sql );
		}
		else if ( lowerCaseSql.startsWith( "create" ) ) {
			return sql;
		}
		else if ( lowerCaseSql.startsWith( "alter table" ) ) {
			return formatAlterTable( sql );
		}
		else if ( lowerCaseSql.startsWith( "comment on" ) ) {
			return formatCommentOn( sql );
		}
		else {
			return INITIAL_LINE + sql;
		}
	}

	private String formatCommentOn(String sql) {
		final StringBuilder result = new StringBuilder( 60 ).append( INITIAL_LINE );
		final StringTokenizer tokens = new StringTokenizer( sql, " '[]\"", true );

		boolean quoted = false;
		while ( tokens.hasMoreTokens() ) {
			final String token = tokens.nextToken();
			result.append( token );
			if ( isQuote( token ) ) {
				quoted = !quoted;
			}
			else if ( !quoted ) {
				if ( "is".equals( token ) ) {
					result.append( OTHER_LINES );
				}
			}
		}

		return result.toString();
	}

	private String formatAlterTable(String sql) {
		final StringBuilder result = new StringBuilder( 60 ).append( INITIAL_LINE );
		final StringTokenizer tokens = new StringTokenizer( sql, " (,)'[]\"", true );

		boolean quoted = false;
		while ( tokens.hasMoreTokens() ) {
			final String token = tokens.nextToken();
			if ( isQuote( token ) ) {
				quoted = !quoted;
			}
			else if ( !quoted ) {
				if ( isBreak( token ) ) {
					result.append( OTHER_LINES );
				}
			}
			result.append( token );
		}

		return result.toString();
	}

	private String formatCreateTable(String sql) {
		final StringBuilder result = new StringBuilder( 60 ).append( INITIAL_LINE );
		final StringTokenizer tokens = new StringTokenizer( sql, "(,)'[]\"", true );

		int depth = 0;
		boolean quoted = false;
		while ( tokens.hasMoreTokens() ) {
			final String token = tokens.nextToken();
			if ( isQuote( token ) ) {
				quoted = !quoted;
				result.append( token );
			}
			else if ( quoted ) {
				result.append( token );
			}
			else {
				if ( ")".equals( token ) ) {
					depth--;
					if ( depth == 0 ) {
						result.append( INITIAL_LINE );
					}
				}
				result.append( token );
				if ( ",".equals( token ) && depth == 1 ) {
					result.append( OTHER_LINES );
				}
				if ( "(".equals( token ) ) {
					depth++;
					if ( depth == 1 ) {
						result.append( OTHER_LINES );
					}
				}
			}
		}

		return result.toString();
	}

	private static boolean isBreak(String token) {
		return "drop".equals( token ) ||
				"add".equals( token ) ||
				"references".equals( token ) ||
				"foreign".equals( token ) ||
				"on".equals( token );
	}

	private static boolean isQuote(String tok) {
		return "\"".equals( tok ) ||
				"`".equals( tok ) ||
				"]".equals( tok ) ||
				"[".equals( tok ) ||
				"'".equals( tok );
	}

}

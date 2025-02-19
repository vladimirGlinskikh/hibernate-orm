/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.engine.jdbc;

import org.hibernate.Length;

import java.io.Serializable;

/**
 * Models size restrictions/requirements on a column's datatype.
 * <p>
 * IMPL NOTE: since we do not necessarily know the datatype up front, and therefore do not necessarily know
 * whether length or precision/scale sizing is needed, we simply account for both here.  Additionally LOB
 * definitions, by standard, are allowed a "multiplier" consisting of 'K' (Kb), 'M' (Mb) or 'G' (Gb).
 *
 * @author Steve Ebersole
 */
public class Size implements Serializable {
	public enum LobMultiplier {
		NONE( 1 ),
		K( NONE.factor * 1024 ),
		M( K.factor * 1024 ),
		G( M.factor * 1024 );

		private final long factor;

		LobMultiplier(long factor) {
			this.factor = factor;
		}

		public long getFactor() {
			return factor;
		}
	}

	public static final long DEFAULT_LENGTH = Length.DEFAULT;
	public static final long LONG_LENGTH = Length.LONG;
	public static final long DEFAULT_LOB_LENGTH = Length.LOB_DEFAULT;
	public static final int DEFAULT_PRECISION = 19;
	public static final int DEFAULT_SCALE = 2;

	private Integer precision;
	private Integer scale;

	private Long length;
	private LobMultiplier lobMultiplier;

	public Size() {
	}

	/**
	 * Complete constructor.
	 *
	 * @param precision numeric precision
	 * @param scale numeric scale
	 * @param length type length
	 * @param lobMultiplier LOB length multiplier
	 */
	public Size(Integer precision, Integer scale, Long length, LobMultiplier lobMultiplier) {
		this.precision = precision;
		this.scale = scale;
		this.length = length;
		this.lobMultiplier = lobMultiplier;
	}

	public Size(Integer precision, Integer scale, Integer length, LobMultiplier lobMultiplier) {
		this.precision = precision;
		this.scale = scale;
		this.length = length == null ?  null : length.longValue();
		this.lobMultiplier = lobMultiplier;
	}

	public static Size nil() {
		return new Size();
	}

	public static Size precision(int precision) {
		return new Size( precision, -1, -1L, null );
	}

	public static Size precision(int precision, int scale) {
		return new Size( precision, scale, -1L, null );
	}

	public static Size length(long length) {
		return new Size( -1, -1, length, null );
	}

	public static Size length(long length, LobMultiplier lobMultiplier) {
		return new Size( -1, -1, length, lobMultiplier );
	}

	public Integer getPrecision() {
		return precision;
	}

	public Integer getScale() {
		return scale;
	}

	public Long getLength() {
		return length;
	}

	public LobMultiplier getLobMultiplier() {
		return lobMultiplier;
	}

	public void initialize(Size size) {
		this.precision = size.precision;
		this.scale =  size.scale;
		this.length = size.length;
	}

	public Size setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}

	public Size setScale(Integer scale) {
		this.scale = scale;
		return this;
	}

	public Size setLength(Long length) {
		this.length = length;
		return this;
	}

	public Size setLobMultiplier(LobMultiplier lobMultiplier) {
		this.lobMultiplier = lobMultiplier;
		return this;
	}
}

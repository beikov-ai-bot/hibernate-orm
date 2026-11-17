package org.hibernate.orm.test.dialect;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.sequence.spi.SequenceSupport;
import org.hibernate.testing.orm.junit.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL Server rejects {@code next value for} inside a set operation, so before
 * SQL Server 2022 (which provides {@code generate_series}) the dialect must not
 * claim that it can fetch several sequence values with one statement. Doing so
 * would make the sequence generator emit an invalid {@code union all} query.
 */
@BaseUnitTest
public class SQLServerSequenceBatchSupportTest {

	@Test
	public void testOlderVersionsDoNotSupportMultiValueFetch() {
		for ( int major : new int[] { 11, 12, 13, 14, 15 } ) {
			final SequenceSupport support = sequenceSupport( major );
			assertThat( support.supportsSequences() ).isTrue();
			assertThat( support.supportsSelectNextValParameterized() ).as( "version " + major ).isFalse();
			assertThat( support.getFromGenerateSeries() ).isNull();
		}
	}

	@Test
	public void testSqlServer2022SupportsMultiValueFetch() {
		final SequenceSupport support = sequenceSupport( 16 );
		assertThat( support.supportsSelectNextValParameterized() ).isTrue();
		assertThat( support.getSequenceNextValParameterizedString( "seq" ) )
				.isEqualTo( "select next value for seq from generate_series(1,?)" );
	}

	private static SequenceSupport sequenceSupport(int major) {
		return new SQLServerDialect( DatabaseVersion.make( major ) ).getSequenceSupport();
	}
}

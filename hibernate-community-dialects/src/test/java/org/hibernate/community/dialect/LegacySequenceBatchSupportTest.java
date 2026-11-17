package org.hibernate.community.dialect;

import org.hibernate.community.dialect.sequence.LegacyDB2SequenceSupport;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.sequence.spi.SequenceSupport;
import org.hibernate.testing.orm.junit.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-value sequence fetching for legacy dialects: the DB2 recursive CTE
 * must declare its column list, and the legacy SQL Server dialect may only
 * advertise multi-value fetching when it can render valid SQL for it.
 */
@BaseUnitTest
public class LegacySequenceBatchSupportTest {

	@Test
	public void testLegacyDB2CteDeclaresColumnList() {
		assertThat( LegacyDB2SequenceSupport.INSTANCE.getSequenceNextValParameterizedString( "seq" ) )
				.contains( "with cte(v) as (select 1 from sysibm.sysdummy1 t union all" );
	}

	@Test
	public void testLegacySqlServerBeforeSqlServer2022() {
		final SequenceSupport support =
				new SQLServerLegacyDialect( DatabaseVersion.make( 15 ) ).getSequenceSupport();
		assertThat( support.supportsSelectNextValParameterized() ).isFalse();
	}

	@Test
	public void testLegacySqlServer2022() {
		final SequenceSupport support =
				new SQLServerLegacyDialect( DatabaseVersion.make( 16 ) ).getSequenceSupport();
		assertThat( support.supportsSelectNextValParameterized() ).isTrue();
		assertThat( support.getSequenceNextValParameterizedString( "seq" ) )
				.isEqualTo( "select next value for seq from generate_series(1,?)" );
	}
}

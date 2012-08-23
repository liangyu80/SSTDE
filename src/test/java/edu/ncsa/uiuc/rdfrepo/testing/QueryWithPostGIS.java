package edu.ncsa.uiuc.rdfrepo.testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.postgis.PGgeometry;

import com.vividsolutions.jts.io.ParseException;

public class QueryWithPostGIS {
	public static void main(String[] args) throws ParseException,
			java.text.ParseException, SQLException, ClassNotFoundException {

		// testNormalStatement();
		// testPreparedStatement();
//		testQueryWithLimit();
//		testQueryWithLimit();
//		testQueryWithLimit2();
		
		testQuery();
	}

	private static void testNormalStatement() throws ClassNotFoundException,
			SQLException {
		// firt query canddiate from postgis
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		// String date = "2011-05-15T23:30:00";
		// String wkt = "POLYGON((-91 33,-75 33,-75 42,-91 42,-91 33))";
		// String sql =
		// "SELECT * FROM idxst2 WHERE st_within(coord, ST_GeomFromText('"
		// + wkt + "')) and timevalue > ' " + date + "'";
		String sql = "SELECT observation FROM spatial_temporal_index WHERE ST_within(wkt,'POLYGON((-91 33,-75 33,-75 42,-91 42,-91 33))')=true";
		Class.forName("org.postgresql.Driver");
		Connection connection = null;
		connection = DriverManager.getConnection(
				"jdbc:postgresql://localhost:5432/useekm_iacat", "postgres",
				"zsmpM942");

		Statement statement = connection.createStatement();
		statement.setFetchSize(100);
		long s = System.currentTimeMillis();
		List<Long> ids = new ArrayList<Long>();

		ResultSet resultSet = statement.executeQuery(sql);
		int i = 0;
		for (; resultSet.next(); i++) {
			String observation = resultSet.getString("observation");
			// System.out.println( resultSet.getArray("nodeid"));
			// ids.add(resultSet.getLong("nodeid"));

		}
		System.out.println("filtered size = " + i);
		System.out.println(System.currentTimeMillis() - s);
	}

	private static void testPreparedStatement() throws SQLException,
			ClassNotFoundException, java.text.ParseException {
		String dateString = "2012-01-13 11:45:00";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = format.parse(dateString);
		String wkt = "POLYGON((-91 33,-75 33,-75 42,-91 42,-91 33))";
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/useekm_iacat");
		dataSource.setUsername("postgres");
		dataSource.setPassword("zsmpM942");
		//
		// Class.forName("org.postgresql.Driver");
		// Connection connection = null;
		// connection = DriverManager.getConnection(
		// "jdbc:postgresql://localhost:5432/useekm_iacat", "postgres",
		// "zsmpM942");
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		String sql = "SELECT observation FROM spatial_temporal_index WHERE ST_within(wkt,?)=true AND timevalue>=? ";
		PreparedStatement statement = connection.prepareStatement(sql);
		PGgeometry geometry = new PGgeometry(wkt.toUpperCase());
		statement.setObject(1, geometry, Types.OTHER);
		statement.setObject(2, new Timestamp(date.getTime()), Types.TIMESTAMP);

		long s = System.currentTimeMillis();
		statement.setFetchSize(100);

		ResultSet resultSet = statement.executeQuery();

		int i = 0;
		for (; resultSet.next(); i++) {
			String observation = resultSet.getString("observation");
			// System.out.println( resultSet.getArray("nodeid"));
			// ids.add(resultSet.getLong("nodeid"));

		}
		System.out.println("filtered size = " + i);
		System.out.println(System.currentTimeMillis() - s);
	}

	String a = "SELECT timevalue FROM frame_temporal_index WHERE stream='http://iacat-enviro.ncsa.illinois.edu/EBITileDrainSensor/CornSoy/site/00000004#LEVEL'  ORDER BY timevalue DESC limit 1";

	private static void testQueryWithLimit() throws SQLException,
			ClassNotFoundException, java.text.ParseException {
		// String dateString = "2012-01-13 11:45:00";
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// Date date = format.parse(dateString);
		// String wkt = "POLYGON((-91 33,-75 33,-75 42,-91 42,-91 33))";
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/useekm_iacat");
		dataSource.setUsername("postgres");
		dataSource.setPassword("zsmpM942");
		//
		// Class.forName("org.postgresql.Driver");
		// Connection connection = null;
		// connection = DriverManager.getConnection(
		// "jdbc:postgresql://localhost:5432/useekm_iacat", "postgres",
		// "zsmpM942");
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		String sql = "SELECT timevalue FROM frame_temporal_index WHERE stream='http://iacat-enviro.ncsa.illinois.edu/EBITileDrainSensor/CornSoy/site/00000004#LEVEL'  ORDER BY timevalue DESC limit 1000";
		sql = "SELECT * FROM frame_temporal_index where timevalue > '2011-10-01 00:00:00' limit 1000000";
		PreparedStatement statement = connection.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
				ResultSet.HOLD_CURSORS_OVER_COMMIT);

		long s = System.currentTimeMillis();
		// statement.setFetchSize(1);
		// statement.setMaxRows(1);
		ResultSet resultSet = statement.executeQuery();

		int i = 0;
//		
//		resultSet.getString(0);
//		boolean b = resultSet.absolute(101);
//		int row = resultSet.getRow();
		for (; resultSet.next(); i++) {
			// String observation = resultSet.getString("observation");
			// System.out.println( resultSet.getArray("nodeid"));
			// ids.add(resultSet.getLong("nodeid"));
			// if (i > 1) {
			// break;
			// }

		}
//		System.out.println("filtered size = " + i);
		System.out.println(System.currentTimeMillis() - s);
	}

	private static void testQueryWithLimit2() throws SQLException,
			ClassNotFoundException, java.text.ParseException {
		// String dateString = "2012-01-13 11:45:00";
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// Date date = format.parse(dateString);
		// String wkt = "POLYGON((-91 33,-75 33,-75 42,-91 42,-91 33))";
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/useekm_iacat");
		dataSource.setUsername("postgres");
		dataSource.setPassword("zsmpM942");
		//
		// Class.forName("org.postgresql.Driver");
		// Connection connection = null;
		// connection = DriverManager.getConnection(
		// "jdbc:postgresql://localhost:5432/useekm_iacat", "postgres",
		// "zsmpM942");
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		String sql = "SELECT timevalue FROM frame_temporal_index WHERE stream='http://iacat-enviro.ncsa.illinois.edu/EBITileDrainSensor/CornSoy/site/00000004#LEVEL'  ORDER BY timevalue DESC limit 1000";
		sql = "SELECT * FROM frame_temporal_index where timevalue > '2011-10-01 00:00:00' limit 10000";
		PreparedStatement statement = connection.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
				ResultSet.HOLD_CURSORS_OVER_COMMIT);

		long s = System.currentTimeMillis();
		// statement.setFetchSize(1);
		// statement.setMaxRows(1);
		
		for (int index = 0; index < 100; index++) {
			ResultSet resultSet = statement.executeQuery();

			int i = 0;
//			boolean b = resultSet.absolute(100);
			for (; resultSet.next(); i++) {
				// String observation = resultSet.getString("observation");
				// System.out.println( resultSet.getArray("nodeid"));
				// ids.add(resultSet.getLong("nodeid"));
				// if (i > 1) {
				// break;
				// }
			}
		}

//		System.out.println("filtered size = " + i);
		System.out.println(System.currentTimeMillis() - s);
	}
	
	private static void testQuery() throws SQLException{
		String sql = "SELECT frame FROM frame_temporal_index WHERE stream='http://iacat-enviro.ncsa.illinois.edu/EBITileDrainSensor/CornSoy/site/00000004/FLOW'  ORDER BY frame ASC  limit '201'";
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/useekm_iacat");
		dataSource.setUsername("postgres");
		dataSource.setPassword("zsmpM942");
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery(sql);
		
		int i =0;
		while(result.next()){
			System.out.println(i++);
		}
		
	}

}

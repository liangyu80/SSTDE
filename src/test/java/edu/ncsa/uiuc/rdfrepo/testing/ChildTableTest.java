package edu.ncsa.uiuc.rdfrepo.testing;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

public class ChildTableTest {
	public static void main(String[] args) throws SQLException {
		String sql = "SELECT frame FROM frame_temporal_index WHERE stream='http://iacat-enviro.ncsa.illinois.edu/EBITileDrainSensor/CornSoy/site/00000004/FLOW'  ORDER BY frame ASC  limit '201'";
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/useekm_iacat");
		dataSource.setUsername("postgres");
		dataSource.setPassword("zsmpM942");
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery(sql);
		while (result.next()) {
			
			
		}
	}
	


	private String generateScript(String parrent){
		return null;
	}
}

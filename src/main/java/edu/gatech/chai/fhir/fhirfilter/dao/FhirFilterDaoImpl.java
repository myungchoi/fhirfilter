package edu.gatech.chai.fhir.fhirfilter.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.gatech.chai.fhir.fhirfilter.model.FilterData;

@Component
public class FhirFilterDaoImpl implements FhirFilterDao {
	final static Logger logger = LoggerFactory.getLogger(FhirFilterDaoImpl.class);

	@Override
	public Connection connect() {
		String url = "jdbc:sqlite::resource:fhirfilter.db";
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(url);
			logger.info("Connected to database");
		} catch (SQLException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}

		return conn;
	}

	@Override
	public int save(FilterData filterData) {
		String sql = "INSERT INTO filterdata (effective_start_date, effective_end_date, json_string) values (?,?,?)";

		int insertedId = 0;
		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, filterData.getEffectiveStartDate().getTime());
			pstmt.setLong(2, filterData.getEffectiveEndDate().getTime());
			
			// remove id from json data.
			filterData.getJsonObject().remove("id");
			pstmt.setString(3, filterData.toString());
			
		    if (pstmt.executeUpdate() > 0) {
	            // Retrieves any auto-generated keys created as a result of executing this Statement object
	            java.sql.ResultSet generatedKeys = pstmt.getGeneratedKeys();
	            if ( generatedKeys.next() ) {
	            	insertedId = generatedKeys.getInt(1);
	            }
	        }
		    
			logger.info("New filter data (id="+insertedId+") added");
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return insertedId;
	}

	@Override
	public void delete(Long id) {
		String sql = "DELETE FROM filterdata where id = ?";
		
		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, id);
			pstmt.executeUpdate();
			logger.info("filter data ("+id+") deleted");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}		
	}

	protected FilterData setFilterData(ResultSet rs) throws SQLException {
		String jsonString = rs.getString("json_string");
		FilterData filterData = new FilterData(jsonString);
		filterData.setEffectiveStartDate(rs.getTimestamp("effective_start_date"));
		filterData.setEffectiveEndDate(rs.getTimestamp("effective_end_date"));
		filterData.setId(rs.getLong("id"));
		filterData.getJsonObject().put("id", rs.getString("id"));

		return filterData;
	}
	
	@Override
	public FilterData getById(Long id) {
		FilterData filterData = null;		
		String sql = "SELECT * FROM filterdata where id = ?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				filterData = setFilterData(rs);
			}
			logger.info("filter data ("+id+") selected");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}		

		return filterData;
	}

	@Override
	public List<FilterData> get() {
		List<FilterData> filterDatas = new ArrayList<FilterData>();
		
		String sql = "SELECT * FROM filterdata";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				FilterData filterData = setFilterData(rs);
				filterDatas.add(filterData);
			}
			logger.info(filterDatas.size()+" filter data obtained");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}		

		return filterDatas;
	}

	@Override
	public void update(FilterData filterData) {
		String sql = "UPDATE filterdata SET effective_start_date=?, effective_end_date=?, json_string=? where id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, filterData.getEffectiveStartDate().getTime());
			pstmt.setLong(2, filterData.getEffectiveEndDate().getTime());
			pstmt.setString(3, filterData.toString());
			pstmt.setLong(4, filterData.getId());
			pstmt.executeUpdate();
			logger.info("filter data ("+filterData.getId()+") updated");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public List<FilterData> getEffectiveFilters(Long now) {
		List<FilterData> filterDatas = new ArrayList<FilterData>();
		
		String sql = "SELECT * FROM filterdata where effective_start_date <= ? AND effective_end_date >= ?";
		
		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, now);
			pstmt.setLong(2, now);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				FilterData filterData = setFilterData(rs);
				filterDatas.add(filterData);
			}
			logger.info(filterDatas.size()+" filter data obtained");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}		

		return filterDatas;
	}

}

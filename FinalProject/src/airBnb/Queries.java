package airBnb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Queries {
	private static String url = "jdbc:mysql://127.0.0.1/airbnb";
	private static String uname = "root";
	private static String pword = "*A18273645a*";
	
	private static enum Views
	{
		LISTING_GENERAL_VIEW("City, Country, type, pricePerNight, firstName, lastName"),
		LISTING_RENTERS_VIEW("listing.address, listing.postalCode, City, Country, type, pricePerNight, firstName, lastName ");
		
		private String view_name;
		
		Views(String vn)
		{
			this.view_name = vn;
		}
		
		public String toString()
		{
			return this.view_name;
		}
	}
	
	/* Takes in a regular (no insert/delete/update) query and executes it
	 * Returns the results of the query on success and null on failure
	 * */
	private static ResultSet executeQuery(String query)
	{
		ResultSet res;
		try {
			Connection con = DriverManager.getConnection(url, uname, pword);
			Statement statement = con.createStatement();
			res = statement.executeQuery(query);
		}
		catch(SQLException e) {
			System.out.println("Request failed");
			res = null;
		}
		return res;
		
	}
	
	/*
	 * Takes in a latitude and longitude and returns all the listings that are <vicinity> km away from that location
	 * Also applies a temporal, price, or amenity filter where applicable
	 * The general conversion used here is: 1 degree of latitude/longitude is roughly 110.574 km
	 * Source: https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-and-km-distance
	 * 
	 * precondition: for filters that are not applied, values are null
	 * i.e. if no temporal filter, then <startDate> and <endDate> will be null,
	 * 		if no price filter, then <lowestPrice> and <highestPrice> will be null
	 * 		if no amenity filter, then <amenities> is null
	 */
	public static ResultSet findListingsInVicinity(String lat, String lng, String vicinity, boolean sortByDistance,
			boolean sortAscending, String resultOffset, String startDate, String endDate,
			String lowestPrice, String highestPrice, String[] amenities)
	{
		String query = String.format("SELECT %s, 110.574 * POWER(POWER(latitude - %s, 2) + POWER(longitude - %s, 2),1/2) as distance "
				+ "FROM Listing JOIN User ON Listing.host=User.SIN "
				+ "WHERE 110.574*POWER(latitude - %s, 2) + POWER(longitude - %s, 2) <= %s "
				+ (startDate != null && endDate != null ? "AND " + temporalFilter(startDate, endDate) + " " : " ")
				+ (lowestPrice != null && highestPrice != null ? "AND " + priceFilter(lowestPrice, highestPrice) + " " : " ")
				+ (amenities != null ? "AND " + amenityFilter(amenities) + " " : " ")
				+ "ORDER BY " + (sortByDistance ? "distance " : "pricePerNight ") + (sortAscending ? "ASC ":"DESC ")
				+ "LIMIT %s, 10"
				, Views.LISTING_RENTERS_VIEW.toString(), lat, lng, lat, lng, vicinity, resultOffset);
		return executeQuery(query);
	}
	
	/*
	 * Takes in a postal code and returns all listings with postal codes that have the same 3 first characters
	 * 
	 * precondition: for filters that are not applied, values are null
	 * i.e. if no temporal filter, then <startDate> and <endDate> will be null,
	 * 		if no price filter, then <lowestPrice> and <highestPrice> will be null
	 * 		if no amenity filter, then <amenities> is null
	 */
	public static ResultSet findListingsByPostalCode(String postalCode, boolean sortByPrice, boolean isAscending, String resultOffset,
			String startDate, String endDate, String lowestPrice, String highestPrice, String[] amenities)
	{
		String query = String.format("SELECT %s "
				+ "FROM Listing JOIN User ON Listing.host=User.SIN "
				+ "WHERE SUBSTRING(postalCode, 1, 3)='%s'"
				+ (startDate != null && endDate != null ? "AND " + temporalFilter(startDate, endDate) + " " : " ")
				+ (lowestPrice != null && highestPrice != null ? "AND " + priceFilter(lowestPrice, highestPrice) + " " : " ")
				+ (amenities != null ? "AND " + amenityFilter(amenities) + " " : " ")
				+ (sortByPrice ? " ORDER BY pricePerNight " : " ")
				+ "LIMIT %s, 10", Views.LISTING_RENTERS_VIEW, postalCode.substring(0, 3), resultOffset);
		
		return executeQuery(query);
	}
	
	/*
	 * Returns the listing with <address> and null if no such listing exists
	 * 
	 * 
	 * precondition: for filters that are not applied, values are null
	 * i.e. if no temporal filter, then <startDate> and <endDate> will be null,
	 * 		if no price filter, then <lowestPrice> and <highestPrice> will be null
	 * 		if no amenity filter, then <amenities> is null
	 */
	public static ResultSet searchByExactAddress(String address, String startDate, String endDate,
			String lowestPrice, String highestPrice, String[] amenities)
	{
		String query = String.format("SELECT %s "
				+ "FROM listing JOIN USER ON listing.host=User.SIN "
				+ "WHERE listing.address='%s'", Views.LISTING_RENTERS_VIEW.toString(), address)
				+ (startDate != null && endDate != null ? "AND " + temporalFilter(startDate, endDate) + " " : " ")
				+ (lowestPrice != null && highestPrice != null ? "AND " + priceFilter(lowestPrice, highestPrice) + " " : " ")
				+ (amenities != null ? "AND " + amenityFilter(amenities) + " " : " ");
		
		return executeQuery(query);
	}
	
	/*
	 * Returns the query used for a temporal filter from <startDate> to <endDate>
	 */
	private static String temporalFilter(String startDate, String endDate)
	{
		return String.format("NOT EXISTS ("
				+ "SELECT * "
				+ "FROM Unavailability "
				+ "WHERE listing.address=Unavailability.listingAddress AND "
				+ "Listing.postalCode=Unavailability.listingPostalCode "
				+ "AND ("
				+ "(startDate<='%s' AND '%s'<=endDate) OR "
				+ "(startDate<='%s' AND '%s'<=endDate))"
				+ ")"
				, startDate, startDate, endDate, endDate);
	}
	
	/*
	 * Returns the query used for a price filter from <lowestPrice> to <highestPrice>
	 */
	private static String priceFilter(String lowestPrice, String highestPrice)
	{
		return String.format("%s <= pricePerNight AND pricePerNight <= %s ", lowestPrice, highestPrice);
	}
	
	/*
	 * Returns the query used to filter listings that offer <amenities>
	 * precondition: <amenities> has at least one list element
	 */
	private static String amenityFilter(String[] amenities)
	{
		String query = "EXISTS ("
				+ "SELECT * "
				+ "FROM Offers "
				+ "WHERE offers.listingAddress=listing.address AND offers.listingPostalCode=listing.postalCode AND ("
				+ "offers.amenity='" + amenities[0] + "'";
		int i = 1;
		while(i < amenities.length && amenities[i] != null)
		{
			query += " OR offers.amenity='" + amenities[i] + "'";
			i++;
		}
		
		query += "))";
		return query;
	}
}

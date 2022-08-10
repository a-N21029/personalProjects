package airBnb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;

public class Reports {
	private static String url = "jdbc:mysql://127.0.0.1/airbnb";
	private static String uname = "root";
	private static String pword = "*A18273645a*";
	
	
	/* Takes in a regular (no insert/delete/update) query and executes it
	 * Returns the results of the query on success and null on failure
	 * */
	private static ResultSet executeReport(String query)
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
	 * 
	 */
	public static ResultSet bookingsInDateRangeByCity(String startDate, String endDate)
	{
		String query = String.format("SELECT L.city, L.country, COUNT(*) as 'Number of bookings'"
				+ "FROM Reservation as R JOIN Listing as L ON R.listingAddress=L.address "
				+ "WHERE '%s' <= R.checkIn AND R.checkout <= '%s' AND R.iscancelled=0 "
				+ "GROUP BY L.city, L.country "
				+ "ORDER BY COUNT(*) DESC"
				, startDate, endDate, startDate, endDate);
		return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet bookingsInDateRangeByCityAndPostalCode(String startDate, String endDate)
	{
		String query = String.format("	SELECT SUBSTRING(L.postalCode, 1, 3) as 'Postal Code', L.city, L.country, count(*) as 'Number of bookings'"
				+ "	FROM Reservation as R JOIN Listing as L ON R.listingAddress=L.address "
				+ "	WHERE '%s' <= R.checkIn AND R.checkout <= '%s' "
				+ "	GROUP BY SUBSTRING(L.postalCode, 1, 3), L.city, L.country "
				+ " ORDER BY count(*) DESC"
				, startDate, endDate, startDate, endDate);
		return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet totalListings(boolean city, boolean postalCode)
	{
		String query = "SELECT Country"
				+ (city ? ", City" : " ")
				+ (postalCode ? ", SUBSTRING(postalCode, 1, 3) as 'Postal Code'" : " ")
				+ ", COUNT(*) as 'Number of Listings'"
				+ "FROM Listing "
				+ "GROUP BY Country"
				+ (city ? ", city" : " ")
				+ (postalCode ? ", SUBSTRING(postalCode, 1, 3) " : " ")
				+"ORDER BY COUNT(*) DESC";
		
		return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet rankHostsByTotalListingsInCountry(boolean city)
	{
			 String query = "SELECT firstName, lastName, " + (city ? "city, " : "") + "country, COUNT(*) "
			 			  + "FROM Listing as L join User as U ON L.host=U.SIN "
			 			  + "GROUP BY host, " + (city ? "city, " : "") + "country "
			 			  + "ORDER BY COUNT(*) DESC";
			 return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet findMonopolizingHosts()
	{
		String query = "SELECT SIN, firstName, lastName, city, country from Listing as L JOIN User as U on l.host=u.sin "
					 + "GROUP BY host, city, country "
					 + "HAVING (10*count(*) > "
					 + "(select count(*) from listing where country=l.country and city=l.city))";
		return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet rankRentersByBookingInTimeRange(String startDate, String endDate)
	{
		String query = "SELECT renterSIN, firstname, lastname, COUNT(*) as 'Number of bookings'"
					 + "FROM Reservation as R JOIN User as U ON R.renterSIN=U.SIN "
					 + "WHERE iscancelled=0 AND " + String.format("'%s' <= R.checkIn AND R.checkout <= '%s' ", startDate, endDate)
					 + "GROUP BY renterSIN ORDER BY COUNT(*) DESC";
		return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet rankRentersByBookingInTimeRangeAndCity(String startDate, String endDate)
	{
		String query = "SELECT R.renterSIN, L.city, L.country, U.firstname, U.lastname, COUNT(*) as 'Number of bookings'"
				 	 + "FROM Reservation as R, Listing as L, User as U "
				 	 + "WHERE R.listingaddress=L.address AND R.listingPostalCode=L.postalCode AND R.renterSIN=U.SIN AND iscancelled=0 "
				 	 + "AND " + String.format("'%s' <= R.checkIn AND R.checkout <= '%s' ", startDate, endDate)
				 	 + "GROUP BY renterSIN HAVING COUNT(*) >= 2 "
				 	 + "ORDER BY COUNT(*) DESC";
	return executeReport(query);
	}
	
	/*
	 * 
	 */
	public static ResultSet rankRentersByCancellation()
	{
		String query = "SELECT R.renterSin, U.firstname, U.lastname, count(*) as 'Number of cancellations' "
					 + "FROM Reservation as R JOIN User as U ON R.renterSIN=U.SIN "
					 + "WHERE iscancelled=1 "
					 + "GROUP BY renterSIN";
		return executeReport(query);
	}
	
	public static ResultSet rankHostsByCancellation()
	{
		String query = "SELECT l.host, u.firstname, u.lastname, count(*) as 'Number of cancellations' "
					 + "FROM Reservation as R, User as U, Listing as L "
					 + "WHERE R.listingaddress=L.address AND R.listingpostalcode=L.postalcode AND L.host=U.SIN AND iscancelled=1";
		return executeReport(query);
	}
	
	public static HashMap<String, String> listingCommonNounPhrases()
	{
		String query = "SELECT listingaddress, listingpostalcode, comment "
				+ "FROM listingreview "
				+ "GROUP BY listingaddress, listingpostalcode";
		
		ResultSet res = executeReport(query);
		
		try
		{
			HashMap<String, String> commonPhrases = new HashMap<String, String>();
			while(res.next())
			{
				HashMap<String, Integer> nounPhrases = new HashMap<String, Integer>();
				int mostCommonOccurrence = 0;
				String mostCommonPhrase= "";
				String prevWord = "";
				String comment = res.getString("comment");
				String[] words = comment.split(" ");
				
				for(String word : words)
				{
					// it is assumed any word following a "the", "a" or "an" is a noun
					if(prevWord.trim().equalsIgnoreCase("the") || prevWord.trim().equalsIgnoreCase("a")
							|| prevWord.trim().equalsIgnoreCase("an"))
					{
						String phrase = prevWord.toLowerCase() + " " + word;
						// first time encountering this phrase
						if(nounPhrases.get(phrase) == null)
						{
							nounPhrases.put(phrase, 1);
							
							if(mostCommonOccurrence == 0)
							{
								mostCommonOccurrence += 1;
								mostCommonPhrase= phrase;
							}
						}
						else
						{
							nounPhrases.put(phrase, nounPhrases.get(phrase) + 1);
							if(nounPhrases.get(phrase) > mostCommonOccurrence)
							{
								mostCommonOccurrence = nounPhrases.get(phrase);
								mostCommonPhrase = phrase;
							}
						}
					}
					
					prevWord = word;
				}
				commonPhrases.put(res.getString("listingAddress") + " " + res.getString("listingPostalCode"), mostCommonPhrase);
			}
			return commonPhrases;
		}
		catch(SQLException e)
		{
			return null;
		}
	}
}
